# Slice Assignment — Boilerplate Skills

This file describes the patterns, extension points, and conventions in this codebase
so any feature requirement can be implemented quickly and consistently.

---

## Architecture

```
HTTP Request
    ↓
@RestController           — HTTP concerns only (routing, status codes, @Valid)
    ↓
Service interface         — contract (Dependency Inversion)
    ↓
@Service @Transactional   — business logic, entity ↔ DTO mapping
    ↓
JpaRepository             — data access (Spring Data generates SQL)
    ↓
MySQL / H2 (tests)
```

All layers communicate via DTOs (request/response), never raw JPA entities.

---

## Adding a New Domain (e.g. Booking, Ticket, Wallet)

Follow this checklist in order:

### 1. Schema — `src/main/resources/schema.sql`

```sql
CREATE TABLE IF NOT EXISTS bookings (
    id           BIGINT        NOT NULL AUTO_INCREMENT,
    -- your columns here
    status       VARCHAR(50)   NOT NULL DEFAULT 'PENDING',
    version      BIGINT        NOT NULL DEFAULT 0,   -- optimistic lock (from BaseEntity)
    created_at   DATETIME(6),
    updated_at   DATETIME(6),
    PRIMARY KEY (id),
    INDEX idx_bookings_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
```

### 2. Status Enum — `model/enums/BookingStatus.java`

```java
public enum BookingStatus {
    PENDING, CONFIRMED, CANCELLED
}
```

### 3. Entity — `model/Booking.java`

```java
@Entity
@Table(name = "bookings")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @SuperBuilder
public class Booking extends BaseEntity {   // id, version, createdAt, updatedAt come free

    @Column(nullable = false)
    private Long userId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 50)
    @Builder.Default
    private BookingStatus status = BookingStatus.PENDING;
}
```

### 4. Request DTOs — `dto/request/`

```java
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class CreateBookingRequest {

    @NotNull(message = "userId is required")
    private Long userId;
}
```

### 5. Response DTO — `dto/response/BookingResponse.java`

```java
@Getter @NoArgsConstructor @AllArgsConstructor @Builder
public class BookingResponse {
    private Long id;
    private Long userId;
    private BookingStatus status;
    private LocalDateTime createdAt;
}
```

### 6. Repository — `repository/BookingRepository.java`

```java
public interface BookingRepository extends JpaRepository<Booking, Long> {
    Page<Booking> findByStatus(BookingStatus status, Pageable pageable);
    boolean existsByUserIdAndStatus(Long userId, BookingStatus status);
}
```

### 7. Service Interface — `service/BookingService.java`

```java
public interface BookingService {
    BookingResponse create(CreateBookingRequest request);
    BookingResponse getById(Long id);
    Page<BookingResponse> getAll(Pageable pageable);
    BookingResponse cancel(Long id);
}
```

### 8. Service Implementation — `service/impl/BookingServiceImpl.java`

```java
@Service @RequiredArgsConstructor @Slf4j @Transactional
public class BookingServiceImpl implements BookingService {

    private final BookingRepository bookingRepository;

    @Override
    public BookingResponse create(CreateBookingRequest request) {
        Booking booking = Booking.builder()
                .userId(request.getUserId())
                .build();
        return toResponse(bookingRepository.save(booking));
    }

    @Override @Transactional(readOnly = true)
    public BookingResponse getById(Long id) {
        return toResponse(findOrThrow(id));
    }

    @Override @Transactional(readOnly = true)
    public Page<BookingResponse> getAll(Pageable pageable) {
        return bookingRepository.findAll(pageable).map(this::toResponse);
    }

    @Override
    public BookingResponse cancel(Long id) {
        Booking booking = findOrThrow(id);
        if (booking.getStatus() == BookingStatus.CANCELLED) {
            throw new BusinessException("Booking is already cancelled");
        }
        booking.setStatus(BookingStatus.CANCELLED);
        return toResponse(bookingRepository.save(booking));
    }

    private Booking findOrThrow(Long id) {
        return bookingRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Booking", "id", id));
    }

    private BookingResponse toResponse(Booking b) {
        return BookingResponse.builder()
                .id(b.getId())
                .userId(b.getUserId())
                .status(b.getStatus())
                .createdAt(b.getCreatedAt())
                .build();
    }
}
```

### 9. Controller — `controller/BookingController.java`

```java
@RestController
@RequestMapping(AppConstants.API_V1 + "/bookings")
@RequiredArgsConstructor @Slf4j
public class BookingController {

    private final BookingService bookingService;

    @PostMapping
    public ResponseEntity<ApiResponse<BookingResponse>> create(
            @Valid @RequestBody CreateBookingRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(AppConstants.MSG_CREATED, bookingService.create(request)));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<BookingResponse>> getById(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success(bookingService.getById(id)));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<Page<BookingResponse>>> getAll(
            @PageableDefault(size = AppConstants.DEFAULT_PAGE_SIZE,
                    sort = AppConstants.DEFAULT_SORT_FIELD) Pageable pageable) {
        return ResponseEntity.ok(ApiResponse.success(bookingService.getAll(pageable)));
    }

    @PatchMapping("/{id}/cancel")
    public ResponseEntity<ApiResponse<BookingResponse>> cancel(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success(AppConstants.MSG_UPDATED, bookingService.cancel(id)));
    }
}
```

