# Getting Started

## Prerequisites
- Java 17+
- Node.js 18+
- npm

## Running the Application

### Backend (Spring Boot)

From the `backend/` directory:

```bash
# Build the application
./gradlew build

# Run the application (default port 8080)
./gradlew bootRun
```

The backend will start at `http://localhost:8080`

### Frontend (React + Vite)

From the `frontend/` directory:

```bash
# Install dependencies (first time only)
npm install

# Start development server (port 5173)
npm start
```

The frontend will start at `http://localhost:5173`

---

# Choices/Assumptions
- At first, only next semester will be possible, as we don't know about passing rate or if students drop off classes
  - For future semesters, a choice may be to use the previous best guess considering 100% student pass rate for all non-closed semesters
- We define the "Demand Satisfaction" clause for the semester as:
  - For core courses:
    - Student has not taken course
    - Student has concluded prerequisites
    - Grade Level compatibility(Student is at Grade Level or above to the course)
  - For elective courses:
    - Every course will have one section at least
