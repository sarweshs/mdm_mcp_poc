# Build Maven projects
mvn clean package -DskipTests

# Start all services
docker-compose up --build