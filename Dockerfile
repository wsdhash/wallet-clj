FROM clojure:openjdk-11-lein
WORKDIR /app
COPY . /app
RUN lein uberjar
EXPOSE 8080
CMD ["java", "-jar", "target/my-app-standalone.jar"]