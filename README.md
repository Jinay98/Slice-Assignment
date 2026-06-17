# Slice Interview Prep — Spring Boot Boilerplate

Production-grade Spring Boot boilerplate for the Slice SDE-2 machine coding round.
Built with Java 17, Spring Boot 3.2, MySQL, Lombok, and Mockito.

---

## 🚀 Quick Start

```bash
# 1. Clone / open the project
cd /Users/jinay-parekh/Dream11/slice-interview-prep

# 2. Ensure MySQL is running with root/root
#    (DB 'slice_db' will be auto-created on first run)

# 3. Start the application
mvn spring-boot:run

# 4. Run all tests (no MySQL needed — uses H2)
mvn test
```

The app starts on **http://localhost:8080**

---

## 📁 Project Structure

```
slice-interview-prep/
├── pom.xml                                  # Spring Boot 3.2, Java 17, Lombok, Mockito
├── src/
│   ├── main/
│   │   ├── java/com/slice/
│   │   │   ├── SliceApplication.java        # Entry point + architecture docs
│   │   │   ├── config/
│   │   │   │   └── AppConfig.java           # Singleton pattern + startup DB check
│   │   │   ├── controller/
│   │   │   │   └── ItemController.java      # REST CRUD (replace with your domain)
│   │   │   ├── service/
│   │   │   │   ├── ItemService.java         # Interface (DIP)
│   │   │   │   └── impl/
│   │   │   │       └── ItemServiceImpl.java # Implementation with @Transactional
│   │   │   ├── repository/
│   │   │   │   └── ItemRepository.java      # Spring Data JPA + custom queries
│   │   │   ├── model/
│   │   │   │   ├── Item.java                # JPA Entity (@Version optimistic lock)
│   │   │   │   └── enums/
│   │   │   │       └── ItemStatus.java      # Status lifecycle enum
│   │   │   ├── dto/
│   │   │   │   ├── request/
│   │   │   │   │   ├── CreateItemRequest.java
│   │   │   │   │   └── UpdateItemRequest.java
│   │   │   │   └── response/
│   │   │   │       ├── ApiResponse.java     # Standard response envelope
│   │   │   │       └── ItemResponse.java
│   │   │   ├── exception/
│   │   │   │   ├── ResourceNotFoundException.java  # 404
│   │   │   │   ├── DuplicateResourceException.java # 409
│   │   │   │   ├── BusinessException.java          # 422
│   │   │   │   └── GlobalExceptionHandler.java     # @RestControllerAdvice
│   │   │   ├── strategy/
│   │   │   │   └── ProcessingStrategy.java  # Strategy pattern interface
│   │   │   └── util/
│   │   │       └── AppConstants.java        # App-wide constants
│   │   └── resources/
│   │       ├── application.properties       # MySQL + HikariCP + JPA config
│   │       └── schema.sql                   # DB schema (runs on startup)
│   └── test/
│       ├── java/com/slice/
│       │   ├── SliceApplicationTests.java   # Context smoke test (H2)
│       │   ├── controller/
│       │   │   └── ItemControllerTest.java  # @WebMvcTest + MockMvc
│       │   └── service/
│       │       └── ItemServiceImplTest.java # Mockito unit tests
│       └── resources/
│           └── application-test.properties  # H2 in-memory (no MySQL needed)
└── docs/
    └── superpowers/
        ├── specs/
        │   └── 2026-06-18-slice-interview-boilerplate-design.md
        └── plans/
            └── 2026-06-18-slice-interview-boilerplate-plan.md
```

---

## 🏗️ Architecture

```
HTTP Request
     │
     ▼
┌─────────────────────┐
│   ItemController    │  ← @RestController — HTTP only, no business logic
│   /api/v1/items     │  ← @Valid on inputs, ApiResponse<T> on all outputs
└────────┬────────────┘
         │ calls
         ▼
┌─────────────────────┐
│   ItemService       │  ← Interface (Dependency Inversion)
│   (interface)       │
└────────┬────────────┘
         │ implemented by
         ▼
┌─────────────────────┐
│  ItemServiceImpl    │  ← @Transactional, business rules, entity → DTO mapping
└────────┬────────────┘
         │ uses
         ▼
┌─────────────────────┐
│  ItemRepository     │  ← Spring Data JPA — zero boilerplate SQL
└────────┬────────────┘
         │
         ▼
     MySQL DB
```

