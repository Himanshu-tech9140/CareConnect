package com.careconnect.service;

import com.careconnect.config.DemoPrincipal;
import com.careconnect.model.AuditEvent;
import com.careconnect.repository.AuditEventRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;

@Service
public class AuditService {
    private final AuditEventRepository repository;
    public AuditService(AuditEventRepository repository) { this.repository=repository; }
    @Transactional public void record(DemoPrincipal actor, String action, String entityType, Long entityId, String details) { AuditEvent event=new AuditEvent(); event.actorUsername=actor == null ? "system" : actor.username(); event.actorRole=actor == null ? "SYSTEM" : actor.role(); event.action=action; event.entityType=entityType; event.entityId=entityId; event.details=details; event.createdAt=Instant.now(); repository.save(event); }
}
