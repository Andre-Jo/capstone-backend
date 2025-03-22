-- Users 테이블에 재학생 정보 삽입
INSERT INTO Users (email, password, nickname, school, department, student_year, user_type, profile_image, is_school_verified)
VALUES ('student@example.com', 'studentPassword', 'studentUser', 'StudentSchool', 'ComputerScience', 2023, 'STUDENT', 'student.png', true);

-- 방금 삽입한 Users 레코드의 id가 2라고 가정하고, student 테이블에 재학생 추가 정보 삽입
INSERT INTO student (id, is_subscribed, subscription_start_date, subscription_end_date)
VALUES (1, false, NULL, NULL);

INSERT INTO Posts (user_id, is_anonymous, title, content, view_count, like_count, created_at, updated_at)
VALUES (1, false, '제목1', '내용1', 0, 0, NOW(), NOW());