package io.github.kladdkaka.kaffe;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.opencsv.exceptions.CsvException;
import org.apache.commons.io.FileUtils;

import com.opencsv.CSVReader;

public class McpMapping {

    private static final Pattern clPattern = 
            Pattern.compile("CL: (?<obfuscated>[^ ]+) (?<actual>[^ ]+)");
    private static final Pattern mdPattern = 
            Pattern.compile("MD: (?<obfuscatedClass>[^ /]+)/(?<obfuscatedMethod>[^ ]+) " +
            		"[^ ]+ (?<method>[^ ]+) [^ ]+");

    private final Map<String, ClassMapping> classes = new HashMap<>();
    private final Map<String, String> methods = new HashMap<>();
    
    public ClassMapping mapClass(String obfuscated) {
        return classes.get(obfuscated);
    }

    public void read(File joinedFile, File methodsFile) throws IOException, CsvException {
        try (FileReader r = new FileReader(methodsFile)) {
            try (CSVReader reader = new CSVReader(r)) {
                List<String[]> entries = reader.readAll();
                processMethodNames(entries);
            }
        }
        
        List<String> lines = FileUtils.readLines(joinedFile, "UTF-8");
        processClasses(lines);
        processMethods(lines);
    }
    
    public String mapMethodId(String id) {
        return methods.get(id);
    }
    
    public String fromMethodId(String id) {
        String method = methods.get(id);
        if (method == null) {
            return id;
        }
        return method;
    }
    
    private void processMethodNames(List<String[]> entries) {
        boolean first = true;
        for (String[] entry : entries) {
            if (entry.length < 2) {
                continue;
            }
            if (first) { // Header
                first = false;
                continue;
            }
            methods.put(entry[0], entry[1]);
        }
    }
    
    private void processClasses(List<String> lines) {
        for (String line : lines) {
            Matcher m = clPattern.matcher(line);
            if (m.matches()) {
                String obfuscated = m.group("obfuscated");
                String actual = m.group("actual").replace("/", ".");
                classes.put(obfuscated, new ClassMapping(obfuscated, actual));
            }
        }
    }
    
    private void processMethods(List<String> lines) {
        for (String line : lines) {
            Matcher m = mdPattern.matcher(line);
            if (m.matches()) {
                String obfuscatedClass = m.group("obfuscatedClass");
                String obfuscatedMethod = m.group("obfuscatedMethod");
                String method = m.group("method");
                String methodId = method.substring(method.lastIndexOf('/') + 1);
                ClassMapping mapping = mapClass(obfuscatedClass);
                if (mapping != null) {
                    mapping.addMethod(obfuscatedMethod, 
                            fromMethodId(methodId));
                }
            }
        }
    }

}
