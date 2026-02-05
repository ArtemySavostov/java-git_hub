## Git Manager

Git Manager is a Spring Boot 3 web application that provides a UI for browsing and managing local Git repositories per user.  
It stores metadata in MySQL and works with the host file system to create and manage repositories under a configurable base path.

### Tech Stack

- **Backend**: Spring Boot 3, Spring MVC, Spring Data JPA, Spring Security  
- **Frontend**: Thymeleaf, Bootstrap, jsTree, Prism.js  
- **Database**: MySQL 8   
- **Build**: Maven

### Configuration

Main application settings are in `src/main/resources/application.properties`.

Important properties:

- **Database**
  - `spring.datasource.url=jdbc:mysql://localhost:3306/java_db`
  - `spring.datasource.username=...`
  - `spring.datasource.password=...`
- **Repositories root path**
  - ```properties
    repositories.path=${REPOSITORIES_PATH:/app/.git-repositories}
    ```
  - This is the base directory where per-user Git repositories are created.
  - In Docker, this path is typically mounted as a volume.

You can also tune upload limits, logging, and Thymeleaf cache via the same file.

### Running Locally

1. **Start MySQL** and create the database:

   ```sql
   CREATE DATABASE java_db CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
   ```

2. **Configure DB connection** in `src/main/resources/application.properties`:

   ```properties
   spring.datasource.url=jdbc:mysql://localhost:3306/java_db
   spring.datasource.username=YOUR_USER
   spring.datasource.password=YOUR_PASSWORD

   repositories.path=/absolute/path/to/.git-repositories
   spring.thymeleaf.cache=false
   ```

   Make sure the `repositories.path` directory exists and is writable.

3. **Build and run**:

   ```bash
   ./mvnw clean package
   java -jar target/git-manager-0.0.1-SNAPSHOT.jar
   ```

4. Open the app in a browser:

   ```text
   http://localhost:8080
   ```

### Running with Docker

The project includes:

- `Dockerfile` – multi-stage build (Maven + JRE).
- `docker-compose.yml` – runs the app and MySQL together.


1. From the project root, run:

   ```bash
   docker-compose up --build
   ```

2. What `docker-compose` does:

   - Starts **MySQL 8** (`db`) with database `java_db`.
   - Builds and runs the **Spring Boot app** (`app`) on port `8080`.
   - Mounts a local directory:

     ```yaml
     - ./.git-repositories:/app/.git-repositories
     ```

   - Passes environment variables to the app:
     - `SPRING_DATASOURCE_URL`
     - `SPRING_DATASOURCE_USERNAME`
     - `SPRING_DATASOURCE_PASSWORD`
     - `REPOSITORIES_PATH=/app/.git-repositories`

3. Open the app in a browser:

   ```text
   http://localhost:8080
   ```