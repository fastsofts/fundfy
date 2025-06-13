package com.fundfy.capinfoservice.controller;

import com.fundfy.capinfoservice.repository.CapInfoRepository;
import com.fundfy.capinfoservice.repository.CapInfoRepository.*;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.stream.Collectors;

public class CapInfoController extends HttpServlet {
    private final CapInfoRepository repository = new CapInfoRepository();

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        resp.setContentType("application/json");
        JSONObject result = new JSONObject()
            .put("success", false)
            .put("error", "");

        try {
            String body = new BufferedReader(req.getReader())
                .lines()
                .collect(Collectors.joining());
            JSONObject json = new JSONObject(body);
            String action = json.optString("action", "").toLowerCase();

            switch (action) {
                case "create" -> handleCreate(json, result);
                case "update" -> handleUpdate(json, result);
                case "delete" -> handleDelete(json, result);
                default -> result.put("error", "Invalid or missing action.");
            }
        } catch (Exception e) {
            e.printStackTrace();
            result.put("error", "Exception: " + e.getMessage());
        }

        resp.getWriter().write(result.toString());
    }

    private void handleCreate(JSONObject json, JSONObject result) {
        int fyId = json.getInt("fy_id");
        String fyCompanyId = json.getString("fy_company_id");
        BigDecimal equity = new BigDecimal(json.optString("fy_equity_percent", "0"));
        String role = json.optString("fy_role", "");
        String bio = json.optString("fy_bio", "");

        CapInsertResult res = repository.insertCapInfo(fyId, fyCompanyId, equity, role, bio);
        if (res == CapInsertResult.SUCCESS) {
            result.put("success", true);
        } else if (res == CapInsertResult.DUPLICATE_COMPANY) {
            result.put("error", "Create failed: Company ID already exists for this founder.");
        } else {
            result.put("error", "Create failed: unexpected error.");
        }
    }

    private void handleUpdate(JSONObject json, JSONObject result) {
        int fyCapId = json.getInt("fy_cap_id");
        int fyId = json.getInt("fy_id");
        String fyCompanyId = json.optString("fy_company_id", json.optString("old_company_id", ""));
        BigDecimal equity = new BigDecimal(json.optString("fy_equity_percent", "0"));
        String role = json.has("fy_role") ? json.optString("fy_role") : null;
        String bio = json.has("fy_bio") ? json.optString("fy_bio") : null;

        CapUpdateResult res = repository.updateCapInfo(fyCapId, fyId, fyCompanyId, equity, role, bio);
        if (res == CapUpdateResult.SUCCESS) {
            result.put("success", true);
        } else if (res == CapUpdateResult.DUPLICATE_COMPANY) {
            result.put("error", "Update failed: Company ID already exists for this founder.");
        } else {
            result.put("error", "Update failed: unexpected error.");
        }
    }

    private void handleDelete(JSONObject json, JSONObject result) {
        int fyCapId = json.getInt("fy_cap_id");
        int fyId = json.getInt("fy_id");

        CapDeleteResult res = repository.deleteCapInfo(fyCapId, fyId);
        if (res == CapDeleteResult.SUCCESS) {
            result.put("success", true);
        } else if (res == CapDeleteResult.NOT_FOUND) {
            result.put("error", "Delete failed: Record not found.");
        } else {
            result.put("error", "Delete failed: unexpected error.");
        }
    }
}
