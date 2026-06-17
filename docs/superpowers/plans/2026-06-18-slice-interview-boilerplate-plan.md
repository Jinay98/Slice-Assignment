# Slice Interview Boilerplate — Implementation Plan

> **For agentic workers:** Use superpowers:subagent-driven-development to implement this plan task-by-task.

**Goal:** A running Spring Boot 3.2 / Java 17 application with full CRUD, MySQL via `schema.sql`, Mockito tests, and pluggable design patterns — ready to extend for any Slice interview problem in < 5 minutes.

**Architecture:** Controller → Service (interface) → ServiceImpl → Repository → MySQL. All layers communicate via DTOs. Exceptions are handled centrally by `GlobalExceptionHandler`.

**Tech Stack:** Java 17, Spring Boot 3.2.5, Spring Data JPA, HikariCP, MySQL 8, Lombok, JUnit 5, Mockito, MockMvc, H2 (test)

---

## Global Constraints

- Java version: 17 (LTS)
- Spring Boot: 3.2.5
- Package root: `com.slice`
- All REST paths under `/api/v1/`
- All responses use `ApiResponse<T>` envelope
- DB credentials: `root / root` on `localhost:3306`
- Schema is managed via `src/main/resources/schema.sql` (not Flyway)
- Tests use H2 in-memory DB (profile: `test`) — no MySQL required to run `mvn test`

---

## Task 1: Project Bootstrap ✅ DONE

**Files:**
- Created: `pom.xml`
- Created: `.gitignore`
- Created: `src/main/resources/application.properties`
- Created: `src/main/resources/schema.sql`
- Created: `src/test/resources/application-test.properties`

**What was done:**
- Spring Boot 3.2.5 parent POM with all dependencies (web, jpa, validation, mysql, lombok, test, h2)
- `application.properties` wired to local MySQL with HikariCP pool settings
- `schema.sql` with `items` table + commented templates for all 3 Slice interview domains
- `application-test.properties` uses H2 in `MySQL` compatibility mode for context tests

**Verify:**
```bash
mvn dependency:resolve   # should download all deps without error
```

---

## Task 2: Infrastructure Layer ✅ DONE

**Files:**
- Created: `src/main/java/com/slice/SliceApplication.java`
- Created: `src/main/java/com/slice/config/AppConfig.java`
- Created: `src/main/java/com/slice/util/AppConstants.java`
- Created: `src/main/java/com/slice/strategy/ProcessingStrategy.java`

**What was done:**
- `SliceApplication` — entry point with full architecture + design pattern documentation
- `AppConfig` — `@Component` with `ApplicationReadyEvent` listener for DB connectivity check. Documents Singleton + SOLID patterns. HikariCP configured via properties (avoids double-bean conflict).
- `AppConstants` — non-instantiable utility class with API prefix, pagination defaults, status strings, and standard messages
- `ProcessingStrategy<T,R>` — Strategy pattern interface pre-wired for interview (swap NotificationStrategy/AllocationStrategy/RewardStrategy at runtime)

---

## Task 3: Exception Handling ✅ DONE

**Files:**
- Created: `src/main/java/com/slice/exception/ResourceNotFoundException.java`
- Created: `src/main/java/com/slice/exception/DuplicateResourceException.java`
- Created: `src/main/java/com/slice/exception/BusinessException.java`
- Created: `src/main/java/com/slice/exception/GlobalExceptionHandler.java`

**HTTP Mapping:**

| Exception | HTTP Status |
|-----------|-------------|
| `ResourceNotFoundException` | 404 NOT FOUND |
| `DuplicateResourceException` | 409 CONFLICT |
| `BusinessException` | 422 UNPROCESSABLE ENTITY |
| `MethodArgumentNotValidException` | 400 BAD REQUEST |
| `Exception` (catch-all) | 500 INTERNAL SERVER ERROR |

**Verify:**
- Start app and hit `GET /api/v1/items/999` → should return `{"status":"ERROR","message":"Item not found..."}` with 404

---

## Task 4: ApiResponse DTO ✅ DONE

**Files:**
- Created: `src/main/java/com/slice/dto/response/ApiResponse.java`

**Contract:**
```java
ApiResponse.success(data)             // { status:"SUCCESS", message:"...", data:{...} }
ApiResponse.success("custom msg", data)
ApiResponse.error("message")          // { status:"ERROR", message:"..." }
```

- `@JsonInclude(NON_NULL)` — null fields are excluded from JSON output
- `@Builder.Default` on `timestamp` so it's always set when built

---

## Task 5: Sample Domain — Item ✅ DONE

**Files:**
- Created: `src/main/java/com/slice/model/Item.java`
- Created: `src/main/java/com/slice/model/enums/ItemStatus.java`
- Created: `src/main/java/com/slice/dto/request/CreateItemRequest.java`
- Created: `src/main/java/com/slice/dto/request/UpdateItemRequest.java`
- Created: `src/main/java/com/slice/dto/response/ItemResponse.java`
- Created: `src/main/java/com/slice/repository/ItemRepository.java`
- Created: `src/main/java/com/slice/service/ItemService.java`
- Created: `src/main/java/com/slice/service/impl/ItemServiceImpl.java`
- Created: `src/main/java/com/slice/controller/ItemController.java`

