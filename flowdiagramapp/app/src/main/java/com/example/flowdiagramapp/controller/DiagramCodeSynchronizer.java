package com.example.flowdiagramapp.controller;

import com.example.flowdiagramapp.model.ConditionalElement;
import com.example.flowdiagramapp.model.Connection;
import com.example.flowdiagramapp.model.EndElement;
import com.example.flowdiagramapp.model.FlowElement;
import com.example.flowdiagramapp.model.StartElement;
import com.example.flowdiagramapp.model.VariableElement;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * This class synchronizes changes between the diagram and code.
 * It can update diagram elements based on code changes and vice versa.
 */
public class DiagramCodeSynchronizer {
    private DiagramController diagramController;

    public DiagramCodeSynchronizer(DiagramController diagramController) {
        this.diagramController = diagramController;
    }

    /**
     * Updates a flow element based on code changes.
     * @param element The element to update
     * @param codeFragment The corresponding code fragment
     */
    public void updateElementFromCode(FlowElement element, String codeFragment) {
        if (element instanceof VariableElement) {
            updateVariableFromCode((VariableElement) element, codeFragment);
        } else if (element instanceof ConditionalElement) {
            updateConditionalFromCode((ConditionalElement) element, codeFragment);
        }

        // Redraw diagram and regenerate code
        diagramController.notifyElementMoved(element);
    }

    private void updateVariableFromCode(VariableElement variable, String code) {
        // Parse variable declaration
        // Example: "int x = 10;"
        Pattern pattern = Pattern.compile("(\\w+)\\s+(\\w+)\\s*=?\\s*([^;]*);");
        Matcher matcher = pattern.matcher(code.trim());

        if (matcher.find()) {
            String type = matcher.group(1);
            String name = matcher.group(2);
            String value = matcher.group(3).trim();

            variable.setVarType(type);
            variable.setVarName(name);
            variable.setVarValue(value);
        }
    }

    private void updateConditionalFromCode(ConditionalElement conditional, String code) {
        // Parse conditional statement
        // Example: "if (x > 5) {"
        Pattern pattern = Pattern.compile("if\\s*\\(([^)]+)\\)");
        Matcher matcher = pattern.matcher(code);

        if (matcher.find()) {
            String condition = matcher.group(1).trim();
            conditional.setCondition(condition);
        }
    }

    /**
     * Creates a new element based on a code fragment.
     * @param codeFragment The code fragment
     * @param x X position for new element
     * @param y Y position for new element
     * @return The created element or null if code type not recognized
     */
    public FlowElement createElementFromCode(String codeFragment, float x, float y) {
        String trimmedCode = codeFragment.trim();

        if (trimmedCode.contains("#include") && trimmedCode.contains("main()")) {
            // Start element
            return new StartElement(-1, x, y);
        } else if (trimmedCode.contains("return 0;") && trimmedCode.contains("}")) {
            // End element
            return new EndElement(-1, x, y);
        } else if (trimmedCode.contains("if") && trimmedCode.contains("(")) {
            // Conditional element
            Pattern pattern = Pattern.compile("if\\s*\\(([^)]+)\\)");
            Matcher matcher = pattern.matcher(trimmedCode);
            if (matcher.find()) {
                String condition = matcher.group(1).trim();
                return new ConditionalElement(-1, x, y, condition);
            }
        } else {
            // Try to parse variable
            Pattern pattern = Pattern.compile("(\\w+)\\s+(\\w+)\\s*=?\\s*([^;]*);");
            Matcher matcher = pattern.matcher(trimmedCode);
            if (matcher.find()) {
                String type = matcher.group(1);
                String name = matcher.group(2);
                String value = matcher.group(3).trim();
                return new VariableElement(-1, x, y, type, name, value);
            }
        }

        return null; // Code type not recognized
    }
}