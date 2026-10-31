package com.careconnect.model;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.Instant;

@Entity
@Table(name="audit_events")
public class AuditEvent {
    @Id @GeneratedValue(strategy=GenerationType.IDENTITY) public Long id;
    public String actorUsername;
    public String actorRole;
    public String action;
    public String entityType;
    public Long entityId;
    public String details;
    public Instant createdAt;
    public AuditEvent() { }
}
