import { CommonModule } from '@angular/common';
import { Component, OnInit, inject, signal } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { Router } from '@angular/router';
import { ApiService } from '../core/api.service';
import { Patient } from '../core/models';

type PatientFilter = 'ALL' | 'ACTIVE' | 'CARE_TEAM';

@Component({ selector: 'cc-patients', standalone: true, imports: [CommonModule, FormsModule], templateUrl: './patients.component.html', styleUrl: './patients.component.css' })
export class PatientsComponent implements OnInit {
  readonly api = inject(ApiService); private readonly router = inject(Router);
  readonly patients = signal<Patient[]>([]); readonly loading = signal(true); search = ''; filter: PatientFilter = 'ALL';
  ngOnInit() { this.load(); }
  load() { this.loading.set(true); this.api.patients(this.search).subscribe((items) => { this.patients.set(items); this.loading.set(false); }); }
  setFilter(filter: PatientFilter) { this.filter = filter; }
  activeCount() { return this.patients().filter((patient) => patient.status === 'ACTIVE').length; }
  careTeamCount() { return this.patients().filter((patient) => Boolean(patient.primaryProvider)).length; }
  filteredPatients() { return this.patients().filter((patient) => this.filter === 'ALL' || (this.filter === 'ACTIVE' && patient.status === 'ACTIVE') || (this.filter === 'CARE_TEAM' && Boolean(patient.primaryProvider))); }
  open(patient: Patient) { void this.router.navigate(['/provider/patients', patient.id]); }
  addPatient() { void this.router.navigate(['/provider/patients/new']); }
}
