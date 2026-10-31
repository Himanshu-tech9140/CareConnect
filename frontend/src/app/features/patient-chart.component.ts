import { CommonModule } from '@angular/common';
import { Component, OnInit, inject, signal } from '@angular/core';
import { ActivatedRoute, RouterLink } from '@angular/router';
import { ApiService } from '../core/api.service';
import { PatientChart } from '../core/models';

@Component({ selector: 'cc-patient-chart', standalone: true, imports: [CommonModule, RouterLink], templateUrl: './patient-chart.component.html', styleUrl: './patient-chart.component.css' })
export class PatientChartComponent implements OnInit {
  private readonly route = inject(ActivatedRoute); readonly api = inject(ApiService);
  readonly chart = signal<PatientChart | null>(null); readonly loading = signal(true); readonly activeTab = signal('overview');
  ngOnInit() { this.route.paramMap.subscribe((params) => { const id = Number(params.get('id') ?? 1); this.loading.set(true); this.api.patientChart(id).subscribe((chart) => { this.chart.set(chart); this.loading.set(false); }); }); }
}
