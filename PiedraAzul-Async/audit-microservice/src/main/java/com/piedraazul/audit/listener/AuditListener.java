package com.piedraazul.audit.listener;

import com.piedraazul.audit.model.AuditLog;
import com.piedraazul.audit.repository.AuditLogRepository;
import org.springframework.amqp.rabbit.annotation.Exchange;
import org.springframework.amqp.rabbit.annotation.Queue;
import org.springframework.amqp.rabbit.annotation.QueueBinding;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Component
public class AuditListener {

    @Autowired
    private AuditLogRepository auditLogRepository;

    @RabbitListener(bindings = @QueueBinding(
            value = @Queue(value = "audit.queue", durable = "true"),
            exchange = @Exchange(value = "audit.exchange", type = "topic", ignoreDeclarationExceptions = "true"),
            key = "audit.routing.key"
    ))
    public void receiveMessage(String message) {
        System.out.println("Mensaje de auditoría recibido: " + message);
        AuditLog auditLog = new AuditLog(message, LocalDateTime.now());
        auditLogRepository.save(auditLog);
    }
}
