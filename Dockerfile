FROM eclipse-temurin:21-jre-alpine
VOLUME /tmp
COPY target/products-0.0.1-SNAPSHOT.jar /products.jar
EXPOSE 8080
ENTRYPOINT ["java","-jar","/products.jar"]