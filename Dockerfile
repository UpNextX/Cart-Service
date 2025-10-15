FROM openjdk:17-slim
WORKDIR /app
RUN mkdir -p /app/libs

COPY ../libs/shared-library-1.0.0.jar /app/libs
COPY Cart-Service/target/cart-service-1.0.0.jar app.jar

EXPOSE 8083
ENTRYPOINT ["java", "-cp", "cart-service-1.0.0.jar:/app/libs/*", "org.upnext.cartservice.CartServiceApplication"]
