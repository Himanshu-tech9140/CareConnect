CREATE TABLE patients (
  id BIGSERIAL PRIMARY KEY,
  mrn VARCHAR(32) NOT NULL UNIQUE,
  first_name VARCHAR(120) NOT NULL,
  last_name VARCHAR(120) NOT NULL,
  preferred_name VARCHAR(120),
  date_of_birth DATE,
  sex VARCHAR(32),
  status VARCHAR(32),
  phone VARCHAR(40),
  email VARCHAR(180),
  city VARCHAR(120),
  state VARCHAR(40),
  blood_type VARCHAR(8),
  primary_provider VARCHAR(160),
  emergency_contact_name VARCHAR(160),
  emergency_contact_phone VARCHAR(40),
  last_visit DATE,
  next_visit DATE,
  initials VARCHAR(8),
  risk VARCHAR(16)
);
CREATE TABLE patient_allergies (patient_id BIGINT NOT NULL REFERENCES patients(id), allergy VARCHAR(180));
CREATE TABLE encounters (
  id BIGSERIAL PRIMARY KEY,
  patient_id BIGINT NOT NULL REFERENCES patients(id),
  provider VARCHAR(160), date DATE, time TIME, type VARCHAR(80), reason VARCHAR(180), status VARCHAR(32), location VARCHAR(120), note_preview TEXT, chief_complaint TEXT, history TEXT, examination TEXT, assessment TEXT, plan TEXT, follow_up TEXT, note_status VARCHAR(32)
);
CREATE TABLE clinical_orders (
  id BIGSERIAL PRIMARY KEY,
  patient_id BIGINT NOT NULL REFERENCES patients(id),
  category VARCHAR(32), item VARCHAR(240), priority VARCHAR(32), status VARCHAR(32), ordered_by VARCHAR(160), ordered_at TIMESTAMP, due DATE, activity TEXT
);
CREATE TABLE medications (
  id BIGSERIAL PRIMARY KEY,
  patient_id BIGINT NOT NULL REFERENCES patients(id),
  name VARCHAR(160), strength VARCHAR(80), route VARCHAR(80), frequency VARCHAR(120), status VARCHAR(32), prescribed_by VARCHAR(160), start_date DATE, end_date DATE, refill VARCHAR(80), instructions TEXT
);
CREATE TABLE app_users (
  id BIGSERIAL PRIMARY KEY,
  username VARCHAR(160) UNIQUE, display_name VARCHAR(160), role VARCHAR(32), initials VARCHAR(8), subtitle VARCHAR(180), patient_id BIGINT, password_hash VARCHAR(255), active BOOLEAN NOT NULL DEFAULT TRUE
);
CREATE TABLE provider_profiles (
  id BIGSERIAL PRIMARY KEY,
  user_id BIGINT NOT NULL UNIQUE REFERENCES app_users(id),
  specialization VARCHAR(160), qualification VARCHAR(240), license_number VARCHAR(120), department VARCHAR(160), active BOOLEAN NOT NULL DEFAULT TRUE
);
CREATE TABLE appointments (
  id BIGSERIAL PRIMARY KEY,
  patient_id BIGINT NOT NULL REFERENCES patients(id),
  provider_id BIGINT NOT NULL REFERENCES app_users(id),
  appointment_date DATE NOT NULL,
  start_time TIME NOT NULL,
  end_time TIME NOT NULL,
  type VARCHAR(160), location VARCHAR(160), status VARCHAR(32)
);
CREATE TABLE audit_events (
  id BIGSERIAL PRIMARY KEY,
  actor_username VARCHAR(160), actor_role VARCHAR(32), action VARCHAR(80) NOT NULL,
  entity_type VARCHAR(80), entity_id BIGINT, details TEXT, created_at TIMESTAMP WITH TIME ZONE NOT NULL
);
