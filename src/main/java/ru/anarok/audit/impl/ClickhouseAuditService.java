package ru.anarok.audit.impl;

import lombok.RequiredArgsConstructor;
import ru.anarok.audit.api.ClickhouseConnection;
import ru.anarok.audit.api.ClickhouseErrorHandler;

import java.sql.SQLException;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.*;
import java.util.stream.Collectors;

@RequiredArgsConstructor
public class ClickhouseAuditService {
    private final TaskExtractor taskExtractor = new TaskExtractor();
    private final ExecutorService executorService;
    private final ClickhouseConnection connection;
    private final ClickhouseErrorHandler errorHandler;

    public Future<Boolean> submit(AuditEvent event) {
        ClickhouseInsertTask task = new ClickhouseInsertTask(event, connection, errorHandler);

        return executorService.submit(task);
    }

    public List<AuditEvent> shutdownInterrupted() {
        if (!taskExtractor.isReady())
            return Collections.emptyList();

        return executorService
                .shutdownNow()
                .stream()
                .map(s -> taskExtractor.getTask((FutureTask) s))
                .filter(Objects::nonNull)
                .map(s -> s.event)
                .collect(Collectors.toList());
    }

    public void shutdownWait(long timeout, TimeUnit unit) throws InterruptedException {
        executorService.awaitTermination(timeout, unit);
    }

    @RequiredArgsConstructor
    static class ClickhouseInsertTask implements Callable<Boolean> {
        private final AuditEvent event;
        private final ClickhouseConnection connection;
        private final ClickhouseErrorHandler errorHandler;

        @Override
        public Boolean call() {
            try {
                connection.insert(event);
                return true;
            } catch (SQLException ex) {
                if (errorHandler != null)
                    errorHandler.onInsertFailed(event, ex);

                return false;
            }
        }
    }

}
