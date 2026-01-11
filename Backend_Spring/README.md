# Backend_Spring - Student-Focused PFA Management API

## Quick Start

This is a **Spring Boot 3.5.9** REST API server providing Student-only access to the PFA (Projet de Fin d'Année) management system. It implements 7 student use cases extracted from the comprehensive Backend_Features project.

### Build & Run

```bash
# Build with Maven
mvn clean install

# Run the application
mvn spring-boot:run

# Default server: http://localhost:8080
```

### Database Configuration

Update `src/main/resources/application.properties`:
```properties
spring.datasource.url=jdbc:mysql://localhost:3306/pfa_db
spring.datasource.username=root
spring.datasource.password=your_password
spring.jpa.hibernate.ddl-auto=create-drop  # or update for production
```

---

## Project Structure

```
src/main/java/com/ensate/pfa/
├── controller/              # REST endpoints
├── service/                 # Service interfaces + implementations
├── entity/                  # JPA entities
├── repository/              # Spring Data JPA repositories
├── dto/                     # Request/Response DTOs
└── exception/               # Global error handling
```

---

## Next Steps

### 2. File Management
```
POST /api/deliverables/upload → File storage (AWS S3, Azure Blob)
POST /api/conventions/{id}/upload-signed → Convention document
```

---

**Framework:** Spring Boot 3.5.9  
**Language:** Java 17  
**Build Tool:** Maven  