### 10. Tests

- **Service test**: `@ExtendWith(MockitoExtension.class)`, mock repository, `@InjectMocks` service impl
- **Controller test**: `@WebMvcTest`, `@MockBean` service, test status codes + JSON paths

---

## Using the Strategy Pattern

Use `ProcessingStrategy<T, R>` when you need to swap algorithms at runtime (e.g. notification channels, allocation policies, pricing tiers).

```java
// 1. Implement the interface for each variant
@Component
public class EmailStrategy implements ProcessingStrategy<NotificationInput, Void> {
    @Override public String getStrategyName() { return "EMAIL"; }
    @Override public Void process(NotificationInput input) {
        // send email
        return null;
    }
}

// 2. Declare a StrategyRegistry bean
@Bean
public StrategyRegistry<NotificationInput, Void> notificationRegistry(
        List<ProcessingStrategy<NotificationInput, Void>> strategies) {
    return new StrategyRegistry<>(strategies);
}

// 3. Inject and resolve in the service
private final StrategyRegistry<NotificationInput, Void> notificationRegistry;

public void send(String channel, NotificationInput input) {
    notificationRegistry.resolve(channel).process(input);
}
```

`StrategyRegistry.resolve()` throws `BusinessException` (→ HTTP 422) for unknown names.

---

## Concurrency

- Every entity extending `BaseEntity` gets `@Version` (optimistic locking) automatically.
- Concurrent updates on the same row result in `ObjectOptimisticLockingFailureException`.
- `GlobalExceptionHandler` maps this to **HTTP 409 CONFLICT**.
- For wallet/balance operations that must never race, consider `@Lock(PESSIMISTIC_WRITE)`:

```java
@Lock(LockModeType.PESSIMISTIC_WRITE)
@Query("SELECT w FROM Wallet w WHERE w.id = :id")
Optional<Wallet> findByIdForUpdate(@Param("id") Long id);
```

---

## Exception Mapping

| Throw this                              | HTTP status |
|-----------------------------------------|-------------|
| `ResourceNotFoundException`             | 404         |
| `DuplicateResourceException`            | 409         |
| `BusinessException`                     | 422         |
| `ObjectOptimisticLockingFailureException` | 409       |
| `DataIntegrityViolationException`       | 409         |
| `MethodArgumentNotValidException`       | 400         |
| Any other `Exception`                   | 500         |

---

## Response Envelope

All endpoints return `ApiResponse<T>`:

```json
{ "status": "SUCCESS", "message": "...", "data": { ... }, "timestamp": "..." }
{ "status": "ERROR",   "message": "...", "data": null,    "timestamp": "..." }
```

Factory helpers:
- `ApiResponse.success(data)` — 200-style fetched
- `ApiResponse.success(message, data)` — custom message
- `ApiResponse.error(message)` — error with no data

---

## Test Patterns

### Service unit test (no Spring)

```java
@ExtendWith(MockitoExtension.class)
class BookingServiceImplTest {
    @Mock BookingRepository repo;
    @InjectMocks BookingServiceImpl service;

    @Test void shouldCreateBooking() {
        when(repo.save(any())).thenReturn(sampleBooking);
        BookingResponse result = service.create(request);
        assertThat(result.getStatus()).isEqualTo(BookingStatus.PENDING);
    }
}
```

### Controller web layer test (no DB)

```java
@WebMvcTest(controllers = {BookingController.class, GlobalExceptionHandler.class})
class BookingControllerTest {
    @Autowired MockMvc mockMvc;
    @MockBean BookingService service;

    @Test void shouldReturn201() throws Exception {
        when(service.create(any())).thenReturn(sampleResponse);
        mockMvc.perform(post("/api/v1/bookings")
                .contentType(APPLICATION_JSON)
                .content(json))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.data.id").value(1));
    }
}
```

---

## Key Constants (`AppConstants`)

| Constant               | Value                        |
|------------------------|------------------------------|
| `API_V1`               | `/api/v1`                    |
| `DEFAULT_PAGE_SIZE`    | `10`                         |
| `MAX_PAGE_SIZE`        | `100`                        |
| `DEFAULT_SORT_FIELD`   | `createdAt`                  |
| `STATUS_SUCCESS`       | `"SUCCESS"`                  |
| `STATUS_ERROR`         | `"ERROR"`                    |
| `MSG_CREATED`          | `"Resource created successfully"` |
| `MSG_UPDATED`          | `"Resource updated successfully"` |
| `MSG_DELETED`          | `"Resource deleted successfully"` |

---

## Common Interview Talking Points

**Why optimistic locking?**
Avoids DB-level locks (which are expensive and don't scale) for low-contention scenarios.
The `@Version` column increments on each update; a stale reader gets `OptimisticLockException`.

**Why constructor injection?**
Makes dependencies explicit and final; works correctly with immutability and testing without Spring context.

**Why DTOs instead of exposing entities?**
Entities carry Hibernate proxies that can trigger lazy loads during serialization;
DTOs give a stable API contract decoupled from the DB schema.

**Why `@Transactional(readOnly=true)` on reads?**
Hibernate skips dirty-check flushing, reducing overhead. Also allows read replicas in multi-DB setups.

**Why `BaseEntity`?**
All entities need id, version, createdAt, updatedAt. Centralising them avoids duplication and ensures consistency.
