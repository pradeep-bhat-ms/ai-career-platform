# 🚀 CareerNexus — AI Career Intelligence Platform

> A full-stack platform that helps developers analyze resumes, understand job
> descriptions, find skill gaps, practice interviews, audit their GitHub
> profile, and learn from their own documents using RAG.

<p align="center">
  <img src="https://img.shields.io/badge/Java-21-orange?style=for-the-badge&logo=openjdk" alt="Java 21">
  <img src="https://img.shields.io/badge/Spring%20Boot-4.1.1-6DB33F?style=for-the-badge&logo=springboot" alt="Spring Boot">
  <img src="https://img.shields.io/badge/Spring%20AI-2.0.0-6DB33F?style=for-the-badge&logo=spring" alt="Spring AI">
  <img src="https://img.shields.io/badge/React-Vite-61DAFB?style=for-the-badge&logo=react" alt="React">
  <img src="https://img.shields.io/badge/PostgreSQL-PGVector-4169E1?style=for-the-badge&logo=postgresql" alt="PostgreSQL + PGVector">
  <img src="https://img.shields.io/badge/Docker-Compose-2496ED?style=for-the-badge&logo=docker" alt="Docker">
  <img src="https://img.shields.io/badge/License-MIT-yellow?style=for-the-badge" alt="MIT License">
</p>

<p align="center">
  <strong>Resume Intelligence • Job Matching • Skill Gap Analysis • Interview Practice • GitHub Analyzer • RAG</strong>
</p>

---

## 🌐 Live Application

| | URL |
|---|---|
| **Frontend** | https://careernexus-frontend-ydgl.onrender.com |
| **Backend API** | https://ai-career-platform-nps4.onrender.com |

> **Heads-up:** the app runs on free hosting tiers. After ~15 minutes without
> traffic the backend goes to sleep, so the **first request can take about a
> minute** to wake up. The backend root is protected by Spring Security, so
> opening it directly may return `403 Forbidden` — use the frontend.

<!--
SCREENSHOTS — add images to docs/screenshots/ and uncomment:

## 📸 Screenshots
| Resume Studio | RAG Assistant |
|---|---|
| ![Resume Studio](docs/screenshots/resume-studio.png) | ![RAG](docs/screenshots/rag.png) |
-->

---

## 📌 About

**CareerNexus** brings resume analysis, job-description analysis, skill-gap
identification, interview practice, coding practice, GitHub profile analysis,
and document-grounded AI answers into one application.

It is built with **Java 21 + Spring Boot 4**, **React + Vite**, **PostgreSQL +
PGVector**, and **Spring AI** — with **Groq** (OpenAI-compatible API) for text
generation and **Google Gemini** for embeddings.

---

# ✨ Features

## 📄 Resume Studio
- Upload a PDF resume; text and structured data are extracted with AI
- **Target-role analysis** — compares your skills with required and recommended skills for a role
- **AI Resume Compatibility Estimate** with a transparent breakdown: Skills, Keywords, Experience, Projects, Education, Summary, Completeness
- **AI rewrite proposals** with *original → suggested → reason*; you choose which to apply, then the resume is re-scored
- **Career Skill Agent** — prioritized skills to learn for the selected role
- Saved resumes with per-user ownership checks

> The score is an in-house estimate. It is **not** an official score from any
> real ATS, and different ATS products score resumes differently.

## 💼 JD Analyzer
Extracts required skills, preferred skills, technologies, and role information from a pasted job description.

## 🎯 Match & Compare
Compares a resume with a job description and shows matching skills, missing skills, and improvement suggestions.

## 🧭 Skill Roadmap
```text
Resume skills + interview performance
              ↓
         Skill gap status
              ↓
 AI-generated week-by-week learning plan
```

## 🎙️ AI Interview Agent
- AI-generated interview questions by difficulty
- Answer evaluation with a 0–10 score, strengths, and weaknesses
- Sessions, questions, answers, and evaluations are stored per user

