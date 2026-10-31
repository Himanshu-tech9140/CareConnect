package com.careconnect.controller;

import com.careconnect.config.DemoPrincipal;
import com.careconnect.model.Encounter;
import com.careconnect.repository.EncounterRepository;
import com.careconnect.repository.PatientRepository;
import com.careconnect.service.ClinicalDataService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api")
public class ClinicalController {
    private final ClinicalDataService service;
    private final PatientRepository patients;
    private final EncounterRepository encounters;

    public ClinicalController(ClinicalDataService service, PatientRepository patients, EncounterRepository encounters) {
        this.service = service;
        this.patients = patients;
        this.encounters = encounters;
    }

    @GetMapping("/dashboard/{role}")
    public Map<String,Object> dashboard(@PathVariable String role, Authentication authentication) {
        DemoPrincipal principal = principal(authentication);
        boolean providerDashboard = "PROVIDER".equalsIgnoreCase(role) && ("PROVIDER".equals(principal.role()) || "NURSE".equals(principal.role()) || "ADMIN".equals(principal.role()));
        boolean patientDashboard = "PATIENT".equalsIgnoreCase(role) && "PATIENT".equals(principal.role());
        if (!providerDashboard && !patientDashboard) throw new IllegalArgumentException("The session role cannot access this dashboard");
        return service.dashboard(role, principal.patientId());
    }

    @GetMapping("/patients") public List<Map<String,Object>> patients(@RequestParam(defaultValue="") String search) { return service.patients(search); }
    @GetMapping("/patients/{id}/chart") public Map<String,Object> chart(@PathVariable Long id) { return service.patientChart(id); }
    @GetMapping("/encounters") public List<Map<String,Object>> encounters(Authentication authentication) { DemoPrincipal principal=principal(authentication); return "PATIENT".equals(principal.role()) && principal.patientId()!=null ? service.encountersForPatient(principal.patientId()) : service.encounters(); }
    @GetMapping("/encounters/{id}") public Map<String,Object> encounter(@PathVariable Long id) { return service.encounter(id); }
    @GetMapping("/orders") public List<Map<String,Object>> orders() { return service.orders(); }

    @GetMapping("/medications")
    public List<Map<String,Object>> medications(Authentication authentication) {
        DemoPrincipal principal = principal(authentication);
        return service.medications(principal.role(), principal.patientId());
    }

    @PatchMapping("/encounters/{id}/note")
    public Map<String,Object> updateNote(@PathVariable Long id, @Valid @RequestBody NoteUpdateRequest request) {
        return service.updateEncounterNote(id, request.history(), request.examination(), request.assessment(), request.plan(), request.followUp(), request.noteStatus());
    }

    @PatchMapping("/orders/{id}/status")
    public Map<String,Object> updateOrder(@PathVariable Long id, @Valid @RequestBody StatusUpdateRequest request) { return service.updateOrder(id, request.status()); }

    @PostMapping("/encounters")
    @ResponseStatus(HttpStatus.CREATED)
    public Encounter createEncounter(@RequestBody Map<String,Object> body) {
        Encounter e = new Encounter();
        e.patient = patients.findById(Long.valueOf(body.get("patientId").toString())).orElseThrow();
        e.provider = String.valueOf(body.getOrDefault("provider", "Dr. Lena Ortiz"));
        e.date = LocalDate.now(); e.time = java.time.LocalTime.now();
        e.type = String.valueOf(body.getOrDefault("type", "Follow-up")); e.reason = String.valueOf(body.getOrDefault("reason", "Clinical follow-up"));
        e.status = "DRAFT"; e.noteStatus = "DRAFT"; e.location = "Exam room 04"; e.notePreview = "New encounter ready for documentation.";
        return encounters.save(e);
    }

    @PostMapping("/orders")
    @ResponseStatus(HttpStatus.CREATED)
    public Map<String,Object> createOrder(@Valid @RequestBody OrderCreateRequest request) { return service.createOrder(request.patientId(), request.category(), request.item(), request.priority(), request.due()); }

    @PostMapping("/medications")
    @ResponseStatus(HttpStatus.CREATED)
    public Map<String,Object> createMedication(@Valid @RequestBody MedicationCreateRequest request) { return service.createMedication(request.patientId(), request.name(), request.strength(), request.route(), request.frequency(), request.instructions(), request.allergyAcknowledged()); }

    @PatchMapping("/medications/{id}/status")
    public Map<String,Object> updateMedicationStatus(@PathVariable Long id, @Valid @RequestBody StatusUpdateRequest request) { return service.updateMedicationStatus(id, request.status()); }

    private DemoPrincipal principal(Authentication authentication) {
        if (authentication != null && authentication.getPrincipal() instanceof DemoPrincipal demoPrincipal) return demoPrincipal;
        throw new IllegalArgumentException("A CareConnect demo session is required");
    }
}
