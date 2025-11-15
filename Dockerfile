# Multi-stage build for a Spring Boot application using layered JARs
FROM eclipse-temurin:21-jdk-jammy AS build

# build arg for the target JAR file name
ARG JAR_FILE_NAME=${JAR_FILE_NAME:-app.jar}

ARG PORT=${SERVER_PORT:-8080}

# Define build arguments for GitHub Package Registry credentials
ARG GPR_USER=${GPR_USER:-webcane}
ARG GPR_KEY

# Define build arguments for user and group
ARG USER_NAME=${DOCKER_USER_NAME:-spring}
ARG USER_ID=1000
ARG GROUP_NAME=${USER_NAME:-spring}
ARG GROUP_ID=1000

# Set default Java options if not provided
ENV JAVA_OPTS=${JAVA_OPTS:-"-Xmx384m -Xms128m"}

# Define build arguments for proxy settings
ENV TGBOT_PROXY_HOSTNAME=${TGBOT_PROXY_HOSTNAME:-}
ENV TGBOT_PROXY_PORT=${TGBOT_PROXY_PORT:-42567}
ENV TGBOT_PROXY_USERNAME=${TGBOT_PROXY_USERNAME:-}
ENV TGBOT_PROXY_PASSWORD=${TGBOT_PROXY_PASSWORD:-}


# Set up the working directory
WORKDIR /app

# Copy gradle
COPY gradle gradle/
COPY gradlew .

# Copy the necessary project files required for building
COPY build.gradle .
COPY settings.gradle .
COPY gradle.properties .

# Copy the source code
COPY src src/

# Preload dependencies to leverage Docker layer caching
RUN ./gradlew dependencies -Pgpr.user=${GPR_USER} -Pgpr.key=${GPR_KEY} --no-daemon

# Build the Spring Boot application and create a layered JAR
RUN ./gradlew bootJar --no-daemon

# Extract the layers from the built JAR
RUN java -Djarmode=tools -jar build/libs/${JAR_FILE_NAME} extract --layers --launcher --destination extracted || exit 1

# Final stage: create the runtime image
FROM eclipse-temurin:21-jre-jammy

# Create the group and user using the build arguments
RUN groupadd ${GROUP_NAME} --gid ${GROUP_ID} || echo "Group already exists" && \
    useradd -m --uid ${USER_ID} -g ${GROUP_NAME} ${USER_NAME} || echo "User already exists"

# Set up the working directory
WORKDIR /app
RUN chown ${USER_NAME}:${GROUP_NAME} /app

# Switch to the non-root user
USER ${USER_NAME}

# Copy the extracted layers from the build stage
# Copy dependencies, spring-boot-loader, and application layers
# Laers will be cached separately by Docker
COPY --from=build --chown=${USER_NAME}:${GROUP_NAME} /app/extracted/dependencies/ ./
COPY --from=build --chown=${USER_NAME}:${GROUP_NAME} /app/extracted/spring-boot-loader/ ./
COPY --from=build --chown=${USER_NAME}:${GROUP_NAME} /app/extracted/application/ ./

# Expose the port your Spring Boot app listens on (e.g., 8080)
EXPOSE ${PORT} 42567

# Run the Spring Boot application with java options
# Proxy settings are passed as system properties
ENTRYPOINT ["sh", "-c", "exec java $JAVA_OPTS \
             -Dhttp.proxyHost=\"$TGBOT_PROXY_HOSTNAME\" \
             -Dhttps.proxyHost=\"$TGBOT_PROXY_HOSTNAME\" \
             -Dhttp.proxyPort=\"$TGBOT_PROXY_PORT\" \
             -Dhttps.proxyPort=\"$TGBOT_PROXY_PORT\" \
             -Dhttp.proxyUser=\"$TGBOT_PROXY_USERNAME\" \
             -Dhttps.proxyUser=\"$TGBOT_PROXY_USERNAME\" \
             -Dhttp.proxyPassword=\"$TGBOT_PROXY_PASSWORD\" \
             -Dhttps.proxyPassword=\"$TGBOT_PROXY_PASSWORD\" \
             org.springframework.boot.loader.launch.JarLauncher"]
