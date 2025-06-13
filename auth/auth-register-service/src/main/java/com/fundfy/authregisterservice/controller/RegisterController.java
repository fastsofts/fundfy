package com.fundfy.authregisterservice.controller;

import com.fundfy.authregisterservice.model.FounderInfo;
import com.fundfy.authregisterservice.repository.RegisterRepository;
import com.fundfy.authregisterservice.repository.RegisterRepository.InsertResult;
import com.fundfy.shared.config.ConfigLoader;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.json.JSONObject;
import org.mindrot.jbcrypt.BCrypt;
import java.io.BufferedReader;
import java.io.IOException;
import java.sql.Timestamp;

public class RegisterController extends HttpServlet {
    private final RegisterRepository repository = new RegisterRepository();

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        resp.setContentType("application/json");
        JSONObject result = new JSONObject().put("success", false).put("error", "");

        try {
            StringBuilder sb = new StringBuilder();
            BufferedReader reader = req.getReader();
            String line;
            while ((line = reader.readLine()) != null) sb.append(line);

            JSONObject json = new JSONObject(sb.toString());
            String action = json.optString("action");

            if ("create".equalsIgnoreCase(action)) {
                FounderInfo founder = new FounderInfo();
                founder.setFy_full_name(json.getString("fy_full_name"));
                founder.setFy_ssn(json.getString("fy_ssn"));
                founder.setFy_email(json.getString("fy_email"));
                founder.setFy_phone_number(json.getString("fy_phone_number"));
                founder.setFy_password(BCrypt.hashpw(json.getString("fy_password"), BCrypt.gensalt()));
                founder.setFy_created_at(new Timestamp(System.currentTimeMillis()));

                InsertResult res = repository.insertFounderInfo(founder);
                switch (res) {
                    case SUCCESS:
                        String link = ConfigLoader.get("activation.url") + "?fy_id=" + founder.getFy_id();
                        System.out.println("Activation link: " + link);
                        result.put("success", true).put("error", "");
                        break;
                    case DUPLICATE_SSN:
                        result.put("success", false).put("error", "Insert failed: SSN already registered.");
                        break;
                    case DUPLICATE_EMAIL:
                        result.put("success", false).put("error", "Insert failed: Email already registered.");
                        break;
                    default:
                        result.put("success", false).put("error", "Insert failed: unexpected error.");
                }

            } else if ("update".equalsIgnoreCase(action)) {
                int fyId = json.getInt("fy_id");
                String name = json.optString("fy_full_name", "");
                String phone = json.optString("fy_phone_number", "");
                String password = json.optString("fy_password", "");
                if (!password.isEmpty()) password = BCrypt.hashpw(password, BCrypt.gensalt());
                boolean updated = repository.updateFounder(fyId, name, phone, password);
                result.put("success", updated);
                if (!updated) result.put("error", "Update failed.");
            } else if ("delete".equalsIgnoreCase(action)) {
                int fyId = json.getInt("fy_id");
                boolean deleted = repository.deactivateFounder(fyId);
                result.put("success", deleted);
                if (!deleted) result.put("error", "Deactivate failed.");
            } else {
                result.put("success", false).put("error", "Invalid action.");
            }

        } catch (Exception e) {
            result.put("success", false).put("error", "Exception: " + e.getMessage());
        }

        resp.getWriter().write(result.toString());
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        resp.setContentType("application/json");
        JSONObject result = new JSONObject().put("success", false).put("error", "");

        try {
            String fid = req.getParameter("fy_id");
            if (fid != null) {
                boolean activated = repository.activateFounder(Integer.parseInt(fid));
                result.put("success", activated);
                if (!activated) result.put("error", "Activation failed.");
            } else {
                result.put("success", false).put("error", "fy_id parameter missing.");
            }
        } catch (Exception e) {
            result.put("success", false).put("error", "Exception: " + e.getMessage());
        }

        resp.getWriter().write(result.toString());
    }
}
