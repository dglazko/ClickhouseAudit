package ru.anarok.audit.test;

import ru.anarok.audit.AuditTable;

@AuditTable("audit")
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
