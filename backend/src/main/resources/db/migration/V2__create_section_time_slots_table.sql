CREATE TABLE IF NOT EXISTS course_section_time_slots (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    course_section_id INTEGER NOT NULL REFERENCES course_sections(id),
    weekday TEXT NOT NULL,
    start_hour INTEGER NOT NULL,
    end_hour INTEGER NOT NULL,
    CHECK (weekday IN ('monday', 'tuesday', 'wednesday', 'thursday', 'friday')),
    CHECK (start_hour >= 0 AND start_hour < 24),
    CHECK (end_hour >= 0 AND end_hour <= 24),
    CHECK (start_hour < end_hour)
);

CREATE INDEX idx_course_section_time_slots_course_section_id ON course_section_time_slots(course_section_id);
CREATE INDEX idx_course_section_time_slots_weekday ON course_section_time_slots(weekday);
