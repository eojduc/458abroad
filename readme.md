
--install postgres with homebrew

# How to Run

- Test API and services
  - `./gradlew clean test` 

- Reset Database (delete all data and create empty database 'abroad')
  - `./gradlew resetDb`

- Fresh Build (reset database and fill it with sample data) and run
  - `./gradlew resetDb clean bootJar`
  - `java -jar build/libs/abroad-0.0.1-SNAPSHOT.jar --fillDb=true`
  - Can also run `java -jar build/libs/abroad-0.0.1-SNAPSHOT.jar --fillDb=true --resetDb=true` to reset database and fill it with sample data.
    - No gradle involved, but it requires the "abroad" database to be created and running. 
  - Sample data can be found in `src/main/resources/data/`. The code responsible
        for reading CSV and saving to the database is in `com.abroad.services.DataInitializerService`.

- Normal Build and run (without modifying existing database)
  - `./gradlew clean bootJar`
  - `java -jar build/libs/abroad-0.0.1-SNAPSHOT.jar`

# Deployment Guide

For detailed deployment instructions, refer to the [Deployment Guide](./DEPLOYMENT.md).