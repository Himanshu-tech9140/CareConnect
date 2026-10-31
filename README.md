# CareConnect Patient-Provider EHR

CareConnect is a functional reference implementation of a patient-provider Electronic Health Record workspace. It combines an Angular clinical UI with a Java Spring Boot REST API, Spring Data JPA, and PostgreSQL-ready persistence. Accounts, patients, appointments, and clinical records are persisted through the API; no client-side fallback records are used.

## Stack

- Angular 19 standalone components, strict TypeScript, Router, reactive-ready forms, and responsive CSS.
- Java 21 / Spring Boot 3.4 / Spring Web / Spring Security / Spring Data JPA / Bean Validation.
- PostgreSQL 16 for the canonical runtime, with Flyway schema migration `V1__create_careconnect_schema.sql`.
- H2 `demo` profile for a dependency-light sandbox preview when Docker/PostgreSQL is unavailable.

## Run the frontend in the sandbox

```bash
cd frontend
pnpm install
pnpm start
```

The Angular server listens on port 3000 and proxies `/api` requests to Spring Boot on port 8080. The UI requires the API for authentication and data; if the API is unavailable it shows the corresponding loading/error state rather than synthetic records.

## Run the backend

With a local PostgreSQL instance or the compose service:

```bash
docker compose up -d postgres
cd backend
mvn spring-boot:run
```

To run without Docker/PostgreSQL for a local empty development database:

```bash
cd backend
mvn spring-boot:run -Dspring-boot.run.profiles=demo
```

The backend reads `DB_HOST`, `DB_PORT`, `DB_NAME`, `DB_USER`, and `DB_PASSWORD` for PostgreSQL. The health response is available at `GET /api/health`.

The `demo` profile uses file-backed H2 under `backend/data/` so locally created accounts survive API restarts; the directory is ignored by Git. PostgreSQL is the recommended durable deployment database.

## Account access

Patients can create accounts from the sign-in screen. Signup creates a `Patient` JPA record and a linked `AppUser` with a BCrypt password hash, then returns a JWT session. Existing users sign in with the same persisted username and password. Staff roles are intentionally provisioned by an administrator rather than selected by public signup. Set `CARECONNECT_DEMO_SEED=true` only when explicitly loading development fixtures; it is disabled by default.

## API surface

On a fresh deployment, set `CARECONNECT_BOOTSTRAP_ADMIN_USERNAME` and an 8+ character `CARECONNECT_BOOTSTRAP_ADMIN_PASSWORD` once to create the initial administrator account. It is never overwritten on later starts. Sign in as that account and use **Staff management** to provision Doctor or Nurse accounts and change their active status.

| Resource | Endpoints |
| --- | --- |
| Auth | `POST /api/auth/signup`, `POST /api/auth/login`, `GET /api/auth/me` |
| Health | `GET /api/health` |
| Dashboard | `GET /api/dashboard/{provider\|patient}` |
| Patients | `GET /api/patients?search=`, `GET /api/patients/{id}/chart`, `POST /api/patients`, `PUT /api/patients/{id}`, `PATCH /api/patients/{id}/status` |
| Providers | `GET /api/providers`, `POST /api/providers` |
| Encounters | `GET /api/encounters`, `GET /api/encounters/{id}`, `POST /api/encounters`, `PATCH /api/encounters/{id}/note` |
| Orders | `GET /api/orders`, `POST /api/orders`, `PATCH /api/orders/{id}/status` with validated status/activity transitions |
| Medications | `GET /api/medications`, `POST /api/medications` with allergy acknowledgement, `PATCH /api/medications/{id}/status` |
| Appointments | `GET /api/appointments`, `POST /api/appointments`, `PATCH /api/appointments/{id}/status`; booking rejects overlapping provider time windows |
| Security | BCrypt password hashes, signed JWT bearer sessions, role boundaries, `GET /api/audit` (admin) |
| Staff administration | `GET`, `POST /api/admin/staff`, `PATCH /api/admin/staff/{id}/status` (admin only) |

## Production container

`Dockerfile` builds the Angular browser bundle, copies it into Spring Boot static resources, packages the backend, and serves the SPA plus `/api/*` from one same-origin process. Set `PORT=3000` for the Web Dev container contract and provide a PostgreSQL connection through environment variables. The platform's managed database capability is MySQL-compatible; this project intentionally keeps PostgreSQL as the requested canonical database, so a production deployment needs a PostgreSQL-capable connection rather than silently changing the persistence layer.

Versioned frontend assets should be cached long-term, while authenticated and clinical API responses should remain private and non-cacheable. Patient-facing routes and staff operations are declared in `frontend/src/public/manus-routes.json` and emitted at `/manus-routes.json`.

## Safety note

This project is not a HIPAA, GDPR, SOC 2, or medical-device compliance claim. Do not enter real protected health information. Before any real clinical use, commission security, privacy, clinical safety, accessibility, disaster recovery, audit, interoperability, and regulatory review.
