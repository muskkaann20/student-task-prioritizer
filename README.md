# UniVerse — The Connected Campus Platform

**One campus. Infinite possibilities.**

UniVerse extends the original student deadline prioritizer into a three-portal campus app: administration assigns academic and administrative work with explainable faculty recommendations, faculty manage profiles and offer extra teaching, and students keep **UniVerse Tasks** while browsing workshops and requesting support.

## Stack

Spring Boot 3.2, Java 17, Spring Security (session cookie), JPA, SQLite locally (PostgreSQL when `DATABASE_URL` is set), static HTML/CSS/JS. No extra frontend build step.

## Run

```bash
./mvnw test
./mvnw spring-boot:run
```

Open [http://localhost:8080](http://localhost:8080).

Demo accounts (password `ChangeMe123!`):

- `admin@universe.local` — Admin portal
- `faculty@universe.local` / `faculty2@universe.local` — Faculty portal
- `student@universe.local` — Student portal (or register a new student)

Existing SQLite file `student_tasks.db` is reused (`ddl-auto=update`). Legacy rows kept under `owner_id` are not auto-attached to new logins; new student tasks are scoped to the authenticated user.

## Roles

| Portal | Capabilities |
| --- | --- |
| Admin | Create faculty, create academic/admin campus tasks, inspect workload, review matcher reasons, **approve** the assignment |
| Faculty | Profile, skills, weekly hours, availability slots, assigned work, workshops / extra classes / doubt sessions, accept student support requests |
| Student | UniVerse Tasks (same prioritization as before), browse sessions, request academic support |

Student task HTTP paths stay at `/api/tasks`. Isolation is by authenticated user, not the old `X-Owner-Id` header.

## Faculty matching

Rule-based score (weights in `application.properties`):

`proficiency × 3 + freeHours × 1 − currentLoad × 2 − conflictPenalty`

Each suggestion includes reason strings (skill match, remaining hours, calendar conflict). The matcher never auto-assigns.

## Layout for later expansion

New tables sit beside `tasks`: `users`, `faculty_profiles`, `campus_tasks`, `faculty_assignments`, `faculty_availability`, `learning_sessions`, `session_signups`, `support_requests`. Java package names are unchanged so existing tests and imports keep working.
