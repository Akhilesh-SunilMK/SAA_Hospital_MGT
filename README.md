# Hospital Management System (HMS)

A microservices implementation of the HMS SRS v1.0: Java 17, Spring Boot 3.2, Spring Cloud 2023.0,
MySQL 8, Docker/Docker Compose, RabbitMQ, Postman. Architecture style: microservices, database-per-service.
Mandatory design patterns: **Factory Method**, **Abstract Factory**, **Builder** (plus supporting Strategy,
Saga, Repository, Circuit Breaker, Observer patterns) — see [Design patterns](#design-patterns-implemented) below.

## Services

| Code | Service | Port | Schema | Responsibility |
|------|---------|------|--------|-----------------|
| SR | service-registry | 8761 | — | Eureka service discovery |
| CS | config-server | 8888 | — | Centralised configuration (native backend, see [Deviations](#deviations-from-the-srs)) |
| GW | api-gateway | 8080 | — | Routing, JWT pre-validation, rate limiting, CORS, correlation ID |
| AU | auth-service | 8081 | hms_auth | Registration, login, JWT issue/refresh, RBAC |
| PT | patient-service | 8082 | hms_patient | Patient demographics, admission, discharge |
| DR | doctor-service | 8083 | hms_doctor | Doctor profiles, schedules, availability |
| AP | appointment-service | 8084 | hms_appointment | Booking, rescheduling, cancellation, OPD queue |
| EM | emr-service | 8085 | hms_emr | Diagnoses, prescriptions, vitals, clinical history |
| LB | lab-service | 8086 | hms_lab | Test orders, sample tracking, results |
| PH | pharmacy-service | 8087 | hms_pharmacy | Drug catalogue, stock, dispensing |
| BL | billing-service | 8088 | hms_billing | Invoices, payments, refunds |
| NT | notification-service | 8089 | hms_notification | Email/SMS/Push dispatch |

All external traffic enters through the gateway: `http://localhost:8080/api/v1/...`. Each service also
exposes Swagger UI directly at `http://localhost:<port>/swagger-ui.html` and RabbitMQ management UI is at
`http://localhost:15672` (guest/guest).

## Running it

```bash
cp .env.example .env   # optional: override JWT_SECRET / DB_USER / DB_PASSWORD / MYSQL_ROOT_PASSWORD
docker compose up --build
```

Startup order is health-check gated (`depends_on: condition: service_healthy`): MySQL instances and
RabbitMQ first, then service-registry, then config-server, then the gateway and business services. First
boot takes a few minutes while Maven resolves dependencies inside each service's build stage.

Once up:
- Eureka dashboard: http://localhost:8761
- Gateway: http://localhost:8080
- Import `postman/HMS.postman_collection.json` into Postman — run **Auth → Register** then **Auth → Login**
  first; the Login request's test script stores the returned access/refresh tokens into collection
  variables so every subsequent request authenticates automatically.

### Building/testing without Docker

```bash
export JAVA_HOME=$(/usr/libexec/java_home -v 21)   # or any JDK 17/21
mvn -q -f common/pom.xml install -DskipTests
mvn test   # runs every module's unit tests via the reactor
```

## Design patterns implemented

| Pattern | Where | Why |
|---|---|---|
| **Factory Method** | `UserFactory` (auth), `NotificationSenderFactory` (notification), `PaymentProcessorFactory` (billing), `SlotAllocator` (appointment), `RecordFactory` (emr), `TestProcessor` (lab) | Each resolves a polymorphic implementation from a role/channel/method/type key via a `Map` built from an injected `List<T>` of Spring beans — adding a new implementation requires zero changes to the factory itself (Open/Closed), proven by unit tests (TC-F-01..04). |
| **Abstract Factory** | `ReportComponentFactory` (billing, emr) | Produces a family of mutually-consistent report components (formatter/exporter/styler) per output format (PDF/EXCEL/CSV); the family is selected once at runtime and never mixed (TC-AF-01/02). |
| **Builder** | `Appointment`, `MedicalRecord`, `Prescription`, `Invoice`, `LabTestOrder`, `PatientProfile`, `DispenseOrder`, `Schedule` | Every entity with >5 constructor parameters or any optional field is built via a static nested `Builder` with invariant validation in `build()` and no public setters (NFR-12) — proven by TC-B-01..06. |
| **Strategy** | `PricingStrategy` (pharmacy, billing) | Retail / insured / staff-concession pricing selected at runtime. |
| **Saga (choreographed compensation)** | appointment ↔ billing | `POST /appointments` → reserve slot → call billing to create a provisional invoice; on failure, the appointment and its slot reservation are rolled back (compensating action), mirroring SRS 3.4. |
| **Repository** | every service | Spring Data JPA. |
| **Circuit Breaker** | appointment/billing outbound Feign calls | Resilience4j, configured via the config-server's shared defaults. |
| **Observer (event-driven)** | RabbitMQ `hms.events` topic exchange | `appointment.confirmed`, `invoice.generated`, `lab.result.ready`, `stock.below.threshold` — published by the owning service, consumed by notification-service (and emr-service for lab results) without direct coupling. |

## Deviations from the SRS

Documented pragmatic simplifications made to keep this a runnable, self-contained deliverable in a single
repository without external infrastructure dependencies:

1. **Config server backend**: native/classpath-backed (`config-server/src/main/resources/config-repo/`)
   instead of Git-backed — avoids requiring a separate Git remote for a single-repo deliverable. Each
   service still calls out to config-server at boot via `spring.config.import=optional:configserver:...`
   for shared cross-cutting defaults (Resilience4j, management endpoints, logging pattern); the `optional:`
   prefix means a service still starts even if config-server is briefly unavailable.
2. **Rate limiting**: an in-memory per-IP token bucket in the gateway rather than a Redis-backed
   `RequestRateLimiter` — adequate for a single-gateway-instance deployment; note this would need to move
   to Redis for a horizontally-scaled gateway.
3. **`roles`/`permissions` tables**: collapsed into a `role` enum column on `users` plus a
   `user_permissions` table, since the SRS's eight roles (Appendix B) are a fixed enumeration, not
   admin-managed data — a normalized roles lookup table would be pure ceremony here.
4. **Notification channels**: `EmailNotificationSender`/`SmsNotificationSender`/`PushNotificationSender`
   log the rendered message instead of calling a real SMTP relay / SMS gateway / FCM, since no such
   credentials exist in this environment (SRS assumption A2). The `NotificationSenderFactory` dispatch
   logic itself is real and fully testable.
5. **Report export (PDF/Excel/CSV)**: the Abstract Factory's family-consistency is real and tested; the
   actual byte output is a lightweight labelled representation rather than wrapping a full PDF/POI library,
   to keep the dependency footprint small — the pattern under test is family selection, not visual fidelity.
6. **Internal service-to-service calls** (e.g. appointment-service creating a provisional invoice on
   billing-service on behalf of a PATIENT caller who isn't authorized to hit billing directly): the calling
   service mints a short-lived internal JWT (`role=ADMIN`) using the same shared `JWT_SECRET`, rather than
   introducing a separate service-account/mTLS scheme.
7. **Payment gateway**: stubbed per SRS assumption A3 — `CardPaymentProcessor`/`UpiPaymentProcessor`/etc.
   validate input shape and "settle" synchronously; no real payment gateway integration.

## Repository layout

```
common/                 shared ApiResponse envelope, JWT util/filter, exception handling, event publisher
service-registry/       Eureka
config-server/          Spring Cloud Config (native backend)
api-gateway/            Spring Cloud Gateway
auth-service/
patient-service/
doctor-service/
appointment-service/
emr-service/
lab-service/
pharmacy-service/
billing-service/
notification-service/
postman/HMS.postman_collection.json
docker-compose.yml
```

Each service module contains its own `pom.xml`, `Dockerfile` (multi-stage Maven build → JRE-alpine
runtime, non-root user, `HEALTHCHECK` against `/actuator/health`), `src/main/resources/application.yml`,
and `src/main/resources/db/migration/` (Flyway).
