package com.fundfy.kycsanctions;

import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;

import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.io.PrintWriter;

public class Main {

    public static void main(String[] args) throws Exception {
        // Initialize Jetty server on port 8090
        Server server = new Server(8090);
        ServletContextHandler context = new ServletContextHandler(ServletContextHandler.SESSIONS);
        context.setContextPath("/");
        server.setHandler(context);

        // Register servlet for KYC address verification
        context.addServlet(new ServletHolder(new KycSanctionsServlet()), "/api/kyc/founder/sanctions");
        System.out.println("KYC Sanctions service running on port 8090...");
        server.start();
        server.join();
    }

    static class KycSanctionsServlet extends HttpServlet {

        @Override
        protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
            resp.setContentType("application/json");
            PrintWriter writer = resp.getWriter();

            String firstName = req.getParameter("firstName");
            String lastName = req.getParameter("lastName");
            String dateOfBirth = req.getParameter("dateOfBirth");
            String country = req.getParameter("country");

            // Validate address parameter
            if (isNullOrEmpty(firstName) || isNullOrEmpty(lastName) || isNullOrEmpty(dateOfBirth)
                    || isNullOrEmpty(country)) {
                writer.write(jsonResponse("Invalid", "All parameters are required", null));
                return;
            }

            writer.write(jsonResponse("Success", "API response received", null));
        }

        /**
         * Checks if a string is null or empty after trimming.
         */
        private boolean isNullOrEmpty(String str) {
            return str == null || str.trim().isEmpty();
        }

        /**
         * Constructs a JSON response string.
         */
        private String jsonResponse(String status, String message, String data) {
            StringBuilder sb = new StringBuilder();
            sb.append("{");
            sb.append("\"status\": \"").append(escapeJson(status)).append("\", ");
            sb.append("\"message\": \"").append(escapeJson(message)).append("\"");
            if (data != null) {
                sb.append(", \"data\": \"").append(escapeJson(data)).append("\"");
            }
            sb.append("}");
            return sb.toString();
        }

        /**
         * Escapes double quotes and backslashes for JSON string values.
         */
        private String escapeJson(String value) {
            return value == null ? "" : value.replace("\\", "\\\\").replace("\"", "\\\"");
        }
    }
}