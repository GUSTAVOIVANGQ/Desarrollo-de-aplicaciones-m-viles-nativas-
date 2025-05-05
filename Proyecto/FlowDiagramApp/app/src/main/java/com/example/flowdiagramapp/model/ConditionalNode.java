package com.example.flowdiagramapp.model;

import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;

/**
 * Nodo para representar condicionales (if/else)
 */
public class ConditionalNode extends Node {
    
    public ConditionalNode(Point position, String condition) {
        super(position, condition);
    }
    
    @Override
    public void draw(Canvas canvas, Paint paint) {
        // Los nodos condicionales se dibujan como rombos
        Path diamondPath = new Path();
        diamondPath.moveTo(position.getX(), bounds.top); // Arriba
        diamondPath.lineTo(bounds.right, position.getY()); // Derecha
        diamondPath.lineTo(position.getX(), bounds.bottom); // Abajo
        diamondPath.lineTo(bounds.left, position.getY()); // Izquierda
        diamondPath.close();
        
        // Relleno
        Paint fillPaint = new Paint(paint);
        fillPaint.setStyle(Paint.Style.FILL);
        canvas.drawPath(diamondPath, fillPaint);
        
        // Contorno
        Paint strokePaint = new Paint(paint);
        strokePaint.setStyle(Paint.Style.STROKE);
        strokePaint.setStrokeWidth(2f);
        canvas.drawPath(diamondPath, strokePaint);
        
        // Texto
        Paint textPaint = new Paint(paint);
        textPaint.setStyle(Paint.Style.FILL);
        textPaint.setTextSize(18f);
        textPaint.setTextAlign(Paint.Align.CENTER);
        
        float textX = position.getX();
        float textY = position.getY() + (textPaint.getTextSize() / 3);
        
        // Ajustar el texto para que quepa dentro del rombo
        String displayText = text;
        float textWidth = textPaint.measureText(displayText);
        float availableWidth = WIDTH * 0.7f; // Usar un porcentaje del ancho total
        
        if (textWidth > availableWidth) {
            // Si el texto es demasiado largo, lo truncamos
            int maxChars = (int) (displayText.length() * availableWidth / textWidth) - 3;
            if (maxChars > 0) {
                displayText = displayText.substring(0, maxChars) + "...";
            } else {
                displayText = "...";
            }
        }
        
        canvas.drawText(displayText, textX, textY, textPaint);
    }

    /**
     * Obtener la conexión para el caso TRUE (normalmente a la derecha)
     */
    public Connection getTrueConnection() {
        for (Connection conn : outputs) {
            if ("true".equalsIgnoreCase(conn.getLabel()) || "si".equalsIgnoreCase(conn.getLabel())) {
                return conn;
            }
        }
        return null;
    }
    
    /**
     * Obtener la conexión para el caso FALSE (normalmente a la izquierda)
     */
    public Connection getFalseConnection() {
        for (Connection conn : outputs) {
            if ("false".equalsIgnoreCase(conn.getLabel()) || "no".equalsIgnoreCase(conn.getLabel())) {
                return conn;
            }
        }
        return null;
    }
}