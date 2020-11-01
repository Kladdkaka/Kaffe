package io.github.kladdkaka.kaffe;

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
    public int compareTo(StackNode o) {
        return Long.compare(o.getTotalTime(), getTotalTime());
    }

}
