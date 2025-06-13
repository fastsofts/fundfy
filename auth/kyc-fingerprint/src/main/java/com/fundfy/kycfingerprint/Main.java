package com.fundfy.kycfingerprint;

import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;

import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Scanner;

public class Main {
    public static void main(String[] args) throws Exception {

        Server server = new Server(8090);
        ServletContextHandler context = new ServletContextHandler(ServletContextHandler.SESSIONS);
        context.setContextPath("/");
        server.setHandler(context);


        context.addServlet(new ServletHolder(new KycFingerprintServlet()), "/api/kyc/founder/fingerprint");

        System.out.println("KYC Fingerprint service running on port 8090...");
        server.start();
        server.join();
    }

    static class KycFingerprintServlet extends HttpServlet {
        private static final String FINGERPRINT_IO_API_BASE_URL = "";

        @Override
        protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
            resp.setContentType("application/json");
            PrintWriter writer = resp.getWriter();

            String address = req.getParameter("address");

            // Validate address parameter
            if (isNullOrEmpty(address)) {
                writer.write(jsonResponse("Invalid", "Address cannot be empty", null));
                return;
            }

            try {
                String url = buildMelissaApiUrl(address);
                String melissaResponse = postToMelissaApi(url);
                writer.write(jsonResponse("Success", "Melissa API response received", melissaResponse));
            } catch (Exception e) {
                // Log error and return error response
                e.printStackTrace();
                writer.write(jsonResponse("Error", "Failed to verify address: " + e.getMessage(), null));
            }
        }

        /**
         * Checks if a string is null or empty after trimming.
         */
        private boolean isNullOrEmpty(String str) {
            return str == null || str.trim().isEmpty();
        }

        /**
         * Builds the Melissa API URL with the given address.
         */
        private String buildMelissaApiUrl(String address) {
            try {
                String encodedAddress = URLEncoder.encode(address, StandardCharsets.UTF_8);
                return FINGERPRINT_IO_API_BASE_URL + "?a1=" + encodedAddress;
            } catch (Exception e) {
                throw new RuntimeException("Failed to encode address", e);
            }
        }

        /**
         * Sends a POST request to the Melissa API and returns the response body.
         */
        private String postToMelissaApi(String url) throws IOException {
            HttpURLConnection conn = null;
            try {
                URL apiUrl = URI.create(url).toURL();
                conn = (HttpURLConnection) apiUrl.openConnection();
                conn.setRequestMethod("POST");
                conn.setDoOutput(true);
                conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");

                int responseCode = conn.getResponseCode();
                try (Scanner s = new Scanner(
                        responseCode >= 200 && responseCode < 400 ? conn.getInputStream() : conn.getErrorStream())
                        .useDelimiter("\\A")) {
                    return s.hasNext() ? s.next() : "";
                }
            } finally {
                if (conn != null) {
                    conn.disconnect();
                }
            }
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