package com.example.flowdiagramapp.model;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PointF;
import android.graphics.RectF;

public class Connection {
    private int id;
    private FlowElement source;
    private FlowElement target;
    private Paint paint;
    private String label;

    public Connection(int id, FlowElement source, FlowElement target) {
        this.id = id;
        this.source = source;
        this.target = target;
        this.paint = new Paint();
        paint.setColor(Color.BLACK);
        paint.setStrokeWidth(3f);
        paint.setStyle(Paint.Style.STROKE);
        paint.setAntiAlias(true);
        this.label = "";
    }

    public void draw(Canvas canvas) {
        PointF startPoint = getConnectionPoint(source, target);
        PointF endPoint = getConnectionPoint(target, source);

        Path path = new Path();
        path.moveTo(startPoint.x, startPoint.y);
        path.lineTo(endPoint.x, endPoint.y);
        canvas.drawPath(path, paint);

        // Draw arrow head
        float angle = (float) Math.atan2(endPoint.y - startPoint.y, endPoint.x - startPoint.x);
        float arrowSize = 20f;

        Path arrowPath = new Path();
        arrowPath.moveTo(endPoint.x, endPoint.y);
        arrowPath.lineTo((float)(endPoint.x - arrowSize * Math.cos(angle - Math.PI/6)),
                (float)(endPoint.y - arrowSize * Math.sin(angle - Math.PI/6)));
        arrowPath.lineTo((float)(endPoint.x - arrowSize * Math.cos(angle + Math.PI/6)),
                (float)(endPoint.y - arrowSize * Math.sin(angle + Math.PI/6)));
        arrowPath.close();

        Paint arrowPaint = new Paint(paint);
        arrowPaint.setStyle(Paint.Style.FILL);
        canvas.drawPath(arrowPath, arrowPaint);

        // Draw label if exists
        if (label != null && !label.isEmpty()) {
            Paint textPaint = new Paint();
            textPaint.setColor(Color.BLACK);
            textPaint.setTextSize(24f);
            textPaint.setTextAlign(Paint.Align.CENTER);

            PointF midPoint = new PointF(
                    (startPoint.x + endPoint.x) / 2,
                    (startPoint.y + endPoint.y) / 2
            );

            canvas.drawText(label, midPoint.x, midPoint.y - 10, textPaint);
        }
    }

    private PointF getConnectionPoint(FlowElement element, FlowElement other) {
        PointF p1 = element.getPosition();
        PointF p2 = other.getPosition();

        // Calculate direction vector
        float dx = p2.x - p1.x;
        float dy = p2.y - p1.y;
        float len = (float) Math.sqrt(dx * dx + dy * dy);
        dx /= len;
        dy /= len;

        // Get intersection with element bounds
        RectF bounds = element.getBounds();
        float x, y;

        if (Math.abs(dx) > Math.abs(dy)) {
            // Horizontal intersection
            x = dx > 0 ? bounds.right : bounds.left;
            y = p1.y + dy * (x - p1.x) / dx;
        } else {
            // Vertical intersection
            y = dy > 0 ? bounds.bottom : bounds.top;
            x = p1.x + dx * (y - p1.y) / dy;
        }

        return new PointF(x, y);
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public int getId() {
        return id;
    }

    public FlowElement getSource() {
        return source;
    }

    public FlowElement getTarget() {
        return target;
    }
}