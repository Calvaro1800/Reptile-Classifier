FROM eclipse-temurin:21-jdk as build
WORKDIR /app
COPY . .
RUN ./mvnw clean package -DskipTests

FROM eclipse-temurin:21-jre
WORKDIR /app

# Copie le JAR
COPY --from=build /app/target/*.jar app.jar

# ✅ Copie le modèle DJL manuellement
COPY src/main/resources/models /app/models

EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
