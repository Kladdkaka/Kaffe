package io.github.kladdkaka.kaffe;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class StackNode implements Comparable<StackNode> {
    
    private static final NumberFormat cssDec = NumberFormat.getPercentInstance(Locale.US);
    private final String name;
    private final Map<String, StackNode> children = new HashMap<>();
    private long totalTime;
    
    static {
        cssDec.setGroupingUsed(false);
        cssDec.setMaximumFractionDigits(2);
    }

    public StackNode(String name) {
        this.name = name;
    }
    
    public String getName() {
        return name;
    }
    
    public String getNameHtml() {
        return escapeHtml(getName());
    }

    public Collection<StackNode> getChildren() {
        List<StackNode> list = new ArrayList<>(children.values());
        Collections.sort(list);
        return list;
    }
    
    public StackNode getChild(String name) {
        StackNode child = children.get(name);
        if (child == null) {
            child = new StackNode(name);
            children.put(name, child);
        }
        return child;
    }
    
    public StackNode getChild(String className, String methodName) {
        StackTraceNode node = new StackTraceNode(className, methodName);
        StackNode child = children.get(node.getName());
        if (child == null) {
            child = node;
            children.put(node.getName(), node);
        }
        return child;
    }
    
    public long getTotalTime() {
        return totalTime;
    }

    public void log(long time) {
        totalTime += time;
    }
    
    private void log(StackTraceElement[] elements, int skip, long time) {
        log(time);
        
        if (elements.length - skip == 0) {
            return;
        }
        
        StackTraceElement bottom = elements[elements.length - (skip + 1)];
        getChild(bottom.getClassName(), bottom.getMethodName())
                .log(elements, skip + 1, time);
    }
    
    public void log(StackTraceElement[] elements, long time) {
        log(elements, 0, time);
    }

    @Override
    public int compareTo(StackNode o) {
        return getName().compareTo(o.getName());
    }
    
    private void writeHtml(StringBuilder builder, long totalTime) {
        builder.append("<div class=\"node collapsed\">");
        builder.append("<div class=\"name\">");
        builder.append(getNameHtml());
        builder.append("<span class=\"percent\">");
        builder
                .append(String.format("%.2f", getTotalTime() / (double) totalTime * 100))
                .append("%");
        builder.append("</span>");
        builder.append("<span class=\"time\">");
        builder.append(getTotalTime()).append("ms");
        builder.append("</span>");
        builder.append("<span class=\"bar\">");
        builder.append("<span class=\"bar-inner\" style=\"width:")
                .append(formatCssPct(getTotalTime() / (double) totalTime))
                .append("\">");
        builder.append("</span>");
        builder.append("</span>");
        builder.append("</div>");
        builder.append("<ul class=\"children\">");
        for (StackNode child : getChildren()) {
            builder.append("<li>");
            child.writeHtml(builder, totalTime);
            builder.append("</li>");
        }
        builder.append("</ul>");
        builder.append("</div>");
    }

    public String toHtml() {
        StringBuilder builder = new StringBuilder();
        writeHtml(builder, getTotalTime());
        return builder.toString();
    }
    
    private void writeString(StringBuilder builder, int indent) {
        StringBuilder b = new StringBuilder();
        for (int i = 0; i < indent; i++) {
            b.append(" ");
        }
        String padding = b.toString();
        
        for (StackNode child : getChildren()) {
            builder.append(padding).append(child.getName());
            builder.append(" ");
            builder.append(getTotalTime()).append("ms");
            builder.append("\n");
            child.writeString(builder, indent + 1);
        }
    }
    
    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        writeString(builder, 0);
        return builder.toString();
    }
    
    protected static String formatCssPct(double pct) {
        return cssDec.format(pct);
    }
    
    protected static String escapeHtml(String str) {
        return str.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;");
    }

}