## 💻 Code Arena
- AI-generated coding challenges and code submissions
- AI-assisted evaluation of submissions

> **Note:** evaluation is performed by an LLM acting as a judge. Code is **not**
> executed in a sandbox, and runtime/memory values are not real measurements.
> Sandboxed execution is on the roadmap.

## 🐙 GitHub Analyzer
Paste a GitHub profile URL (validated to `github.com`) to see:
- Profile overview (bio, followers, following, repositories)
- Repository list with language, stars, and license
- Repository statistics (total / original / forked / archived)
- Language distribution by each repository's **primary language** (repository count — not code volume, and not a skill measure)

Uses GitHub's public REST API (unauthenticated requests are rate-limited).
README, deployment, CI, and license-category analysis and an AI optimizer are planned.

## 🧠 RAG Assistant & Document Vault
Upload your own notes or guides and ask questions answered from **your own content**, with sources shown.

---

# 🧠 RAG Pipeline

```text
PDF / text document
        ↓
Text extraction
        ↓
Text splitting → chunks
        ↓
Gemini embeddings (gemini-embedding-001)
        ↓
PostgreSQL + PGVector
        ↓
User question → embedding → similarity search
        ↓
Top-K relevant chunks (filtered to the current user)
        ↓
Context construction
        ↓
LLM (Groq, via Spring AI)
        ↓
Grounded answer + sources
```

| Setting | Value |
|---|---|
| Embedding model | `gemini-embedding-001` |
| Dimensions | 1536 |
| Index | HNSW |
| Distance | Cosine |
| Vector store | PostgreSQL + PGVector |
| Top-K | 4 |
| Retrieval filter | `userEmail` (+ optional `category`) |

Retrieval is filtered by the authenticated user, so one user's documents are never used to answer another user's questions.

---

# 🔐 Security

```text
Register / Login
      ↓
Spring Security (BCrypt)
      ↓
JWT generated
      ↓
HTTP-only cookie
      ↓
JWT authentication filter
      ↓
SecurityContext
      ↓
Protected API
```

- JWT in an **HTTP-only cookie** (not readable by frontend JavaScript); `Authorization: Bearer` also accepted for API clients
- `SameSite=None; Secure` cookies in production, `Lax` for local development
- BCrypt password hashing and stateless sessions
- **Rate limiting** on login, forgot-password, and reset-password
- Generic responses on password reset to prevent **email enumeration**
- Ownership checks on resumes and documents
- CORS allow-list, secrets supplied via environment variables

---

# 🏗️ Architecture

```text
                     ┌──────────────────────┐
                     │   React + Vite UI    │
                     └──────────┬───────────┘
                                │ REST (cookies)
                                ▼
                     ┌──────────────────────┐
                     │  Spring Boot 4 API   │
                     ├──────────────────────┤
                     │ Spring Security+JWT  │
                     │ Controllers          │
                     │ Services             │
                     │ AI + RAG services    │
                     └───┬────────┬─────┬───┘
                         │        │     │
        ┌────────────────┘        │     └──────────────────┐
        ▼                         ▼                        ▼
┌──────────────────┐   ┌────────────────────┐   ┌───────────────────┐
│ PostgreSQL       │   │ Groq               │   │ Gemini            │
│ + PGVector       │   │ chat generation    │   │ embeddings        │
│ data + vectors   │   │ (OpenAI-compatible)│   │ (1536 dims)       │
└──────────────────┘   └────────────────────┘   └───────────────────┘
                                │
                     GitHub REST API (GitHub Analyzer)
                     Gmail SMTP (password-reset OTP)
```

---

# 🛠️ Technology Stack

### Backend
| Technology | Purpose |
|---|---|
| Java 21, Spring Boot 4.1.1 | Application framework |
| Spring Security + JWT (jjwt) | Authentication & authorization |
| Spring Data JPA / Hibernate | Persistence |
| PostgreSQL + PGVector | Relational data + vector search |
| Spring AI 2.0 | LLM, embeddings, vector store |
| Spring WebFlux `WebClient` | Calls to the GitHub REST API |
| Apache PDFBox / Spring AI PDF reader | Resume & document text extraction |
| JavaMail | Password-reset OTP emails |
| Maven | Build |

