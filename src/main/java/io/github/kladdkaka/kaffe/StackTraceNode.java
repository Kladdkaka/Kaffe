package io.github.kladdkaka.kaffe;

import java.util.List;

public class StackTraceNode extends StackNode {
    
    private final String className;
    private final String methodName;

    public StackTraceNode(String className, String methodName) {
        super(className + "." + methodName + "()");
        this.className = className;
        this.methodName = methodName;
    }

    public String getClassName() {
        return className;
    }

    public String getMethodName() {
        return methodName;
    }
    
    @Override
    public String getNameHtml(McpMapping mapping) {
        ClassMapping classMapping = mapping.mapClass(getClassName());
        if (classMapping != null) {
            String className = "<span class=\"matched\" title=\"" + 
                    escapeHtml(getClassName()) + "\">" +
                    escapeHtml(classMapping.getActual()) + "</span>";
            
            List<String> actualMethods = classMapping.mapMethod(getMethodName());
            if (actualMethods.size() == 0) {
                return className + "." + escapeHtml(getMethodName()) + "()";
            } else if (actualMethods.size() == 1) {
                return className + 
                        ".<span class=\"matched\" title=\"" + 
                        escapeHtml(getMethodName()) + "\">" + 
                        escapeHtml(actualMethods.get(0)) + "</span>()";
            } else {
                StringBuilder builder = new StringBuilder();
                boolean first = true;
                for (String m : actualMethods) {
                    if (!first) {
                        builder.append(" ");
                    }
                    builder.append(m);
                    first = false;
                }
                return className + 
                        ".<span class=\"multiple-matches\" title=\"" + 
                            builder.toString() + "\">" + escapeHtml(getMethodName()) + "</span>()";
            }
        } else {
            String actualMethod = mapping.mapMethodId(getMethodName());
            if (actualMethod == null) {
                return escapeHtml(getClassName()) + "." + escapeHtml(getMethodName()) + "()";
            } else {
                return className + 
                        ".<span class=\"matched\" title=\"" + 
                        escapeHtml(getMethodName()) + "\">" + 
                        escapeHtml(actualMethod) + "</span>()";
            }
        }
    }

    @Override
    public int compareTo(StackNode o) {
        if (getTotalTime() == o.getTotalTime()) {
            return 0;
        } else if (getTotalTime()> o.getTotalTime()) {
            return -1;
        } else {
            return 1;
        }
    }

}
