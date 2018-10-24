package ru.anarok.audit.service.impl;

import lombok.extern.slf4j.Slf4j;
import ru.anarok.audit.dao.Dao;
import ru.anarok.audit.domain.AuditEvent;
import ru.anarok.audit.service.IAuditService;

import static ru.anarok.audit.constants.LoggerMessages.AUDIT_EVENT_MESSAGE;

@Slf4j
public class AuditServiceImpl implements IAuditService {
    private Dao<AuditEvent, Integer> auditEventDao; //Create clickhouse dao

    @Override
    public void audit(AuditEvent auditEvent) {
        auditEvent = auditEventDao.save(auditEvent);
        log.info(AUDIT_EVENT_MESSAGE, auditEvent.getType(), auditEvent.getId(), auditEvent.getMessage(), auditEvent.getPrincipal());
    }
}
