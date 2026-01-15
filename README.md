# Java Spring Evaluation
# Signup and Login Backend API with JWT Tokens

This project is a backend implementation of a login and signup REST API with Spring security and JWT tokens. It is built using Java, and Spring Boot, and utilizes the H2 database for data storage. The API endpoints provided below demonstrate the functionality of the application.


## Installation and Setup

```
### Prerequisites
- Java Development Kit (JDK) 21 or later
- Maven
- Postman (for testing the API)
```

### 1. Clone the Repository

```
git clone https://github.com/gl-training/Java_Spring_Evaluation.git
```

### 2. Go the Project

```
cd /Java_Spring_Evaluation

```

### 3. Run the Application
- For GitBash
```
./mvnw spring-boot:run

```
**The application will start running on [http://localhost:8888](http://localhost:8888)**

### **API Endpoints**

### User Signup

- Method: POST
- Path: `http://localhost:8888/app/sign-up`
- Description: Register a new user.
- Request Body: User data in the JSON format (e.g., name, email, password).

```
{
  "name": "Michael",  
  "email": "mfelipe@gmail.com",
  "password": "a2asfGfdfdf3",
  "phones": [{
    "number": "10",
    "cityCode": "11",
    "countryCode": "12"
  }]
}

```

- Response:

```
{
    "id": "af47d09f-23f1-4d31-a1c6-9e6710c9c612",
    "created": "2026-01-15T12:04:41.925126",
    "token": "eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJtZmVsaXBlQGdtYWlsLmNvbSIsInVzZXJuYW1lIjoibWZlbGlwZUBnbWFpbC5jb20iLCJpYXQiOjE3Njg0OTY2ODEsImV4cCI6MTc2ODUzMjY4MX0.p3c2xzjoqAz5hcNdJX_iG7nzcQKX4qMQiWNLAGhAad8",
    "isActive": true
}

```

### User Login (Requires JWT Authentication)

- Method: GET
- Path: `http://localhost:8888/app/login`
- Description: A protected endpoint that requires authentication to access.
- Authentication: Bearer Token
- Request Header:
    - Authorization: Bearer <token>
- Example Token:
    - Bearer Token: eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJtZmVsaXBlQGdtYWlsLmNvbSIsInVzZXJuYW1lIjoibWZlbGlwZUBnbWFpbC5jb20iLCJpYXQiOjE3Njg0OTY2ODEsImV4cCI6MTc2ODUzMjY4MX0.p3c2xzjoqAz5hcNdJX_iG7nzcQKX4qMQiWNLAGhAad8
- Response:
```
{
    "id": "af47d09f-23f1-4d31-a1c6-9e6710c9c612",
    "created": "2026-01-15T12:04:41.925126",
    "lastLogin": "2026-01-15T12:07:15.8422162",
    "name": "Michael",
    "email": "mfelipe@gmail.com",
    "password": "a2asfGfdfdf3",
    "phones": [
        {
            "number": "10",
            "cityCode": "11",
            "countryCode": "12"
        }
    ],
    "token": "eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJtZmVsaXBlQGdtYWlsLmNvbSIsInVzZXJuYW1lIjoibWZlbGlwZUBnbWFpbC5jb20iLCJpYXQiOjE3Njg0OTY4MzUsImV4cCI6MTc2ODUzMjgzNX0.iiGkmH4mdj2-w3QGN6BN336LsNcSIc-A86yd-SbEIIk",
    "isActive": true
}
```

### Tech Stack

- Java
- Spring Boot
- H2 Database
- Spring Security
- JWT Token
- Lombok
- Maven

### Validation Rules

The following validation rules are applied to the user entity:

- Name: Optional
- Phones: Optional
    - Minimum length: 3 characters
    - Maximum length: 20 characters
- Password:
    - Between 8 and 12 characters
    - Contains just one uppercase letter
    - Contains just two digits
    - Contains at least one lowercase letter
    - Encrypted
- Email:
    - Valid email format

### Development

The project can be imported and run using an IDE like IntelliJ Idea.

### Test API

You can use Postman to test the API endpoints.

## H2 Database Configuration

The project uses the H2 in-memory database by default.

The application is configured to use the H2 database. The configuration can be found in the `application.properties` file:

```
# Server Port Configuration
server.port=8888

# H2 Database Configuration
spring.datasource.url=jdbc:h2:mem:testdb
spring.datasource.driverClassName=org.h2.Driver
spring.datasource.username=sa
spring.datasource.password=
spring.jpa.database-platform=org.hibernate.dialect.H2Dialect
spring.h2.console.enabled=true
spring.h2.console.path=/h2-console

```
