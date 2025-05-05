package com.example.flowdiagramapp.model;

import android.graphics.Canvas;
import android.graphics.Paint;

/**
 * Nodo de fin del diagrama de flujo
 */
public class EndNode extends Node {
    
    public EndNode(Point position) {
        super(position, "Fin");
    }
    
    @Override
    public void draw(Canvas canvas, Paint paint) {
        // Los nodos de fin se suelen dibujar como óvalos similar al inicio pero con un trazo más grueso
        Paint fillPaint = new Paint(paint);
        fillPaint.setStyle(Paint.Style.FILL);
        canvas.drawOval(bounds, fillPaint);
        
        // Contorno doble para diferenciar del inicio
        Paint strokePaint = new Paint(paint);
        strokePaint.setStyle(Paint.Style.STROKE);
        strokePaint.setStrokeWidth(3f);
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