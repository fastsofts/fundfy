
package com.fundfy.capinfoservice;

import com.fundfy.capinfoservice.controller.CapInfoController;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;

import java.util.logging.Logger;

public class Main {
    private static final Logger LOGGER = Logger.getLogger(Main.class.getName());    
    public static void main(String[] args) throws Exception {
        Server server = new Server(8082);

        ServletContextHandler context = new ServletContextHandler(ServletContextHandler.SESSIONS);
        context.setContextPath("/");
        server.setHandler(context);

        context.addServlet(new ServletHolder(new CapInfoController()), "/");
      //  context.addServlet(CapInfoController.class, "/capinfo");
        LOGGER.info("API Gateway running on port 8082...");
        server.start();
        server.join();
    }
}
