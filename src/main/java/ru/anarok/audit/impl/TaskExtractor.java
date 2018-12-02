package ru.anarok.audit.impl;

import lombok.extern.slf4j.Slf4j;

import java.lang.reflect.Field;
import java.util.concurrent.FutureTask;

@Slf4j
class TaskExtractor {
    private Field field;

    TaskExtractor() {
        try {
            field = FutureTask.class.getDeclaredField("callable");
            field.setAccessible(true);
        } catch (NoSuchFieldException e) {
            field = null;
            log.error("ClickhouseAudit task extractor failed to use Reflection to obtain reference to 'callable' field of FutureTask. " +
                    "ClickhouseAuditService.shutdownInterrupted() will not return task list.", e);
        }
    }

    public boolean isReady() {
        return field != null;
    }


    public DefaultAuditService.ClickhouseInsertTask getTask(FutureTask task) {
        if (field == null) return null;
        try {
            return (DefaultAuditService.ClickhouseInsertTask) field.get(task);
        } catch (IllegalAccessException e) {
            log.warn("Unable to access field 'callable' of FutureTask.", e);
            return null;
        }
    }
}
