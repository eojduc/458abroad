
--install postgres with homebrew

# How to Run

- Test API and services
  - `./gradlew clean test` 

- Reset Database (delete all data and create empty database 'abroad')
  - `./gradlew resetDb`

- Fresh Build (reset database and fill it with sample data) and run
  - `./gradlew resetDb clean bootJar`
  - `java -jar build/libs/abroad-0.0.1-SNAPSHOT.jar --fillDb=true`
  - Sample data can be found in `src/main/resources/data/`. The code responsible
        for reading CSV and saving to the database is in `com.abroad.services.DataInitializerService`.

- Normal Build and run (without modifying existing database)
  - `./gradlew clean bootJar`
  - `java -jar build/libs/abroad-0.0.1-SNAPSHOT.jar`