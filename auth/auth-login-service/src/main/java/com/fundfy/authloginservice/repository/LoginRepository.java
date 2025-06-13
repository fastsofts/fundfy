package com.fundfy.authloginservice.repository;

import java.sql.*;
import org.mindrot.jbcrypt.BCrypt;
import com.fundfy.shared.config.ConfigLoader;

public class LoginRepository {

    private Connection getConnection() throws SQLException {
        String url = ConfigLoader.get("db.url").replace("\"", "").trim();
        String user = ConfigLoader.get("db.username").replace("\"", "").trim();
        String pw = ConfigLoader.get("db.password").replace("\"", "").trim();
        return DriverManager.getConnection(url, user, pw);
    }

    public Integer verifyCredentialsAndGetId(String email, String password) {
        String sql = "SELECT fy_id, fy_password FROM fy_founders_info WHERE fy_email = ?";

        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, email);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    String storedHash = rs.getString("fy_password");
                    if (BCrypt.checkpw(password, storedHash)) {
                        return rs.getInt("fy_id");
                    }
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public String hashPassword(String plainPassword) {
        return BCrypt.hashpw(plainPassword, BCrypt.gensalt(12));
    }
}
