FROM eclipse-temurin:17-jdk

WORKDIR /app

COPY . .

RUN chmod +x mvnw 2>/dev/null || true
RUN ./mvnw clean package -DskipTests || mvn clean package -DskipTests

EXPOSE 8081

CMD ["java", "-jar", "target/medbot-no-db-1.0.0.jar"]
