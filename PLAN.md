# Plan: NFC Wristband PoC - Brainstorming Session

## Context

User ingin membangun sistem NFC Wristband Proof of Concept (PoC) untuk keperluan demo:
- **Android App** (Petugas) - Kotlin, Jetpack Compose, MVVM, Retrofit
- **Backend** - Golang, Gin, GORM, PostgreSQL, JWT
- **Database** - PostgreSQL

Target demo:
1. Wristband dapat diregistrasikan
2. Wristband dapat ditulis data
3. Wristband dapat dibaca
4. Android mengambil data user dari backend
5. User dapat melakukan pembayaran
6. Saldo berubah
7. Tiket dapat divalidasi
8. Semua transaksi tersimpan di database

Asumsi: 1 developer, timeline singkat, fokus demo profesional.

---

## Topik Brainstorming

### 1. High-Level Architecture
- Component Diagram
- Sequence Diagram (Login, Payment, Ticket Validation)
- Deployment Diagram
- Data Flow

### 2. User Flow
- Login → Dashboard → Scan NFC → Profile/Payment → Success
- Complete user journey mapping

### 3. Android Architecture
- Folder Structure
- MVVM Pattern
- Repository Pattern
- Navigation
- State Management
- Dependency Injection

### 4. Backend Architecture
- Golang project structure
- Layer separation rationale

### 5. Database Design
- ERD
- Tables: users, wristbands, wallets, wallet_transactions, tickets, transactions, audit_logs
- Relationships

### 6. NFC Design
- UID vs UUID vs Random Token
- Recommendation for demo

### 7. REST API
- Endpoint specification
- Request/Response examples

### 8. Payment Flow
- Step-by-step flow
- Backend validation
- Balance deduction

### 9. Ticket Validation
- Validation states
- Flow design

### 10. UI Demo
- Material Design 3 recommendations
- Screen list

### 11. Error Handling
- Error scenarios
- User feedback

### 12. MVP Features
- Wajib vs Nice to Have

### 13. Roadmap
- Implementation phases
- Timeline estimation

### 14. Deliverables
- All artifacts to be produced
- Risks and mitigation
- Priority

---

## Output

Plan file akan berisi:
- 14 topik lengkap dengan diagram (Mermaid format)
- ERD dan Database Schema
- REST API Specification
- Folder Structure untuk Android dan Golang
- User Flow dan Screen Flow
- Roadmap Implementasi
- Risiko dan Mitigasi
- Prioritas Pengerjaan
