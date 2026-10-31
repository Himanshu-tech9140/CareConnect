export type Role = 'ADMIN' | 'PROVIDER' | 'NURSE' | 'PATIENT';
export type OrderStatus = 'DRAFT' | 'PENDING' | 'IN_PROGRESS' | 'COMPLETED' | 'CANCELLED';

export interface User {
  id: number;
  displayName: string;
  role: Role;
  initials: string;
  subtitle: string;
  patientId?: number;
}

export interface AuthResponse { token: string; user: User; }

export interface DashboardSummary {
  todayLabel: string;
  greeting: string;
  headline: string;
  subline: string;
  metrics: { label: string; value: string; detail: string; tone: 'teal' | 'blue' | 'amber' | 'coral' }[];
  tasks: { label: string; count: number; detail: string; tone: 'teal' | 'amber' | 'coral' }[];
  nextAppointment?: Appointment;
  recentPatients: Patient[];
}

export interface Patient {
  id: number;
  mrn: string;
  firstName: string;
  lastName: string;
  preferredName?: string;
  dateOfBirth: string;
  age: number;
  sex: string;
  status: 'ACTIVE' | 'INACTIVE';
  phone: string;
  email: string;
  city: string;
  state: string;
  bloodType: string;
  primaryProvider: string;
  allergies: string[];
  lastVisit: string;
  nextVisit?: string;
  initials: string;
  risk: 'LOW' | 'MEDIUM' | 'HIGH';
  emergencyContactName?: string;
  emergencyContactPhone?: string;
}

export interface Encounter {
  id: number;
  patientId: number;
  patientName: string;
  patientMrn: string;
  provider: string;
  date: string;
  time: string;
  type: string;
  reason: string;
  status: 'OPEN' | 'SIGNED' | 'DRAFT';
  location: string;
  notePreview: string;
  chiefComplaint?: string;
  history?: string;
  examination?: string;
  assessment?: string;
  plan?: string;
  followUp?: string;
  noteStatus?: 'DRAFT' | 'SIGNED' | 'LOCKED';
}

export interface ClinicalOrder {
  id: number;
  patientId: number;
  patientName: string;
  patientMrn: string;
  category: 'LAB' | 'IMAGING' | 'REFERRAL' | 'PROCEDURE';
  item: string;
  priority: 'ROUTINE' | 'URGENT' | 'STAT';
  status: OrderStatus;
  orderedBy: string;
  orderedAt: string;
  due: string;
  activity?: string;
}

export interface Medication {
  id: number;
  patientId: number;
  patientName: string;
  name: string;
  strength: string;
  route: string;
  frequency: string;
  status: 'ACTIVE' | 'INACTIVE' | 'PENDING';
  prescribedBy: string;
  startDate: string;
  endDate?: string;
  refill: string;
  instructions: string;
}

export interface Appointment {
  id: number;
  reference?: string;
  patientId?: number;
  patientName: string;
  type: string;
  date: string;
  time: string;
  endTime?: string;
  providerId?: number;
  provider: string;
  location: string;
  status: 'CONFIRMED' | 'CHECKED_IN' | 'REQUESTED' | 'CANCELLED' | 'COMPLETED';
}

export interface ProviderProfile { id: number; userId: number; displayName: string; username: string; role: Role; specialization: string; qualification: string; licenseNumber: string; department: string; active: boolean; }
export interface StaffMember { id: number; username: string; displayName: string; role: 'PROVIDER' | 'NURSE'; initials: string; subtitle: string; active: boolean; specialization: string; qualification: string; licenseNumber: string; department: string; }
export interface StaffCreateRequest { username: string; password: string; displayName: string; role: 'PROVIDER' | 'NURSE'; specialization: string; qualification: string; licenseNumber: string; department: string; }
export interface AuditEvent { id: number; actorUsername: string; actorRole: Role | 'SYSTEM'; action: string; entityType: string; entityId: number; details: string; createdAt: string; }
export interface PatientCreateRequest { firstName: string; lastName: string; dateOfBirth: string; sex: string; phone: string; email: string; city: string; state: string; emergencyContactName: string; emergencyContactPhone: string; }
export interface AppointmentCreateRequest { patientId: number; providerId: number; date: string; startTime: string; endTime: string; type: string; location: string; }

export interface PatientChart {
  patient: Patient;
  diagnoses: { name: string; code: string; status: string; since: string }[];
  vitals: { label: string; value: string; unit: string; captured: string; tone: 'normal' | 'watch' }[];
  encounters: Encounter[];
  medications: Medication[];
  allergies: { name: string; reaction: string; severity: 'High' | 'Moderate' | 'Low' }[];
  careTeam: { name: string; role: string; initials: string; color: string }[];
}

export interface OrderCreateRequest { patientId: number; category: ClinicalOrder['category']; item: string; priority: ClinicalOrder['priority']; due: string; }
export interface MedicationCreateRequest { patientId: number; name: string; strength: string; route: string; frequency: string; instructions: string; allergyAcknowledged: boolean; }
export interface NoteUpdateRequest { history: string; examination: string; assessment: string; plan: string; followUp: string; noteStatus: 'DRAFT' | 'SIGNED' | 'LOCKED'; }
