import { CommonModule, DatePipe } from '@angular/common';
import { Component, Input, OnInit, inject, signal } from '@angular/core';
import { RouterLink } from '@angular/router';
import { ApiService } from '../core/api.service';
import { Appointment, DashboardSummary, Role } from '../core/models';
import { AishaAssistantComponent } from './aisha-assistant.component';

@Component({ selector: 'cc-dashboard', standalone: true, imports: [CommonModule, DatePipe, RouterLink, AishaAssistantComponent], templateUrl: './dashboard.component.html', styleUrl: './dashboard.component.css' })
export class DashboardComponent implements OnInit {
  @Input() mode: 'provider' | 'patient' = 'provider';
  readonly api = inject(ApiService);
  readonly summary = signal<DashboardSummary | null>(null);
  readonly loading = signal(true);
  readonly error = signal('');
  readonly Math = Math;

  ngOnInit() {
    const role: Role = this.mode === 'patient' ? 'PATIENT' : 'PROVIDER';
    this.api.dashboard(role).subscribe({
      next: (data) => { this.summary.set(data); this.loading.set(false); },
      error: (error) => { this.error.set(error?.error?.message ?? 'Your workspace could not be loaded. Please try again.'); this.loading.set(false); }
    });
  }

  get isPatient() { return this.mode === 'patient'; }
  get appointment(): Appointment | undefined { return this.summary()?.nextAppointment; }
}
