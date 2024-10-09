package com.quartztop.bonus.servises;

import com.quartztop.bonus.orders.Order;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.List;

@Service
@Slf4j
public class FileService {

    @Async  // Указываем, что метод должен выполняться асинхронно
    public void moveFileAsync(List<String> tempFilePaths, Order order) {

        // Генерируем директорию для файлов заявки
        String orderDirPath = "C:\\bonus\\app\\uploads\\orders\\" + order.getId(); // Папка для файлов этой заявки
        File orderDir = new File(orderDirPath);
        if (!orderDir.exists()) {
            orderDir.mkdirs(); // Создаем папку, если она не существует
        }

        // Перемещаем каждый временный файл в папку заявки
        for (String tempFilePath : tempFilePaths) {
            Path tempPath = Paths.get(tempFilePath);
            Path newFilePath = Paths.get(orderDirPath, tempPath.getFileName().toString());

            // Запустите ClamAV для проверки
            boolean result = scanFileWithClamAV(tempFilePath);
            // Если заражение - то удалить
            if (!result) {
                deleteFile(tempFilePath);
                log.warn("File " + tempFilePath + " was infected and deleted.");
                continue;
            }

            try {
                Thread.sleep(1000); // Задержка в 1 секунду
                // Перемещаем файл
                Files.move(tempPath, newFilePath, StandardCopyOption.REPLACE_EXISTING);

                System.out.println("Файл перемещен в: " + newFilePath.toString());
            } catch (IOException e) {
                e.printStackTrace();
                System.err.println("Ошибка при перемещении файла: " + e.getMessage());
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
            // TODO Здесь нужно привязать файл к заявке в базе данных, если нужно
            // order.addFile(newFilePath.toString()); // Например, так можно добавить путь к файлу в заказ
        }
    }

    private boolean scanFileWithClamAV(String filePath) {

        try {
            // Указываем полный путь к clamscan, если он не в PATH
            String clamAVPath = "C:\\bonus\\clamav-1.4.1.win.x64\\clamscan.exe";
            //String clamAVPath = "C:\\Users\\Administrator\\Desktop\\clamav-1.4.1.win.x64\\clamscan.exe";
            // Выполняем команду clamscan
            ProcessBuilder processBuilder = new ProcessBuilder(clamAVPath, filePath);
            processBuilder.redirectErrorStream(true); // Объединяем стандартный и поток ошибок
            Process process = processBuilder.start();

            // Читаем выходной поток
            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            StringBuilder output = new StringBuilder();
            String line;
            boolean isSafe = true; // По умолчанию считаем, что файл безопасен

            while ((line = reader.readLine()) != null) {
                output.append(line).append("\n");
                // Проверяем результат сканирования
                if (line.contains("FOUND")) {
                    isSafe = false; // Если найден вирус, изменяем флаг на false
                }
            }
            // Ждем завершения процесса
            process.waitFor();

            // Возвращаем результат сканирования: true если всё ок, false если найден вирус
            return isSafe;
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("ClamAV Error Process: " + e.getMessage());
        }
    }

    private void deleteFile(String filePath) {
        try {
            Path pathToDelete = Paths.get(filePath);
            Files.deleteIfExists(pathToDelete); // Удаляем файл, если он существует

        } catch (Exception e) {
            e.printStackTrace();
            System.err.println("Ошибка при удалении файла: " + e.getMessage());
        }
    }
}
