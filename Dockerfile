# 1. 실행에는 가벼운 Alpine 기반 이미지를 사용하여 최종 용량을 최소화합니다.
FROM amazoncorretto:17-alpine

# 2. 컨테이너 내부의 작업 공간을 설정합니다.
WORKDIR /app

# 3. 환경 변수 설정 (옵션 조정용)
ENV JVM_OPTS=""

# 4. ★핵심★ GitHub Actions 로봇이 미리 빌드해 둔 jar 파일만 쏙 가져옵니다.
# (이름이 길고 복잡하니 app.jar라는 쉬운 이름으로 바꿉니다.)
COPY build/libs/*-SNAPSHOT.jar app.jar

# 5. 컨테이너가 사용할 포트를 노출합니다. (스프링부트 기본 포트)
EXPOSE 8080

# 6. 애플리케이션 실행 명령어 (prod 프로필로 실행)
ENTRYPOINT ["sh", "-c", "java $JVM_OPTS -Dspring.profiles.active=prod -jar app.jar"]