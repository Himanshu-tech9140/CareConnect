import { CommonModule } from '@angular/common';
import { Component, Input, OnInit, inject, signal } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { ActivatedRoute, RouterLink } from '@angular/router';
import { ApiService } from '../core/api.service';
import { ClinicalOrder, Encounter, Medication, NoteUpdateRequest } from '../core/models';

@Component({ selector: 'cc-workspace', standalone: true, imports: [CommonModule, FormsModule, RouterLink], templateUrl: './workspace.component.html', styleUrl: './workspace.component.css' })
export class WorkspaceComponent implements OnInit {
  @Input() page: 'encounters' | 'orders' | 'medications' = 'encounters'; @Input() detail = false;
  readonly api = inject(ApiService); private readonly route = inject(ActivatedRoute);
  readonly encounters = signal<Encounter[]>([]); readonly orders = signal<ClinicalOrder[]>([]); readonly medications = signal<Medication[]>([]); readonly loading = signal(true); readonly selectedOrder = signal<number | null>(null); readonly detailEncounter = signal<Encounter | null>(null); readonly patientContextId = signal<number | null>(null); readonly showOrderForm = signal(false); readonly showMedicationForm = signal(false);
  noteDraft: NoteUpdateRequest = { history: '', examination: '', assessment: '', plan: '', followUp: '', noteStatus: 'DRAFT' };
  newOrder = { patientId: 2, category: 'LAB' as ClinicalOrder['category'], item: '', priority: 'ROUTINE' as ClinicalOrder['priority'], due: '2026-10-02' };
  newMedication = { patientId: 1, name: '', strength: '', route: 'Oral', frequency: 'Once daily', instructions: '', allergyAcknowledged: false };

  ngOnInit() { this.route.data.subscribe((data) => { this.page = (data['page'] as 'encounters' | 'orders' | 'medications') ?? 'encounters'; this.detail = Boolean(data['detail']); this.patientContextId.set(Number(this.route.snapshot.queryParamMap.get('patientId')) || null); this.load(); }); }
  load() { this.loading.set(true); const finish = () => this.loading.set(false); if (this.detail) { const id = Number(this.route.snapshot.paramMap.get('id')); this.api.encounter(id).subscribe((item) => { this.detailEncounter.set(item); this.noteDraft = { history: item.history ?? '', examination: item.examination ?? '', assessment: item.assessment ?? '', plan: item.plan ?? '', followUp: item.followUp ?? '', noteStatus: item.noteStatus ?? 'DRAFT' }; finish(); }); } else if (this.page === 'orders') this.api.orders().subscribe((items) => { this.orders.set(items); finish(); }); else if (this.page === 'medications') this.api.medications().subscribe((items) => { this.medications.set(items); finish(); }); else this.api.encounters().subscribe((items) => { this.encounters.set(items); finish(); }); }
  toggleOrder(order: ClinicalOrder) { this.selectedOrder.set(this.selectedOrder() === order.id ? null : order.id); }
  initials(name: string) { return name.split(' ').map((part) => part[0]).join(''); }
  saveNote(status: NoteUpdateRequest['noteStatus']) { if (!this.detailEncounter()) return; this.noteDraft.noteStatus = status; this.api.updateEncounterNote(this.detailEncounter()!.id, this.noteDraft).subscribe((item) => this.detailEncounter.set(item)); }
  createOrder() { this.api.createOrder(this.newOrder).subscribe((item) => { this.orders.update((items) => [item, ...items]); this.showOrderForm.set(false); this.newOrder.item = ''; }); }
  changeOrderStatus(order: ClinicalOrder) { const next: Record<string, string> = { DRAFT: 'PENDING', PENDING: 'IN_PROGRESS', IN_PROGRESS: 'COMPLETED' }; const status = next[order.status]; if (!status) return; this.api.updateOrderStatus(order.id, status).subscribe((item) => this.orders.update((items) => items.map((current) => current.id === item.id ? item : current))); }
  createMedication() { this.api.createMedication(this.newMedication).subscribe((item) => { this.medications.update((items) => [item, ...items]); this.showMedicationForm.set(false); this.newMedication.name = ''; this.newMedication.strength = ''; this.newMedication.instructions = ''; }); }
  toggleMedication(medication: Medication) { const status = medication.status === 'ACTIVE' ? 'INACTIVE' : 'ACTIVE'; this.api.updateMedicationStatus(medication.id, status).subscribe((item) => this.medications.update((items) => items.map((current) => current.id === item.id ? item : current))); }
}
