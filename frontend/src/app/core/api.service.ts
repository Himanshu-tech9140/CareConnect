import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Appointment, AppointmentCreateRequest, AuditEvent, AuthResponse, ClinicalOrder, DashboardSummary, Encounter, Medication, MedicationCreateRequest, NoteUpdateRequest, OrderCreateRequest, Patient, PatientChart, PatientCreateRequest, ProviderProfile, Role, StaffCreateRequest, StaffMember } from './models';

export interface SignupRequest { username: string; password: string; firstName: string; lastName: string; email: string; phone: string; dateOfBirth: string; }

@Injectable({ providedIn: 'root' })
export class ApiService {
  private readonly http=inject(HttpClient);
  login(username: string, password: string) { return this.http.post<AuthResponse>('/api/auth/login', { username, password }); }
  signup(payload: SignupRequest) { return this.http.post<AuthResponse>('/api/auth/signup', payload); }
  dashboard(role: Role) { return this.http.get<DashboardSummary>('/api/dashboard/' + role.toLowerCase()); }
  patients(search='') { return this.http.get<Patient[]>('/api/patients?search=' + encodeURIComponent(search)); }
  patientChart(id:number) { return this.http.get<PatientChart>('/api/patients/' + id + '/chart'); }
  encounters() { return this.http.get<Encounter[]>('/api/encounters'); }
  encounter(id:number) { return this.http.get<Encounter>('/api/encounters/' + id); }
  orders() { return this.http.get<ClinicalOrder[]>('/api/orders'); }
  medications() { return this.http.get<Medication[]>('/api/medications'); }
  appointments() { return this.http.get<Appointment[]>('/api/appointments'); }
  providers() { return this.http.get<ProviderProfile[]>('/api/providers'); }
  auditEvents() { return this.http.get<AuditEvent[]>('/api/audit'); }
  updateOrderStatus(id:number,status:string) { return this.http.patch<ClinicalOrder>('/api/orders/' + id + '/status', { status }); }
  createOrder(payload:OrderCreateRequest) { return this.http.post<ClinicalOrder>('/api/orders', payload); }
  updateEncounterNote(id:number,payload:NoteUpdateRequest) { return this.http.patch<Encounter>('/api/encounters/' + id + '/note', payload); }
  createMedication(payload:MedicationCreateRequest) { return this.http.post<Medication>('/api/medications', payload); }
  updateMedicationStatus(id:number,status:string) { return this.http.patch<Medication>('/api/medications/' + id + '/status', { status }); }
  createPatient(payload:PatientCreateRequest) { return this.http.post<Patient>('/api/patients', payload); }
  updatePatientStatus(id:number,status:'ACTIVE'|'INACTIVE') { return this.http.patch<Patient>('/api/patients/' + id + '/status', { status }); }
  bookAppointment(payload:AppointmentCreateRequest) { return this.http.post<Appointment>('/api/appointments', payload); }
  updateAppointmentStatus(id:number,status:string) { return this.http.patch<Appointment>('/api/appointments/' + id + '/status', { status }); }
  staff() { return this.http.get<StaffMember[]>('/api/admin/staff'); }
  createStaff(payload:StaffCreateRequest) { return this.http.post<StaffMember>('/api/admin/staff', payload); }
  updateStaffStatus(id:number,status:'ACTIVE'|'INACTIVE') { return this.http.patch<StaffMember>('/api/admin/staff/' + id + '/status', { status }); }
}
