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
  - Teachers have 1000 hours free/week(4 daily hours x 5 weekdays x 50 teachers)
  - Pre bug that ignored first-graders:
    - ~~Students are eligible for:~~
       - ~~CORE courses: around 61 sections -> 322 hours/week~~
       - ~~ELECTIVE courses: around 352 sections -> 1383 hours/week~~
     - ~~Thus, with only 33% of the full capacity for CORE courses, a greedy algorithm was chosen for simplicity~~
       - ~~Prioritize CORE courses first~~
       - ~~Electives will be created in a best-effort manner using round-robin~~
       - ~~A backtracking algorithm with rollbacks maybe would be overkill for this scenario~~
       - ~~For scenarios with less slack, a backtracking algorithm could also become too time-consuming due to combinatorial explosion~~
  - Post fix that ignored first-graders:
    - Extracted data from application:
      - Specialization 1:
        - Course Code: MAT101, Hours per week:6, Students Total: 117, Students Grade 9: 100, Students Grade 10: 17, Students Grade 11: 0, Students Grade 12: 0
        - Course Code: MAT201, Hours per week:6, Students Total: 113, Students Grade 9: 0, Students Grade 10: 83, Students Grade 11: 26, Students Grade 12: 4
        - Course Code: MAT301, Hours per week:6, Students Total: 76, Students Grade 9: 0, Students Grade 10: 0, Students Grade 11: 0, Students Grade 12: 76 
      - Specialization 2:
        - Course Code: ENG101, Hours per week:5, Students Total: 100, Students Grade 9: 100, Students Grade 10: 0, Students Grade 11: 0, Students Grade 12: 0 
        - Course Code: ENG401, Hours per week:5, Students Total: 38, Students Grade 9: 0, Students Grade 10: 0, Students Grade 11: 0, Students Grade 12: 38
      - Specialization 3:
        - Course Code: SCI101, Hours per week:6, Students Total: 116, Students Grade 9: 100, Students Grade 10: 16, Students Grade 11: 0, Students Grade 12: 0 
        - Course Code: SCI201, Hours per week:6, Students Total: 123, Students Grade 9: 0, Students Grade 10: 84, Students Grade 11: 29, Students Grade 12: 10
      - Specialization 4:
        - Course Code: SOC101, Hours per week:4, Students Total: 200, Students Grade 9: 100, Students Grade 10: 100, Students Grade 11: 0, Students Grade 12: 0 
        - Course Code: SOC301, Hours per week:4, Students Total: 100, Students Grade 9: 0, Students Grade 10: 0, Students Grade 11: 0, Students Grade 12: 100
    - Core courses per specialization:
      - Specialization 1 - Mathematics ⚠️ OVERLOADED
        - Total Students: 306 (117 + 113 + 76)
        - Hours per Week: 6
        - Sections Needed: 31 sections
        - Teaching Hours Required: 186 hours/week
        - Teacher Capacity: 160 hours/week (8 teachers)
        - Pressure Ratio: 1.16 (116%) ⚠️
        - Shortage: -26 hours/week (~3 additional teacher needed)
      - Specialization 2 - English ✅
        - Total Students: 138 (100 + 38)
        - Hours per Week: 5
        - Sections Needed: 14 sections
        - Teaching Hours Required: 70 hours/week
        - Teacher Capacity: 160 hours/week (8 teachers)
        - Pressure Ratio: 0.44 (44%)
        - Surplus: +90 hours/week
      - Specialization 3 - Science ✅
        - Total Students: 239 (116 + 123)
        - Hours per Week: 6
        - Sections Needed: 24 sections
        - Teaching Hours Required: 144 hours/week
        - Teacher Capacity: 200 hours/week (10 teachers)
        - Pressure Ratio: 0.72 (72%)
        - Surplus: +56 hours/week 
      - Specialization 4 - Social Studies ⚠️ AT LIMIT
        - Total Students: 300 (200 + 100)
        - Hours per Week: 4
        - Sections Needed: 30 sections
        - Teaching Hours Required: 120 hours/week
        - Teacher Capacity: 120 hours/week (6 teachers)
        - Pressure Ratio: 1.00 (100%) ⚠️
        - Status: Exactly at capacity
    - This means that it is impossible to allow all possible students to have access to their core courses.
- Strategy for scheduling:
  - Schedules all students on maximum grade level. If not possible, fails.
  - Schedules all possible remaining students on best-effort basis. If not possible, logs warning, but continues.
  - Schedules elective courses in a round-robin manner with no students. Will not force students into electives.
- For simplicity's sake, no master Schedule object has been created
  - Although it could be useful in scenarios where drafts and reviews may be necessary, it would have been a completely out of scope choice for this MVP
