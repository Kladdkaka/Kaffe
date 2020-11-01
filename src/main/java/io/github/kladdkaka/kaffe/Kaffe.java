package io.github.kladdkaka.kaffe;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.management.ManagementFactory;
import java.lang.management.ThreadInfo;
import java.lang.management.ThreadMXBean;
import java.net.InetSocketAddress;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedMap;
import java.util.Timer;
import java.util.TimerTask;
import java.util.TreeMap;

import javax.management.MBeanServerConnection;
import javax.management.MalformedObjectNameException;
import javax.management.ObjectName;
import javax.management.remote.JMXConnector;
import javax.management.remote.JMXConnectorFactory;
import javax.management.remote.JMXServiceURL;

import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.servlet.DefaultServlet;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;

import com.beust.jcommander.JCommander;
import com.sun.tools.attach.AgentInitializationException;
import com.sun.tools.attach.AgentLoadException;
import com.sun.tools.attach.AttachNotSupportedException;
import com.sun.tools.attach.VirtualMachine;
import com.sun.tools.attach.VirtualMachineDescriptor;

public class Kaffe extends TimerTask {

    private static final String SEPARATOR = 
            "------------------------------------------------------------------------";
    
    private final int interval;
    private final VirtualMachine vm;
    private final Timer timer = new Timer("Roast Pan", true);
    private final SortedMap<String, StackNode> nodes = new TreeMap<>();
    private JMXConnector connector;
    private MBeanServerConnection mbsc;
    private ThreadMXBean threadBean;
    private String filterThread;
    private long endTime = -1;
    
    public Kaffe(VirtualMachine vm, int interval) {
        this.vm = vm;
        this.interval = interval;
    }
    
    public Map<String, StackNode> getData() {
        return nodes;
    }
    
    private StackNode getNode(String name) {
        StackNode node = nodes.get(name);
        if (node == null) {
            node = new StackNode(name);
            nodes.put(name, node);
        }
        return node;
    }

    public String getFilterThread() {
        return filterThread;
    }

    public void setFilterThread(String filterThread) {
        this.filterThread = filterThread;
    }

    public long getEndTime() {
        return endTime;
    }

    public void setEndTime(long l) {
        this.endTime = l;
    }

    public void connect() 
            throws IOException, AgentLoadException, AgentInitializationException {
        // Load the agent
        String connectorAddr = vm.getAgentProperties().getProperty(
                "com.sun.management.jmxremote.localConnectorAddress");
        if (connectorAddr == null) {
            String agent = vm.getSystemProperties().getProperty("java.home")
                    + File.separator + "lib" + File.separator
                    + "management-agent.jar";
            vm.loadAgent(agent);
            connectorAddr = vm.getAgentProperties().getProperty(
                    "com.sun.management.jmxremote.localConnectorAddress");
        }

        // Connect
        JMXServiceURL serviceURL = new JMXServiceURL(connectorAddr);
        connector = JMXConnectorFactory.connect(serviceURL);
        mbsc = connector.getMBeanServerConnection();
        try {
            threadBean = getThreadMXBean();
        } catch (MalformedObjectNameException e) {
            throw new IOException("Bad MX bean name", e);
        }
    }

    private ThreadMXBean getThreadMXBean() 
            throws IOException, MalformedObjectNameException {
        ObjectName objName = new ObjectName(ManagementFactory.THREAD_MXBEAN_NAME);
        Set<ObjectName> mbeans = mbsc.queryNames(objName, null);
        for (ObjectName name : mbeans) {
            return ManagementFactory.newPlatformMXBeanProxy(
                    mbsc, name.toString(), ThreadMXBean.class);
        }
        throw new IOException("No thread MX bean found");
    }

    @Override
    public synchronized void run() {
        if (endTime >= 0) {
            if (endTime <= System.currentTimeMillis()) {
                cancel();
                System.err.println("Sampling has stopped.");
                return;
            }
        }
        
        ThreadInfo[] threadDumps = threadBean.dumpAllThreads(false, false);
        for (ThreadInfo threadInfo : threadDumps) {
            String threadName = threadInfo.getThreadName();
            StackTraceElement[] stack = threadInfo.getStackTrace();
            
            if (threadName == null || stack == null) {
                continue;
            }
            
            if (filterThread != null && !filterThread.equals(threadName)) {
                continue;
            }
            
            StackNode node = getNode(threadName);
            node.log(stack, interval);
        }
    }

