FROM eclipse-temurin:21-jdk-alpine AS build
WORKDIR /workspace
COPY .mvn .mvn
COPY mvnw pom.xml ./
RUN ./mvnw -B dependency:go-offline
COPY src src
RUN ./mvnw -B -DskipTests package

FROM eclipse-temurin:21-jre-alpine
VOLUME /tmp
COPY --from=build /workspace/target/products-0.0.1-SNAPSHOT.jar /products.jar
EXPOSE 8080
ENTRYPOINT ["java","-jar","/products.jar"]