---

## 🔌 API Reference

| Method | Path               | Description         | Success | Error         |
|--------|--------------------|---------------------|---------|---------------|
| POST   | `/api/v1/items`    | Create item         | 201     | 400, 409      |
| GET    | `/api/v1/items`    | List items (paged)  | 200     | 500           |
| GET    | `/api/v1/items/{id}` | Get by ID          | 200     | 404           |
| PUT    | `/api/v1/items/{id}` | Update item        | 200     | 400, 404      |
| DELETE | `/api/v1/items/{id}` | Delete item        | 200     | 404           |

**Pagination:** `GET /api/v1/items?page=0&size=10&sort=createdAt,desc`

**Response Envelope (all endpoints):**
```json
{
  "status": "SUCCESS",
  "message": "Resource created successfully",
  "data": { ... },
  "timestamp": "2026-06-18T00:00:00"
}
```

---

## 🎨 Design Patterns

| Pattern    | Where used                                     |
|------------|------------------------------------------------|
| Singleton  | All Spring beans; HikariCP pool                |
| Strategy   | `ProcessingStrategy<T,R>` interface            |
| Builder    | Lombok `@Builder` on all DTOs + entities       |
| Repository | `ItemRepository extends JpaRepository`         |
| Template   | Consistent CRUD pattern in `ItemServiceImpl`   |

---

## 🔒 Concurrency: Optimistic Locking

The `Item` entity has `@Version private Long version`. When two threads read the same row and both try to update it:
- Thread A commits first → version bumps from 0 → 1 ✅
- Thread B tries to commit → JPA sees its version (0) ≠ DB version (1) → throws `OptimisticLockException` ❌

Handle it with a retry or surface a `409 CONFLICT` to the client.

---

## 🧪 Test Strategy

| Test Class              | Type            | Tool              | DB needed |
|------------------------|-----------------|-------------------|-----------|
| `SliceApplicationTests` | Smoke / Integration | `@SpringBootTest` | H2 (auto) |
| `ItemControllerTest`   | Web layer       | `@WebMvcTest` + MockMvc | None  |
| `ItemServiceImplTest`  | Unit            | Mockito `@ExtendWith` | None   |

Run tests: `mvn test`

---

## ⚡ Interview Cheat Sheet: Adding a New Domain

**Step 1** — Add your table to `schema.sql`
```sql
CREATE TABLE IF NOT EXISTS your_table (...);
```

**Step 2** — Create the domain model
```
model/YourEntity.java          (@Entity, @Version for concurrency)
model/enums/YourEntityStatus.java
```

**Step 3** — Create the repository
```
repository/YourEntityRepository.java  (extends JpaRepository)
```

**Step 4** — Create request/response DTOs
```
dto/request/CreateYourEntityRequest.java  (@NotBlank, @Valid)
dto/response/YourEntityResponse.java      (@Builder)
```

**Step 5** — Create service interface + impl
```
service/YourEntityService.java            (interface)
service/impl/YourEntityServiceImpl.java   (@Service @Transactional)
```

**Step 6** — Create the controller
```
controller/YourEntityController.java      (@RestController /api/v1/your-entity)
```

**Step 7** — Add Mockito tests
```
test/service/YourEntityServiceImplTest.java
test/controller/YourEntityControllerTest.java
```

---

## 🛠️ Maven Commands

```bash
mvn spring-boot:run       # Start the app
mvn test                  # Run all tests (H2, no MySQL needed)
mvn test -pl . -Dtest=ItemServiceImplTest  # Run one test class
mvn clean package         # Build JAR
mvn dependency:tree       # Show all dependencies
```
