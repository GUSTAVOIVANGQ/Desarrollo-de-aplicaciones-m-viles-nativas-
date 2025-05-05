package com.example.flowdiagramapp.view;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.View;

import com.example.flowdiagramapp.model.Connection;
import com.example.flowdiagramapp.model.ConditionalNode;
import com.example.flowdiagramapp.model.EndNode;
import com.example.flowdiagramapp.model.Node;
import com.example.flowdiagramapp.model.Point;
import com.example.flowdiagramapp.model.StartNode;
import com.example.flowdiagramapp.model.VariableNode;

import java.util.ArrayList;
import java.util.List;

/**
 * Vista personalizada que representa un editor de diagramas de flujo
 */
public class FlowDiagramView extends View {
    
    // Lista de todos los nodos en el diagrama
    private List<Node> nodes;
    // Lista de todas las conexiones en el diagrama
    private List<Connection> connections;
    
    // Variables para manejar la interacción del usuario
    private Node selectedNode;
    private Connection selectedConnection;
    private Point lastTouchPoint;
    private Point startConnectionPoint;
    private Node sourceNode;
    private boolean isCreatingConnection;
    
    // Para el zoom y pan
    private float scaleFactor = 1.0f;
    private float translateX = 0.0f;
    private float translateY = 0.0f;
    private ScaleGestureDetector scaleDetector;
    
    // Paint objects
    private Paint nodePaint;
    private Paint selectedPaint;
    private Paint connectionPaint;
    private Paint tempConnectionPaint;
    private Paint backgroundPaint;
    
    // Constantes
    private static final float TOUCH_TOLERANCE = 20f;
    private static final float CONNECTION_TOLERANCE = 30f;

    // Constructor
    public FlowDiagramView(Context context) {
        super(context);
        init();
    }

