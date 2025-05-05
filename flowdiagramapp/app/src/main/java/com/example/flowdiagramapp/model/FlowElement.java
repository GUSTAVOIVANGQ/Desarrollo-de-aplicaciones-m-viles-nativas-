package com.example.flowdiagramapp.model;

import android.graphics.Canvas;
import android.graphics.PointF;
import android.graphics.RectF;

public abstract class FlowElement {
    protected int id;
    protected PointF position;
    protected RectF bounds;
    protected String label;

    public FlowElement(int id, float x, float y, String label) {
        this.id = id;
        this.position = new PointF(x, y);
        this.label = label;
        this.bounds = new RectF();
        updateBounds();
    }

    public abstract void draw(Canvas canvas);
    protected abstract void updateBounds();
    public abstract String generateCode();

    public boolean contains(float x, float y) {
        return bounds.contains(x, y);
    }

    public void move(float dx, float dy) {
        position.x += dx;
        position.y += dy;
        updateBounds();
    }

    public int getId() {
        return id;
    }

    public RectF getBounds() {
        return bounds;
    }

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public PointF getPosition() {
        return position;
    }
}

