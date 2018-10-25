package ru.anarok.audit.test;

import ru.anarok.audit.AuditTable;

@AuditTable("audit")
public class AuthAudit {
    private String username;
    private String ip;
    private boolean success;
    private byte b1;
    private short s1;
    private char c1;
    private int i1;
    private long l1;
    private float f1;
    private double d1;
    private boolean bool;

    public AuthAudit(String username, String ip, boolean success) {
        this.username = username;
        this.ip = ip;
        this.success = success;
    }
}
