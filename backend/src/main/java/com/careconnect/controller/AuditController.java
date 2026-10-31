package com.careconnect.controller;

import com.careconnect.repository.AuditEventRepository;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/audit")
public class AuditController {
    private final AuditEventRepository events;
    public AuditController(AuditEventRepository events) { this.events=events; }
    @GetMapping public List<Map<String,Object>> list() {
        List<Map<String,Object>> result=new ArrayList<>();
        events.findTop100ByOrderByCreatedAtDesc().forEach(event -> { Map<String,Object> row=new HashMap<>(); row.put("id",event.id); row.put("actorUsername",event.actorUsername == null ? "system" : event.actorUsername); row.put("actorRole",event.actorRole == null ? "SYSTEM" : event.actorRole); row.put("action",event.action); row.put("entityType",event.entityType == null ? "" : event.entityType); row.put("entityId",event.entityId == null ? 0 : event.entityId); row.put("details",event.details == null ? "" : event.details); row.put("createdAt",event.createdAt == null ? "" : event.createdAt.toString()); result.add(row); });
        return result;
    }
}
