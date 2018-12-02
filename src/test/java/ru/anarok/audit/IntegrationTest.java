package ru.anarok.audit;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import java.util.Random;
import java.util.concurrent.Executors;

public class IntegrationTest {
    @Test
    @Disabled("Run only if you have clickhouse running")
    public void integrationTest() {
        ClickhouseAuditService auditService = ClickhouseAuditFactory
                .create("localhost")
                .executorService(Executors.newFixedThreadPool(16))
                .build();

        Random random = new Random();

        while (true) {
            AuditEvent event = new AuditEvent("test event", "test event 2", "test message");
            event.getData().put("property 1", "value 1");
            event.getData().put("property 2", "value 2");
            event.getData().put("random", Double.toString(random.nextDouble()));

            auditService.submit(event);
        }

    }
}
