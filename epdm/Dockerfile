# Use official Tomcat 9 with Java 11 support
FROM tomcat:9.0.82-jdk11

# Set the working directory inside the container
WORKDIR /usr/local/tomcat/webapps/

# Copy the built JAR file into the container
COPY target/*.war epdm-0.0.1-SNAPSHOT.war

# Expose the application port (Make sure this matches your Spring Boot server.port)
EXPOSE 8090

# Start Tomcat properly
CMD ["catalina.sh", "run"]

# Run the application
#ENTRYPOINT ["java", "-war", "epdm-0.0.1-SNAPSHOT.war"]
