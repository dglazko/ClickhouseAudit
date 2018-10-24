package ru.anarok.audit.test;

public class AuthAudit {
    private String username;
    private String ip;
    private boolean success;

    public AuthAudit(String username, String ip, boolean success) {
        this.username = username;
        this.ip = ip;
        this.success = success;
    }
}
