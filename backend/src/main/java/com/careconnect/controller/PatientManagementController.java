package com.careconnect.controller;

import com.careconnect.config.DemoPrincipal;
import com.careconnect.model.Patient;
import com.careconnect.repository.PatientRepository;
import com.careconnect.service.AuditService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;

@RestController
@RequestMapping("/api/patients")
public class PatientManagementController {
    private final PatientRepository patients; private final AuditService audit;
    public PatientManagementController(PatientRepository patients, AuditService audit) { this.patients=patients; this.audit=audit; }
    @PostMapping @ResponseStatus(HttpStatus.CREATED)
    public Patient create(@Valid @RequestBody PatientCreateRequest request, Authentication authentication) { Patient p=new Patient(); p.mrn="CC-" + String.format("%05d", 10482 + patients.count() + 1); p.firstName=request.firstName(); p.lastName=request.lastName(); p.preferredName=request.firstName(); p.dateOfBirth=request.dateOfBirth()==null || request.dateOfBirth().isBlank() ? null : LocalDate.parse(request.dateOfBirth()); p.sex=request.sex(); p.status="ACTIVE"; p.phone=request.phone(); p.email=request.email(); p.city=request.city(); p.state=request.state(); p.emergencyContactName=request.emergencyContactName(); p.emergencyContactPhone=request.emergencyContactPhone(); p.initials=request.firstName().substring(0,1)+request.lastName().substring(0,1); p.risk="LOW"; DemoPrincipal actor=principal(authentication); Patient saved=patients.save(p); audit.record(actor,"PATIENT_REGISTERED","Patient",saved.id,"Patient number " + saved.mrn); return saved; }
    @PutMapping("/{id}") public Patient update(@PathVariable Long id, @Valid @RequestBody PatientCreateRequest request, Authentication authentication) { Patient p=patients.findById(id).orElseThrow(); p.firstName=request.firstName(); p.lastName=request.lastName(); p.preferredName=request.firstName(); p.dateOfBirth=request.dateOfBirth()==null || request.dateOfBirth().isBlank() ? null : LocalDate.parse(request.dateOfBirth()); p.sex=request.sex(); p.phone=request.phone(); p.email=request.email(); p.city=request.city(); p.state=request.state(); p.emergencyContactName=request.emergencyContactName(); p.emergencyContactPhone=request.emergencyContactPhone(); Patient saved=patients.save(p); audit.record(principal(authentication),"PATIENT_UPDATED","Patient",id,"Contact and demographic details updated"); return saved; }
    @PatchMapping("/{id}/status") public Patient status(@PathVariable Long id, @RequestBody StatusUpdateRequest request, Authentication authentication) { if (!request.status().equals("ACTIVE") && !request.status().equals("INACTIVE")) throw new IllegalArgumentException("Patient status must be ACTIVE or INACTIVE"); Patient p=patients.findById(id).orElseThrow(); p.status=request.status(); Patient saved=patients.save(p); audit.record(principal(authentication),"PATIENT_STATUS_CHANGED","Patient",id,"Status changed to " + request.status()); return saved; }
    private DemoPrincipal principal(Authentication auth) { if (auth != null && auth.getPrincipal() instanceof DemoPrincipal p) return p; throw new IllegalArgumentException("A valid staff session is required"); }
}
