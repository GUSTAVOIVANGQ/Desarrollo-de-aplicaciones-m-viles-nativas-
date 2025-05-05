package com.example.flowdiagramapp.model;

import android.graphics.Canvas;
import android.graphics.Paint;

/**
 * Nodo para declaración de variables
 */
public class VariableNode extends Node {
    
    public VariableNode(Point position, String text) {
        super(position, text);
    }
    
    @Override
    public void draw(Canvas canvas, Paint paint) {
        // Los nodos de declaración se dibujan como rectángulos
        Paint fillPaint = new Paint(paint);
        fillPaint.setStyle(Paint.Style.FILL);
        canvas.drawRect(bounds, fillPaint);
        
        // Contorno
        Paint strokePaint = new Paint(paint);
        strokePaint.setStyle(Paint.Style.STROKE);
        strokePaint.setStrokeWidth(2f);
        canvas.drawRect(bounds, strokePaint);
        
        // Texto
        Paint textPaint = new Paint(paint);
        textPaint.setStyle(Paint.Style.FILL);
        textPaint.setTextSize(20f);
        textPaint.setTextAlign(Paint.Align.CENTER);
        
        float textX = position.getX();
        float textY = position.getY() + (textPaint.getTextSize() / 3);
        
        canvas.drawText(text, textX, textY, textPaint);
    }
}