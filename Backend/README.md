# Kill Bill 2 Backend
The Spring-based HTTP server plus UDP game server for Kill Bill 2.

## Compilation
The Gradle build system is used by default. If you prefer Maven, a `pom.xml` is included in the demo directory.
```sh
# Gradle (recommended)
./gradlew run

# Maven
cd demo
mvn package
java -jar target/demo*.jar
```