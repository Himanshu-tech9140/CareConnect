package com.careconnect.service;

import com.careconnect.config.DemoPrincipal;
import com.careconnect.model.AppUser;
import com.careconnect.model.Appointment;
import com.careconnect.model.ClinicalOrder;
import com.careconnect.model.Encounter;
import com.careconnect.model.Medication;
import com.careconnect.model.Patient;
import com.careconnect.model.ProviderProfile;
import com.careconnect.repository.AppUserRepository;
import com.careconnect.repository.AppointmentRepository;
import com.careconnect.repository.ClinicalOrderRepository;
import com.careconnect.repository.EncounterRepository;
import com.careconnect.repository.MedicationRepository;
import com.careconnect.repository.PatientRepository;
import com.careconnect.repository.ProviderProfileRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.Period;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class ClinicalDataService {
    @Value("${careconnect.demo-seed:false}") private boolean demoSeed;
    private final PatientRepository patientRepository;
    private final EncounterRepository encounterRepository;
    private final ClinicalOrderRepository orderRepository;
    private final MedicationRepository medicationRepository;
    private final AppUserRepository userRepository;
    private final ProviderProfileRepository providerProfileRepository;
    private final AppointmentRepository appointmentRepository;
    private final AuditService auditService;
    private final PasswordEncoder passwordEncoder;

    public ClinicalDataService(PatientRepository patientRepository, EncounterRepository encounterRepository, ClinicalOrderRepository orderRepository, MedicationRepository medicationRepository, AppUserRepository userRepository, ProviderProfileRepository providerProfileRepository, AppointmentRepository appointmentRepository, AuditService auditService, PasswordEncoder passwordEncoder) {
        this.patientRepository = patientRepository; this.encounterRepository = encounterRepository; this.orderRepository = orderRepository; this.medicationRepository = medicationRepository; this.userRepository = userRepository; this.providerProfileRepository=providerProfileRepository; this.appointmentRepository=appointmentRepository; this.auditService=auditService; this.passwordEncoder=passwordEncoder;
    }

    @Bean
    CommandLineRunner seedDemoData() { return args -> { if (demoSeed) seed(); }; }

    @Bean
    CommandLineRunner seedSupplementalDemoData() { return args -> { if (demoSeed) supplementalSeed(); }; }

    @Transactional
    public void seed() {
        // Keep the demo dataset small and idempotent; never overwrite user-created records.
        if (userRepository.findByUsername("lena.ortiz").isPresent()) return;
        Patient maya = patient("CC-10482", "Maya", "Thompson", "1988-04-12", "Female", "O+", "Dr. Lena Ortiz", "LOW", "(415) 555-0182", "maya.thompson@example.com", "San Francisco", List.of("Penicillin"));
        maya.nextVisit = LocalDate.of(2026, 10, 14); maya = patientRepository.save(maya);
        Patient jordan = patient("CC-10631", "Jordan", "Lee", "1975-09-03", "Male", "A-", "Dr. Lena Ortiz", "MEDIUM", "(415) 555-0147", "jordan.lee@example.com", "Oakland", List.of());
        jordan.nextVisit = LocalDate.of(2026, 10, 5); jordan = patientRepository.save(jordan);
        Patient amina = patient("CC-10908", "Amina", "Rahman", "1992-02-18", "Female", "B+", "Dr. Noah Chen", "HIGH", "(510) 555-0109", "amina.rahman@example.com", "Berkeley", List.of("Sulfa drugs", "Latex")); amina = patientRepository.save(amina);
        Patient elias = patient("CC-11126", "Elias", "Morgan", "1964-11-20", "Male", "AB+", "Dr. Priya Shah", "MEDIUM", "(628) 555-0122", "elias.morgan@example.com", "Daly City", List.of()); elias.nextVisit = LocalDate.of(2026, 10, 11); elias = patientRepository.save(elias);

        encounter(maya, "Dr. Lena Ortiz", "2026-09-30", "09:20", "Follow-up", "Hypertension review", "OPEN", "Exam room 04", "Blood pressure trending down after dosage adjustment. Continue home readings.", "Blood pressure review", "Essential hypertension", "Continue current dose; repeat home readings.");
        encounter(jordan, "Dr. Lena Ortiz", "2026-09-30", "08:45", "Annual physical", "Preventive care", "DRAFT", "Exam room 02", "Annual physical in progress. Labs reviewed with patient.", "Preventive health review", "Preventive care assessment", "Complete screening labs and review results.");
        encounter(amina, "Dr. Noah Chen", "2026-09-29", "15:10", "Acute visit", "Persistent cough", "SIGNED", "Exam room 08", "Cough improved. Chest X-ray ordered to rule out lower respiratory involvement.", "Persistent cough", "Acute cough, improving", "Chest X-ray ordered; follow up if symptoms worsen.");
        encounter(elias, "Dr. Priya Shah", "2026-09-28", "11:30", "Medication review", "Diabetes management", "SIGNED", "Telehealth", "A1c reviewed. Continue current regimen and repeat in 12 weeks.", "Diabetes medication review", "Type 2 diabetes management", "Continue regimen; endocrinology consultation requested.");
        order(amina, "IMAGING", "Chest X-ray · 2 views", "URGENT", "IN_PROGRESS", "Dr. Noah Chen", "2026-10-01");
        order(jordan, "LAB", "Lipid panel + HbA1c", "ROUTINE", "PENDING", "Dr. Lena Ortiz", "2026-10-02");
        order(elias, "REFERRAL", "Endocrinology consultation", "ROUTINE", "COMPLETED", "Dr. Priya Shah", "2026-10-12");
        order(maya, "LAB", "Basic metabolic panel", "STAT", "COMPLETED", "Dr. Lena Ortiz", "2026-09-28");
        medication(maya, "Lisinopril", "10 mg", "Oral", "Once daily", "ACTIVE", "Dr. Lena Ortiz", "2026-08-15", "2 refills", "Take in the morning with or without food.");
        medication(jordan, "Atorvastatin", "20 mg", "Oral", "Once nightly", "ACTIVE", "Dr. Lena Ortiz", "2026-01-12", "4 refills", "Take in the evening. Report new muscle pain.");
        medication(amina, "Albuterol HFA", "90 mcg", "Inhaled", "Every 4–6 hours PRN", "ACTIVE", "Dr. Noah Chen", "2026-09-29", "1 refill", "Use 2 puffs as needed for wheezing.");
        medication(elias, "Metformin ER", "500 mg", "Oral", "Twice daily", "PENDING", "Dr. Priya Shah", "2026-09-28", "0 refills", "Take with breakfast and dinner.");
        AppUser provider = user("lena.ortiz", "Dr. Lena Ortiz", "PROVIDER", "LO", "Internal medicine · Bayview Clinic", null); provider=userRepository.save(provider);
        AppUser nurse = user("nina.patel", "Nina Patel, RN", "NURSE", "NP", "Care coordinator · Bayview Clinic", null); userRepository.save(nurse);
        if (userRepository.findByUsername("admin").isEmpty()) userRepository.save(user("admin", "CareConnect Admin", "ADMIN", "AD", "System administration", null));
        AppUser patient = user("maya.thompson", "Maya Thompson", "PATIENT", "MT", "Patient portal", maya.id); userRepository.save(patient);
        providerProfile(provider, "Internal medicine", "MD · UCSF School of Medicine", "CA-MD-20481", "Primary Care", true);
        appointment(maya, provider, "2026-10-14", "10:30", "11:00", "Primary care follow-up", "Clinic · Room 04", "CONFIRMED");
        appointment(jordan, provider, "2026-10-05", "09:00", "09:45", "Annual physical", "Clinic · Room 02", "CONFIRMED");
    }

    @Transactional
    public void supplementalSeed() {
        AppUser noah = demoProvider("noah.chen", "Dr. Noah Chen", "NC", "Pulmonology", "MD · Stanford Medicine", "CA-MD-30912", "Respiratory Care");
        AppUser priya = demoProvider("priya.shah", "Dr. Priya Shah", "PS", "Endocrinology", "MD · UCSF Health", "CA-MD-41208", "Specialty Medicine");
        Patient samir = demoPatient("CC-12004", "Samir", "Khan", "1981-06-21", "Male", "A+", "Dr. Noah Chen", "HIGH", "(415) 555-0194", "samir.khan@example.com", "San Francisco", List.of("Penicillin"));
        Patient chloe = demoPatient("CC-12005", "Chloe", "Nguyen", "1990-02-11", "Female", "O-", "Dr. Priya Shah", "LOW", "(510) 555-0195", "chloe.nguyen@example.com", "Oakland", List.of());
        Patient omar = demoPatient("CC-12006", "Omar", "Williams", "1969-11-04", "Male", "B-", "Dr. Priya Shah", "MEDIUM", "(628) 555-0196", "omar.williams@example.com", "Daly City", List.of("Sulfa drugs"));

        medicationIfMissing(samir, "Budesonide inhaler", "180 mcg", "Inhaled", "Twice daily", "ACTIVE", noah.displayName, "2026-09-20", "2 refills", "Use after morning and evening rinse.");
        medicationIfMissing(samir, "Montelukast", "10 mg", "Oral", "Once nightly", "PENDING", noah.displayName, "2026-09-25", "1 refill", "Review response at the next respiratory visit.");
        medicationIfMissing(chloe, "Levothyroxine", "50 mcg", "Oral", "Once daily", "ACTIVE", priya.displayName, "2026-08-12", "3 refills", "Take before breakfast.");
        medicationIfMissing(omar, "Glipizide", "5 mg", "Oral", "Once daily", "ACTIVE", priya.displayName, "2026-09-03", "2 refills", "Take with the first meal and monitor glucose.");
        appointmentIfMissing(samir, noah, "2026-10-07", "14:00", "14:30", "Respiratory follow-up", "Clinic · Room 08", "CONFIRMED");
        appointmentIfMissing(chloe, priya, "2026-10-09", "11:00", "11:30", "Endocrine medication review", "Telehealth", "REQUESTED");
        appointmentIfMissing(omar, priya, "2026-10-12", "15:30", "16:00", "Diabetes care planning", "Clinic · Room 06", "CONFIRMED");
        if (userRepository.findByUsername("samir.khan").isEmpty()) userRepository.save(user("samir.khan", "Samir Khan", "PATIENT", "SK", "Patient portal", samir.id));
    }

    private AppUser demoProvider(String username, String displayName, String initials, String specialization, String qualification, String license, String department) {
        AppUser provider = userRepository.findByUsername(username).orElseGet(() -> userRepository.save(user(username, displayName, "PROVIDER", initials, specialization + " · Bayview Clinic", null)));
        if (providerProfileRepository.findByUserId(provider.id).isEmpty()) providerProfile(provider, specialization, qualification, license, department, true);
        return provider;
    }

    private Patient demoPatient(String mrn, String firstName, String lastName, String dob, String sex, String bloodType, String provider, String risk, String phone, String email, String city, List<String> allergies) {
        return patientRepository.findByMrn(mrn).orElseGet(() -> patientRepository.save(patient(mrn, firstName, lastName, dob, sex, bloodType, provider, risk, phone, email, city, allergies)));
    }

    private void medicationIfMissing(Patient patient, String name, String strength, String route, String frequency, String status, String by, String start, String refill, String instructions) {
        boolean exists = medicationRepository.findByPatientIdOrderByStatusAscNameAsc(patient.id).stream().anyMatch(item -> name.equals(item.name));
        if (!exists) medication(patient, name, strength, route, frequency, status, by, start, refill, instructions);
    }

    private void appointmentIfMissing(Patient patient, AppUser provider, String date, String start, String end, String type, String location, String status) {
        LocalDate appointmentDate = LocalDate.parse(date);
        boolean exists = appointmentRepository.findAll().stream().anyMatch(item -> patient.id.equals(item.patient.id) && provider.id.equals(item.provider.id) && appointmentDate.equals(item.appointmentDate) && LocalTime.parse(start).equals(item.startTime));
        if (!exists) appointment(patient, provider, date, start, end, type, location, status);
    }

    private Patient patient(String mrn, String firstName, String lastName, String dob, String sex, String bloodType, String provider, String risk, String phone, String email, String city, List<String> allergies) { Patient p = new Patient(); p.mrn=mrn; p.firstName=firstName; p.lastName=lastName; p.preferredName=firstName; p.dateOfBirth=LocalDate.parse(dob); p.sex=sex; p.status="ACTIVE"; p.phone=phone; p.email=email; p.city=city; p.state="CA"; p.bloodType=bloodType; p.primaryProvider=provider; p.lastVisit=LocalDate.of(2026, 9, 28); p.initials=firstName.substring(0,1)+lastName.substring(0,1); p.risk=risk; p.allergies=new ArrayList<>(allergies); return p; }
    private void encounter(Patient patient, String provider, String date, String time, String type, String reason, String status, String location, String preview, String chief, String assessment, String plan) { Encounter e=new Encounter(); e.patient=patient; e.provider=provider; e.date=LocalDate.parse(date); e.time=LocalTime.parse(time); e.type=type; e.reason=reason; e.status=status; e.location=location; e.notePreview=preview; e.chiefComplaint=chief; e.history="Patient history reviewed with the care team."; e.examination="Focused examination documented during the visit."; e.assessment=assessment; e.plan=plan; e.followUp="Follow up with the care team as planned."; e.noteStatus="SIGNED".equals(status) ? "SIGNED" : "DRAFT"; encounterRepository.save(e); }
    private void order(Patient patient, String category, String item, String priority, String status, String by, String due) { ClinicalOrder o=new ClinicalOrder(); o.patient=patient; o.category=category; o.item=item; o.priority=priority; o.status=status; o.orderedBy=by; o.orderedAt=LocalDateTime.of(2026,9,30,9,5); o.due=LocalDate.parse(due); o.activity="Order created by " + by + "."; orderRepository.save(o); }
    private void medication(Patient patient, String name, String strength, String route, String frequency, String status, String by, String start, String refill, String instructions) { Medication m=new Medication(); m.patient=patient; m.name=name; m.strength=strength; m.route=route; m.frequency=frequency; m.status=status; m.prescribedBy=by; m.startDate=LocalDate.parse(start); m.refill=refill; m.instructions=instructions; medicationRepository.save(m); }
    private AppUser user(String username, String displayName, String role, String initials, String subtitle, Long patientId) { AppUser u=new AppUser(); u.username=username; u.displayName=displayName; u.role=role; u.initials=initials; u.subtitle=subtitle; u.patientId=patientId; u.passwordHash=passwordEncoder.encode("demo"); u.active=true; return u; }
    private void providerProfile(AppUser user, String specialization, String qualification, String license, String department, boolean active) { ProviderProfile profile=new ProviderProfile(); profile.user=user; profile.specialization=specialization; profile.qualification=qualification; profile.licenseNumber=license; profile.department=department; profile.active=active; providerProfileRepository.save(profile); }
    private void appointment(Patient patient, AppUser provider, String date, String start, String end, String type, String location, String status) { Appointment item=new Appointment(); item.patient=patient; item.provider=provider; item.appointmentDate=LocalDate.parse(date); item.startTime=LocalTime.parse(start); item.endTime=LocalTime.parse(end); item.type=type; item.location=location; item.status=status; appointmentRepository.save(item); }

    public List<Map<String,Object>> patients(String search) { List<Patient> values = search == null || search.isBlank() ? patientRepository.findAll() : patientRepository.findByFirstNameContainingIgnoreCaseOrLastNameContainingIgnoreCaseOrMrnContainingIgnoreCase(search, search, search); return values.stream().map(this::patientView).toList(); }
    public Map<String,Object> patientChart(Long id) { Patient p=patientRepository.findById(id).orElseThrow(); Map<String,Object> chart=new HashMap<>(); chart.put("patient", patientView(p)); chart.put("diagnoses", List.of(Map.of("name","Care plan review","code","CC-CARE","status","Active","since",p.lastVisit.toString()))); chart.put("vitals", List.of(Map.of("label","Blood group","value",p.bloodType,"unit","","captured",p.lastVisit.toString(),"tone","normal"),Map.of("label","Care risk","value",p.risk,"unit","","captured",p.lastVisit.toString(),"tone","watch"))); chart.put("encounters", encounterRepository.findByPatientIdOrderByDateDescTimeDesc(id).stream().map(this::encounterView).toList()); chart.put("medications", medicationRepository.findByPatientIdOrderByStatusAscNameAsc(id).stream().map(this::medicationView).toList()); chart.put("allergies", p.allergies.stream().map(a -> Map.of("name",a,"reaction","Recorded allergy","severity","High")).toList()); chart.put("careTeam", List.of(Map.of("name",p.primaryProvider,"role","Primary provider","initials",p.primaryProvider.replaceAll("[^A-Za-z]","").substring(0,Math.min(2,p.primaryProvider.replaceAll("[^A-Za-z]","").length())).toUpperCase(),"color","teal"))); return chart; }
    public List<Map<String,Object>> encounters() { return encounterRepository.findAllByOrderByDateDescTimeDesc().stream().map(this::encounterView).toList(); }
    public List<Map<String,Object>> encountersForPatient(Long patientId) { return encounterRepository.findByPatientIdOrderByDateDescTimeDesc(patientId).stream().map(this::encounterView).toList(); }
    public List<Map<String,Object>> orders() { return orderRepository.findAllByOrderByOrderedAtDesc().stream().map(this::orderView).toList(); }
    public List<Map<String,Object>> medications() { return medications(null, null); }
    public List<Map<String,Object>> medications(String role, Long patientId) { var values = medicationRepository.findAllByOrderByStatusAscNameAsc(); if ("PATIENT".equalsIgnoreCase(role) && patientId != null) values = values.stream().filter(item -> patientId.equals(item.patient.id)).toList(); return values.stream().map(this::medicationView).toList(); }
    public List<Map<String,Object>> appointments() { return appointments(null, null); }
    public List<Map<String,Object>> appointments(String role, Long patientId) { var values = "PATIENT".equalsIgnoreCase(role) && patientId != null ? appointmentRepository.findByPatientIdOrderByAppointmentDateAscStartTimeAsc(patientId) : appointmentRepository.findAllByOrderByAppointmentDateAscStartTimeAsc(); return values.stream().map(this::appointmentEntityView).toList(); }

    public Map<String,Object> dashboard(String role, Long patientId) { boolean patient = "PATIENT".equalsIgnoreCase(role); var appointmentValues = patient && patientId != null ? appointmentRepository.findByPatientIdOrderByAppointmentDateAscStartTimeAsc(patientId) : appointmentRepository.findAllByOrderByAppointmentDateAscStartTimeAsc(); long patientCount=patientRepository.count(); long encounterCount=encounterRepository.count(); long orderCount=orderRepository.count(); long medicationCount=patient && patientId != null ? medicationRepository.findAllByOrderByStatusAscNameAsc().stream().filter(item -> patientId.equals(item.patient.id)).count() : medicationRepository.count(); Map<String,Object> result=new HashMap<>(); result.put("todayLabel",LocalDate.now().toString()); result.put("greeting","Good morning"); result.put("headline",patient ? "Your care, in one place." : "Your clinical day, in focus."); result.put("subline",patient ? "Your appointments, medications, and records from CareConnect." : "Live operational data from the connected care team."); result.put("metrics", patient ? List.of(Map.of("label","Upcoming visits","value",String.valueOf(appointmentValues.size()),"detail","from your care team","tone","teal"),Map.of("label","Active medications","value",String.valueOf(medicationCount),"detail","persisted medication records","tone","blue")) : List.of(Map.of("label","Patients","value",String.valueOf(patientCount),"detail","registered records","tone","teal"),Map.of("label","Encounters","value",String.valueOf(encounterCount),"detail","documented visits","tone","blue"),Map.of("label","Orders","value",String.valueOf(orderCount),"detail","clinical orders","tone","coral"),Map.of("label","Medications","value",String.valueOf(medicationCount),"detail","prescription records","tone","amber"))); result.put("tasks", List.of()); result.put("nextAppointment", appointmentValues.isEmpty() ? null : appointmentEntityView(appointmentValues.get(0))); result.put("recentPatients", patient ? List.of() : patients("")); return result; }
    public Map<String,Object> encounter(Long id) { return encounterView(encounterRepository.findById(id).orElseThrow()); }
    @Transactional public Map<String,Object> updateEncounterNote(Long id, String history, String examination, String assessment, String plan, String followUp, String noteStatus) { if (!List.of("DRAFT","SIGNED","LOCKED").contains(noteStatus)) throw new IllegalArgumentException("Note status must be DRAFT, SIGNED, or LOCKED"); Encounter e=encounterRepository.findById(id).orElseThrow(); if ("LOCKED".equals(e.noteStatus) && !"LOCKED".equals(noteStatus)) throw new IllegalArgumentException("Locked notes cannot be edited"); e.history=history; e.examination=examination; e.assessment=assessment; e.plan=plan; e.followUp=followUp; e.noteStatus=noteStatus; e.status="SIGNED".equals(noteStatus) || "LOCKED".equals(noteStatus) ? "SIGNED" : "DRAFT"; e.notePreview=assessment; Map<String,Object> result=encounterView(encounterRepository.save(e)); auditService.record(new DemoPrincipal(null,"lena.ortiz","PROVIDER",null),"ENCOUNTER_NOTE_UPDATED","Encounter",id,"Clinical note moved to " + noteStatus); return result; }
    @Transactional public Map<String,Object> createOrder(Long patientId, String category, String item, String priority, String due) { if (!List.of("LAB","IMAGING","REFERRAL","PROCEDURE").contains(category)) throw new IllegalArgumentException("Unsupported order category"); if (!List.of("ROUTINE","URGENT","STAT").contains(priority)) throw new IllegalArgumentException("Unsupported order priority"); ClinicalOrder o=new ClinicalOrder(); o.patient=patientRepository.findById(patientId).orElseThrow(); o.category=category; o.item=item; o.priority=priority; o.status="PENDING"; o.orderedBy="Dr. Lena Ortiz"; o.orderedAt=LocalDateTime.now(); o.due=LocalDate.parse(due); o.activity="Order created by Dr. Lena Ortiz."; ClinicalOrder saved=orderRepository.save(o); auditService.record(new DemoPrincipal(null,"lena.ortiz","PROVIDER",null),"CLINICAL_ORDER_CREATED","ClinicalOrder",saved.id,item); return orderView(saved); }
    @Transactional public Map<String,Object> updateOrder(Long id, String status) { if (!List.of("DRAFT","PENDING","IN_PROGRESS","COMPLETED","CANCELLED").contains(status)) throw new IllegalArgumentException("Unsupported order status"); ClinicalOrder o=orderRepository.findById(id).orElseThrow(); if ("COMPLETED".equals(o.status) || "CANCELLED".equals(o.status)) throw new IllegalArgumentException("Completed or cancelled orders cannot transition"); o.status=status; o.activity=(o.activity == null ? "" : o.activity + " ") + "Status changed to " + status + " on " + LocalDateTime.now() + "."; ClinicalOrder saved=orderRepository.save(o); auditService.record(new DemoPrincipal(null,"lena.ortiz","PROVIDER",null),"CLINICAL_ORDER_STATUS_CHANGED","ClinicalOrder",id,status); return orderView(saved); }
    @Transactional public Map<String,Object> createMedication(Long patientId, String name, String strength, String route, String frequency, String instructions, boolean allergyAcknowledged) { Patient p=patientRepository.findById(patientId).orElseThrow(); if (!p.allergies.isEmpty() && !allergyAcknowledged) throw new IllegalArgumentException("Confirm the patient's allergies before adding a medication"); Medication m=new Medication(); m.patient=p; m.name=name; m.strength=strength; m.route=route; m.frequency=frequency; m.status="PENDING"; m.prescribedBy="Dr. Lena Ortiz"; m.startDate=LocalDate.now(); m.refill="0 refills"; m.instructions=instructions; Medication saved=medicationRepository.save(m); auditService.record(new DemoPrincipal(null,"lena.ortiz","PROVIDER",null),"MEDICATION_CREATED","Medication",saved.id,name); return medicationView(saved); }
    @Transactional public Map<String,Object> updateMedicationStatus(Long id, String status) { if (!List.of("ACTIVE","INACTIVE","PENDING").contains(status)) throw new IllegalArgumentException("Unsupported medication status"); Medication m=medicationRepository.findById(id).orElseThrow(); m.status=status; Medication saved=medicationRepository.save(m); auditService.record(new DemoPrincipal(null,"lena.ortiz","PROVIDER",null),"MEDICATION_STATUS_CHANGED","Medication",id,status); return medicationView(saved); }

    private Map<String,Object> patientView(Patient p) { Map<String,Object> v=new HashMap<>(); v.put("id",p.id); v.put("mrn",p.mrn); v.put("firstName",p.firstName); v.put("lastName",p.lastName); v.put("preferredName",p.preferredName); v.put("dateOfBirth",p.dateOfBirth == null ? null : p.dateOfBirth.toString()); v.put("age",p.dateOfBirth == null ? null : Period.between(p.dateOfBirth,LocalDate.of(2026,9,30)).getYears()); v.put("sex",p.sex); v.put("status",p.status); v.put("phone",p.phone); v.put("email",p.email); v.put("city",p.city); v.put("state",p.state); v.put("bloodType",p.bloodType); v.put("primaryProvider",p.primaryProvider); v.put("emergencyContactName",p.emergencyContactName); v.put("emergencyContactPhone",p.emergencyContactPhone); v.put("lastVisit","Sep 28, 2026"); v.put("nextVisit",p.nextVisit == null ? null : p.nextVisit.toString()); v.put("initials",p.initials); v.put("risk",p.risk); v.put("allergies",p.allergies); return v; }
    private Map<String,Object> encounterView(Encounter e) { Map<String,Object> v=new HashMap<>(); v.put("id",e.id); v.put("patientId",e.patient.id); v.put("patientName",e.patient.firstName+" "+e.patient.lastName); v.put("patientMrn",e.patient.mrn); v.put("provider",e.provider); v.put("date",e.date.toString()); v.put("time",e.time.toString()); v.put("type",e.type); v.put("reason",e.reason); v.put("status",e.status); v.put("location",e.location); v.put("notePreview",e.notePreview); v.put("chiefComplaint",e.chiefComplaint); v.put("history",e.history); v.put("examination",e.examination); v.put("assessment",e.assessment); v.put("plan",e.plan); v.put("followUp",e.followUp); v.put("noteStatus",e.noteStatus); return v; }
    private Map<String,Object> orderView(ClinicalOrder o) { Map<String,Object> v=new HashMap<>(); v.put("id",o.id); v.put("patientId",o.patient.id); v.put("patientName",o.patient.firstName+" "+o.patient.lastName); v.put("patientMrn",o.patient.mrn); v.put("category",o.category); v.put("item",o.item); v.put("priority",o.priority); v.put("status",o.status); v.put("orderedBy",o.orderedBy); v.put("orderedAt",o.orderedAt.toString().replace('T',' ')); v.put("due",o.due.toString()); v.put("activity",o.activity); return v; }
    private Map<String,Object> medicationView(Medication m) { Map<String,Object> v=new HashMap<>(); v.put("id",m.id); v.put("patientId",m.patient.id); v.put("patientName",m.patient.firstName+" "+m.patient.lastName); v.put("name",m.name); v.put("strength",m.strength); v.put("route",m.route); v.put("frequency",m.frequency); v.put("status",m.status); v.put("prescribedBy",m.prescribedBy); v.put("startDate",m.startDate.toString()); v.put("endDate",m.endDate == null ? null : m.endDate.toString()); v.put("refill",m.refill); v.put("instructions",m.instructions); return v; }
    private Map<String,Object> appointmentEntityView(Appointment item) { Map<String,Object> v=new HashMap<>(); v.put("id",item.id); v.put("patientId",item.patient.id); v.put("patientName",item.patient.firstName+" "+item.patient.lastName); v.put("type",item.type); v.put("date",item.appointmentDate.toString()); v.put("time",item.startTime.toString()); v.put("endTime",item.endTime.toString()); v.put("provider",item.provider.displayName); v.put("location",item.location); v.put("status",item.status); return v; }
}
