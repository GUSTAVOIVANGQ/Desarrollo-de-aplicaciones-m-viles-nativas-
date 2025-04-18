package com.example.squareconnector;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import java.util.ArrayList;
import java.util.List;

public class DrawingView extends View {

    private List<Square> squares = new ArrayList<>();
    private Paint squarePaint;
    private Paint arrowPaint;
    private Square selectedSquare = null;
    private Square startSquare = null;
    private float lastTouchX, lastTouchY;
    private boolean isMoving = false;
    private boolean isConnecting = false;
    private float connectingEndX, connectingEndY;

    public DrawingView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    private void init() {
        squarePaint = new Paint();
        squarePaint.setColor(Color.BLUE);
        squarePaint.setStyle(Paint.Style.FILL);

        arrowPaint = new Paint();
        arrowPaint.setColor(Color.BLACK);
        arrowPaint.setStyle(Paint.Style.STROKE);
        arrowPaint.setStrokeWidth(5);
    }

    public void addSquare() {
        // Add a new square to a default position
        float size = 150;
        float x = getWidth() / 2 - size / 2;
        float y = getHeight() / 2 - size / 2;

        // Position the new square avoiding overlap with existing squares
        if (!squares.isEmpty()) {
            y = squares.get(squares.size() - 1).rect.top + 200;
            if (y > getHeight() - size) {
                y = 100;
                x += 200;
            }
        }

        Square newSquare = new Square(new RectF(x, y, x + size, y + size));
        squares.add(newSquare);
        invalidate();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        // Draw all connections (arrows) first
        for (Square square : squares) {
            for (Connection connection : square.connections) {
                drawArrow(canvas, square.getCenterX(), square.getCenterY(),
                        connection.target.getCenterX(), connection.target.getCenterY());
            }
        }

        // Draw squares
        for (Square square : squares) {
            if (square == selectedSquare) {
                squarePaint.setColor(Color.RED);
            } else {
                squarePaint.setColor(Color.BLUE);
            }
            canvas.drawRect(square.rect, squarePaint);
        }

        // Draw connecting line if user is in connecting mode
        if (isConnecting && startSquare != null) {
            canvas.drawLine(startSquare.getCenterX(), startSquare.getCenterY(),
                    connectingEndX, connectingEndY, arrowPaint);
        }
    }

    private void drawArrow(Canvas canvas, float startX, float startY, float endX, float endY) {
        // Draw the main line
        canvas.drawLine(startX, startY, endX, endY, arrowPaint);

        // Calculate the arrow head
        float angle = (float) Math.atan2(endY - startY, endX - startX);
        float arrowHeadLength = 40;
        float arrowAngle = (float) Math.toRadians(30);

        Path path = new Path();
        path.moveTo(endX, endY);
        path.lineTo((float)(endX - arrowHeadLength * Math.cos(angle - arrowAngle)),
                (float)(endY - arrowHeadLength * Math.sin(angle - arrowAngle)));
        path.moveTo(endX, endY);
        path.lineTo((float)(endX - arrowHeadLength * Math.cos(angle + arrowAngle)),
                (float)(endY - arrowHeadLength * Math.sin(angle + arrowAngle)));

        canvas.drawPath(path, arrowPaint);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        float x = event.getX();
        float y = event.getY();

        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                // Check if user touched a square
                selectedSquare = null;
                for (int i = squares.size() - 1; i >= 0; i--) {
                    if (squares.get(i).contains(x, y)) {
                        selectedSquare = squares.get(i);
                        lastTouchX = x;
                        lastTouchY = y;
                        break;
                    }
                }

                // If a square is already selected and user taps another, start connecting them
                if (selectedSquare != null && startSquare != null && startSquare != selectedSquare) {
                    // Create a connection from startSquare to selectedSquare
                    startSquare.addConnection(selectedSquare);
                    startSquare = null;
                    selectedSquare = null;
                    isConnecting = false;
                    invalidate();
                    return true;
                }

                // If user taps on a square, prepare for either moving or connecting
                if (selectedSquare != null) {
                    isMoving = true;
                    // Check if this is a long press (for now we'll use a simple tap to toggle connect mode)
                    if (event.getEventTime() - event.getDownTime() > 500) {
                        isMoving = false;
                        isConnecting = true;
                        startSquare = selectedSquare;
                        connectingEndX = x;
                        connectingEndY = y;
                    }
                }

                break;

            case MotionEvent.ACTION_MOVE:
                if (isMoving && selectedSquare != null) {
                    // Move the square
                    float dx = x - lastTouchX;
                    float dy = y - lastTouchY;
                    selectedSquare.rect.offset(dx, dy);
                    lastTouchX = x;
                    lastTouchY = y;
                    invalidate();
                } else if (isConnecting && startSquare != null) {
                    // Update the end position of connecting line
                    connectingEndX = x;
                    connectingEndY = y;
                    invalidate();
                }
                break;

            case MotionEvent.ACTION_UP:
                // If in connecting mode and released on another square, create connection
                if (isConnecting && startSquare != null) {
                    Square endSquare = null;
                    for (Square square : squares) {
                        if (square != startSquare && square.contains(x, y)) {
                            endSquare = square;
                            break;
                        }
                    }

                    if (endSquare != null) {
                        startSquare.addConnection(endSquare);
                    }

                    // Reset connection state
                    isConnecting = false;
                    startSquare = null;
                }

                isMoving = false;
                invalidate();
                break;
        }

        return true;
    }

    // Square class to represent each square
    private class Square {
        RectF rect;
        List<Connection> connections = new ArrayList<>();

        Square(RectF rect) {
            this.rect = rect;
        }

        boolean contains(float x, float y) {
            return rect.contains(x, y);
        }

        float getCenterX() {
            return rect.centerX();
        }

        float getCenterY() {
            return rect.centerY();
        }

        void addConnection(Square target) {
            // Check if connection already exists
            for (Connection conn : connections) {
                if (conn.target == target) {
                    return; // Connection already exists
                }
            }
            connections.add(new Connection(target));
        }
    }

    // Connection class to represent connections between squares
    private class Connection {
        Square target;

        Connection(Square target) {
            this.target = target;
        }
    }
}