import { CommonModule } from '@angular/common';
import { Component, Input, OnInit, inject, signal } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { ApiService } from '../core/api.service';
import { AuthService } from '../core/auth.service';
import { Appointment, Encounter, Medication, ProviderProfile } from '../core/models';

@Component({ selector:'cc-portal-page', standalone:true, imports:[CommonModule, FormsModule], templateUrl:'./portal-page.component.html', styleUrl:'./portal-page.component.css' })
export class PortalPageComponent implements OnInit {
  @Input() page:'appointments'|'medications'|'visits'|'settings'='appointments';
  readonly api=inject(ApiService); readonly auth=inject(AuthService);
  readonly appointments=signal<Appointment[]>([]); readonly medications=signal<Medication[]>([]); readonly encounters=signal<Encounter[]>([]); readonly providers=signal<ProviderProfile[]>([]);
  readonly loading=signal(true); readonly bookingOpen=signal(false); readonly booking=signal(false); readonly notice=signal(''); readonly error=signal(''); readonly latestAppointment=signal<Appointment|null>(null);
  bookingForm={ providerId:0, date:this.today(), startTime:'10:00', endTime:'10:30', type:'Consultation', location:'Bayview Clinic' };

  ngOnInit(){
    if(this.page==='appointments') {
      this.api.appointments().subscribe({next:items=>{ this.appointments.set(items); this.loading.set(false); },error:()=>{this.error.set('Appointments could not be loaded. Please refresh.');this.loading.set(false);}});
      this.api.providers().subscribe({next:items=>{ this.providers.set(items.filter(item=>item.active)); this.bookingForm.providerId=items.find(item=>item.active)?.userId || 0; },error:()=>this.error.set('The doctor directory is unavailable right now.')});
    } else if(this.page==='medications') this.api.medications().subscribe({next:items=>{this.medications.set(items);this.loading.set(false);},error:()=>{this.error.set('Medication records could not be loaded.');this.loading.set(false);}});
    else if(this.page==='visits') this.api.encounters().subscribe({next:items=>{this.encounters.set(items);this.loading.set(false);},error:()=>{this.error.set('Visit summaries could not be loaded.');this.loading.set(false);}});
    else this.loading.set(false);
  }

  openBooking(){ this.notice.set(''); this.error.set(''); this.bookingOpen.set(true); }
  submitBooking(){
    const patientId=this.auth.user()?.patientId;
    if(!patientId || !this.bookingForm.providerId || !this.bookingForm.date || !this.bookingForm.startTime || !this.bookingForm.endTime) { this.error.set('Choose a doctor, date, and time before requesting your visit.'); return; }
    this.booking.set(true); this.error.set('');
    this.api.bookAppointment({patientId, ...this.bookingForm}).subscribe({
      next:appointment=>{ this.appointments.set([...this.appointments(),appointment].sort((a,b)=>(a.date+a.time).localeCompare(b.date+b.time))); this.latestAppointment.set(appointment); this.notice.set('Your appointment request is saved. Bring this reference to reception.'); this.bookingOpen.set(false); this.booking.set(false); },
      error:response=>{ this.error.set(response?.error?.message || 'That slot is no longer available. Please choose another time.'); this.booking.set(false); }
    });
  }
  reference(appointment:Appointment){ return appointment.reference || `CC-${appointment.id}`; }
  private today(){ return new Date().toISOString().slice(0,10); }
}
