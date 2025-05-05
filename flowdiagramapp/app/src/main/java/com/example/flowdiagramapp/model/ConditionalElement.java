package com.example.flowdiagramapp.model;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;

public class ConditionalElement extends FlowElement {
    private static final float WIDTH = 180f;
    private static final float HEIGHT = 120f;
    private Paint paint;
    private String condition;

    public ConditionalElement(int id, float x, float y, String condition) {
        super(id, x, y, "");
        this.condition = condition;
        this.label = condition;
        paint = new Paint();
        paint.setAntiAlias(true);
    }

    @Override
    public void draw(Canvas canvas) {
        paint.setColor(Color.YELLOW);
        paint.setStyle(Paint.Style.FILL);

        Path path = new Path();
        path.moveTo(position.x, position.y - HEIGHT/2);  // Top
        path.lineTo(position.x + WIDTH/2, position.y);   // Right
        path.lineTo(position.x, position.y + HEIGHT/2);  // Bottom
        path.lineTo(position.x - WIDTH/2, position.y);   // Left
        path.close();

        canvas.drawPath(path, paint);

        paint.setColor(Color.BLACK);
        paint.setTextSize(24f);
        paint.setTextAlign(Paint.Align.CENTER);
        canvas.drawText(label, position.x, position.y + 10, paint);
    }

    @Override
    protected void updateBounds() {
        bounds.set(position.x - WIDTH/2, position.y - HEIGHT/2,
                position.x + WIDTH/2, position.y + HEIGHT/2);
    }

    @Override
    public String generateCode() {
        return "    if (" + condition + ") {\n" +
                "        // Bloque verdadero\n" +
                "    } else {\n" +
                "        // Bloque falso\n" +
                "    }\n";
    }

    public void setCondition(String condition) {
        this.condition = condition;
        this.label = condition;
    }
}