package io.github.kladdkaka.kaffe;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.Collection;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class DataViewServlet extends HttpServlet {
    
    private static final long serialVersionUID = -2331397310804298286L;
    
    private final Kaffe roast;

    public DataViewServlet(Kaffe roast) {
        this.roast = roast;
    }

    @Override
    protected void doGet(HttpServletRequest request,
            HttpServletResponse response) throws ServletException, IOException {
        response.setContentType("text/html; charset=utf-8");
        response.setStatus(HttpServletResponse.SC_OK);
        
        PrintWriter w = response.getWriter();
        w.println("<!DOCTYPE html><html><head><title>Kaffe</title>");
        w.println("<link rel=\"stylesheet\" type=\"text/css\" href=\"style.css\">");
        w.println("</head><body>");
        w.println("<h1>Kaffe</h1>");
        w.println("<div class=\"loading\">Downloading snapshot; please wait...</div>");
        w.println("<div class=\"stack\" style=\"display: none\">");
        synchronized (roast) {
            Collection<StackNode> nodes = roast.getData().values();
            for (StackNode node : nodes) {
                w.println(node.toHtml());
            }
            if (nodes.size() == 0) {
                w.println("<p class=\"no-results\">There are no results. " +
                		"(Thread filter does not match thread?)</p>");
            }
        }
        w.println("</div>");
        w.println("<p class=\"legend\">Legend: ");
        w.println("<span class=\"matched\">Mapped</span> ");
        w.println("<span class=\"multiple-matches\">Multiple Mappings</span> ");
        w.println("</p>");
        w.println("<div id=\"overlay\"></div>");
        w.println("<p class=\"footer\">");
        w.println("Icons from <a href=\"http://www.fatcow.com/\">FatCow</a> &mdash; ");
        w.println("<a href=\"https://github.com/Kladdkaka/Kaffe\">github.com/Kladdkaka/Kaffe</a></p>");
        w.println("<script src=\"//ajax.googleapis.com/ajax/libs/jquery/1.10.1/jquery.min.js\"></script>");
        w.println("<script src=\"kaffe.js\"></script>");
        w.println("</body></html>");
    }
}