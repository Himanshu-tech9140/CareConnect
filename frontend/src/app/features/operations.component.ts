import { CommonModule } from '@angular/common';
import { Component, OnInit, inject, signal } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { ActivatedRoute } from '@angular/router';
import { ApiService } from '../core/api.service';
import { AuthService } from '../core/auth.service';
import { Appointment, AuditEvent, Patient, PatientCreateRequest, ProviderProfile, StaffCreateRequest, StaffMember } from '../core/models';

type OperationsTab = 'SCHEDULE' | 'PATIENTS' | 'PROVIDERS' | 'STAFF' | 'AUDIT';

@Component({ selector:'cc-operations', standalone:true, imports:[CommonModule,FormsModule], templateUrl:'./operations.component.html', styleUrl:'./operations.component.css' })
export class OperationsComponent implements OnInit {
  private readonly api=inject(ApiService); private readonly route=inject(ActivatedRoute); readonly auth=inject(AuthService);
  readonly tab=signal<OperationsTab>('SCHEDULE'); readonly providers=signal<ProviderProfile[]>([]); readonly appointments=signal<Appointment[]>([]); readonly audits=signal<AuditEvent[]>([]); readonly patients=signal<Patient[]>([]); readonly staff=signal<StaffMember[]>([]); readonly notice=signal('');
  patient: PatientCreateRequest={firstName:'',lastName:'',dateOfBirth:'',sex:'',phone:'',email:'',city:'',state:'CA',emergencyContactName:'',emergencyContactPhone:''};
  booking={patientId:1,providerId:1,date:'',startTime:'10:00',endTime:'10:30',type:'Follow-up',location:'Clinic'};
  staffForm: StaffCreateRequest={username:'',password:'',displayName:'',role:'PROVIDER',specialization:'',qualification:'',licenseNumber:'',department:''};
  get isAdmin(){ return this.auth.user()?.role==='ADMIN'; }
  get staffPasswordTooShort(){ return this.staffForm.password.length > 0 && this.staffForm.password.length < 8; }
  get staffReady(){ return Boolean(this.staffForm.displayName.trim() && this.staffForm.username.trim() && this.staffForm.password.length >= 8 && this.staffForm.department.trim()); }
  ngOnInit(){ const requested=this.route.snapshot.data['tab'] as OperationsTab | undefined; if(requested) this.tab.set(requested); this.load(); }
  load(){ this.api.providers().subscribe(v=>{this.providers.set(v); if(v.length && !v.some(item=>item.userId===this.booking.providerId)) this.booking.providerId=v[0].userId;}); this.api.appointments().subscribe(v=>this.appointments.set(v)); const setPatients=(v:Patient[])=>{this.patients.set(v); if(v.length && !v.some(item=>item.id===this.booking.patientId)) this.booking.patientId=v[0].id;}; if(this.isAdmin){ this.api.auditEvents().subscribe(v=>this.audits.set(v)); this.api.patients().subscribe(setPatients); this.api.staff().subscribe(v=>this.staff.set(v)); } else { this.api.patients().subscribe(setPatients); } }
  registerPatient(){ this.api.createPatient(this.patient).subscribe({next:(patient)=>{this.notice.set(`Registered ${patient.firstName} ${patient.lastName} · ${patient.mrn}`); this.patient={firstName:'',lastName:'',dateOfBirth:'',sex:'',phone:'',email:'',city:'',state:'CA',emergencyContactName:'',emergencyContactPhone:''}; this.load();},error:e=>this.notice.set(e?.error?.message ?? 'Patient registration failed.')}); }
  book(){ if(!this.booking.date || !this.providers().length || !this.patients().length) { this.notice.set('Select a patient, an active Doctor, and an appointment date before booking.'); return; } this.api.bookAppointment(this.booking).subscribe({next:()=>{this.notice.set('Appointment booked and recorded in the audit log.'); this.load();},error:e=>this.notice.set(e?.error?.message ?? 'Appointment could not be booked.')}); }
  createStaff(){ if(!this.staffReady) { this.notice.set('Enter a name, username, department, and a password with at least 8 characters.'); return; } const payload={...this.staffForm,username:this.staffForm.username.trim(),displayName:this.staffForm.displayName.trim(),department:this.staffForm.department.trim()}; this.api.createStaff(payload).subscribe({next:staff=>{this.notice.set(`${staff.role==='PROVIDER'?'Doctor':'Nurse'} account ${staff.username} created. They can now sign in.`); this.staffForm={username:'',password:'',displayName:'',role:'PROVIDER',specialization:'',qualification:'',licenseNumber:'',department:''}; this.load();},error:e=>this.notice.set(e?.error?.message ?? 'Staff account could not be created.')}); }
  setStaffStatus(item:StaffMember){ const status=item.active?'INACTIVE':'ACTIVE'; this.api.updateStaffStatus(item.id,status).subscribe({next:updated=>{this.staff.update(items=>items.map(current=>current.id===updated.id?updated:current)); this.notice.set(`${updated.displayName} is now ${status.toLowerCase()}.`);},error:e=>this.notice.set(e?.error?.message ?? 'Staff status update failed.')}); }
  setAppointmentStatus(item:Appointment,status:string){ this.api.updateAppointmentStatus(item.id,status).subscribe({next:updated=>{this.appointments.update(items=>items.map(current=>current.id===updated.id?updated:current)); this.notice.set(`Appointment ${status.toLowerCase()}.`);},error:e=>this.notice.set(e?.error?.message ?? 'Appointment update failed.')}); }
  setTab(tab:OperationsTab){this.tab.set(tab);}
}
