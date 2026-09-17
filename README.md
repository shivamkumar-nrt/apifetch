# APIForge Studio — Next-Gen Enterprise API Platform

Welcome to **APIForge Studio**, a consolidated enterprise API development, testing, monitoring, and AI-assisted performance engineering platform. 

This repository houses the entire Phase 1 software architecture, organized into three decoupled repositories/modules:
1. [**`desktop/`**](file:///e:/Api%20Tester/desktop): Offline-first JavaFX client application.
2. [**`backend/`**](file:///e:/Api%20Tester/backend): Core Spring Boot microservice backend.
3. [**`admin-panel/`**](file:///e:/Api%20Tester/admin-panel): React Admin Operator Portal.

---

## 🛠 Technology Stack
- **Desktop Application**: JavaFX 17 (Java SE 17 Compliance)
- **Local Client Cache**: SQLite JDBC (Offline mode persistence)
- **Enterprise Server Engine**: Spring Boot 3.1.2, JPA Hibernate
- **Database Engine**: PostgreSQL support (default: H2 database for local debug)
- **Admin Operator Panel**: React.js (Vite compiler), CSS layout

---

## 🌟 Core Phase 1 Completed Capabilities

### 1. Multi-Protocol API Tester
- **REST & SSE Connections**: Standard HTTP method operations (GET, POST, etc.) with latency profiling.
- **WebSocket frame streams**: Client connects directly to server endpoints and publishes message frames live.

### 2. Postman-like Collections & Workspace
- **Nested Collections & Folders**: SQLite-backed folders tree structure (`TreeView`) with full CRUD support.
- **Context Actions**: Right-click on sidebar nodes to add collections, directories, or endpoints.
- **Reloading Configs**: Double-clicking saved endpoints restores URL headers, payloads, and tokens.

### 3. Dynamic Visualizer & Pretty Print
- **Spreadsheet Grid Visualizer**: Auto-parses JSON arrays into searchable tables.
- **Image rendering**: Auto-detects image resource links and displays thumbnail avatars inside cells.
- **Dynamic Themes**: Interactive Dark/Light mode theme switcher.

### 4. SaaS RBAC Gateway (Access Controls)
- **Master Admin Seed**: Seeds `masteradmin` user with absolute system permissions.
- **Access Restrictions**: Block standard `DEVELOPER` credentials from logging into the React admin portal.
- **Permissions Matrix**: Visually verify roles permissions inside React dashboard.

---

## 🚀 Execution & Deployment Instructions

### 1. Backend Server
```bash
cd backend
mvn spring-boot:run
```
- Rest Endpoints: `http://localhost:8080`
- Active seed user profiles:
  - **Master Admin**: `masteradmin` (Password: `mastersecret`)
  - **Admin**: `admin` (Password: `admin123`)
  - **Developer**: `shivam` (Password: `password`)

### 2. React Admin Dashboard
```bash
cd admin-panel
npm install
npm run dev
```
- Dashboard Portal: `http://localhost:5173`

### 3. JavaFX Desktop Client
```bash
cd desktop
mvn javafx:run
```
- Persistence location: Local cache is stored in `~/.apiforge/apiforge.db`.
