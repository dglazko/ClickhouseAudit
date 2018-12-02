package ru.anarok.audit;

import lombok.*;

import java.util.HashMap;
import java.util.Map;

@ToString
@EqualsAndHashCode
@Getter
public class AuditEvent {
    private final long timestamp = System.currentTimeMillis();
    private final String emitter;
    private final String type;
    private final String message;
    @Setter(AccessLevel.NONE)
    private final Map<String, String> data = new HashMap<>();

    public AuditEvent(String emitter, String type, String message) {
        this.emitter = emitter;
        this.type = type;
        this.message = message;
    }
}
