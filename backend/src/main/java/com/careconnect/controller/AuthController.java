package com.careconnect.controller;

import com.careconnect.config.DemoPrincipal;
import com.careconnect.config.JwtService;
import com.careconnect.model.AppUser;
import com.careconnect.model.Patient;
import com.careconnect.repository.AppUserRepository;
import com.careconnect.repository.PatientRepository;
import com.careconnect.service.AuditService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/auth")
public class AuthController {
    private final AppUserRepository users; private final PatientRepository patients; private final JwtService jwt; private final AuditService audit; private final PasswordEncoder passwordEncoder;
    public AuthController(AppUserRepository users, PatientRepository patients, JwtService jwt, AuditService audit, PasswordEncoder passwordEncoder) { this.users=users; this.patients=patients; this.jwt=jwt; this.audit=audit; this.passwordEncoder=passwordEncoder; }

    @PostMapping("/login")
    public Map<String,Object> login(@Valid @RequestBody LoginRequest request) {
        AppUser user=users.findByUsername(request.username()).filter(item -> item.active && item.passwordHash != null && passwordEncoder.matches(request.password(), item.passwordHash)).orElseThrow(() -> new IllegalArgumentException("Invalid username or password"));
        return response(user);
    }

    @PostMapping("/signup")
    @ResponseStatus(HttpStatus.CREATED)
    @Transactional
    public Map<String,Object> signup(@Valid @RequestBody SignupRequest request) {
        if (users.findByUsername(request.username()).isPresent()) throw new IllegalArgumentException("That username is already registered");
        Patient patient=new Patient(); patient.mrn=uniqueMrn(); patient.firstName=request.firstName(); patient.lastName=request.lastName(); patient.preferredName=request.firstName(); patient.email=request.email(); patient.phone=request.phone(); patient.dateOfBirth=request.dateOfBirth()==null || request.dateOfBirth().isBlank() ? null : LocalDate.parse(request.dateOfBirth()); patient.status="ACTIVE"; patient.initials=request.firstName().substring(0,1)+request.lastName().substring(0,1); patient.risk="LOW"; patient.allergies=new java.util.ArrayList<>(); patient=patients.save(patient);
        AppUser user=new AppUser(); user.username=request.username(); user.displayName=request.firstName()+" "+request.lastName(); user.role="PATIENT"; user.initials=patient.initials; user.subtitle="Patient portal"; user.patientId=patient.id; user.passwordHash=passwordEncoder.encode(request.password()); user.active=true; user=users.save(user);
        DemoPrincipal actor=new DemoPrincipal(user.id,user.username,user.role,user.patientId); audit.record(actor,"SIGNUP","AppUser",user.id,"Patient account and record created"); return response(user);
    }

    @GetMapping("/me")
    public Map<String,Object> me(org.springframework.security.core.Authentication authentication) { if (authentication == null || !(authentication.getPrincipal() instanceof DemoPrincipal principal)) throw new IllegalArgumentException("A valid JWT session is required"); return Map.of("username", principal.username(), "role", principal.role(), "userId", principal.userId(), "patientId", principal.patientId() == null ? 0 : principal.patientId()); }

    private String uniqueMrn() { String mrn; do { mrn="CC-"+UUID.randomUUID().toString().replace("-", "").substring(0,8).toUpperCase(); } while (patients.findByMrn(mrn).isPresent()); return mrn; }
    private Map<String,Object> response(AppUser user) { DemoPrincipal actor=new DemoPrincipal(user.id,user.username,user.role,user.patientId); audit.record(actor,"LOGIN","AppUser",user.id,"JWT session issued"); Map<String,Object> profile=new HashMap<>(); profile.put("id",user.id); profile.put("displayName",user.displayName); profile.put("role",user.role); profile.put("initials",user.initials); profile.put("subtitle",user.subtitle); profile.put("patientId",user.patientId == null ? 0 : user.patientId); return Map.of("token",jwt.issue(user),"user",profile); }
}
