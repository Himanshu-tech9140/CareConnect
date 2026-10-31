package com.careconnect.repository;

import com.careconnect.model.AuditEvent;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface AuditEventRepository extends JpaRepository<AuditEvent, Long> { List<AuditEvent> findTop100ByOrderByCreatedAtDesc(); }
