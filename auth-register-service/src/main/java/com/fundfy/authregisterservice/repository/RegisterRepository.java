package com.fundfy.authregisterservice.repository;

import com.fundfy.authregisterservice.model.FounderInfo;
import com.fundfy.shared.config.ConfigLoader;
import org.postgresql.util.PSQLException;
import java.sql.*;

public class RegisterRepository {

    public enum InsertResult {
        SUCCESS,
        DUPLICATE_SSN,
        DUPLICATE_EMAIL,
        FAILURE
    }

    private Connection getConnection() throws SQLException {
        String url = ConfigLoader.get("db.url").replace("\"", "").trim();
        String user = ConfigLoader.get("db.username").replace("\"", "").trim();
        String pw = ConfigLoader.get("db.password").replace("\"", "").trim();
        return DriverManager.getConnection(url, user, pw);
    }

    public InsertResult insertFounderInfo(FounderInfo info) throws SQLException {
        String sql = """
            INSERT INTO fy_founders_info
              (fy_full_name, fy_ssn, fy_password, fy_email, fy_phone_number, fy_created_at, fy_active)
            VALUES (?, ?, ?, ?, ?, ?, ?)
            RETURNING fy_id
        """;

        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, info.getFy_full_name());
            stmt.setString(2, info.getFy_ssn());
            stmt.setString(3, info.getFy_password());
            stmt.setString(4, info.getFy_email());
            stmt.setString(5, info.getFy_phone_number());
            stmt.setTimestamp(6, info.getFy_created_at());
            stmt.setBoolean(7, false);

            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                info.setFy_id(rs.getInt(1));
                info.setFy_active(false);
                return InsertResult.SUCCESS;
            }
            return InsertResult.FAILURE;

        } catch (PSQLException e) {
            if ("23505".equals(e.getSQLState()) && e.getServerErrorMessage() != null) {
                String detail = e.getServerErrorMessage().getDetail();
                if (detail != null) {
                    if (detail.contains("Key (fy_ssn)=")) return InsertResult.DUPLICATE_SSN;
                    if (detail.contains("Key (fy_email)=")) return InsertResult.DUPLICATE_EMAIL;
                }
            }
            return InsertResult.FAILURE;
        }
    }

    public boolean updateFounder(int fyId, String name, String phone, String password) throws SQLException {
        StringBuilder sql = new StringBuilder("UPDATE fy_founders_info SET ");
        if (!name.isEmpty()) sql.append("fy_full_name = ?, ");
        if (!phone.isEmpty()) sql.append("fy_phone_number = ?, ");
        if (!password.isEmpty()) sql.append("fy_password = ?, ");
        sql = new StringBuilder(sql.substring(0, sql.length() - 2) + " WHERE fy_id = ?");
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql.toString())) {
            int idx = 1;
            if (!name.isEmpty()) stmt.setString(idx++, name);
            if (!phone.isEmpty()) stmt.setString(idx++, phone);
            if (!password.isEmpty()) stmt.setString(idx++, password);
            stmt.setInt(idx, fyId);
            return stmt.executeUpdate() > 0;
        }
    }

    public boolean deactivateFounder(int fyId) throws SQLException {
        String sql = "UPDATE fy_founders_info SET fy_active = false WHERE fy_id = ?";
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, fyId);
            return stmt.executeUpdate() > 0;
        }
    }

    public boolean activateFounder(int fyId) throws SQLException {
        String sql = "UPDATE fy_founders_info SET fy_active = true WHERE fy_id = ?";
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, fyId);
            return stmt.executeUpdate() > 0;
        }
    }
}
