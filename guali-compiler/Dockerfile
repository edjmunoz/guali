# ---------- Etapa 1: compilar con Maven + JDK 25 ----------
FROM maven:3.9-eclipse-temurin-25 AS build
WORKDIR /app
COPY pom.xml .
COPY libs ./libs
RUN mvn -B -q dependency:go-offline || true
COPY src ./src
RUN mvn -B -q package -DskipTests

# ---------- Etapa 2: imagen final ----------
# Se usa el JDK completo (no solo JRE) porque guali-dev necesita "javac".
FROM eclipse-temurin:25-jdk
RUN useradd --system --uid 1001 guali
WORKDIR /app
COPY --from=build /app/target/guali-compiler-*.jar app.jar
USER guali
EXPOSE 8081
ENTRYPOINT ["java", "-XX:MaxRAMPercentage=60", "-jar", "app.jar"]
