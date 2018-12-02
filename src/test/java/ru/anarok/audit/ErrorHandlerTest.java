package ru.anarok.audit;

import cn.danielw.fop.ObjectPool;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import ru.anarok.audit.impl.DefaultAuditService;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

import static ru.anarok.audit.AuditServiceTest.newMockConnection;

@SuppressWarnings("Duplicates")
public class ErrorHandlerTest {

    @Test
    public void interruptExceptionHandling() throws InterruptedException {
        CountDownLatch neverEndingBlockingLatch = new CountDownLatch(1);

        ObjectPool<ClickhouseConnection> mockConnection = newMockConnection(e -> neverEndingBlockingLatch.await());

        AtomicBoolean errorFired = new AtomicBoolean(false);
        AtomicReference<Throwable> exceptionReference = new AtomicReference<>();
        CountDownLatch errorLatch = new CountDownLatch(1);
        ClickhouseErrorHandler errorHandler = (audit, throwable) -> {
            errorFired.set(true);
            exceptionReference.set(throwable);
            errorLatch.countDown();
        };

        DefaultAuditService service = new DefaultAuditService(
                Executors.newSingleThreadExecutor(),
                mockConnection,
                errorHandler
        );

        AuditEvent auditObject1 = new AuditEvent("tests", "test message", "test audit service message");
        AuditEvent auditObject2 = new AuditEvent("tests", "test message", "second test audit service message");
        service.submit(auditObject1);
        service.submit(auditObject2);

        service.shutdownInterrupted();

        errorLatch.await(5, TimeUnit.SECONDS);

        Assertions.assertTrue(errorFired.get(), "Executor service interruption didn't cause error handle to fire");
        Assertions.assertTrue(exceptionReference.get() instanceof InterruptedException, "Unexpected exception type of " + exceptionReference.get());
    }

    @Test
    public void customExceptionHandling() {
        ObjectPool<ClickhouseConnection> mockConnection = newMockConnection(auditEvent -> {
            throw new IllegalStateException();
        });

        AtomicBoolean errorFired = new AtomicBoolean(false);
        AtomicReference<Throwable> exceptionReference = new AtomicReference<>();
        ClickhouseErrorHandler errorHandler = (audit, throwable) -> {
            errorFired.set(true);
            exceptionReference.set(throwable);
        };

        DefaultAuditService service = new DefaultAuditService(
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
