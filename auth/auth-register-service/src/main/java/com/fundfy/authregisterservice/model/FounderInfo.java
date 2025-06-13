package com.fundfy.authregisterservice.model;

import java.sql.Timestamp;

public class FounderInfo {
    private int fy_id;
    private String fy_full_name;
    private String fy_ssn;
    private String fy_password;
    private String fy_email;
    private String fy_phone_number;
    private Timestamp fy_created_at;
    private boolean fy_active = false; 

    public int getFy_id() {
        return fy_id;
    }

    public void setFy_id(int fy_id) {
        this.fy_id = fy_id;
    }

    public String getFy_full_name() {
        return fy_full_name;
    }

    public void setFy_full_name(String fy_full_name) {
        this.fy_full_name = fy_full_name;
    }

    public String getFy_ssn() {
        return fy_ssn;
    }

    public void setFy_ssn(String fy_ssn) {
        this.fy_ssn = fy_ssn;
    }

    public String getFy_password() {
        return fy_password;
    }

    public void setFy_password(String fy_password) {
        this.fy_password = fy_password;
    }

    public String getFy_email() {
        return fy_email;
    }

    public void setFy_email(String fy_email) {
        this.fy_email = fy_email;
    }

    public String getFy_phone_number() {
        return fy_phone_number;
    }

    public void setFy_phone_number(String fy_phone_number) {
        this.fy_phone_number = fy_phone_number;
    }

    public boolean isFy_active() { return fy_active; }
    public void setFy_active(boolean fy_active) { 
         this.fy_active = fy_active; 
    }

    public Timestamp getFy_created_at() {
        return fy_created_at;
    }

    public void setFy_created_at(Timestamp fy_created_at) {
        this.fy_created_at = fy_created_at;
    }

}
