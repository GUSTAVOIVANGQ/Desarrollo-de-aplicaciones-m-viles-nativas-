package com.example.flowdiagramapp.controller;

import com.example.flowdiagramapp.model.ConditionalElement;
import com.example.flowdiagramapp.model.Connection;
import com.example.flowdiagramapp.model.EndElement;
import com.example.flowdiagramapp.model.FlowElement;
import com.example.flowdiagramapp.model.StartElement;
import com.example.flowdiagramapp.model.VariableElement;
import com.example.flowdiagramapp.view.CodeView;
import com.example.flowdiagramapp.view.FlowDiagramView;

import java.util.ArrayList;
import java.util.List;

public class DiagramController {
    private List<FlowElement> elements;
    private List<Connection> connections;
    private int nextElementId = 0;
    private int nextConnectionId = 0;

    private FlowElement selectedElement;
    private FlowDiagramView diagramView;
    private CodeView codeView;
    private CodeGenerator codeGenerator;

    public DiagramController() {
        elements = new ArrayList<>();
        connections = new ArrayList<>();
        codeGenerator = new CodeGenerator();
    }

    public void setViews(FlowDiagramView diagramView, CodeView codeView) {
        this.diagramView = diagramView;
        this.codeView = codeView;
        diagramView.setController(this);
    }

    public void addElement(String type, float x, float y) {
        FlowElement newElement = null;

        switch (type) {
            case "start":
                newElement = new StartElement(nextElementId++, x, y);
                break;
            case "end":
                newElement = new EndElement(nextElementId++, x, y);
                break;
            case "variable":
                newElement = new VariableElement(nextElementId++, x, y, "int", "varName", "0");
                break;
            case "conditional":
                newElement = new ConditionalElement(nextElementId++, x, y, "condición");
                break;
        }

        if (newElement != null) {
            elements.add(newElement);
            updateCode();
            if (diagramView != null) {
                diagramView.invalidate();
            }
        }
    }

    public void addConnection(FlowElement source, FlowElement target) {
        Connection connection = new Connection(nextConnectionId++, source, target);
        connections.add(connection);
        updateCode();
        if (diagramView != null) {
            diagramView.invalidate();
        }
    }

    public void removeElement(FlowElement element) {
        // First remove all connections to/from this element
        List<Connection> connectionsToRemove = new ArrayList<>();
        for (Connection connection : connections) {
            if (connection.getSource() == element || connection.getTarget() == element) {
                connectionsToRemove.add(connection);
            }
        }

        connections.removeAll(connectionsToRemove);
        elements.remove(element);

        if (selectedElement == element) {
            selectedElement = null;
        }

        updateCode();
        if (diagramView != null) {
            diagramView.invalidate();
        }
    }

    public void updateElement(FlowElement element) {
        // Update element properties based on type
        if (element instanceof VariableElement) {
            // Update variable properties
            // This would typically be done through a dialog
        } else if (element instanceof ConditionalElement) {
            // Update conditional properties
        }

        updateCode();
        if (diagramView != null) {
            diagramView.invalidate();
        }
    }

    public void notifyElementMoved(FlowElement element) {
        // Called when an element is moved
        // We don't need to update the code for this, but we might want to in a more complex app
        if (diagramView != null) {
            diagramView.invalidate();
        }
    }

    public List<FlowElement> getElements() {
        return elements;
    }

    public List<Connection> getConnections() {
        return connections;
    }

    public void setSelectedElement(FlowElement element) {
        this.selectedElement = element;
        // Here you would typically update UI to show element properties
    }

    public FlowElement getSelectedElement() {
        return selectedElement;
    }

    private void updateCode() {
        if (codeView != null) {
            String generatedCode = codeGenerator.generateCode(elements, connections);
            codeView.setCode(generatedCode);
        }
    }

    // Method to create a sample diagram for testing
    public void createSampleDiagram() {
        // Clear existing elements and connections
        elements.clear();
        connections.clear();
        nextElementId = 0;
        nextConnectionId = 0;

        // Create a simple program with start, variable, conditional, and end
        StartElement start = new StartElement(nextElementId++, 200, 100);
        VariableElement var = new VariableElement(nextElementId++, 200, 200, "int", "x", "10");
        ConditionalElement cond = new ConditionalElement(nextElementId++, 200, 300, "x > 5");
        EndElement end = new EndElement(nextElementId++, 200, 400);

        // Add elements
        elements.add(start);
        elements.add(var);
        elements.add(cond);
        elements.add(end);

        // Connect them
        connections.add(new Connection(nextConnectionId++, start, var));
        connections.add(new Connection(nextConnectionId++, var, cond));
        connections.add(new Connection(nextConnectionId++, cond, end));

        // Update the view and code
        updateCode();
        if (diagramView != null) {
            diagramView.invalidate();
        }
    }
}