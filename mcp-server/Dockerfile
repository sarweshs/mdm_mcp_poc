# mcp-server/Dockerfile
FROM eclipse-temurin:17-jdk
WORKDIR /app
COPY target/mcp-server-*-exec.jar app.jar
ENTRYPOINT ["java", "-jar", "app.jar"]