    public FlowDiagramView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public FlowDiagramView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }
    
    /**
     * Inicializa la vista
     */
    private void init() {
        // Inicializar colecciones
        nodes = new ArrayList<>();
        connections = new ArrayList<>();
        
        // Inicializar pinturas
        nodePaint = new Paint();
        nodePaint.setColor(Color.WHITE);
        nodePaint.setAntiAlias(true);
        
        selectedPaint = new Paint();
        selectedPaint.setColor(Color.rgb(173, 216, 230)); // Light blue
        selectedPaint.setAntiAlias(true);
        
        connectionPaint = new Paint();
        connectionPaint.setColor(Color.BLACK);
        connectionPaint.setAntiAlias(true);
        connectionPaint.setStrokeWidth(3f);
        
        tempConnectionPaint = new Paint(connectionPaint);
        tempConnectionPaint.setColor(Color.GRAY);
        tempConnectionPaint.setStrokeWidth(2f);
        tempConnectionPaint.setStyle(Paint.Style.STROKE);
        
        backgroundPaint = new Paint();
        backgroundPaint.setColor(Color.rgb(240, 240, 240)); // Light gray
        
        // Inicializar detector de gestos para zoom
        scaleDetector = new ScaleGestureDetector(getContext(), new ScaleListener());
        
        // Establecer banderas para habilitar UI más suave
        setLayerType(LAYER_TYPE_HARDWARE, null);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        
        // Dibujar fondo
        canvas.drawRect(0, 0, getWidth(), getHeight(), backgroundPaint);
        
        // Aplicar transformaciones para zoom y pan
        canvas.save();
        canvas.translate(translateX, translateY);
        canvas.scale(scaleFactor, scaleFactor);
        
        // Dibujar todas las conexiones
        for (Connection connection : connections) {
            Paint paint = connection == selectedConnection ? selectedPaint : connectionPaint;
            connection.draw(canvas, paint);
        }
        
        // Dibujar conexión temporal durante la creación
        if (isCreatingConnection && sourceNode != null && lastTouchPoint != null) {
            // Simular una conexión temporal desde el nodo fuente al punto donde está el dedo
            Point start = new Point(sourceNode.getPosition().getX(), sourceNode.getPosition().getY());
            drawArrow(canvas, tempConnectionPaint, start, lastTouchPoint);
        }
        
        // Dibujar todos los nodos
        for (Node node : nodes) {
            Paint paint = node == selectedNode ? selectedPaint : nodePaint;
            node.draw(canvas, paint);
        }
        
        canvas.restore();
    }
    
    /**
     * Método auxiliar para dibujar una flecha temporal durante la creación de conexiones
     */
    private void drawArrow(Canvas canvas, Paint paint, Point start, Point end) {
        canvas.drawLine(start.getX(), start.getY(), end.getX(), end.getY(), paint);
        
        // Calcular la punta de flecha
        float angle = (float) Math.atan2(end.getY() - start.getY(), end.getX() - start.getX());
        float arrowSize = 15f;

        float arrowX1 = (float) (end.getX() - arrowSize * Math.cos(angle - Math.PI/6));
        float arrowY1 = (float) (end.getY() - arrowSize * Math.sin(angle - Math.PI/6));
        float arrowX2 = (float) (end.getX() - arrowSize * Math.cos(angle + Math.PI/6));
        float arrowY2 = (float) (end.getY() - arrowSize * Math.sin(angle + Math.PI/6));

        canvas.drawLine(end.getX(), end.getY(), arrowX1, arrowY1, paint);
        canvas.drawLine(end.getX(), end.getY(), arrowX2, arrowY2, paint);
    }
    
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        // Procesar gestos de zoom
        scaleDetector.onTouchEvent(event);
        
        // Convertir las coordenadas de pantalla a coordenadas del diagrama
        float x = (event.getX() - translateX) / scaleFactor;
        float y = (event.getY() - translateY) / scaleFactor;
        Point touchPoint = new Point(x, y);
        
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                handleActionDown(touchPoint);
                break;
                
            case MotionEvent.ACTION_MOVE:
                handleActionMove(touchPoint);
                break;
                
            case MotionEvent.ACTION_UP:
                handleActionUp(touchPoint);
                break;
        }
        
        invalidate(); // Redibuja la vista
        return true;
    }
    
    /**
     * Maneja el evento de tocar la pantalla
     */
    private void handleActionDown(Point touchPoint) {
        lastTouchPoint = touchPoint;
        
        // Comprobar si se tocó un nodo
        selectedNode = findNodeAt(touchPoint);
        if (selectedNode != null) {
            // Si se seleccionó un nodo, iniciar la creación de conexión
            sourceNode = selectedNode;
            startConnectionPoint = touchPoint;
            isCreatingConnection = false; // Todavía no estamos creando una conexión, solo seleccionando
            return;
        }
        
        // Si no se tocó un nodo, comprobar si se tocó una conexión
        selectedConnection = findConnectionNear(touchPoint);
        
        // Si no se seleccionó nada, deseleccionar todo
        if (selectedNode == null && selectedConnection == null) {
            deselectAll();
        }
    }
    
    /**
     * Maneja el evento de mover el dedo por la pantalla
     */
    private void handleActionMove(Point touchPoint) {
        if (selectedNode != null) {
            // Calcular la distancia recorrida desde el punto inicial
            float dx = touchPoint.getX() - lastTouchPoint.getX();
            float dy = touchPoint.getY() - lastTouchPoint.getY();
            
            // Si el movimiento es significativo, considerar que estamos arrastrando el nodo
            if (Math.abs(dx) > TOUCH_TOLERANCE || Math.abs(dy) > TOUCH_TOLERANCE) {
                if (!isCreatingConnection) {
                    // Mover el nodo
                    Point newPosition = new Point(
                            selectedNode.getPosition().getX() + dx,
                            selectedNode.getPosition().getY() + dy);
                    selectedNode.setPosition(newPosition);
                } else {
                    // Estamos creando una conexión
                    // Solo actualizar el punto final para dibujar la línea temporal
                }
            } else if (!isCreatingConnection && Math.abs(dx) > TOUCH_TOLERANCE*3 || Math.abs(dy) > TOUCH_TOLERANCE*3) {
                // Si el movimiento es más grande, iniciar creación de conexión
                isCreatingConnection = true;
            }
        } else if (selectedConnection == null) {
            // Si no hay nada seleccionado, estamos haciendo pan (desplazamiento)
            translateX += (touchPoint.getX() - lastTouchPoint.getX()) * scaleFactor;
            translateY += (touchPoint.getY() - lastTouchPoint.getY()) * scaleFactor;
        }
        
        // Actualizar último punto tocado
        lastTouchPoint = touchPoint;
    }
    
    /**
     * Maneja el evento de levantar el dedo de la pantalla
     */
    private void handleActionUp(Point touchPoint) {
        if (isCreatingConnection && sourceNode != null) {
            // Comprobar si el punto final está sobre un nodo
            Node targetNode = findNodeAt(touchPoint);
            if (targetNode != null && targetNode != sourceNode) {
                // Crear una nueva conexión entre los nodos
                createConnection(sourceNode, targetNode);
            }
            
            // Resetear estado
            isCreatingConnection = false;
            sourceNode = null;
        }
        
        lastTouchPoint = null;
    }

    /**
     * Clase interna para manejar gestos de zoom
     */
    private class ScaleListener extends ScaleGestureDetector.SimpleOnScaleGestureListener {
        @Override
        public boolean onScale(ScaleGestureDetector detector) {
            scaleFactor *= detector.getScaleFactor();
            
            // Limitar el factor de escala
            scaleFactor = Math.max(0.5f, Math.min(scaleFactor, 5.0f));
            
            invalidate();
            return true;
        }
    }
    
    /**
     * Busca un nodo en las coordenadas dadas
     */
    private Node findNodeAt(Point point) {
        for (int i = nodes.size() - 1; i >= 0; i--) { // Empezar por los nodos superiores
            Node node = nodes.get(i);
            if (node.contains(point)) {
                return node;
            }
        }
        return null;
    }
    
    /**
     * Busca una conexión cerca de las coordenadas dadas
     */
    private Connection findConnectionNear(Point point) {
        for (Connection connection : connections) {
            if (connection.isNear(point, CONNECTION_TOLERANCE)) {
                return connection;
            }
        }
        return null;
    }
    
    /**
     * Crea una conexión entre dos nodos
     */
    private void createConnection(Node source, Node target) {
        // Verificar si ya existe una conexión entre estos nodos
        for (Connection conn : connections) {
            if (conn.getSource() == source && conn.getTarget() == target) {
                return; // Ya existe esta conexión
            }
        }
        
        Connection connection = new Connection(source, target);
        connections.add(connection);
        
        source.addOutput(connection);
        target.addInput(connection);
        
        // Si el nodo fuente es condicional, establecer etiquetas adecuadas
        if (source instanceof ConditionalNode) {
            if (target == findNodeToTheRight(source)) {
                connection.setLabel("Si");
            } else if (target == findNodeToTheLeft(source)) {
                connection.setLabel("No");
            }
        }
    }
    
    /**
     * Encuentra el nodo más cercano a la derecha del nodo dado
     */
    private Node findNodeToTheRight(Node node) {
        Node rightNode = null;
        float minDistance = Float.MAX_VALUE;
        
        for (Node n : nodes) {
            if (n != node && n.getPosition().getX() > node.getPosition().getX()) {
                float distance = n.getPosition().distanceTo(node.getPosition());
                if (distance < minDistance) {
                    minDistance = distance;
                    rightNode = n;
                }
            }
        }
        
        return rightNode;
    }
    
    /**
     * Encuentra el nodo más cercano a la izquierda del nodo dado
     */
    private Node findNodeToTheLeft(Node node) {
        Node leftNode = null;
        float minDistance = Float.MAX_VALUE;
        
        for (Node n : nodes) {
            if (n != node && n.getPosition().getX() < node.getPosition().getX()) {
                float distance = n.getPosition().distanceTo(node.getPosition());
                if (distance < minDistance) {
                    minDistance = distance;
                    leftNode = n;
                }
            }
        }
        
        return leftNode;
    }
    
    /**
     * Deselecciona todos los elementos
     */
    private void deselectAll() {
        selectedNode = null;
        selectedConnection = null;
        sourceNode = null;
        isCreatingConnection = false;
    }
    
    // --- Métodos públicos para manipular el diagrama ---
    
    /**
     * Añade un nuevo nodo de inicio
     */
    public StartNode addStartNode(float x, float y) {
        Point position = new Point(x, y);
        StartNode node = new StartNode(position);
        nodes.add(node);
        invalidate();
        return node;
    }
    
    /**
     * Añade un nuevo nodo de fin
     */
    public EndNode addEndNode(float x, float y) {
        Point position = new Point(x, y);
        EndNode node = new EndNode(position);
        nodes.add(node);
        invalidate();
        return node;
    }
    
    /**
     * Añade un nuevo nodo de variable
     */
    public VariableNode addVariableNode(float x, float y, String text) {
        Point position = new Point(x, y);
        VariableNode node = new VariableNode(position, text);
        nodes.add(node);
        invalidate();
        return node;
    }
    
    /**
     * Añade un nuevo nodo condicional
     */
    public ConditionalNode addConditionalNode(float x, float y, String condition) {
        Point position = new Point(x, y);
        ConditionalNode node = new ConditionalNode(position, condition);
        nodes.add(node);
        invalidate();
        return node;
    }
    
    /**
     * Elimina un nodo y sus conexiones
     */
    public void removeNode(Node node) {
        if (node == null) return;
        
        // Eliminar todas las conexiones asociadas
        List<Connection> connectionsToRemove = new ArrayList<>();
        for (Connection conn : connections) {
            if (conn.getSource() == node || conn.getTarget() == node) {
                connectionsToRemove.add(conn);
            }
        }
        
        for (Connection conn : connectionsToRemove) {
            removeConnection(conn);
        }
        
        // Eliminar el nodo
        nodes.remove(node);
        if (selectedNode == node) {
            selectedNode = null;
        }
        
        invalidate();
    }
    
    /**
     * Elimina una conexión
     */
    public void removeConnection(Connection connection) {
        if (connection == null) return;
        
        // Eliminar referencias en los nodos
        Node source = connection.getSource();
        Node target = connection.getTarget();
        
        if (source != null) {
            source.removeOutput(connection);
        }
        
        if (target != null) {
            target.removeInput(connection);
        }
        
        // Eliminar la conexión
        connections.remove(connection);
        if (selectedConnection == connection) {
            selectedConnection = null;
        }
        
        invalidate();
    }
    
    /**
     * Limpia todo el diagrama
     */
    public void clear() {
        nodes.clear();
        connections.clear();
        deselectAll();
        invalidate();
    }
    
    /**
     * Obtiene el nodo seleccionado actualmente
     */
    public Node getSelectedNode() {
        return selectedNode;
    }
    
    /**
     * Obtiene la conexión seleccionada actualmente
     */
    public Connection getSelectedConnection() {
        return selectedConnection;
    }
    
    /**
     * Establece el centro de la vista
     */
    public void centerView() {
        translateX = getWidth() / 2f;
        translateY = getHeight() / 2f;
        scaleFactor = 1.0f;
        invalidate();
    }
    
    /**
     * Obtiene todos los nodos del diagrama
     */
    public List<Node> getNodes() {
        return new ArrayList<>(nodes);
    }
    
    /**
     * Obtiene todas las conexiones del diagrama
     */
    public List<Connection> getConnections() {
        return new ArrayList<>(connections);
    }
}