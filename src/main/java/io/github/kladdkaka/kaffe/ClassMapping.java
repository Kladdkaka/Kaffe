package io.github.kladdkaka.kaffe;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ClassMapping {
    
    private final String obfuscated;
    private final String actual;
    private final Map<String, List<String>> methods = new HashMap<>();
    
    public ClassMapping(String obfuscated, String actual) {
        this.obfuscated = obfuscated;
        this.actual = actual;
    }

    public String getObfuscated() {
        return obfuscated;
    }
    
    public String getActual() {
        return actual;
    }
    
    public void addMethod(String obfuscated, String actual) {
        List<String> m = methods.get(obfuscated);
        if (m == null) {
            m = new ArrayList<>();
            methods.put(obfuscated, m);
        }
        m.add(actual);
    }
    
    public List<String> mapMethod(String obfuscated) {
        List<String> m = methods.get(obfuscated);
        if (m == null) {
            return new ArrayList<>();
        }
        return m;
    }
    
    @Override
    public String toString() {
        return getObfuscated() + "->" + getActual();
    }

}
