FROM gradle:7.5-jdk17-alpine AS builder
RUN mkdir -p /app/KG2PG
COPY . /app/KG2PG
WORKDIR /app/KG2PG
RUN gradle build
RUN gradle shadowJar

FROM builder
RUN mkdir -p /app/data
RUN mkdir -p /app/local
COPY --from=builder /app/KG2PG/build/libs/*-all.jar /app/app.jar

ENTRYPOINT ["java","-jar", "/app/app.jar"]
CMD ["configFile"]
