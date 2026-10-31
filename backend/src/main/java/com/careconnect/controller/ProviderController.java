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
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/providers")
public class ProviderController {
    private final ProviderProfileRepository profiles; private final AppUserRepository users; private final AuditService audit;
    public ProviderController(ProviderProfileRepository profiles, AppUserRepository users, AuditService audit) { this.profiles=profiles; this.users=users; this.audit=audit; }
    @GetMapping public List<Map<String,Object>> list() { return profiles.findAll().stream().filter(profile -> "PROVIDER".equals(profile.user.role)).map(this::view).toList(); }
    @PostMapping @ResponseStatus(HttpStatus.CREATED)
    public Map<String,Object> create(@Valid @RequestBody ProviderProfileRequest request, Authentication authentication) { AppUser user=users.findById(request.userId()).orElseThrow(); ProviderProfile profile=new ProviderProfile(); profile.user=user; profile.specialization=request.specialization(); profile.qualification=request.qualification(); profile.licenseNumber=request.licenseNumber(); profile.department=request.department(); profile.active=request.active(); ProviderProfile saved=profiles.save(profile); audit.record(principal(authentication),"PROVIDER_PROFILE_CREATED","ProviderProfile",saved.id,"Provider directory profile created"); return view(saved); }
    private Map<String,Object> view(ProviderProfile p) { return Map.of("id",p.id,"userId",p.user.id,"displayName",p.user.displayName,"username",p.user.username,"role",p.user.role,"specialization",p.specialization,"qualification",p.qualification,"licenseNumber",p.licenseNumber,"department",p.department,"active",p.active); }
    private DemoPrincipal principal(Authentication auth) { if (auth != null && auth.getPrincipal() instanceof DemoPrincipal p) return p; throw new IllegalArgumentException("A valid staff session is required"); }
}
