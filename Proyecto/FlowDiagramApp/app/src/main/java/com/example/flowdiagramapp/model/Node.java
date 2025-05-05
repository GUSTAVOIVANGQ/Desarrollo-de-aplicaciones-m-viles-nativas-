package com.example.flowdiagramapp.model;

import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.RectF;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Clase base para todos los nodos de diagrama de flujo
 */
public abstract class Node {
    protected String id;
    protected Point position;
    protected String text;
    protected RectF bounds;
    protected List<Connection> inputs;
    protected List<Connection> outputs;
    
    // Constantes para dimensiones
    protected static final float WIDTH = 150f;
    protected static final float HEIGHT = 80f;

    public Node(Point position, String text) {
        this.id = UUID.randomUUID().toString();
        this.position = position;
        this.text = text;
        this.inputs = new ArrayList<>();
        this.outputs = new ArrayList<>();
        updateBounds();
    }

    /**
     * Actualiza los límites del nodo basado en su posición
     */
    public void updateBounds() {
        float left = position.getX() - (WIDTH / 2);
        float top = position.getY() - (HEIGHT / 2);
        this.bounds = new RectF(left, top, left + WIDTH, top + HEIGHT);
    }

    /**
     * Método para dibujar el nodo en el canvas
     */
    public abstract void draw(Canvas canvas, Paint paint);

    /**
     * Comprueba si un punto está dentro de los límites del nodo
     */
    public boolean contains(Point point) {
        return bounds.contains(point.getX(), point.getY());
    }

    /**
     * Mueve el nodo a una nueva posición
     */
    public void moveTo(Point newPosition) {
        this.position = newPosition;
        updateBounds();
    }

    /**
     * Añade una conexión entrante
     */
    public void addInput(Connection connection) {
        if (!inputs.contains(connection)) {
            inputs.add(connection);
        }
    }

    /**
     * Añade una conexión saliente
     */
    public void addOutput(Connection connection) {
        if (!outputs.contains(connection)) {
            outputs.add(connection);
        }
    }

    /**
     * Elimina una conexión entrante
     */
    public void removeInput(Connection connection) {
        inputs.remove(connection);
    }

    /**
     * Elimina una conexión saliente
     */
    public void removeOutput(Connection connection) {
        outputs.remove(connection);
    }

    // Getters y setters
    public String getId() {
        return id;
    }

    public Point getPosition() {
        return position;
    }

    public void setPosition(Point position) {
        this.position = position;
        updateBounds();
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public List<Connection> getInputs() {
        return inputs;
    }

    public List<Connection> getOutputs() {
        return outputs;
    }

    public RectF getBounds() {
        return bounds;
    }
}