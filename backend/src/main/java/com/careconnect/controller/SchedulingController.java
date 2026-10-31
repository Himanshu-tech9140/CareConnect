package com.careconnect.controller;

import com.careconnect.config.DemoPrincipal;
import com.careconnect.model.AppUser;
import com.careconnect.model.Appointment;
import com.careconnect.repository.AppUserRepository;
import com.careconnect.repository.AppointmentRepository;
import com.careconnect.repository.PatientRepository;
import com.careconnect.service.AuditService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/appointments")
public class SchedulingController {
    private final AppointmentRepository appointments; private final PatientRepository patients; private final AppUserRepository users; private final AuditService audit;
    public SchedulingController(AppointmentRepository appointments, PatientRepository patients, AppUserRepository users, AuditService audit) { this.appointments=appointments; this.patients=patients; this.users=users; this.audit=audit; }
    @GetMapping public List<Map<String,Object>> list(Authentication authentication) { DemoPrincipal actor=principal(authentication); var values="PATIENT".equals(actor.role()) && actor.patientId()!=null ? appointments.findByPatientIdOrderByAppointmentDateAscStartTimeAsc(actor.patientId()) : appointments.findAllByOrderByAppointmentDateAscStartTimeAsc(); List<Map<String,Object>> result=new ArrayList<>(); values.forEach(item -> result.add(view(item))); return result; }
    @PostMapping @ResponseStatus(HttpStatus.CREATED)
    public Map<String,Object> book(@Valid @RequestBody AppointmentCreateRequest request, Authentication authentication) { DemoPrincipal actor=principal(authentication); if ("PATIENT".equals(actor.role()) && (actor.patientId()==null || !actor.patientId().equals(request.patientId()))) throw new IllegalArgumentException("Patients can only book appointments for their own care record"); LocalDate date=LocalDate.parse(request.date()); LocalTime start=LocalTime.parse(request.startTime()); LocalTime end=LocalTime.parse(request.endTime()); if (date.isBefore(LocalDate.now())) throw new IllegalArgumentException("Appointments must be scheduled for today or later"); if (!start.isBefore(end)) throw new IllegalArgumentException("Appointment end time must be after start time"); AppUser provider=users.findById(request.providerId()).orElseThrow(); if (!"PROVIDER".equals(provider.role)) throw new IllegalArgumentException("Appointment provider must have a provider role"); if (appointments.existsByProviderIdAndAppointmentDateAndStartTimeLessThanAndEndTimeGreaterThan(provider.id,date,end,start)) throw new IllegalArgumentException("The provider already has an overlapping appointment"); Appointment item=new Appointment(); item.patient=patients.findById(request.patientId()).orElseThrow(); item.provider=provider; item.appointmentDate=date; item.startTime=start; item.endTime=end; item.type=request.type(); item.location=request.location(); item.status="REQUESTED"; Appointment saved=appointments.save(item); audit.record(actor,"APPOINTMENT_BOOKED","Appointment",saved.id,"Appointment requested for " + date); return view(saved); }
    @PatchMapping("/{id}/status") public Map<String,Object> status(@PathVariable Long id, @Valid @RequestBody StatusUpdateRequest request, Authentication authentication) { if (!List.of("REQUESTED","CONFIRMED","CANCELLED","COMPLETED").contains(request.status())) throw new IllegalArgumentException("Unsupported appointment status"); Appointment item=appointments.findById(id).orElseThrow(); item.status=request.status(); Appointment saved=appointments.save(item); audit.record(principal(authentication),"APPOINTMENT_STATUS_CHANGED","Appointment",id,"Status changed to " + request.status()); return view(saved); }
    private Map<String,Object> view(Appointment item) { Map<String,Object> result=new HashMap<>(); result.put("id",item.id); result.put("reference","CC-" + item.appointmentDate.toString().replace("-", "") + "-" + item.id); result.put("patientId",item.patient.id); result.put("patientName",item.patient.firstName+" "+item.patient.lastName); result.put("type",item.type); result.put("date",item.appointmentDate.toString()); result.put("time",item.startTime.toString()); result.put("endTime",item.endTime.toString()); result.put("provider",item.provider.displayName); result.put("providerId",item.provider.id); result.put("location",item.location); result.put("status",item.status); return result; }
    private DemoPrincipal principal(Authentication auth) { if (auth != null && auth.getPrincipal() instanceof DemoPrincipal p) return p; throw new IllegalArgumentException("A valid session is required"); }
}
