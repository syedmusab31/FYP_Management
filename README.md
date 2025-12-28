# FYP Management System

A comprehensive Final Year Project (FYP) management system designed for universities to streamline the process of managing student projects, supervisions, document submissions, grading, and notifications.

##  Table of Contents

- [Features](#features)
- [Tech Stack](#tech-stack)
- [Project Structure](#project-structure)
- [Prerequisites](#prerequisites)
- [Installation & Setup](#installation--setup)
  - [Backend Setup](#backend-setup)
  - [Frontend Setup](#frontend-setup)
- [Running the Application](#running-the-application)
- [API Documentation](#api-documentation)
- [Project Overview](#project-overview)

##  Features

### Core Functionality
- **User Management**: Role-based access control (Students, Supervisors, Committee Members, FYP Committee)
- **Group Management**: Create and manage FYP groups with multiple members
- **Document Management**: Upload, version control, and review FYP documents
- **Grading System**: Assign grades using customizable rubrics
- **Deadline Management**: Set and track project deadlines
- **Review Workflow**: Multi-level document review and approval process

### Notification System (11 Notification Types)
- **Student Notifications**:
  -  Grade Released
  -  Deadline Created
  -  Document Approved
  -  Revision Requested

- **Supervisor Notifications**:
  - Document Uploaded
  -  Document Resubmitted
  -  Committee Revision Requested

- **Committee Notifications**:
  -  Grades Released
  -  Document Resubmitted for Review

- **FYP Committee Notifications**:
  -  Grades Completed

- **General**:
  -  General Announcements

### Technical Highlights
- **Time Standardization**: All temporal data uses `java.time.Instant` for UTC consistency
- **JWT Authentication**: Secure token-based authentication
- **Real-time Notifications**: 30-second auto-refresh for notification count
- **CORS Enabled**: Cross-origin resource sharing for frontend-backend communication
- **Responsive UI**: Mobile-friendly interface built with Tailwind CSS

##  Tech Stack

### Backend
- **Java 17** with Spring Boot 3.2.0
- **Spring Security** with JWT authentication
- **Spring Data JPA** with Hibernate ORM
- **MySQL 8.0+** database
- **Maven** for build management

### Frontend
- **React 18+** with Vite
- **Tailwind CSS** for styling
- **Lucide React** for icons
- **Axios** for HTTP requests
- **React Router** for navigation

### Database
- **MySQL 8.0+** for persistent storage
- **Liquibase/Hibernate** for schema management

##  Project Structure

```
SCD_PROJECT/
├── backend/
│   ├── src/
│   │   ├── main/
│   │   │   ├── java/com/university/fyp/
│   │   │   │   ├── config/           # Configuration classes
│   │   │   │   ├── controller/       # REST API endpoints
│   │   │   │   ├── dto/              # Data Transfer Objects
│   │   │   │   ├── entity/           # JPA entities
│   │   │   │   ├── repository/       # Data access layer
│   │   │   │   ├── security/         # Security configuration
│   │   │   │   └── service/          # Business logic
│   │   │   └── resources/
│   │   │       ├── application.properties
│   │   │       └── db/migration/     # Database migrations
│   └── pom.xml                       # Maven dependencies
│
├── frontend/
│   ├── src/
│   │   ├── components/               # React components
│   │   ├── pages/                    # Page components
│   │   ├── services/                 # API services
│   │   ├── context/                  # React context
│   │   ├── hooks/                    # Custom hooks
│   │   ├── layouts/                  # Layout components
│   │   ├── utils/                    # Utility functions
│   │   └── App.jsx                   # Root component
│   ├── package.json                  # NPM dependencies
│   ├── vite.config.js               # Vite configuration
│   └── tailwind.config.js           # Tailwind configuration
│
└── README.md                         # This file
```

##  Prerequisites

- **Java Development Kit (JDK)**: Version 17 or higher
- **Node.js & npm**: Version 14 or higher (for frontend)
- **MySQL Server**: Version 8.0 or higher
- **Git**: For version control
- **Maven**: Version 3.8 or higher (for backend)

### Installation Verification

```bash
# Check Java
java -version

# Check Node.js and npm
node --version
npm --version

# Check MySQL
mysql --version

# Check Maven
mvn --version
```

##  Installation & Setup

### Backend Setup

1. **Navigate to backend directory:**
   ```bash
   cd SCD_PROJECT
   ```

2. **Configure MySQL Database:**
   - Ensure MySQL server is running
   - The application will create the database automatically
   - Update credentials in `src/main/resources/application.properties` if needed:
   ```properties
   spring.datasource.url=jdbc:mysql://localhost:3306/fyp_management?createDatabaseIfNotExist=true
   spring.datasource.username=root
   spring.datasource.password=12345
   ```

3. **Build the application:**
   ```bash
   mvn clean compile
   ```

4. **Run the application:**
   ```bash
   mvn spring-boot:run
   ```
   
   The backend will start on `http://localhost:3301`

### Frontend Setup

1. **Navigate to frontend directory:**
   ```bash
   cd frontend
   ```

2. **Install dependencies:**
   ```bash
   npm install
   ```

3. **Configure API endpoint:**
   - Update `src/services/api.js` if backend is on different host/port:
   ```javascript
   const api = axios.create({
     baseURL: 'http://localhost:3301/api',
     // ... other config
   });
   ```

4. **Start development server:**
   ```bash
   npm run dev
   ```
   
   The frontend will typically run on `http://localhost:5173`

##  Running the Application

### Option 1: Sequential Start (Recommended for Development)

**Terminal 1 - Backend:**
```bash
cd SCD_PROJECT
mvn spring-boot:run
```

**Terminal 2 - Frontend:**
```bash
cd frontend
npm run dev
```

### Option 2: Production Build

**Backend (Production):**
```bash
cd SCD_PROJECT
mvn clean package
java -jar target/fyp-management-1.0.0.jar
```

**Frontend (Production):**
```bash
cd frontend
npm run build
npm run preview
```

## API Documentation

### Base URL
```
http://localhost:3301/api
```

### Authentication
All protected endpoints require a JWT token in the `Authorization` header:
```
Authorization: Bearer <your_jwt_token>
```

### Key Endpoints

#### Authentication
- `POST /auth/register` - Register new user
- `POST /auth/login` - Login and get JWT token

#### Notifications
- `GET /notifications` - Get all notifications
- `GET /notifications/unread` - Get unread notifications
- `GET /notifications/unread/count` - Get unread count
- `PUT /notifications/{id}/read` - Mark notification as read
- `PUT /notifications/read-all` - Mark all as read
- `DELETE /notifications/{id}` - Delete notification
- `DELETE /notifications` - Delete all notifications

#### Documents
- `GET /documents` - Get user's documents
- `POST /documents/upload` - Upload new document
- `GET /documents/{id}` - Get document details
- `PUT /documents/{id}/review` - Submit document review
- `DELETE /documents/{id}` - Delete document

#### Grades
- `POST /grades` - Assign grade
- `GET /grades/{id}` - Get grade details
- `PUT /grades/{id}/final` - Mark grade as final
- `GET /grades/group/{groupId}` - Get group grades

#### Deadlines
- `GET /deadlines` - Get all deadlines
- `POST /deadlines` - Create deadline
- `PUT /deadlines/{id}` - Update deadline
- `DELETE /deadlines/{id}` - Delete deadline

##  Project Overview

### Time Management
- **Instant-based**: All temporal fields use `java.time.Instant` for UTC consistency
- **No timezone issues**: Frontend sends ISO 8601 timestamps; backend stores as UTC
- **Automatic timestamps**: `@CreatedDate` and `@UpdatedDate` annotations handle timestamps

### Authentication Flow
1. User registers with email and password
2. User logs in and receives JWT token
3. Token stored in localStorage for subsequent requests
4. Token included in Authorization header for protected endpoints
5. Components check token before making API calls

### Notification Workflow
1. Service triggers notification creation on specific events
2. Notification saved to database with type and user reference
3. Frontend polls `/api/notifications/unread/count` every 30 seconds
4. Notification bell badge updates in real-time
5. Clicking bell opens notification panel with full list
6. Users can mark as read or delete notifications

### Role-Based Access Control

| Role | Capabilities |
|------|--------------|
| **Student** | Submit documents, view grades, receive notifications |
| **Supervisor** | Review documents, provide feedback, assign preliminary grades |
| **Committee Member** | Review work, request revisions, provide grading |
| **FYP Committee** | Manage deadlines, finalize grades, oversee process |

##  Security Features

- **JWT Authentication**: Stateless, token-based authentication
- **Password Encryption**: Passwords hashed using BCrypt
- **CORS Protection**: Configurable cross-origin access
- **Role-Based Authorization**: Method-level security with `@PreAuthorize`
- **Entity-Level Security**: User validation for sensitive operations

## Troubleshooting

### Backend Issues

**Port 3301 already in use:**
```bash
# Kill process on port 3301 (Windows)
netstat -ano | findstr :3301
taskkill /PID <PID> /F

# Or change port in application.properties
server.port=3302
```

**MySQL Connection Error:**
- Ensure MySQL is running
- Check credentials in `application.properties`
- Database will be auto-created if it doesn't exist

**Compilation Errors:**
```bash
mvn clean compile -X  # Run with debug output
```

### Frontend Issues

**Port 5173 already in use:**
```bash
npm run dev -- --port 3000  # Use different port
```

**API connection errors:**
- Check backend is running on 3301
- Verify API base URL in `src/services/api.js`
- Check browser console for CORS errors

**Module not found:**
```bash
rm -rf node_modules package-lock.json
npm install
```

##  Default Credentials

The system creates a default admin user:
- **Email**: `admin@university.edu`
- **Password**: `password123`
- **Role**: FYP Committee

##  Database Schema

### Key Tables
- `users` - User accounts with roles
- `groups` - FYP project groups
- `documents` - Submitted documents with versions
- `deadlines` - Project deadlines
- `grades` - Grade assignments with rubrics
- `notifications` - User notifications (11 types)
- `reviews` - Document reviews and feedback

All tables use Instant (UTC) for timestamps.

---

**Last Updated**: December 28, 2025  
**Version**: 1.0.0
