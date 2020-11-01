package io.github.kladdkaka.kaffe;

import j2html.tags.Tag;

import static j2html.TagCreator.*;

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

    public StackNode(String name, long totalTime) {
        this.name = name;
        this.totalTime = totalTime;
    }

    /**
     * For regression testing only
     */
    protected StackNode(String name, long totalTime, StackNode... children) {
        this.name = name;
        this.totalTime = totalTime;
        for (StackNode child : children) {
            this.children.put(child.getName(), child);
        }
    }

    public String getName() {
        return name;
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

    private Tag generateHtml(long totalTime) {
        return div(attrs(".node collapsed"),
                div(attrs(".name"), text(getName()),
                        span(attrs(".percent"), String.format("%.2f", getTotalTime() / (double) totalTime * 100) + "%"),
                        span(attrs(".time"), getTotalTime() + "ms"),
                        span(attrs(".bar"),
                                span(attrs(".bar-inner")).withStyle("width:" + formatCssPct(getTotalTime() / (double) totalTime))
                        )
                ),
                ul(attrs(".children"), each(getChildren(), child -> li(child.generateHtml(totalTime))))
        );
    }

    public String toHtml() {
        return generateHtml(getTotalTime()).render();
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

}
