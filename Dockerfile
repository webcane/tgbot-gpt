# Multi-stage build для создания легкого и кэшируемого образа
FROM eclipse-temurin:21-jdk-jammy AS build

# Объявляем build argument для имени JAR-файла
ARG JAR_FILE_NAME=${JAR_FILE_NAME:-app.jar}

# Define build arguments for user and group
ARG USER_NAME=${USER_NAME:-spring}
ARG USER_ID=1000
ARG GROUP_NAME=${USER_NAME:-spring}
ARG GROUP_ID=1000


# Устанавливаем Gradle, если вы собираете проект внутри контейнера Dockerfile
# Если вы предпочитаете собирать проект на хосте, этот блок можно убрать,
# и просто скопировать готовый JAR в финальный образ.
WORKDIR /app
COPY gradle gradle/
COPY gradlew .
COPY build.gradle .
COPY settings.gradle .
COPY gradle.properties .
COPY src src/

# Предварительная загрузка зависимостей Gradle для кэширования
RUN ./gradlew dependencies --no-daemon

# Сборка приложения в слоеный JAR
RUN ./gradlew bootJar --no-daemon

# Извлечение слоев из слоеного JAR во временную директорию
RUN java -Djarmode=tools -jar build/libs/${JAR_FILE_NAME} extract --layers --launcher --destination extracted || exit 1

# --- Финальный образ ---
FROM eclipse-temurin:21-jre-jammy

# Create the group and user using the build arguments
RUN groupadd ${GROUP_NAME} --gid ${GROUP_ID} || echo "Group already exists" && \
    useradd -m --uid ${USER_ID} -g ${GROUP_NAME} ${USER_NAME} || echo "User already exists"

WORKDIR /app
RUN chown ${USER_NAME}:${GROUP_NAME} /app

USER ${USER_NAME}

# Копируем извлеченные слои в нужные директории в финальном образе
# Эти слои будут кэшироваться Docker'ом независимо
COPY --from=build --chown=spring:spring /app/extracted/dependencies/ ./
# COPY --from=build --chown=spring:spring /app/extracted/snapshot-dependencies/ ./
COPY --from=build --chown=spring:spring /app/extracted/spring-boot-loader/ ./
COPY --from=build --chown=spring:spring /app/extracted/application/ ./

# Expose the port your Spring Boot app listens on (e.g., 8080)
EXPOSE 8080 42567

# Команда для запуска приложения, использующая встроенный загрузчик Spring Boot
ENTRYPOINT ["java", "org.springframework.boot.loader.launch.JarLauncher"]