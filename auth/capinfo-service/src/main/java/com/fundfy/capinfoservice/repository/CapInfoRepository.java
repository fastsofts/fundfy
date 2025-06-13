package com.fundfy.capinfoservice.repository;

import com.fundfy.shared.config.ConfigLoader;
import java.sql.*;
import java.math.BigDecimal;

public class CapInfoRepository {

    public enum CapInsertResult { SUCCESS, DUPLICATE_COMPANY, FAILURE }
    public enum CapUpdateResult { SUCCESS, DUPLICATE_COMPANY, FAILURE }
    public enum CapDeleteResult   { SUCCESS, NOT_FOUND, FAILURE }

    private Connection getConnection() throws SQLException {
        String url  = ConfigLoader.get("db.url").replace("\"", "").trim();
        String user = ConfigLoader.get("db.username").replace("\"", "").trim();
        String pw   = ConfigLoader.get("db.password").replace("\"", "").trim();
        return DriverManager.getConnection(url, user, pw);
    }

    public CapInsertResult insertCapInfo(int fyId, String fyCompanyId,
                                         BigDecimal equityPercent, String role, String bio) {
        String sql = """
            INSERT INTO fy_cap_info
            (fy_id, fy_company_id, fy_equity_percent, fy_role, fy_bio, fy_created_at)
            VALUES (?, ?, ?, ?, ?, CURRENT_TIMESTAMP)
        """;
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, fyId);
            stmt.setString(2, fyCompanyId);
            stmt.setBigDecimal(3, equityPercent);
            stmt.setString(4, role);
            stmt.setString(5, bio);
            stmt.executeUpdate();
            return CapInsertResult.SUCCESS;

        } catch (SQLException e) {
            if ("23505".equals(e.getSQLState())) {
                return CapInsertResult.DUPLICATE_COMPANY;
            }
            e.printStackTrace();
            return CapInsertResult.FAILURE;
        }
    }

    public CapUpdateResult updateCapInfo(int fyCapId, int fyId, String newCompanyId,
                                         BigDecimal equityPercent, String role, String bio) {
        String fetchSql = "SELECT fy_company_id FROM fy_cap_info WHERE fy_cap_id = ? AND fy_id = ?";
        try (Connection conn = getConnection();
             PreparedStatement fetchStmt = conn.prepareStatement(fetchSql)) {

            fetchStmt.setInt(1, fyCapId);
            fetchStmt.setInt(2, fyId);
            ResultSet rs = fetchStmt.executeQuery();

            if (!rs.next()) {
                return CapUpdateResult.FAILURE;
            }

            String existingId = rs.getString("fy_company_id");

            if (!newCompanyId.equals(existingId)) {
                String dupSql = """
                    SELECT 1 FROM fy_cap_info
                    WHERE fy_id = ? AND fy_company_id = ? AND fy_cap_id != ?
                """;
                try (PreparedStatement dupStmt = conn.prepareStatement(dupSql)) {
                    dupStmt.setInt(1, fyId);
                    dupStmt.setString(2, newCompanyId);
                    dupStmt.setInt(3, fyCapId);
                    if (dupStmt.executeQuery().next()) {
                        return CapUpdateResult.DUPLICATE_COMPANY;
                    }
                }
            }

            String updateSql = """
                UPDATE fy_cap_info
                SET fy_company_id = ?, fy_equity_percent = ?, fy_role = ?, fy_bio = ?
                WHERE fy_cap_id = ? AND fy_id = ?
            """;
            try (PreparedStatement upd = conn.prepareStatement(updateSql)) {
                upd.setString(1, newCompanyId);
                upd.setBigDecimal(2, equityPercent);
                upd.setString(3, role);
                upd.setString(4, bio);
                upd.setInt(5, fyCapId);
                upd.setInt(6, fyId);

                int rows = upd.executeUpdate();
                return rows > 0 ? CapUpdateResult.SUCCESS : CapUpdateResult.FAILURE;
            }

        } catch (SQLException e) {
            e.printStackTrace();
            return CapUpdateResult.FAILURE;
        }
    }

    public CapDeleteResult deleteCapInfo(int fyCapId, int fyId) {
        String sql = "DELETE FROM fy_cap_info WHERE fy_cap_id = ? AND fy_id = ?";
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, fyCapId);
            stmt.setInt(2, fyId);

            int rows = stmt.executeUpdate();
            return rows > 0 ? CapDeleteResult.SUCCESS : CapDeleteResult.NOT_FOUND;

        } catch (SQLException e) {
            e.printStackTrace();
            return CapDeleteResult.FAILURE;
        }
    }
}
