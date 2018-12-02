package ru.anarok.audit;

import java.util.List;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

public interface ClickhouseAuditService {
    /**
     * Submit audit to the Clickhouse server
     *
     * @param audit audit to be submitted
     * @return future that returns true if the task has been successfully executed
     */
    Future<Boolean> submit(AuditEvent audit);

    /**
     * Shutdown audit service, interrupt currently executing tasks,
     * return queued tasks.
     * <p>
     * Warning! This method might not return all the tasks that were currently running at the time
     * service was shutdown. To obtain these tasks one should register custom {@link ClickhouseErrorHandler}
     *
     * @return queued tasks
     */
    List<AuditEvent> shutdownInterrupted();

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
    boolean shutdownWait(long timeout, TimeUnit unit) throws InterruptedException;
}
