import { Routes } from '@angular/router';
import { LoginComponent } from './features/login.component';
import { DashboardComponent } from './features/dashboard.component';
import { PatientsComponent } from './features/patients.component';
import { PatientChartComponent } from './features/patient-chart.component';
import { WorkspaceComponent } from './features/workspace.component';
import { PortalPageComponent } from './features/portal-page.component';
import { OperationsComponent } from './features/operations.component';
import { adminGuard, authGuard, patientGuard, providerGuard } from './core/auth.service';

export const routes: Routes = [
  { path: '', pathMatch: 'full', redirectTo: 'provider/dashboard' },
  { path: 'login', component: LoginComponent },
  { path: 'provider/dashboard', component: DashboardComponent, data: { mode: 'provider' }, canActivate: [providerGuard] },
  { path: 'provider/patients', component: PatientsComponent, canActivate: [providerGuard] },
  { path: 'provider/patients/new', component: OperationsComponent, data: { tab: 'PATIENTS' }, canActivate: [providerGuard] },
  { path: 'provider/patients/:id', component: PatientChartComponent, canActivate: [providerGuard] },
  { path: 'provider/encounters', component: WorkspaceComponent, data: { page: 'encounters' }, canActivate: [providerGuard] },
  { path: 'provider/encounters/:id', component: WorkspaceComponent, data: { page: 'encounters', detail: true }, canActivate: [providerGuard] },
  { path: 'provider/orders', component: WorkspaceComponent, data: { page: 'orders' }, canActivate: [providerGuard] },
  { path: 'provider/medications', component: WorkspaceComponent, data: { page: 'medications' }, canActivate: [providerGuard] },
  { path: 'provider/operations', component: OperationsComponent, canActivate: [providerGuard] },
  { path: 'admin/staff', component: OperationsComponent, data: { tab: 'STAFF' }, canActivate: [adminGuard] },
  { path: 'portal', component: DashboardComponent, data: { mode: 'patient' }, canActivate: [patientGuard] },
  { path: 'portal/appointments', component: PortalPageComponent, data: { page: 'appointments' }, canActivate: [patientGuard] },
  { path: 'portal/medications', component: PortalPageComponent, data: { page: 'medications' }, canActivate: [patientGuard] },
  { path: 'portal/visits', component: PortalPageComponent, data: { page: 'visits' }, canActivate: [patientGuard] },
  { path: 'settings', component: PortalPageComponent, data: { page: 'settings' }, canActivate: [authGuard] },
  { path: '**', redirectTo: 'login' }
];