### Frontend
| Technology | Purpose |
|---|---|
| React | UI |
| Vite | Build tool |
| React Router | Client-side routing |
| Axios | API calls (with credentials) |

### AI
| Provider | Used for |
|---|---|
| Groq — `openai/gpt-oss-120b` (OpenAI-compatible API) | All text generation |
| Google Gemini — `gemini-embedding-001` | Embeddings for RAG |

### Testing & DevOps
| Technology | Purpose |
|---|---|
| JUnit 5, Mockito, AssertJ | Unit tests |
| MockMvc | Controller / security tests |
| Testcontainers | Real PostgreSQL repository tests |
| Docker, Docker Compose | Containers & local stack |
| Render | Frontend (static site) + backend (Docker web service) |
| Neon | Managed PostgreSQL + PGVector |

---

# 🧪 Testing

30+ automated tests across several layers:

```text
AuthService (Mockito) → AuthController (MockMvc) → JWT / Spring Security
   → Authorization (ownership) → Exception handling
   → Controller + service integration → PostgreSQL repository (Testcontainers)
```

```bash
cd backend
./mvnw test        # Windows PowerShell: .\mvnw test
```

> Repository tests use **Testcontainers**, so Docker must be running.

---

# 🐳 Run with Docker

```text
docker compose
   ├── PostgreSQL + PGVector   → localhost:5433
   ├── Spring Boot backend     → http://localhost:8080
   └── React + Nginx frontend  → http://localhost:5173
```

```bash
docker compose up --build      # start
docker compose down            # stop
```

---

# ⚙️ Local Setup

### Prerequisites
Java 21+, Node.js 22, Docker Desktop, and API keys for **Groq** and **Google Gemini**.

### 1. Clone
```bash
git clone https://github.com/pradeep-bhat-ms/ai-career-platform.git
cd ai-career-platform
```

### 2. Create `backend/.env`
```env
GEMINI_API_KEY=your_gemini_key
GROQ_API_KEY=your_groq_key
JWT_SECRET=a_long_random_string_at_least_48_characters
MAIL_USERNAME=your_gmail_address
MAIL_APP_PASSWORD=your_gmail_app_password
```

> ⚠️ Never commit `.env` files, API keys, database passwords, or JWT secrets.
> `.env` is already listed in `.gitignore`.

### 3a. Start everything with Docker
```bash
docker compose up --build
```
Open **http://localhost:5173**

### 3b. …or run each part yourself
```bash
docker compose up -d postgres          # database only

cd backend                             # set the env vars above first
./mvnw spring-boot:run

cd ../frontend
npm install
npm run dev                            # http://localhost:5173
```

### Environment variables

| Variable | Where | Purpose |
|---|---|---|
| `GEMINI_API_KEY` | backend | Embeddings |
| `GROQ_API_KEY` | backend | Text generation |
| `JWT_SECRET` | backend | Signs JWTs — use a long random value |
| `MAIL_USERNAME`, `MAIL_APP_PASSWORD` | backend | Gmail SMTP for OTP emails |
| `SPRING_DATASOURCE_URL` / `_USERNAME` / `_PASSWORD` | backend | Database (overrides the local default) |
| `APP_COOKIE_SECURE` | backend | Set `true` on HTTPS deployments |
| `VITE_API_URL` | frontend (build time) | Backend base URL, e.g. `https://<backend>/api` |

JWT lifetime is 24 hours (set in `application.yaml`).

---

# 🌍 Production Deployment

```text
GitHub
  ├──► Render Static Site ──► React frontend
  └──► Render Docker Web Service ──► Spring Boot ──► Neon PostgreSQL + PGVector
```

