package com.example.flowdiagramapp.model;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;

public class VariableElement extends FlowElement {
    private static final float WIDTH = 200f;
    private static final float HEIGHT = 80f;
    private Paint paint;
    private String varType;
    private String varName;
    private String varValue;

    public VariableElement(int id, float x, float y, String varType, String varName, String varValue) {
        super(id, x, y, "");
        this.varType = varType;
        this.varName = varName;
        this.varValue = varValue;
        paint = new Paint();
        paint.setAntiAlias(true);
        updateLabel();
    }

    private void updateLabel() {
        this.label = varType + " " + varName + (varValue.isEmpty() ? "" : " = " + varValue);
    }

    @Override
    public void draw(Canvas canvas) {
        paint.setColor(Color.BLUE);
        paint.setStyle(Paint.Style.FILL);
        canvas.drawRect(bounds, paint);

        paint.setColor(Color.WHITE);
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
        return "    " + varType + " " + varName +
                (varValue.isEmpty() ? ";" : " = " + varValue + ";") + "\n";
    }

    public void setVarType(String varType) {
        this.varType = varType;
        updateLabel();
    }

    public void setVarName(String varName) {
        this.varName = varName;
        updateLabel();
    }

    public void setVarValue(String varValue) {
        this.varValue = varValue;
        updateLabel();
    }
}