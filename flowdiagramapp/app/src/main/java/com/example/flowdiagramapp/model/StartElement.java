package com.example.flowdiagramapp.model;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;

public class StartElement extends FlowElement {
    private static final float RADIUS = 50f;
    private Paint paint;

    public StartElement(int id, float x, float y) {
        super(id, x, y, "Inicio");
        paint = new Paint();
        paint.setAntiAlias(true);
    }

    @Override
    public void draw(Canvas canvas) {
        paint.setColor(Color.GREEN);
        paint.setStyle(Paint.Style.FILL);
        canvas.drawCircle(position.x, position.y, RADIUS, paint);

        paint.setColor(Color.BLACK);
        paint.setTextSize(30f);
        paint.setTextAlign(Paint.Align.CENTER);
        canvas.drawText(label, position.x, position.y + 10, paint);
    }

    @Override
    protected void updateBounds() {
        bounds.set(position.x - RADIUS, position.y - RADIUS,
                position.x + RADIUS, position.y + RADIUS);
    }

    @Override
    public String generateCode() {
        return "/* Inicio del programa */\n" +
                "#include <stdio.h>\n\n" +
                "int main() {\n";
    }
}