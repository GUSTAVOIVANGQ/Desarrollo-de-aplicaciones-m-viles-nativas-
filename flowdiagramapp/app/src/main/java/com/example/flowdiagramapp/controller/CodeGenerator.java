package com.example.flowdiagramapp.controller;

import com.example.flowdiagramapp.model.Connection;
import com.example.flowdiagramapp.model.FlowElement;
import com.example.flowdiagramapp.model.StartElement;
import com.example.flowdiagramapp.model.EndElement;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CodeGenerator {

    public String generateCode(List<FlowElement> elements, List<Connection> connections) {
        StringBuilder code = new StringBuilder();

        // First find the start element
        FlowElement startElement = findStartElement(elements);
        if (startElement == null) {
            return "// Diagrama incompleto: No se encontró un elemento de inicio";
        }

        // Generate code recursively starting from the start element
        Map<Integer, Boolean> visited = new HashMap<>();
        code.append(generateCodeRecursive(startElement, elements, connections, visited, 0));

        return code.toString();
    }

    private FlowElement findStartElement(List<FlowElement> elements) {
        for (FlowElement element : elements) {
            if (element instanceof StartElement) {
                return element;
            }
        }
        return null;
    }

    private String generateCodeRecursive(FlowElement element, List<FlowElement> elements,
                                         List<Connection> connections, Map<Integer, Boolean> visited, int depth) {
        // Avoid cycles
        if (visited.containsKey(element.getId())) {
            return "// Ciclo detectado\n";
        }
        visited.put(element.getId(), true);

        // Get code for this element
        String elementCode = element.generateCode();

        // Find outgoing connections
        List<Connection> outgoing = findOutgoingConnections(element, connections);

        // If this is an end element or there are no outgoing connections, just return the element code
        if (element instanceof EndElement || outgoing.isEmpty()) {
            return elementCode;
        }

        // For each outgoing connection, recursively generate code
        StringBuilder result = new StringBuilder(elementCode);
        for (Connection connection : outgoing) {
            FlowElement target = connection.getTarget();
            // Indent based on depth (not perfect for all C constructs but helps readability)
            String indent = getIndent(depth);
            result.append(indent);
            result.append(generateCodeRecursive(target, elements, connections, visited, depth + 1));
        }

        return result.toString();
    }

    private List<Connection> findOutgoingConnections(FlowElement source, List<Connection> connections) {
        List<Connection> outgoing = new ArrayList<>();
        for (Connection connection : connections) {
            if (connection.getSource().getId() == source.getId()) {
                outgoing.add(connection);
            }
        }
        return outgoing;
    }

    private String getIndent(int depth) {
        StringBuilder indent = new StringBuilder();
        for (int i = 0; i < depth; i++) {
            indent.append("    ");
        }
        return indent.toString();
    }
}
