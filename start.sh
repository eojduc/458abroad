#!/bin/bash

# Ensure gradlew is executable
chmod +x ./gradlew

# Run Gradle tasks
./gradlew fillDb bootJar

# Run the generated JAR file
java -jar build/libs/abroad-0.0.1-SNAPSHOT.jar
./gradlew fillDb bootJar

java -jar build/libs/abroad-0.0.1-SNAPSHOT.jar
