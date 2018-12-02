package ru.anarok.audit;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import ru.anarok.audit.api.ClickhouseConnection;
import ru.anarok.audit.api.ClickhouseErrorHandler;
import ru.anarok.audit.impl.AuditEvent;
import ru.anarok.audit.impl.ClickhouseAuditService;

import java.sql.SQLException;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

@SuppressWarnings("Duplicates")
public class ErrorHandlerTest {

    @Test
    public void interruptExceptionHandling() {
        CountDownLatch latch = new CountDownLatch(1);

        ClickhouseConnection mockConnection = new ClickhouseConnection() {
            @Override
            public void connect() throws SQLException {

            }

            @Override
            public void insert(AuditEvent e) throws Exception {
                latch.await();
            }

            @Override
            public void shutdownImmediately() throws SQLException {

            }
        };

        AtomicBoolean errorFired = new AtomicBoolean(false);
        AtomicReference<Throwable> exceptionReference = new AtomicReference<>();
        ClickhouseErrorHandler errorHandler = (audit, throwable) -> {
            errorFired.set(true);
            exceptionReference.set(throwable);
        };

        ClickhouseAuditService service = new ClickhouseAuditService(
                Executors.newSingleThreadExecutor(),
                mockConnection,
                errorHandler
        );

        AuditEvent auditObject1 = new AuditEvent("tests", "test message", "test audit service message");
        AuditEvent auditObject2 = new AuditEvent("tests", "test message", "second test audit service message");
        service.submit(auditObject1);
        service.submit(auditObject2);

        service.shutdownInterrupted();

        Assertions.assertTrue(errorFired.get(), "Executor service interruption didn't cause error handle to fire");
        Assertions.assertTrue(exceptionReference.get() instanceof InterruptedException, "Unexpected exception type of " + exceptionReference.get());
    }

    @Test
    public void customExceptionHandling() {
        CountDownLatch latch = new CountDownLatch(1);

        ClickhouseConnection mockConnection = new ClickhouseConnection() {
            @Override
            public void connect() throws SQLException {

            }

            @Override
            public void insert(AuditEvent e) throws Exception {
                throw new IllegalStateException();
            }

            @Override
            public void shutdownImmediately() throws SQLException {

            }
        };

        AtomicBoolean errorFired = new AtomicBoolean(false);
        AtomicReference<Throwable> exceptionReference = new AtomicReference<>();
        ClickhouseErrorHandler errorHandler = (audit, throwable) -> {
            errorFired.set(true);
            exceptionReference.set(throwable);
        };

        ClickhouseAuditService service = new ClickhouseAuditService(
                Executors.newSingleThreadExecutor(),
                mockConnection,
                errorHandler
        );

        AuditEvent auditObject1 = new AuditEvent("tests", "test message", "test audit service message");
        AuditEvent auditObject2 = new AuditEvent("tests", "test message", "second test audit service message");
        service.submit(auditObject1);
        try {
            service.submit(auditObject2).get();
        } catch (InterruptedException | ExecutionException ignored) {

        }

        Assertions.assertTrue(errorFired.get(), "Connection error didn't cause error handle to fire");
        Assertions.assertTrue(exceptionReference.get() instanceof IllegalStateException, "Unexpected exception type of " + exceptionReference.get());
    }
}
