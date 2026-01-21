# Dockerfile (builds FileNode image)
FROM eclipse-temurin:21-jdk-jammy AS build
WORKDIR /app
COPY mvnw .
COPY .mvn .mvn
COPY pom.xml .
COPY src src
RUN chmod +x mvnw
RUN ./mvnw clean package -DskipTests

FROM eclipse-temurin:21-jdk-jammy
WORKDIR /app
COPY --from=build /app/target/FileNode-0.0.1-SNAPSHOT.jar filenode.jar
RUN mkdir -p /app/storage
EXPOSE 8090
ENV SERVER_PORT=8090
ENV STORAGE_LOCATION=/app/storage
ENTRYPOINT ["java", "-jar", "filenode.jar"]
