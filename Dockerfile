# ===== 1단계: 빌드 환경 (Builder Stage) =====
# 무거운 요리 도구(JDK)를 가져와서 'builder'라는 별명을 붙여줍니다.
FROM amazoncorretto:17 AS builder
WORKDIR /build

# 1. [레이어 캐싱 핵심] 의존성(라이브러리) 관련 파일만 먼저 복사합니다.
COPY gradlew .
COPY gradle gradle
COPY build.gradle settings.gradle ./

# gradlew 실행 권한 부여
RUN chmod +x ./gradlew

# 2. [레이어 캐싱 핵심] 소스 코드를 넣기 전에 라이브러리만 미리 다운로드합니다.
# (이후에 소스 코드만 고치면 이 단계는 1초 만에 패스됩니다!)
RUN ./gradlew dependencies --no-daemon

# 3. 이제 진짜 소스 코드를 복사하고 jar 파일로 빌드합니다.
COPY src src
RUN ./gradlew bootJar -x test --no-daemon


# ===== 2단계: 실행 환경 (Runtime Stage) =====
# 가벼운 서빙용 쟁반(Alpine)을 새로 가져옵니다.
FROM amazoncorretto:17-alpine
WORKDIR /app

# 환경 변수 설정
ENV JVM_OPTS=""

# 4. [멀티 스테이지 핵심] 1단계(builder)에서 완성된 통뼈 jar 파일만 쏙 가져옵니다.
# 나머지 무거운 JDK와 소스 코드 찌꺼기들은 이 최종 이미지에 포함되지 않습니다.
COPY --from=builder /build/build/libs/*-SNAPSHOT.jar app.jar

# 5. 포트 노출 및 실행
EXPOSE 8080
ENTRYPOINT ["sh", "-c", "java $JVM_OPTS -Dspring.profiles.active=prod -jar app.jar"]