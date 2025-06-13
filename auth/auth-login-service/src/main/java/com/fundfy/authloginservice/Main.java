package com.fundfy.authloginservice;

import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;

import com.fundfy.authloginservice.controller.LoginController;

public class Main {
    public static void main(String[] args) throws Exception {
        Server server = new Server(8083);  // choose an available port
        ServletContextHandler context = new ServletContextHandler(ServletContextHandler.SESSIONS);
        context.setContextPath("/");
        server.setHandler(context);

        // Map LoginController servlet to handle all requests
        context.addServlet(new ServletHolder(new LoginController()), "/");

        System.out.println("auth-login-service running on port 8083...");
        server.start();
        server.join();
    }
}