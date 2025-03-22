# 1단계: 빌드 단계 - Gradle Wrapper 사용
FROM gradle:8.1.1-jdk17 AS builder
WORKDIR /app
# gradlew 및 관련 파일 복사
COPY --chown=gradle:gradle gradlew .
COPY --chown=gradle:gradle gradle gradle
# 프로젝트 파일 전체 복사
COPY --chown=gradle:gradle . .
# gradlew에 실행 권한 부여
RUN chmod +x gradlew
# Gradle Wrapper를 사용하여 bootJar 생성 (테스트 건너뜀)
RUN ./gradlew clean bootJar -x test --no-daemon

# 2단계: 실행 단계 - OpenJDK 17 이미지 사용
FROM openjdk:17-jdk-slim
WORKDIR /app
COPY --from=builder /app/build/libs/*.jar app.jar
# 9092 : h2
EXPOSE 8080 9092
ENTRYPOINT ["java", "-jar", "app.jar"]