Deployment notes:
- Set `APP_COOKIE_SECURE=true` and use a **new, strong `JWT_SECRET`** (never reuse a local one)
- Set `SPRING_DATASOURCE_*` to the managed database
- Set `VITE_API_URL` at **build time** on the frontend
- Add a rewrite rule `/*` → `/index.html` on the static site so page refreshes work with React Router
- Allowed CORS origins are defined in `SecurityConfig`; update them if you deploy under different URLs
- Consider `SPRING_JPA_SHOW_SQL=false` in production

---

# 📁 Project Structure

```text
ai-career-platform/
├── backend/
│   ├── src/main/java/com/pradeep/aicareerplatform/
│   │   ├── config/        # AI providers (Groq chat + Gemini embeddings), WebClient, role-skill config
│   │   ├── controller/    # REST controllers
│   │   ├── dto/           # Request / response models
│   │   ├── entity/        # JPA entities
│   │   ├── exception/     # Global exception handling
│   │   ├── repository/    # Spring Data repositories
│   │   ├── security/      # JWT filter, cookie utility, rate limiter, security config
│   │   ├── service/       # Business, AI, RAG and GitHub services
│   │   └── util/
│   ├── src/main/resources/application.yaml
│   ├── src/test/java/     # Unit, MockMvc, security and Testcontainers tests
│   ├── Dockerfile
│   └── pom.xml
│
├── frontend/
│   ├── src/               # pages, components, services, context
│   ├── Dockerfile
│   └── package.json
│
├── docker-compose.yml
├── LICENSE
└── README.md
```

---

# 🗃️ Database

PostgreSQL with PGVector. Main entities:

`User` · `Resume` · `JobDescription` · `Document` · `DocumentChunk` ·
`InterviewSession` · `InterviewQuestion` · `InterviewAnswer` · `InterviewEvaluation` ·
`SkillGap` · `LearningPlan` · `LearningItem` · `CodeChallenge` · `CodeSubmission` · `PasswordResetOtp`

Vector embeddings are stored by Spring AI's PGVector store (HNSW index, cosine distance).

---

# 🎯 What This Project Demonstrates

Java · Spring Boot · Spring Security · JWT · REST APIs · JPA/Hibernate · PostgreSQL ·
React · Docker & Docker Compose · JUnit · Mockito · MockMvc · Testcontainers ·
Spring AI · Prompt engineering · Embeddings · Vector databases · Semantic search · RAG ·
Multi-provider AI setup · Cloud deployment

---

# 🚧 Status

### Implemented
- ✅ Resume Studio (analysis, weighted compatibility estimate, AI rewrite proposals)
- ✅ JD Analyzer, Match & Compare, Skill Roadmap, Interview Agent, Code Arena
- ✅ RAG: upload → chunk → embed → PGVector → retrieval → grounded answer with sources
- ✅ GitHub Analyzer (profile, repositories, statistics, language distribution)
- ✅ JWT in HTTP-only cookies, rate limiting, ownership checks
- ✅ Automated tests including Testcontainers
- ✅ Docker Compose stack and cloud deployment

### Known limitations
- Code Arena uses an LLM as the judge; there is no sandboxed code execution
- The resume compatibility score is an estimate, not an official ATS score
- Deleting a document does not yet fully remove its vectors from PGVector
- GitHub Analyzer uses unauthenticated GitHub API requests (rate-limited)
- Free-tier hosting means cold starts after inactivity

### Roadmap
- 🔄 AI agent with secure tool calling and permission checks
- 🔄 GitHub Actions CI (build + tests on every push)
- 🔄 OpenAPI / Swagger documentation
- 🔄 Sandboxed code execution for Code Arena
- 🔄 Pagination for large lists
- 🔄 RAG evaluation and broader test coverage
- 🔄 GitHub Analyzer: README, license, deployment, and CI analysis

---

# 👨‍💻 Author

**Pradeep Bhat M S** — MCA (Artificial Intelligence & Machine Learning)
Java Full Stack Developer | AI & Generative AI

GitHub: https://github.com/pradeep-bhat-ms

---

## 📜 License

Released under the [MIT License](LICENSE).
