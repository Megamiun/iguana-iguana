CREATE TABLE IF NOT EXISTS course_sections (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    course_id INTEGER NOT NULL REFERENCES courses(id),
    teacher_id INTEGER NOT NULL REFERENCES teachers(id),
    classroom_id INTEGER NOT NULL REFERENCES classrooms(id),
    semester_id INTEGER NOT NULL REFERENCES semesters(id),
    section_number INTEGER NOT NULL,
    UNIQUE(course_id, semester_id, section_number)
);

CREATE INDEX idx_course_sections_course_id ON course_sections(course_id);
CREATE INDEX idx_course_sections_teacher_id ON course_sections(teacher_id);
CREATE INDEX idx_course_sections_classroom_id ON course_sections(classroom_id);
CREATE INDEX idx_course_sections_semester_id ON course_sections(semester_id);

CREATE TABLE IF NOT EXISTS course_section_students (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    student_id INTEGER NOT NULL REFERENCES students(id),
    course_section_id INTEGER NOT NULL REFERENCES course_sections(id) ON DELETE CASCADE,
    enrolled_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    UNIQUE(student_id, course_section_id)
);

CREATE INDEX idx_course_section_students_student_id ON course_section_students(student_id);
CREATE INDEX idx_course_section_students_course_section_id ON course_section_students(course_section_id);
