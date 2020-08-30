FROM java:8
ADD target/*.jar app.jar
VOLUME /tmp
EXPOSE 80
ENTRYPOINT ["java","-jar","app.jar"]