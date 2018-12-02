package ru.anarok.audit;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import ru.anarok.audit.api.ClickhouseConnection;
import ru.anarok.audit.impl.AuditEvent;
import ru.anarok.audit.impl.ClickhouseAuditService;

import java.sql.SQLException;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

@SuppressWarnings("Duplicates")
public class AuditServiceTest {

    @Test
    public void normalExecutionTest() {
        AtomicInteger executionCounter = new AtomicInteger();
        AtomicReference<AuditEvent> lastAudit = new AtomicReference<>();

        ClickhouseConnection mockConnection = new ClickhouseConnection() {
            @Override
            public void connect() throws SQLException {

            }

            @Override
            public void insert(AuditEvent e) throws SQLException {
                executionCounter.incrementAndGet();
                lastAudit.set(e);
            }

            @Override
            public void shutdownImmediately() throws SQLException {

            }
        };

        ClickhouseAuditService service = new ClickhouseAuditService(
                Executors.newSingleThreadExecutor(),
                mockConnection,
                null
        );

        AuditEvent auditObject1 = new AuditEvent("tests", "test message", "test audit service message");
        AuditEvent auditObject2 = new AuditEvent("tests", "test message", "second test audit service message");
        service.submit(auditObject1);

        try {
            service.submit(auditObject2).get();
        } catch (InterruptedException | ExecutionException e) {
            Assertions.fail("Audit service future has thrown an unexpected exception");
        }

        Assertions.assertEquals(2, executionCounter.get(), "Audit service has executed insert(AuditEvent e) unexpected number of times");
        Assertions.assertEquals(auditObject2, lastAudit.get(), "Last insert(AuditEvent e) argument mismatches the argument passed to submit");

        service.shutdownInterrupted();
    }

    @Test
    public void executorStopTest() {
        CountDownLatch latch = new CountDownLatch(1);

        ClickhouseConnection mockConnection = new ClickhouseConnection() {
            @Override
            public void connect() throws SQLException {

            }

            @Override
            public void insert(AuditEvent e) throws SQLException {
                try {
                    latch.await(); // emulate blocking task
                } catch (InterruptedException ignored) {

                }
            }

            @Override
            public void shutdownImmediately() throws SQLException {

            }
        };

        ClickhouseAuditService service = new ClickhouseAuditService(
                Executors.newSingleThreadExecutor(),
                mockConnection,
                null
        );

        AuditEvent auditObject1 = new AuditEvent("tests", "test message", "test audit service message");
        AuditEvent auditObject2 = new AuditEvent("tests", "test message", "second test audit service message");
        service.submit(auditObject1);
        service.submit(auditObject2);

        Assertions.assertEquals(1, service.shutdownInterrupted().size(), "shutdownInterrupted() has return an unexpected number of tasks");
    }
}
