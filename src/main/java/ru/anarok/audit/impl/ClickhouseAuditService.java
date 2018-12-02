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

    /**
     * Submit audit to the Clickhouse server
     *
     * @param audit audit to be submitted
     * @return future that returns true if the task has been successfully executed
     */
    public Future<Boolean> submit(AuditEvent audit) {
        ClickhouseInsertTask task = new ClickhouseInsertTask(audit, connection, errorHandler);

        return executorService.submit(task);
    }

    /**
     * Shutdown audit service, interrupt currently executing tasks,
     * return queued tasks.
     * <p>
     * Warning! This method might not return all the tasks that were currently running at the time
     * service was shutdown. To obtain these tasks one should register custom {@link ClickhouseErrorHandler}
     *
     * @return queued tasks
     */
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

    /**
     * Shutdown audit service, await until all active & queued tasks are compelled
     * or until this call times out
     *
     * @param timeout the maximum time to wait
     * @param unit    the time unit of the timeout argument
     * @return {@code true} if this executor terminated and
     * {@code false} if the timeout elapsed before termination
     * @throws InterruptedException if interrupted while waiting
     */
    public boolean shutdownWait(long timeout, TimeUnit unit) throws InterruptedException {
        return executorService.awaitTermination(timeout, unit);
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
