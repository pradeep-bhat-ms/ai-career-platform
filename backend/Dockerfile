FROM eclipse-temurin:21-jdk AS build

WORKDIR /app

# Copy only what's needed to resolve dependencies first, so this layer is
# cached and skipped on rebuilds unless pom.xml itself changes.
COPY mvnw .
COPY .mvn .mvn
COPY pom.xml .
RUN chmod +x mvnw
RUN ./mvnw dependency:go-offline -B

# Now copy the actual source and build — only this layer re-runs when you
# change Java code.
COPY src src
RUN ./mvnw clean package -DskipTests -B

FROM eclipse-temurin:21-jre

WORKDIR /app

COPY --from=build /app/target/*.jar app.jar

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "app.jar"]