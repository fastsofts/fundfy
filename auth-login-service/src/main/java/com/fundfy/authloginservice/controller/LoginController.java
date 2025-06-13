package com.fundfy.authloginservice.controller;

import com.fundfy.authloginservice.repository.LoginRepository;
import org.json.JSONObject;
import jakarta.servlet.http.*;
import java.io.BufferedReader;
import java.io.IOException;
import java.util.stream.Collectors;

public class LoginController extends HttpServlet {
    private final LoginRepository repo;

    // Constructor used by Main.java
    public LoginController(LoginRepository repo) {
        this.repo = repo;
    }

    // No-arg constructor required for servlet container instantiation :contentReference[oaicite:0]{index=0}
    public LoginController() {
        this(new LoginRepository());
    }

    // Expose a login method so Main.java works
    public boolean login(String email, String password) {
        return repo.verifyCredentialsAndGetId(email, password) != null;
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        resp.setContentType("application/json; charset=UTF-8");

        String body = new BufferedReader(req.getReader())
            .lines()
            .collect(Collectors.joining());
        JSONObject json = new JSONObject(body);

        JSONObject result = new JSONObject().put("success", false);
        String action = json.optString("action", "");

        if ("userget".equalsIgnoreCase(action)) {
            String email = json.optString("email", "");
            String password = json.optString("password", "");
            Integer fyId = repo.verifyCredentialsAndGetId(email, password);

            if (fyId != null) {
                result.put("success", true).put("fy_id", fyId);
            } else {
                result.put("error", "Invalid email or password.");
            }
        } else {
            result.put("error", "Invalid action.");
        }

        resp.getWriter().write(result.toString());
    }
}