    public void start(InetSocketAddress address) throws Exception {
        timer.scheduleAtFixedRate(this, interval, interval);
        
        Server server = new Server(address);

        ServletContextHandler context = new ServletContextHandler();
        context.setContextPath("/");
        server.setHandler(context);

        ServletHolder holderDataViewServlet = new ServletHolder(new DataViewServlet(this));
        context.addServlet(holderDataViewServlet, "/stack");

        String filesDir = Kaffe.class.getResource("/www").toExternalForm();

        ServletHolder holderResources = new ServletHolder(DefaultServlet.class);
        holderResources.setInitParameter("resourceBase", filesDir);
        holderResources.setInitParameter("dirAllowed","true");
        holderResources.setInitParameter("pathInfoOnly","true");
        context.addServlet(holderResources,"/*");

        server.start();
        server.join();
    }

    public static void main(String[] args) throws AgentLoadException {
        RoastOptions opt = new RoastOptions();
        JCommander jc = new JCommander(opt);
        jc.parse(args);
        jc.setProgramName("kaffe");
        
        if (opt.help) {
            jc.usage();
            System.exit(0);
        }

        System.err.println(SEPARATOR);
        System.err.println("Kaffe");
        System.err.println("https://github.com/Kladdkaka/Kaffe");
        System.err.println(SEPARATOR);
        System.err.println("");
        
        VirtualMachine vm = null;
        
        if (opt.pid != null) {
            try {
                vm = VirtualMachine.attach(String.valueOf(opt.pid));
                System.err.println("Attaching to PID " + opt.pid + "...");
            } catch (AttachNotSupportedException | IOException e) {
                System.err.println("Failed to attach VM by PID " + opt.pid);
                e.printStackTrace();
                System.exit(1);
            }
        } else if (opt.vmName != null) {
            for (VirtualMachineDescriptor desc : VirtualMachine.list()) {
                if (desc.displayName().contains(opt.vmName)) {
                    try {
                        vm = VirtualMachine.attach(desc);
                        System.err.println("Attaching to '" + desc.displayName() + "'...");
                        
                        break;
                    } catch (AttachNotSupportedException | IOException e) {
                        System.err.println("Failed to attach VM by name '" + opt.vmName + "'");
                        e.printStackTrace();
                        System.exit(1);
                    }
                }
            }
        }
        
        if (vm == null) {
            List<VirtualMachineDescriptor> descriptors = VirtualMachine.list();
            System.err.println("Choose a VM:");
            
            descriptors.sort(Comparator.comparing(VirtualMachineDescriptor::displayName));
            
            // Print list of VMs
            int i = 1;
            for (VirtualMachineDescriptor desc : descriptors) {
                System.err.println("[" + (i++) + "] " + desc.displayName());
            }
            
            // Ask for choice
            System.err.println("");
            System.err.print("Enter choice #: ");
            BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
            String s;
            try {
                s = reader.readLine();
            } catch (IOException e) {
                return;
            }
            
            // Get the VM
            try {
                int choice = Integer.parseInt(s) - 1;
                if (choice < 0 || choice >= descriptors.size()) {
                    System.err.println("");
                    System.err.println("Given choice is out of range.");
                    System.exit(1);
                }
                vm = VirtualMachine.attach(descriptors.get(choice));
            } catch (NumberFormatException e) {
                System.err.println("");
                System.err.println("That's not a number. Bye.");
                System.exit(1);
            } catch (AttachNotSupportedException | IOException e) {
                System.err.println("");
                System.err.println("Failed to attach VM");
                e.printStackTrace();
                System.exit(1);
            }
        }
        
        InetSocketAddress address = new InetSocketAddress(opt.bindAddress, opt.port);

        Kaffe roast = new Kaffe(vm, opt.interval);

        System.err.println(SEPARATOR);
        
        roast.setFilterThread(opt.threadName);
        
        if (opt.timeout != null && opt.timeout > 0) {
            roast.setEndTime(System.currentTimeMillis() + opt.timeout * 1000);
            System.err.println("Sampling set to stop in " + opt.timeout + " seconds.");
        }

        System.err.println("Starting a server on " + address.toString() + "...");
        System.err.println("Once the server starts (shortly), visit the URL in your browser.");
        System.err.println("Note: The longer you wait before using the output of that " +
        		"webpage, the more accurate the results will be.");
        
        try {
            roast.connect();
            roast.start(address);
        } catch (Throwable t) {
            t.printStackTrace();
            System.exit(3);
        }
    }

}