**Key decisions:**
- `@Version` on `Item` → optimistic locking for concurrent writes
- `@CreationTimestamp` / `@UpdateTimestamp` → auto-managed audit fields
- Service depends on `ItemService` interface, NOT `ItemServiceImpl` → DIP
- `@Transactional(readOnly=true)` on read methods → Hibernate skips dirty tracking
- Controller is deliberately thin — validation and HTTP status only; no business logic
- Pagination via Spring's `Pageable` — caller passes `?page=0&size=10&sort=createdAt,desc`

---

## Task 6: Tests ✅ DONE

**Files:**
- Created: `src/test/java/com/slice/SliceApplicationTests.java`
- Created: `src/test/java/com/slice/service/ItemServiceImplTest.java`
- Created: `src/test/java/com/slice/controller/ItemControllerTest.java`

**Test coverage:**

| Test | Scenarios covered |
|------|-------------------|
| `ItemServiceImplTest` | create (happy + duplicate), getById (happy + not-found), getAll (page + empty), update (happy + not-found), delete (happy + not-found) |
| `ItemControllerTest` | POST 201 + 400 validation + 409 conflict, GET 200 + 404, PUT 200, DELETE 200 + 404 |
| `SliceApplicationTests` | Full Spring context loads cleanly |

**Run:**
```bash
mvn test
# Expected: BUILD SUCCESS, all tests GREEN
```

---

## Task 7: Git Init ⬜ TODO

```bash
cd /Users/jinay-parekh/Dream11/slice-interview-prep
git init
git add .
git commit -m "feat: initial Spring Boot boilerplate for Slice interview"
```

---

## Task 8 (Interview Day): Add Your Domain

When the problem statement is given, follow this checklist:

```
[ ] 1. Add table(s) to schema.sql
[ ] 2. Create model/YourEntity.java  (@Entity, @Version if concurrent writes)
[ ] 3. Create model/enums/YourEntityStatus.java
[ ] 4. Create dto/request/CreateYourEntityRequest.java  (validation annotations)
[ ] 5. Create dto/response/YourEntityResponse.java  (@Builder)
[ ] 6. Create repository/YourEntityRepository.java  (JpaRepository + custom queries)
[ ] 7. Create service/YourEntityService.java  (interface)
[ ] 8. Create service/impl/YourEntityServiceImpl.java  (@Service @Transactional)
[ ] 9. Create controller/YourEntityController.java  (@RestController /api/v1/...)
[ ] 10. Write service unit tests with Mockito
[ ] 11. Write controller tests with @WebMvcTest
[ ] 12. Restart app: mvn spring-boot:run
```

### Domain-specific notes

**If it's a Notification System:**
- Add `idempotency_key UNIQUE` column → prevents duplicate dispatches
- Implement `ProcessingStrategy<NotificationRequest, Boolean>` for EMAIL/SMS/PUSH
- Add `retry_count` + exponential backoff in service
- Use `@Scheduled` for polling PENDING notifications (or Kafka consumer)

**If it's a Wallet/Ledger:**
- `@Version` on Wallet is critical (concurrent credit/debit → optimistic lock)
- Always create a `Transaction` record before updating wallet balance
- Use `@Transactional` with proper isolation — `SERIALIZABLE` for high consistency
- `Debit` throws `BusinessException` if `balance < amount`

**If it's a Parking Lot:**
- `@Version` on `ParkingSlot` prevents double-booking
- Use Strategy pattern: `FirstFitStrategy`, `NearestExitStrategy` implementing `ProcessingStrategy<Vehicle, ParkingSlot>`
- Inject strategy via `Map<String, ProcessingStrategy>` — Spring auto-populates by bean name

---

## Discussion Talking Points (Interviewer Questions)

**Q: How do you handle concurrent requests to the same resource?**
> "I use optimistic locking via `@Version`. JPA auto-increments the version on each UPDATE. If two threads read version=1 and both try to update, the second commit fails with `OptimisticLockException`. I catch this and either retry or return 409."

**Q: How is your DB connection managed?**
> "Spring Boot auto-configures HikariCP as a singleton connection pool. I've set `maximum-pool-size=10` and `minimum-idle=2` via properties. HikariCP reuses connections across requests — far cheaper than creating a new connection per request."

**Q: Why separate Service interface and Impl?**
> "Dependency Inversion — the controller depends on the abstraction (interface), not the concrete class. This makes the service trivially mockable in tests (`@MockBean ItemService`) and lets me swap implementations (e.g., cache-backed) without touching the controller."

**Q: How do you handle validation?**
> "I use Bean Validation annotations (`@NotBlank`, `@Size`, `@DecimalMin`) on DTOs. Spring's `@Valid` on the `@RequestBody` triggers validation before the controller method runs. `MethodArgumentNotValidException` is caught by `GlobalExceptionHandler` and returns a field-level error map with HTTP 400."

**Q: Why `schema.sql` instead of `ddl-auto=update`?**
> "`ddl-auto=update` is risky in production — it can silently drop columns or rename constraints. `schema.sql` gives full control, is version-controlled, and is idempotent (`IF NOT EXISTS`). In production I'd use Flyway versioned migrations."
