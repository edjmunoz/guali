# ---------- Etapa 1: compilar con Maven + JDK 25 ----------
FROM maven:3.9-eclipse-temurin-25 AS build
WORKDIR /app
COPY pom.xml .
RUN mvn -B -q dependency:go-offline || true
COPY src ./src
RUN mvn -B -q package -DskipTests

# ---------- Etapa 2: imagen final (solo JRE) ----------
FROM eclipse-temurin:25-jre
RUN useradd --system --uid 1001 guali
WORKDIR /app
COPY --from=build /app/target/guali-web-*.jar app.jar
USER guali
ENV TZ=America/Guatemala
EXPOSE 8080
ENTRYPOINT ["java", "-XX:MaxRAMPercentage=70", "-jar", "app.jar"]
