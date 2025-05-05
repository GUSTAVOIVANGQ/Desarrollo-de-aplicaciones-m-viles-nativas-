package com.example.flowdiagramapp.model;

import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;

/**
 * Nodo de inicio del diagrama de flujo
 */
public class StartNode extends Node {
    
    public StartNode(Point position) {
        super(position, "Inicio");
    }
    
    @Override
    public void draw(Canvas canvas, Paint paint) {
        // Los nodos de inicio se suelen dibujar como óvalos
        Paint fillPaint = new Paint(paint);
        fillPaint.setStyle(Paint.Style.FILL);
        canvas.drawOval(bounds, fillPaint);
        
        // Contorno
        Paint strokePaint = new Paint(paint);
        strokePaint.setStyle(Paint.Style.STROKE);
        strokePaint.setStrokeWidth(2f);
        canvas.drawOval(bounds, strokePaint);
        
        // Texto
        Paint textPaint = new Paint(paint);
        textPaint.setStyle(Paint.Style.FILL);
        textPaint.setTextSize(24f);
        textPaint.setTextAlign(Paint.Align.CENTER);
        
        float textX = position.getX();
        float textY = position.getY() + (textPaint.getTextSize() / 3); // Ajuste vertical para centrar
        
        canvas.drawText(text, textX, textY, textPaint);
    }
}