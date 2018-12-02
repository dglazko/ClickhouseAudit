package ru.anarok.audit;

import cn.danielw.fop.ObjectPool;
import cn.danielw.fop.PoolConfig;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import ru.anarok.audit.impl.DefaultAuditService;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

@SuppressWarnings("Duplicates")
public class AuditServiceTest {

    public static ObjectPool<ClickhouseConnection> newMockConnection(TestConnectionFactory.TestConnectionInsertHandler eventConsumer) {
        TestConnectionFactory objectFactory = new TestConnectionFactory(eventConsumer);
        PoolConfig config = new PoolConfig();
        config.setPartitionSize(5);
        config.setMaxSize(10);
        config.setMinSize(1);
        config.setMaxIdleMilliseconds(60 * 1000 * 5);

        return new ObjectPool<>(config, objectFactory);
    }

    @Test
    public void normalExecutionTest() {
        AtomicInteger executionCounter = new AtomicInteger();
        AtomicReference<AuditEvent> lastAudit = new AtomicReference<>();

        ObjectPool<ClickhouseConnection> mockConnection = newMockConnection(auditEvent -> {
            executionCounter.incrementAndGet();
            lastAudit.set(auditEvent);
        });

        DefaultAuditService service = new DefaultAuditService(
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

        ObjectPool<ClickhouseConnection> mockConnection = newMockConnection(auditEvent -> {
            try {
                latch.await(); // emulate blocking task
            } catch (InterruptedException ignored) {

            }
        });

        DefaultAuditService service = new DefaultAuditService(
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
