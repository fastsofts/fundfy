package com.fundfy.kycveriff;

import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;

import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;

public class Main {
    public static void main(String[] args) throws Exception {
        Server server = new Server(8090);
        ServletContextHandler context = new ServletContextHandler(ServletContextHandler.SESSIONS);
        context.setContextPath("/");
        server.setHandler(context);

        context.addServlet(new ServletHolder(new HttpServlet() {
            @Override
            protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
                resp.setContentType("application/json");
                resp.getWriter().write("{\"status\": \"Veriff session created (stub)\"}");
            }
        }), "/api/kyc/veriff/create");

        System.out.println("KYC Veriff service running on port 8090...");
        server.start();
        server.join();
    }
}