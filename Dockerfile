# Java 17 경량 이미지
FROM eclipse-temurin:17-jdk-jammy

# 빌드된 JAR 파일을 복사할 변수 지정
ARG JAR_FILE=build/libs/*.jar

# 복사해서 컨테이너 내부에 저장
COPY ${JAR_FILE} app.jar

# (문서용) Spring Boot 기본 포트
EXPOSE 8080

# 컨테이너 실행 시 JAR 실행
ENTRYPOINT ["java", "-jar", "/app.jar"]