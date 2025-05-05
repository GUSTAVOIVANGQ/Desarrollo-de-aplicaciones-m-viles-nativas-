package com.example.flowdiagramapp.view;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

import com.example.flowdiagramapp.controller.DiagramController;
import com.example.flowdiagramapp.model.Connection;
import com.example.flowdiagramapp.model.FlowElement;

import java.util.ArrayList;
import java.util.List;

public class FlowDiagramView extends View {
    private DiagramController controller;
    private Paint backgroundPaint;

    // For touch handling
    private FlowElement selectedElement;
    private float lastTouchX, lastTouchY;
    private boolean isMoving = false;
    private static final int TOUCH_TOLERANCE = 5;

    public FlowDiagramView(Context context) {
        super(context);
        init();
    }

    public FlowDiagramView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    private void init() {
        backgroundPaint = new Paint();
        backgroundPaint.setColor(Color.WHITE);
    }

    public void setController(DiagramController controller) {
        this.controller = controller;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        // Draw background
        canvas.drawRect(0, 0, getWidth(), getHeight(), backgroundPaint);

        if (controller == null) return;

        // Draw grid (optional)
        drawGrid(canvas);

        // Draw connections first (so they appear behind elements)
        for (Connection connection : controller.getConnections()) {
            connection.draw(canvas);
        }

        // Draw all elements
        for (FlowElement element : controller.getElements()) {
            element.draw(canvas);
        }
    }

    private void drawGrid(Canvas canvas) {
        Paint gridPaint = new Paint();
        gridPaint.setColor(Color.LTGRAY);
        gridPaint.setStrokeWidth(1);

        int gridSpacing = 40;

        // Draw vertical lines
        for (int i = 0; i < getWidth(); i += gridSpacing) {
            canvas.drawLine(i, 0, i, getHeight(), gridPaint);
        }

        // Draw horizontal lines
        for (int i = 0; i < getHeight(); i += gridSpacing) {
            canvas.drawLine(0, i, getWidth(), i, gridPaint);
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        float x = event.getX();
        float y = event.getY();

        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                // Find if we're touching an element
                FlowElement touchedElement = findElementAt(x, y);
                if (touchedElement != null) {
                    selectedElement = touchedElement;
                    lastTouchX = x;
                    lastTouchY = y;
                    isMoving = false;
                }
                return true;

            case MotionEvent.ACTION_MOVE:
                if (selectedElement != null) {
                    float dx = x - lastTouchX;
                    float dy = y - lastTouchY;

                    // Check if we've moved enough to consider it a drag
                    if (!isMoving && Math.abs(dx) > TOUCH_TOLERANCE || Math.abs(dy) > TOUCH_TOLERANCE) {
                        isMoving = true;
                    }

                    if (isMoving) {
                        selectedElement.move(dx, dy);
                        lastTouchX = x;
                        lastTouchY = y;
                        invalidate();
                    }
                }
                return true;

            case MotionEvent.ACTION_UP:
                if (selectedElement != null) {
                    if (!isMoving) {
                        // This was a click, handle selection
                        controller.setSelectedElement(selectedElement);
                    } else {
                        // This was a move, update the code
                        controller.notifyElementMoved(selectedElement);
                    }
                    selectedElement = null;
                }
                return true;
        }

        return super.onTouchEvent(event);
    }

    private FlowElement findElementAt(float x, float y) {
        if (controller == null) return null;

        // Check in reverse order (top elements first)
        List<FlowElement> elements = controller.getElements();
        for (int i = elements.size() - 1; i >= 0; i--) {
            FlowElement element = elements.get(i);
            if (element.contains(x, y)) {
                return element;
            }
        }
        return null;
    }

    public void addElement(String type, float x, float y) {
        if (controller != null) {
            controller.addElement(type, x, y);
            invalidate();
        }
    }
}