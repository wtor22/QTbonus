package com.quartztop.bonus.controllers.api;

import jakarta.servlet.http.HttpSession;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.*;

@Slf4j
@RestController
@RequestMapping("/api/scan")
public class FileController {

    // Максимальный размер файла (5 MB)
    private static final long MAX_FILE_SIZE = 5 * 1024 * 1024;

    // Допустимые типы файлов
    private static final List<String> VALID_FILE_TYPES = Arrays.asList("image/jpeg", "image/png");


    @PostMapping("/upload")
    public ResponseEntity<Map<String, String>> uploadFile(@RequestParam("file") MultipartFile file, HttpSession session) {

        // Карта для ответа
        Map<String, String> response = new HashMap<>();

        // Проверка типа файла
        String contentType = file.getContentType();
        if (!VALID_FILE_TYPES.contains(contentType)) {
            response.put("status", "Недопустимый тип файла. Разрешены только JPEG и PNG.");
            return ResponseEntity.status(HttpStatus.UNSUPPORTED_MEDIA_TYPE)
                    .body(response);
        }

        // Проверка размера файла
        if (file.getSize() > MAX_FILE_SIZE) {
            response.put("status", "Файл превышает максимальный размер 5Mb");
            return ResponseEntity.status(HttpStatus.PAYLOAD_TOO_LARGE).body(response);
        }

        // Сохраните файл на диск временно
        String filePath = saveFile(file, session);

        response.put("status", "safe");
        response.put("imageUrl",filePath);

        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/delete")
    public ResponseEntity<?> deleteFile(@RequestBody Map<String, String> request, HttpSession session) {
        String filePath = request.get("filePath");

        log.info("DELETE FILE " + filePath);

        try {
            deleteFile(filePath, session); // Используем твой метод для удаления файла
            return ResponseEntity.ok(Map.of("message", "Файл успешно удалён"));
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Ошибка при удалении файла: " + e.getMessage()));
        }
    }

    private String saveFile(MultipartFile file, HttpSession session) {
        try {
            // Создаем временную директорию, если она не существует
            Path tempDir = Paths.get("temp/uploads");
            if (!Files.exists(tempDir)) {
                Files.createDirectories(tempDir);
            }
            // Генерируем уникальное имя файла
            String fileName = System.currentTimeMillis() + "_" + file.getOriginalFilename();
            Path filePath = tempDir.resolve(fileName);

            // Сохраняем файл
            try (InputStream inputStream = file.getInputStream()) {
                Files.copy(inputStream, filePath, StandardCopyOption.REPLACE_EXISTING);
            }

            // Получаем список загруженных файлов из сессии или создаем новый
            List<String> uploadedFiles = (List<String>) session.getAttribute("uploadedFiles");
            if (uploadedFiles == null) {
                uploadedFiles = new ArrayList<>();
            }
            uploadedFiles.add(filePath.toString());

            // Сохраняем обновленный список в сессии
            session.setAttribute("uploadedFiles", uploadedFiles);

            return filePath.toString(); // Возвращаем путь к файлу
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("Ошибка при сохранении файла: " + e.getMessage());
        }
    }

    private void deleteFile(String filePath, HttpSession session) {
        try {
            Path pathToDelete = Paths.get(filePath);
            Files.deleteIfExists(pathToDelete); // Удаляем файл, если он существует

            // Получаем список загруженных файлов из сессии или создаем новый
            List<String> uploadedFiles = (List<String>) session.getAttribute("uploadedFiles");

            if(uploadedFiles != null) {
                uploadedFiles.remove(filePath);
            }

        } catch (Exception e) {
            e.printStackTrace();
            System.err.println("Ошибка при удалении файла: " + e.getMessage());
        }
    }

}






