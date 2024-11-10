# Используем базовый образ с OpenJDK 17
FROM openjdk:17-jdk-slim

# Устанавливаем рабочую директорию внутри контейнера
WORKDIR /app

# Копируем скомпилированный jar-файл в контейнер
COPY target/bonus-0.0.1-SNAPSHOT.jar /app/bonus-0.0.1-SNAPSHOT.jar

# Открываем порт, если приложение слушает определенный порт (например, 8080)
EXPOSE 8081

# Запуск jar-файла при старте контейнера
ENTRYPOINT ["java", "-Xmx512m", "-Xms256m", "-jar", "/app/bonus-0.0.1-SNAPSHOT.jar"]