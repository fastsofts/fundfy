package com.fundfy.apigateway;

import com.fundfy.shared.config.ConfigLoader;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;

import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URI;
import java.net.URISyntaxException;

import software.amazon.awssdk.core.SdkBytes;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.lambda.LambdaClient;
import software.amazon.awssdk.services.lambda.model.InvokeRequest;
import software.amazon.awssdk.services.lambda.model.InvokeResponse;

import java.util.logging.Level;
import java.util.logging.Logger;

public class Main {

    private static final Logger LOGGER = Logger.getLogger(Main.class.getName());

    public static void main(String[] args) throws Exception {
        boolean AWS_ENABLED = Boolean.parseBoolean(ConfigLoader.get("AWS_ENABLED"));
        Region region = Region.of(ConfigLoader.get("AWS_REGION"));

        Server server = new Server(8080);
        ServletContextHandler context = new ServletContextHandler(ServletContextHandler.SESSIONS);
        context.setContextPath("/");
        server.setHandler(context);

        addProxy(context, "/gateway/auth/register", AWS_ENABLED ? "register-service" : "http://localhost:8081", AWS_ENABLED, region);
        addProxy(context, "/gateway/auth/capinfo", AWS_ENABLED ? "capinfo-service" : "http://localhost:8082", AWS_ENABLED, region);
        addProxy(context, "/gateway/auth/login", AWS_ENABLED ? "login-service" : "http://localhost:8083", AWS_ENABLED, region);
        addProxy(context, "/gateway/kyc/veriff", AWS_ENABLED ? "kyc-veriff-service" : "http://localhost:8090", AWS_ENABLED, region);
        addProxy(context, "/gateway/kyc/faceio", AWS_ENABLED ? "kyc-faceio-service" : "http://localhost:8091", AWS_ENABLED, region);
        addProxy(context, "/gateway/kyc/melissa", AWS_ENABLED ? "kyc-melissa-service" : "http://localhost:8092", AWS_ENABLED, region);
        addProxy(context, "/gateway/kyc/sanctions", AWS_ENABLED ? "kyc-sanctions-service" : "http://localhost:8093", AWS_ENABLED, region);
        addProxy(context, "/gateway/kyc/fingerprint", AWS_ENABLED ? "kyc-fingerprint-service" : "http://localhost:8094", AWS_ENABLED, region);

        LOGGER.info("API Gateway running on port 8080...");
        server.start();
        server.join();
    }

    private static void copy(InputStream in, OutputStream out) throws IOException {
        byte[] buffer = new byte[8192];
        int bytesRead;
        while ((bytesRead = in.read(buffer)) != -1) {
            out.write(buffer, 0, bytesRead);
        }
        out.flush();
    }

    private static void addProxy(ServletContextHandler context, String path, String targetOrLambda, boolean AWS_ENABLED, Region region) {
        context.addServlet(new ServletHolder(new HttpServlet() {
            @Override
            protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
                if (!AWS_ENABLED) {
                    // Proxying to local HTTP service
                    URL url;
                    try {
                        String fullUrl = targetOrLambda + req.getRequestURI().replace("/gateway", "");
                        URI uri = new URI(fullUrl);
                        url = uri.toURL();
                    } catch (URISyntaxException e) {
                        LOGGER.log(Level.SEVERE, "Invalid URI: " + e.getMessage(), e);
                        resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                        resp.getWriter().write("Invalid URI: " + e.getMessage());
                        return;
                    }

                    HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                    conn.setRequestProperty("Content-Type", req.getContentType());
                    conn.setRequestMethod("POST");
                    conn.setDoOutput(true);
                    conn.setConnectTimeout(5000);
                    conn.setReadTimeout(5000);

                    // Send request body
                    try (InputStream in = req.getInputStream(); OutputStream out = conn.getOutputStream()) {
                        copy(in, out);
                    } catch (IOException e) {
                        LOGGER.log(Level.SEVERE, "Error forwarding the request: " + e.getMessage(), e);
                        resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
                        resp.getWriter().write("Error forwarding the request.");
                        return;
                    }

                    // Handle response properly
                    int statusCode = conn.getResponseCode();
                    resp.setStatus(statusCode);

                    try (InputStream in = (statusCode >= 400 ? conn.getErrorStream() : conn.getInputStream());
                         OutputStream out = resp.getOutputStream()) {
                        if (in != null) {
                            copy(in, out);
                        } else {
                            LOGGER.warning("No response body received from target server.");
                        }
                    } catch (IOException e) {
                        LOGGER.log(Level.SEVERE, "Error copying response from target server: " + e.getMessage(), e);
                        resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
                        resp.getWriter().write("Error copying response from target server.");
                    }

                } else {
                    // Proxying to AWS Lambda
                    try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
                        copy(req.getInputStream(), baos);

                        LambdaClient lambdaClient = LambdaClient.builder().region(region).build();
                        InvokeRequest invokeRequest = InvokeRequest.builder()
                                .functionName(targetOrLambda)
                                .payload(SdkBytes.fromByteArray(baos.toByteArray()))
                                .build();

                        InvokeResponse response = lambdaClient.invoke(invokeRequest);
                        resp.setStatus(response.statusCode());

                        try (OutputStream out = resp.getOutputStream()) {
                            out.write(response.payload().asByteArray());
                        } catch (IOException e) {
                            LOGGER.log(Level.SEVERE, "Error writing response from Lambda: " + e.getMessage(), e);
                            resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
                            resp.getWriter().write("Error writing response from Lambda.");
                        }

                    } catch (Exception e) {
                        LOGGER.log(Level.SEVERE, "Error invoking Lambda: " + e.getMessage(), e);
                        resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
                        resp.getWriter().write("Lambda error: " + e.getMessage());
                    }
                }
            }
        }), path + "/*");
    }
}
