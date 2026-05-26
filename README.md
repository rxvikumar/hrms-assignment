# HRMS Assignment - Worker Attendance & Overtime Settlement Engine

## Forked From
[aakashverma1124/CRUD-RESTful-API-with-Spring-Boot-Hibernate-JPA-and-PostgreSQL-Database](https://github.com/aakashverma1124/CRUD-RESTful-API-with-Spring-Boot-Hibernate-JPA-and-PostgreSQL-Database)

**Why this repo?** Clean JPA + PostgreSQL structure, easy to extend without rewriting. The existing `Employee` CRUD was kept intact while adding the full attendance/overtime system on top.

---

## Tech Stack
- **Java 17** + **Spring Boot 2.7.18**
- **Hibernate/JPA** — ORM with `ddl-auto=update`
- **Supabase** — PostgreSQL via PgBouncer (port 6543)
- **Redis** — Active worker caching with graceful degradation
- **HikariCP** — Connection pooling tuned for cloud DB

---

## Setup Instructions

### 1. Clone the repo
```bash
git clone https://github.com/<your-username>/CRUD-RESTful-API-with-Spring-Boot-Hibernate-JPA-and-PostgreSQL-Database.git
cd CRUD-RESTful-API-with-Spring-Boot-Hibernate-JPA-and-PostgreSQL-Database
```

### 2. Install Docker and start Redis
```bash
docker run -d -p 6379:6379 redis
```
> The app works even without Redis (graceful degradation), but caching is disabled.

### 3. Create a Supabase project
1. Go to [supabase.com](https://supabase.com) and create a free project
2. Wait for the project to finish provisioning

### 4. Get the Transaction Pooler URL
1. In your Supabase project dashboard, click **Connect**
2. Select **Direct** connection type
3. Copy the **Transaction Pooler** URL (port **6543**, NOT 5432)
   - ⚠️ **Never use port 5432** — direct connections cause connection exhaustion under load

### 5. Update `application.properties`
```properties
spring.datasource.url=jdbc:postgresql://<YOUR_POOLER_HOST>:6543/postgres
spring.datasource.username=postgres.<YOUR_PROJECT_REF>
spring.datasource.password=<YOUR_DB_PASSWORD>
```

### 6. Build and Run
```bash
mvn spring-boot:run
```

### 7. App starts at
```
http://localhost:8080
```

Hibernate will auto-create all tables (`workers`, `sites`, `attendance_logs`, `overtime_entries`) on first run.

---

## AI Tools Used
- **Claude** — Architecture decisions, ticket root cause analysis, design pattern selection
- **Antigravity / Claude Opus** — Code generation, implementation, and debugging

All generated code was reviewed, tested, and modified to match the assignment's business rules and coding standards.

---

## API Endpoints

### Workers
| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | `/api/workers` | List all workers |
| GET | `/api/workers/{id}` | Get worker by ID |
| POST | `/api/workers` | Create a worker |
| PUT | `/api/workers/{id}` | Update a worker |

### Sites
| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | `/api/sites` | List all sites |
| GET | `/api/sites/{id}` | Get site by ID |
| POST | `/api/sites` | Create a site |
| PUT | `/api/sites/{id}` | Update a site |

### Attendance
| Method | Endpoint | Description |
|--------|----------|-------------|
| POST | `/api/attendance/clock-in` | Clock in a worker at a site |
| POST | `/api/attendance/clock-out` | Clock out a worker (auto-calculates OT) |
| GET | `/api/attendance/active` | List active workers (from Redis) |
| GET | `/api/attendance/log` | Paginated attendance history |

### Overtime
| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | `/api/overtime/summary/{workerId}?month=YYYY-MM` | Monthly overtime breakdown |
| POST | `/api/overtime/settle/{workerId}?month=YYYY-MM` | Settle overtime (atomic) |

---

## curl Examples

### Create a Worker
```bash
curl -X POST http://localhost:8080/api/workers \
  -H "Content-Type: application/json" \
  -d '{"name":"Ravi Kumar","phone":"9876543210","designation":"MASON","dailyWageRate":800,"active":true}'
```

### List All Workers
```bash
curl http://localhost:8080/api/workers
```

### Create a Site
```bash
curl -X POST http://localhost:8080/api/sites \
  -H "Content-Type: application/json" \
  -d '{"siteName":"Greenfield Phase 1","location":"Whitefield Bengaluru","active":true}'
```

### List All Sites
```bash
curl http://localhost:8080/api/sites
```

### Clock In
```bash
curl -X POST http://localhost:8080/api/attendance/clock-in \
  -H "Content-Type: application/json" \
  -d '{"workerId":1,"siteId":1}'
```

### Clock Out
```bash
curl -X POST http://localhost:8080/api/attendance/clock-out \
  -H "Content-Type: application/json" \
  -d '{"workerId":1}'
```

### Get Active Workers (from Redis)
```bash
curl http://localhost:8080/api/attendance/active
```

### Get Attendance Log (Paginated)
```bash
curl "http://localhost:8080/api/attendance/log?workerId=1&from=2026-01-01&to=2026-12-31&page=0&size=20"
```

### Get Overtime Summary
```bash
curl "http://localhost:8080/api/overtime/summary/1?month=2026-05"
```

### Settle Overtime
```bash
curl -X POST "http://localhost:8080/api/overtime/settle/1?month=2026-04"
```

---

## Design Decisions

### Why BigDecimal for ALL money — never double or float
Floating-point arithmetic causes rounding errors in financial calculations. `800.00 * 1.5` in `double` can yield `1199.9999999999998`. In construction payroll, even ₹0.01 discrepancy across 500 workers over a month compounds into audit failures. `BigDecimal` with explicit `RoundingMode.HALF_UP` guarantees deterministic, cent-accurate results.

### Why FetchType.LAZY on all @ManyToOne
The default `EAGER` fetch fires a separate SQL query for every related entity. Loading 1000 attendance records would fire 1000 worker queries + 1000 site queries = **2001 total queries** (N+1 problem). `LAZY` + `JOIN FETCH` in repository queries loads everything in **1 query**. This is the fix for Ticket LF-203.

### Why Redis Hash for active workers
Using `HSET active_workers <workerId> <json>` instead of individual `SET worker:<id>` keys means we can fetch ALL active workers with a single `HGETALL active_workers` command — O(1) network call instead of scanning/pipeline. TTL of 16 hours acts as a safety net for missed clock-outs.

### Why @TransactionalEventListener(AFTER_COMMIT) for SMS
The settlement operation updates multiple database rows atomically. If SMS fires mid-transaction and the DB rolls back, the worker gets a "Payment processed" message for a payment that didn't happen. You can't unsend a message. `AFTER_COMMIT` guarantees the SMS only fires after the DB confirms the transaction is permanent.

### Why port 6543 PgBouncer, never direct 5432
Supabase limits direct connections. PgBouncer on port 6543 multiplexes connections, allowing many app threads to share fewer DB connections. `prepareThreshold=0` is required because PgBouncer in transaction mode doesn't support prepared statements across transactions.

### Why HikariCP max-lifetime 240s < Supabase 300s
Supabase kills idle connections after ~300 seconds. If HikariCP holds a connection for 300s, Supabase kills it server-side, and the next query gets a "connection reset" error. Setting `max-lifetime=240000` (240s) ensures HikariCP proactively recycles connections before Supabase can kill them. `keepalive-time=60000` sends periodic pings to keep connections warm.

### Why external API call moved OUTSIDE @Transactional
The `getMonthlySummary` method originally called an external government minimum wage API inside a `@Transactional` method. That API takes 3-5 seconds to respond. During that time, the DB connection is held hostage — doing nothing, blocking other requests. With only 10 connections in the pool, 10 concurrent summary requests would exhaust the pool completely. Fix: fetch external data BEFORE opening the transaction.

### Why @Transactional only on public methods called from controller
Spring's `@Transactional` works via AOP proxies. If a `@Transactional` private method is called internally (not through the proxy), the annotation is silently ignored — the "Spring proxy trap." All our transactional methods are public and called directly from controllers, ensuring the proxy intercepts correctly.

### Why CORS at SecurityFilterChain level, not @CrossOrigin
`@CrossOrigin` on controllers doesn't work when Spring Security is active because Security's filter chain runs BEFORE the controller. Preflight `OPTIONS` requests get rejected by Spring Security before `@CrossOrigin` can process them. Configuring CORS via `CorsConfigurationSource` in `SecurityFilterChain` ensures the `CorsFilter` runs before security filters.

---

## Ticket Fixes

### LF-201: CORS Errors from React Frontend
**Root cause:** No CORS configuration. Spring Security blocking preflight `OPTIONS` requests before they reach `@CrossOrigin` annotations.

**Fix:** `SecurityConfig.java` with `CorsConfigurationSource` bean registered at the security filter chain level. Allowed origins externalized in `application.properties` — not hardcoded. Preflight cache set to 1 hour (`maxAge=3600`).

**Files:** `SecurityConfig.java`, `application.properties`

---

### LF-202: App Crashes When Redis is Down
**Root cause:** Redis treated as a hard dependency. Any Redis timeout or connection failure throws an uncaught exception that kills the endpoint.

**Fix:** Custom `CacheErrorHandler` in `RedisConfig.java` that logs and swallows all cache errors. `ActiveWorkerCacheService` wraps every Redis operation in try-catch. Connection and read timeouts configured at 2 seconds. App degrades to DB-only mode when Redis is unavailable — no crash, no restart needed.

**Files:** `RedisConfig.java`, `ActiveWorkerCacheService.java`, `application.properties`

---

### LF-203: Attendance Endpoint Returns All Records, Slow Response
**Root cause:** No pagination. `findAll()` returns every record. Each record triggers separate SQL queries for worker and site (N+1 problem due to `EAGER` fetch defaults).

**Fix:** `FetchType.LAZY` on all `@ManyToOne` relationships. `JOIN FETCH` in all repository JPQL queries to load associations in a single SQL query. `Pageable` parameter added to controller/service/repository. `PaginatedResponse<T>` wrapper with page metadata. Max page size capped at 100.

**Files:** `AttendanceLogRepository.java`, `AttendanceController.java`, `AttendanceService.java`, `PaginatedResponse.java`, `AttendanceLog.java`, `OvertimeEntry.java`

---

### LF-204: Partial Settlement + Premature SMS
**Root cause:** Settlement loop commits each entry individually. If entry #5 of 10 fails, entries 1-4 are already committed (partial state). SMS fires inside the loop — workers receive "Payment processed" before the transaction completes, and if it rolls back, the message is already sent.

**Fix:** `@Transactional` on the entire `settleOvertime()` method — all entries settle in one atomic transaction. If any fails, all roll back. SMS notification published via `ApplicationEventPublisher` and handled by `NotificationListener` with `@TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)`. SMS only fires after the DB confirms the transaction is permanent.

**Files:** `OvertimeService.java`, `SettlementCompletedEvent.java`, `NotificationListener.java`

---

### LF-205: Connection Pool Exhaustion on Staging
**Root cause:** Three issues compounding: (1) HikariCP default `max-lifetime=1800000` (30 min) exceeds Supabase's ~300s idle timeout, causing stale connections. (2) Using port 5432 (direct) instead of 6543 (pooler). (3) External government API call inside `@Transactional` holds DB connection for 3-5 seconds during network call.

**Fix:** HikariCP tuned: `max-lifetime=240000` (240s < 300s Supabase timeout), `keepalive-time=60000` (ping every 60s), `connection-timeout=20000`, `leak-detection-threshold=30000`. Port changed to 6543 (PgBouncer). External API call moved outside `@Transactional` boundary. `RestTemplate` configured with explicit connect/read timeouts. Staging profile created for environment-specific overrides.

**Files:** `application.properties`, `application-staging.properties`, `OvertimeService.java`, `RestTemplateConfig.java`

---

## Project Structure
```
src/main/java/net/guides/springboot2/crud/
├── Application.java                    # Main Spring Boot class
├── config/
│   ├── RedisConfig.java                # Redis + CacheErrorHandler (LF-202)
│   ├── RestTemplateConfig.java         # RestTemplate with timeouts (LF-205)
│   └── SecurityConfig.java            # CORS + Security (LF-201)
├── controller/
│   ├── AttendanceController.java       # Clock-in/out, active, log
│   ├── EmployeeController.java         # Original CRUD (untouched)
│   ├── OvertimeController.java         # Summary, settlement
│   ├── SiteController.java             # Site CRUD
│   └── WorkerController.java           # Worker CRUD
├── dto/
│   ├── ActiveWorkerInfo.java           # Redis cache DTO
│   ├── AttendanceResponse.java         # Attendance response
│   ├── ClockInRequest.java             # Clock-in request body
│   ├── ClockOutRequest.java            # Clock-out request body
│   ├── OvertimeDayDetail.java          # Per-day OT breakdown
│   ├── OvertimeSummaryResponse.java    # Monthly OT summary
│   ├── PaginatedResponse.java          # Generic pagination wrapper (LF-203)
│   └── SettlementResponse.java         # Settlement result
├── event/
│   ├── NotificationListener.java       # @TransactionalEventListener (LF-204)
│   └── SettlementCompletedEvent.java   # Settlement domain event
├── exception/
│   ├── ApiErrorResponse.java           # {error, message, timestamp}
│   ├── BusinessException.java          # Custom business exception
│   ├── ErrorDetails.java               # Original (untouched)
│   ├── GlobalExceptionHandler.java     # @RestControllerAdvice
│   └── ResourceNotFoundException.java  # 404 exception
├── model/
│   ├── AttendanceLog.java              # Attendance entity
│   ├── Designation.java                # Worker designation enum
│   ├── Employee.java                   # Original (untouched)
│   ├── OvertimeEntry.java              # Overtime entity
│   ├── SettlementStatus.java           # PENDING/SETTLED enum
│   ├── Site.java                       # Site entity
│   └── Worker.java                     # Worker entity
├── repository/
│   ├── AttendanceLogRepository.java    # JOIN FETCH queries (LF-203)
│   ├── EmployeeRepository.java         # Original (untouched)
│   ├── OvertimeEntryRepository.java    # Monthly aggregation queries
│   ├── SiteRepository.java             # Site repo
│   └── WorkerRepository.java           # Worker repo
└── service/
    ├── ActiveWorkerCacheService.java   # Redis ops with degradation (LF-202)
    ├── AttendanceService.java          # Clock-in/out business logic
    └── OvertimeService.java            # Settlement + summary (LF-204, LF-205)
```
