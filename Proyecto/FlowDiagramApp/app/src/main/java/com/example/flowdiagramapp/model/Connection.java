package com.example.flowdiagramapp.model;

import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;

/**
 * Clase que representa una conexión entre dos nodos
 */
public class Connection {
    private Node source;
    private Node target;
    private String label; // Para condiciones en conexiones de if/else
    private boolean isSelected;

    public Connection(Node source, Node target) {
        this.source = source;
        this.target = target;
        this.label = "";
        this.isSelected = false;
    }

    public Connection(Node source, Node target, String label) {
        this(source, target);
        this.label = label;
    }

    /**
     * Dibuja la conexión entre dos nodos
     */
    public void draw(Canvas canvas, Paint paint) {
        if (source == null || target == null) return;

        // Puntos de inicio y fin
        Point start = getConnectionPoint(source, target);
        Point end = getConnectionPoint(target, source);

        // Dibujar flecha
        drawArrow(canvas, paint, start, end);

        // Si hay una etiqueta, dibujarla en el medio de la conexión
        if (label != null && !label.isEmpty()) {
            float midX = (start.getX() + end.getX()) / 2;
            float midY = (start.getY() + end.getY()) / 2;
            
            Paint textPaint = new Paint(paint);
            textPaint.setStyle(Paint.Style.FILL);
            canvas.drawText(label, midX, midY - 10, textPaint);
        }
    }

    /**
     * Calcula el punto de conexión en el borde de un nodo
     */
    private Point getConnectionPoint(Node node, Node other) {
        float nodeX = node.getPosition().getX();
        float nodeY = node.getPosition().getY();
        float otherX = other.getPosition().getX();
        float otherY = other.getPosition().getY();

        float angle = (float) Math.atan2(otherY - nodeY, otherX - nodeX);
        
        // Encontrar el punto de intersección con el borde del nodo
        float width = Node.WIDTH / 2;
        float height = Node.HEIGHT / 2;

        float x = 0, y = 0;
        float tanAngle = (float) Math.abs(Math.tan(angle));

        if (tanAngle > height / width) {
            // Intersección con el borde superior/inferior
            y = (float) Math.signum(otherY - nodeY) * height;
            x = y / tanAngle * (float) Math.signum(otherX - nodeX);
        } else {
            // Intersección con el borde izquierdo/derecho
            x = (float) Math.signum(otherX - nodeX) * width;
            y = x * tanAngle * (float) Math.signum(otherY - nodeY);
        }

        return new Point(nodeX + x, nodeY + y);
    }

    /**
     * Dibuja una flecha entre dos puntos
     */
    private void drawArrow(Canvas canvas, Paint paint, Point start, Point end) {
        // Dibujar línea principal
        canvas.drawLine(start.getX(), start.getY(), end.getX(), end.getY(), paint);

        // Calcular la punta de flecha
        float angle = (float) Math.atan2(end.getY() - start.getY(), end.getX() - start.getX());
        float arrowSize = 15f;

        float arrowX1 = (float) (end.getX() - arrowSize * Math.cos(angle - Math.PI/6));
        float arrowY1 = (float) (end.getY() - arrowSize * Math.sin(angle - Math.PI/6));
        float arrowX2 = (float) (end.getX() - arrowSize * Math.cos(angle + Math.PI/6));
        float arrowY2 = (float) (end.getY() - arrowSize * Math.sin(angle + Math.PI/6));

        Path arrowPath = new Path();
        arrowPath.moveTo(end.getX(), end.getY());
        arrowPath.lineTo(arrowX1, arrowY1);
        arrowPath.lineTo(arrowX2, arrowY2);
        arrowPath.close();

        // Dibujar la punta de flecha
        Paint arrowPaint = new Paint(paint);
        arrowPaint.setStyle(Paint.Style.FILL);
        canvas.drawPath(arrowPath, arrowPaint);
    }

    /**
     * Comprueba si un punto está cerca de la conexión
     */
    public boolean isNear(Point point, float threshold) {
        if (source == null || target == null) return false;

        Point start = getConnectionPoint(source, target);
        Point end = getConnectionPoint(target, source);

        // Calcular la distancia del punto a la línea
        float lineLength = start.distanceTo(end);
        
        if (lineLength == 0) return false;

        // Calcular la distancia del punto a la línea usando el producto cruz
        float dx = end.getX() - start.getX();
        float dy = end.getY() - start.getY();
        float distance = Math.abs(dy * point.getX() - dx * point.getY() + 
                         end.getX() * start.getY() - end.getY() * start.getX()) / lineLength;
        
        return distance <= threshold;
    }

    // Getters y setters
    public Node getSource() {
        return source;
    }

    public void setSource(Node source) {
        this.source = source;
    }

    public Node getTarget() {
        return target;
    }

    public void setTarget(Node target) {
        this.target = target;
    }

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public boolean isSelected() {
        return isSelected;
    }

    public void setSelected(boolean selected) {
        isSelected = selected;
    }
}