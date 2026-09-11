# BookNowGo - Spring Boot Backend Service

Enterprise-grade Spring Boot 3 REST API backend powering the **BookNowGo** Hotel Booking & Reservation Platform.

---

## 🛠 Tech Stack
- **Framework**: Spring Boot 3.3.4 (Java 17+)
- **Security**: Spring Security 6 with stateless JWT authentication & role-based access control (RBAC)
- **Database**: Cloud PostgreSQL (Neon) with HikariCP connection pooling & Spring Data JPA / Hibernate
- **Documentation**: SpringDoc OpenAPI / Swagger UI 3
- **Containerization**: Multi-stage Docker image (Alpine Temurin JRE 17)

---

## 🚀 Quick Start

### Prerequisites
- Java 17 or higher
- Maven 3.8+ (or `./mvnw`)

### 1. Configure Database Connection
The backend comes preconfigured to connect to the cloud **Neon PostgreSQL** database out-of-the-box (`postgres` profile).

You can override credentials via environment variables:
```bash
export SPRING_PROFILES_ACTIVE=postgres
export DB_URL="jdbc:postgresql://<neon-host>:5432/<dbname>?sslmode=require"
export DB_USER="<neon-user>"
export DB_PASSWORD="<neon-password>"
export JWT_SECRET="your-256-bit-secret"
export CORS_ALLOWED_ORIGINS="http://localhost:3000,http://localhost:5173,http://localhost:*"
```

### 2. Run Locally
```bash
mvn clean spring-boot:run
```

The server will start at: `http://localhost:8080`

### 3. API Documentation (Swagger)
Once the server is running, navigate to:
- **Interactive Swagger UI**: [http://localhost:8080/swagger-ui.html](http://localhost:8080/swagger-ui.html)
- **OpenAPI JSON Spec**: [http://localhost:8080/v3/api-docs](http://localhost:8080/v3/api-docs)

---

## 🐳 Docker Deployment

### Build Docker Image
```bash
docker build -t booknowgo-backend .
```

### Run Docker Container
```bash
docker run -p 8080:8080 \
  -e SPRING_PROFILES_ACTIVE=postgres \
  -e DB_URL="jdbc:postgresql://<neon-host>:5432/<dbname>?sslmode=require" \
  -e DB_USER="<neon-user>" \
  -e DB_PASSWORD="<neon-password>" \
  booknowgo-backend
```

---

## 🔑 Demo User Accounts
On initial startup, `DataInitializer` seeds the database with the following demo credentials:

| Role | Email | Password | Permissions |
|---|---|---|---|
| **Customer** | `customer@booknowgo.com` | `Customer@123` | Browse, search, book rooms, add reviews |
| **Hotel Owner** | `owner@booknowgo.com` | `Owner@123` | Manage properties, rooms, daily inventory, pricing |
| **Platform Admin** | `admin@booknowgo.com` | `Admin@123` | System oversight, approve hotels, manage coupons, user management |

---

## 🌐 Deploy to Cloud (Render / Railway / Fly.io / AWS)
1. Push this repository to GitHub.
2. In Render / Railway / Fly.io:
   - Create a new **Web Service** from your GitHub repository.
   - Environment: **Docker** or **Java Maven**.
   - Build Command: `mvn clean package -DskipTests`
   - Start Command: `java -jar target/booknowgo-backend-1.0.0.jar` (or use the provided `Dockerfile`).
   - Add Environment Variables:
     - `SPRING_PROFILES_ACTIVE`: `postgres`
     - `PORT`: `8080`
     - `DB_URL`, `DB_USER`, `DB_PASSWORD`, `JWT_SECRET`, `CORS_ALLOWED_ORIGINS`
