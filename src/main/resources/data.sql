-- Users 테이블에 졸업생 정보 삽입
INSERT INTO Users (email, password, nickname, school, department, student_year, user_type, profile_image, is_school_verified)
VALUES ('graduate@example.com', 'studentPassword', 'studentUser', 'StudentSchool', 'ComputerScience', 2023, 'GRADUATE', 'student.png', true);

-- graduates 테이블에 졸업생 추가 정보 삽입
INSERT INTO Graduates (id, is_company_verified, current_company, current_salary, skills)
VALUES (1, true, 'SomeCompany', '5000', 'Java, Spring');