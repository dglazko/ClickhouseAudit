package ru.anarok.audit.domain;

import lombok.Data;

import java.util.HashMap;
import java.util.Map;

@Data
public class AuditEvent {
    private long timestamp = System.currentTimeMillis();
    private String principal;
    private String type;
    private String message;
    private Map<String, String> data = new HashMap<>();
}
