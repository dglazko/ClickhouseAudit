# ClickhouseAudit

Java library for pushing audit messages to Yandex Clickhouse database.


## Example
```java

ClickhouseAuditService auditService = ClickhouseAuditFactory
        .create("localhost")
        .build();

AuditEvent event = new AuditEvent("example", "test-message-type", "This is the audit message");
event.getData().put("property 1", "value 1");
event.getData().put("property 2", "value 2");

auditService.submit(event);

```