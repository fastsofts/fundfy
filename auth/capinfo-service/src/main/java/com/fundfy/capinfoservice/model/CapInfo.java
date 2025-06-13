package com.fundfy.capinfoservice.model;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.util.UUID;

public class CapInfo {
    private UUID fy_cap_id;
    private UUID fy_id;
    private String role;
    private BigDecimal equity_percent;
    private String bio;
    private String company_id;
    private Timestamp fy_created_at;

    // Getters and setters
    public UUID getFy_cap_id() {
        return fy_cap_id;
    }

    public void setFy_cap_id(UUID fy_cap_id) {
        this.fy_cap_id = fy_cap_id;
    }

    public UUID getFy_id() {
        return fy_id;
    }

    public void setFy_id(UUID fy_id) {
        this.fy_id = fy_id;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public BigDecimal getEquity_percent() {
        return equity_percent;
    }

    public void setEquity_percent(BigDecimal equity_percent) {
        this.equity_percent = equity_percent;
    }

    public String getBio() {
        return bio;
    }

    public void setBio(String bio) {
        this.bio = bio;
    }

    public String getCompany_id() {
        return company_id;
    }

    public void setCompany_id(String company_id) {
        this.company_id = company_id;
    }

    public Timestamp getFy_created_at() {
        return fy_created_at;
    }

    public void setFy_created_at(Timestamp fy_created_at) {
        this.fy_created_at = fy_created_at;
    }
}
