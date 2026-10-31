package com.careconnect.controller;

import com.careconnect.config.DemoPrincipal;
import com.careconnect.model.AppUser;
import com.careconnect.model.ProviderProfile;
import com.careconnect.repository.AppUserRepository;
import com.careconnect.repository.ProviderProfileRepository;
import com.careconnect.service.AuditService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/admin/staff")
public class StaffManagementController {
    private final AppUserRepository users; private final ProviderProfileRepository profiles; private final PasswordEncoder passwordEncoder; private final AuditService audit;
    public StaffManagementController(AppUserRepository users, ProviderProfileRepository profiles, PasswordEncoder passwordEncoder, AuditService audit) { this.users=users; this.profiles=profiles; this.passwordEncoder=passwordEncoder; this.audit=audit; }

    @GetMapping
    public List<Map<String,Object>> list() { return users.findAll().stream().filter(user -> "PROVIDER".equals(user.role) || "NURSE".equals(user.role)).map(this::view).toList(); }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Transactional
    public Map<String,Object> create(@Valid @RequestBody StaffCreateRequest request, Authentication authentication) {
        String role=request.role().trim().toUpperCase();
        if (!List.of("PROVIDER","NURSE").contains(role)) throw new IllegalArgumentException("Staff role must be PROVIDER or NURSE");
        if (users.findByUsername(request.username().trim()).isPresent()) throw new IllegalArgumentException("That username is already registered");
        AppUser user=new AppUser(); user.username=request.username().trim(); user.displayName=request.displayName().trim(); user.role=role; user.initials=initials(user.displayName); user.subtitle=role.equals("PROVIDER") ? blankToEmpty(request.specialization())+" · "+request.department().trim() : "Nursing · "+request.department().trim(); user.passwordHash=passwordEncoder.encode(request.password()); user.active=true; user=users.save(user);
        if ("PROVIDER".equals(role)) {
            ProviderProfile profile=new ProviderProfile(); profile.user=user; profile.specialization=blankToEmpty(request.specialization()); profile.qualification=blankToEmpty(request.qualification()); profile.licenseNumber=blankToEmpty(request.licenseNumber()); profile.department=request.department().trim(); profile.active=true; profiles.save(profile);
        }
        DemoPrincipal actor=principal(authentication); audit.record(actor,"STAFF_ACCOUNT_CREATED","AppUser",user.id,role+" account created for "+user.username); return view(user);
    }

    @PatchMapping("/{id}/status")
    @Transactional
    public Map<String,Object> status(@PathVariable Long id, @Valid @RequestBody StatusUpdateRequest request, Authentication authentication) {
        if (!List.of("ACTIVE","INACTIVE").contains(request.status())) throw new IllegalArgumentException("Staff status must be ACTIVE or INACTIVE");
        AppUser user=users.findById(id).orElseThrow(); if (!List.of("PROVIDER","NURSE").contains(user.role)) throw new IllegalArgumentException("Only Doctor or Nurse accounts can be managed here"); user.active="ACTIVE".equals(request.status()); users.save(user); profiles.findByUserId(id).ifPresent(profile->{ profile.active=user.active; profiles.save(profile); }); audit.record(principal(authentication),"STAFF_STATUS_CHANGED","AppUser",id,user.username+" set to "+request.status()); return view(user);
    }

    private DemoPrincipal principal(Authentication authentication) { return authentication != null && authentication.getPrincipal() instanceof DemoPrincipal p ? p : new DemoPrincipal(null,"admin","ADMIN",null); }
    private String initials(String name) { String[] parts=name.trim().split("\\s+"); return (parts[0].substring(0,1)+(parts.length>1?parts[parts.length-1].substring(0,1):"")).toUpperCase(); }
    private String blankToEmpty(String value) { return value == null ? "" : value.trim(); }
    private Map<String,Object> view(AppUser user) { Map<String,Object> result=new HashMap<>(); ProviderProfile profile=profiles.findByUserId(user.id).orElse(null); result.put("id",user.id); result.put("username",user.username); result.put("displayName",user.displayName); result.put("role",user.role); result.put("initials",user.initials); result.put("subtitle",user.subtitle); result.put("active",user.active); result.put("specialization",profile == null ? "" : profile.specialization); result.put("qualification",profile == null ? "" : profile.qualification); result.put("licenseNumber",profile == null ? "" : profile.licenseNumber); result.put("department",profile == null ? "" : profile.department); return result; }
}
