# docker build -t zeebe-cherry-officepdf:1.0.0 .
#FROM openjdk:17-alpine
#COPY target/zeebe-cherry-officepdf-*-jar-with-dependencies.jar app.jar
#ENTRYPOINT ["java","-jar","/app.jar", "io.camunda.CherryApplication"]

FROM maven:3-openjdk-17 as builder
WORKDIR /usr/src/app
COPY pom.xml pom.xml
RUN mvn dependency:resolve-plugins dependency:resolve package -Dspring-boot.repackage.skip=true -Dmaven.test.skip=true -DskipTests -DskipChecks
COPY src/ src/
RUN mvn package -Dspring-boot.repackage.layers.enabled=true -Dmaven.test.skip=true -DskipTests -DskipChecks
# RUN java -Djarmode=layertools -jar target/*.jar extract

FROM azul/zulu-openjdk-alpine:17-jre-headless
CMD java -Djarmode=layertools -jar *.jar extract
COPY --from=builder /usr/src/app/target/ ./
