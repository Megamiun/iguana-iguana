# Getting Started

## Prerequisites
- Java 21+
- Node.js 22+
- npm

## Running the Application

### Dockerized

From the root directory, run:

```bash
docker compose up
```

This will allow you to access the FE from `http://localhost:5173`

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
- Enums for CourseHistoryStatus and CourseType created, as they seem exhaustive
  - The same has not been done for Student.status, as it has contains 'active' state on the database
- Room types were not available in the original dataset. I have decided to classify then as follow:
  - Classroom - Mathematics, English, Social Studies, Foreign Language
  - Science Lab - Science
  - Art Studio - Arts
  - Gym - Physical Education
  - Computer Lab - Computer Science
  - Music Room - Music
  - Library - None
  - Auditorium - None
- Over calculations:
  - Students are eligible for:
    - CORE courses: ~61 sections -> 322 hours/week
    - ELECTIVE courses: ~352 sections -> 1383 hours/week
  - Teachers have 1000 hours free/week(4 daily hours x 5 weekdays x 50 teachers)
  - Thus, with only 33% of the full capacity for CORE courses, a greedy algorithm was chosen for simplicity
    - Prioritize CORE courses first
    - Electives will be created in a best-effort manner using round-robin
    - A backtracking algorithm with rollbacks maybe would be overkill for this scenario
    - For scenarios with less slack, a backtracking algorithm could also become too time-consuming due to combinatorial explosion
