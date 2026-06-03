FROM maven:3.9-eclipse-temurin-21 AS builder
WORKDIR /build
COPY pom.xml .
COPY protocol/ protocol/
COPY backend/ backend/
RUN mvn -N install -DskipTests -q \
 && mvn -f protocol/pom.xml install -DskipTests -q \
 && mvn -f backend/pom.xml package -DskipTests -q

FROM eclipse-temurin:21-jre
WORKDIR /app
COPY --from=builder /build/backend/target/afterfall-backend-*.jar app.jar
EXPOSE 8080 9090
ENTRYPOINT ["java", "-jar", "app.jar"]
