<div align="center">

<img src="./careconnect-logo.png" alt="CareConnect logo" width="120" />

# CareConnect

### Patient and Provider EHR Workspace

<p>Connected care. Clear records. Better collaboration.</p>

[![Angular](https://img.shields.io/badge/Angular-19-DD0031?logo=angular&logoColor=white)](https://angular.dev/)
[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.4-6DB33F?logo=springboot&logoColor=white)](https://spring.io/projects/spring-boot)
[![Java Backend](https://img.shields.io/badge/Backend-Java%2021%20%2B%20Spring%20Boot-ED8B00?logo=openjdk&logoColor=white)](https://spring.io/projects/spring-boot)
[![PostgreSQL](https://img.shields.io/badge/PostgreSQL-16-4169E1?logo=postgresql&logoColor=white)](https://www.postgresql.org/)
[![Docker](https://img.shields.io/badge/Docker-ready-2496ED?logo=docker&logoColor=white)](https://www.docker.com/)

<br />

[**Open the repository**](https://github.com/Himanshu-tech9140/CareConnect) · [**Run locally**](#quick-start-local-demo) · [**View API overview**](#api-overview)

</div>

---

## 🌟 Overview

CareConnect is a full-stack electronic health record (EHR) reference application for connecting patients, providers, and care teams in one secure workspace. It provides role-aware clinical workflows for authentication, patient records, appointments, encounters, orders, medications, staff administration, and audit visibility.

> [!IMPORTANT]
> CareConnect is a demonstration and reference implementation. It is not certified for clinical use and does not make HIPAA, GDPR, SOC 2, medical-device, or other regulatory compliance claims. Never enter real protected health information (PHI).

## ✨ Highlights

| | Capability | Description |
|---|---|---|
| 🔐 | **Secure access** | JWT sessions, BCrypt password hashing, and role-based authorization |
| 🩺 | **Clinical workspace** | Patient profiles, charts, encounters, medications, and orders |
| 📅 | **Care coordination** | Appointment scheduling with provider availability validation |
| 👥 | **Staff administration** | Admin-controlled staff provisioning and account status management |
| 🧾 | **Audit visibility** | Audit events for security-sensitive operations |
| 🚀 | **Deployment ready** | PostgreSQL persistence, Flyway migrations, and Docker support |

## 🧰 Technology Stack

> [!NOTE]
> **Backend clarification:** CareConnect uses **Java 21 with Spring Boot 3.4** for its backend REST API. It does **not** use Node.js for the backend. Node.js and pnpm are only used to build and run the Angular frontend.

### 🎨 Frontend

- Angular 19
- TypeScript with strict compiler settings
- Angular Router and standalone components
- Reactive forms and RxJS
- Angular development server with `/api` proxying

### ⚙️ Backend

- **Java 21 + Spring Boot 3.4**
- Spring Web, Spring Security, Spring Data JPA, and Bean Validation
- JWT bearer authentication
- BCrypt password hashing
- Flyway database migrations

### 🗄️ Data and Delivery

- PostgreSQL 16 for the canonical runtime
- H2 for the local `demo` profile
- Docker Compose for PostgreSQL development
- Multi-stage Dockerfile for combined frontend and backend packaging

## 🏗️ Architecture

```text
Browser
  |
  v
Angular 19 frontend (:3000)
  |  /api proxy in development
  v
Java 21 + Spring Boot REST API (:8080)
  |
  v
PostgreSQL 16  |  H2 demo database
```

The frontend never uses synthetic client-side records. Authentication and clinical data are provided by the backend API and persisted through JPA.

## ✅ Requirements

For local development, install:

- Node.js 22.13 or newer
- pnpm 11
- Java 21
- Maven 3.9 or newer
- Docker Desktop, optional but recommended for PostgreSQL

Verify the installed tools:

```bash
node --version
pnpm --version
java --version
mvn --version
```

## 🚀 Quick Start: Local Demo

The demo profile uses a file-backed H2 database and does not require Docker or PostgreSQL.

### 1. Start the backend

```bash
cd backend
mvn spring-boot:run -Dspring-boot.run.profiles=demo
```

The API starts at `http://localhost:8080`.

### 2. Start the frontend

In a second terminal:

```bash
cd frontend
pnpm install
pnpm start
```

Open [http://localhost:3000](http://localhost:3000) in a browser. The frontend proxies `/api` requests to the backend at port `8080`.

Check backend health at [http://localhost:8080/api/health](http://localhost:8080/api/health).

## 🐘 PostgreSQL Development

Start PostgreSQL with Docker Compose:

```bash
docker compose up -d postgres
```

Then start the backend using the default PostgreSQL configuration:

```bash
cd backend
mvn spring-boot:run
```

The backend reads these environment variables for PostgreSQL:

| Variable | Default |
| --- | --- |
| `DB_HOST` | `localhost` |
| `DB_PORT` | `5432` |
| `DB_NAME` | `careconnect` |
| `DB_USER` | `careconnect` |
| `DB_PASSWORD` | `careconnect` |

For a first deployment, set the bootstrap administrator credentials once:

```bash
set CARECONNECT_BOOTSTRAP_ADMIN_USERNAME=admin
set CARECONNECT_BOOTSTRAP_ADMIN_PASSWORD=replace-with-a-strong-password
```

On PowerShell, use `$env:NAME = "value"` instead. The password must contain at least eight characters. The bootstrap account is not overwritten on later starts.

## 🐳 Docker Deployment

The multi-stage Dockerfile builds the Angular application, copies the browser bundle into Spring Boot static resources, packages the backend, and serves the combined application from one process.

```bash
docker compose up --build
```

The container exposes port `3000`. Configure a PostgreSQL-capable database and production secrets through environment variables before deploying to a shared or public environment.

## 🔌 API Overview

All API routes are served below `/api`.

| Area | Endpoints |
| --- | --- |
| Authentication | `POST /auth/signup`, `POST /auth/login`, `GET /auth/me` |
| Health | `GET /health` |
| Dashboards | `GET /dashboard/{provider\|patient}` |
| Patients | `GET /patients`, `GET /patients/{id}/chart`, `POST /patients`, `PUT /patients/{id}`, `PATCH /patients/{id}/status` |
| Providers | `GET /providers`, `POST /providers` |
| Encounters | `GET /encounters`, `GET /encounters/{id}`, `POST /encounters`, `PATCH /encounters/{id}/note` |
| Orders | `GET /orders`, `POST /orders`, `PATCH /orders/{id}/status` |
| Medications | `GET /medications`, `POST /medications`, `PATCH /medications/{id}/status` |
| Appointments | `GET /appointments`, `POST /appointments`, `PATCH /appointments/{id}/status` |
| Staff administration | `GET /admin/staff`, `POST /admin/staff`, `PATCH /admin/staff/{id}/status` |
| Audit | `GET /audit` |

Authorization is enforced by the backend. Staff roles are provisioned by an administrator rather than selected through public patient signup.

## 🧪 Testing and Build Commands

### Frontend

```bash
cd frontend
pnpm typecheck
pnpm build
pnpm test
```

### Backend

```bash
cd backend
mvn test
```

## 📁 Project Structure

```text
.
├── backend/
│   ├── src/main/java/        Spring Boot controllers, services, models, and security
│   ├── src/main/resources/   Configuration and Flyway migrations
│   └── src/test/             Backend tests
├── frontend/
│   ├── src/app/core/         API, authentication, models, and interceptors
│   ├── src/app/features/     Screens and clinical workflows
│   └── src/assets/           Frontend assets
├── docker-compose.yml         Local PostgreSQL service
├── Dockerfile                 Production container build
└── README.md                  Project documentation
```

## 🛡️ Security and Privacy

- Keep secrets out of source control and use environment variables or a secret manager.
- Use HTTPS and secure cookie/token policies in production.
- Replace all development database credentials before deployment.
- Review authorization, audit retention, backup, access control, and incident response requirements before handling real patient data.
- Commission independent security, privacy, accessibility, clinical-safety, interoperability, and regulatory reviews before any real-world use.

## 📄 License

No license has been declared for this repository. Add an appropriate license before distributing or reusing the project.
