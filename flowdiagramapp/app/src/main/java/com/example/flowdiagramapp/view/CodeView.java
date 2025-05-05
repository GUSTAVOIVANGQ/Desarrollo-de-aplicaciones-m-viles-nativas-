package com.example.flowdiagramapp.view;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.Typeface;
import android.util.AttributeSet;
import android.view.View;

public class CodeView extends View {
    private String code = "";
    private Paint textPaint;
    private Paint backgroundPaint;
    private Paint linePaint;
    private float lineHeight = 40f;
    private float paddingLeft = 40f;
    private float paddingTop = 20f;
    private float textSize = 24f;

    public CodeView(Context context) {
        super(context);
        init();
    }

    public CodeView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    private void init() {
        textPaint = new Paint();
        textPaint.setColor(Color.BLACK);
        textPaint.setTextSize(textSize);
        textPaint.setAntiAlias(true);
        textPaint.setTypeface(Typeface.MONOSPACE);

        backgroundPaint = new Paint();
        backgroundPaint.setColor(Color.rgb(245, 245, 245));

        linePaint = new Paint();
        linePaint.setColor(Color.LTGRAY);
        linePaint.setStrokeWidth(1f);
    }

    public void setCode(String code) {
        this.code = code;
        invalidate();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        // Draw background
        canvas.drawRect(0, 0, getWidth(), getHeight(), backgroundPaint);

        if (code == null || code.isEmpty()) {
            return;
        }

        // Split code into lines
        String[] lines = code.split("\n");

        // Calculate line height based on text metrics
        Rect bounds = new Rect();
        textPaint.getTextBounds("Ty", 0, 2, bounds);
        float actualLineHeight = bounds.height() * 1.5f;

        // Draw code with line numbers
        float y = paddingTop + actualLineHeight;
        for (int i = 0; i < lines.length; i++) {
            // Draw line number
            String lineNumber = String.valueOf(i + 1);
            textPaint.setColor(Color.GRAY);
            canvas.drawText(lineNumber, 10, y, textPaint);

            // Draw line separator
            canvas.drawLine(paddingLeft - 15, 0, paddingLeft - 15, getHeight(), linePaint);

            // Draw actual code with syntax highlighting
            textPaint.setColor(Color.BLACK);
            String line = lines[i];

            // Basic syntax highlighting
            if (line.contains("#include") || line.contains("#define")) {
                textPaint.setColor(Color.rgb(128, 0, 128)); // Purple for preprocessor
            } else if (line.contains("int ") || line.contains("float ")
                    || line.contains("double ") || line.contains("char ")) {
                textPaint.setColor(Color.rgb(0, 0, 200)); // Blue for types
            } else if (line.contains("if") || line.contains("else")
                    || line.contains("while") || line.contains("for")
                    || line.contains("return")) {
                textPaint.setColor(Color.rgb(0, 100, 0)); // Green for control keywords
            }

            canvas.drawText(line, paddingLeft, y, textPaint);
            y += actualLineHeight;
        }
    }
}