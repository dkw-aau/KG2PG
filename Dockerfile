FROM gradle:8.14.2-jdk17-ubi-minimal AS builder

WORKDIR /app
COPY . .
RUN gradle clean shadowJar --no-daemon

FROM openjdk:17-jre-slim

WORKDIR /app

# Copy the JAR file
COPY --from=builder /app/build/libs/kg2pg.jar kg2pg.jar

# Create default directory structure for user data
RUN mkdir -p data output config

# Copy sample config for user customization (optional)
# COPY --from=builder /app/config.properties ./config.properties.example

ENTRYPOINT ["java", "-jar", "kg2pg.jar"]
