package io.github.kladdkaka.kaffe;

import com.beust.jcommander.Parameter;

public class RoastOptions {
    
    @Parameter(names = { "-h", "--help" }, help = true)
    public boolean help;

    @Parameter(names = { "--bind" }, description = "The address to bind the HTTP server to")
    public String bindAddress = "0.0.0.0";

    @Parameter(names = { "-p", "--port" }, description = "The port to bind the HTTP server to")
    public Integer port = 23000;

    @Parameter(names = { "--pid" }, description = "The PID of the VM to attach to")
    public Integer pid;

    @Parameter(names = { "--name" }, description = "The name of the VM to attach to")
    public String vmName;

    @Parameter(names = { "-t", "--thread" }, description = "Optionally specify a thread to log only")
    public String threadName;

    @Parameter(names = { "-m", "--mappings" }, description = "A directory with joined.srg and methods.csv")
    public String mappingsDir;

    @Parameter(names = { "--interval" }, description = "The sample rate, in milliseconds")
    public Integer interval = 100;
    
    @Parameter(names = { "--timeout" }, description = "The number of seconds before ceasing sampling (optional)")
    public Integer timeout;

}
