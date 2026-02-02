FROM eclipse-temurin:17-jdk
WORKDIR /app

COPY mvnw pom.xml ./
COPY .mvn .mvn
RUN ./mvnw -q -DskipTests dependency:go-offline

COPY src src

EXPOSE 3001
CMD ["./mvnw","spring-boot:run","-Dspring-boot.run.jvmArguments=-Dserver.port=3001"]
