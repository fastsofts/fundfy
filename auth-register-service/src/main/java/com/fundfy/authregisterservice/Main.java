
package com.fundfy.authregisterservice;


import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;

import com.fundfy.authregisterservice.controller.RegisterController;


public class Main {
    public static void main(String[] args) throws Exception {
        Server server = new Server(8081);
        ServletContextHandler context = new ServletContextHandler(ServletContextHandler.SESSIONS);
        context.setContextPath("/");
        server.setHandler(context);

        context.addServlet(new ServletHolder(new RegisterController()), "/");

        System.out.println("authregisterservice running on port 8081...");
        server.start();
        server.join();
    }
}
