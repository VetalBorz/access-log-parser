import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;

public class Main {
    public static void main(String[] args) {
        String filePath = "access.log";

        File file = new File(filePath);
        if (!file.exists()) {
            System.out.println("Файл не найден: " + filePath);
            return;
        }

        Statistics statistics = new Statistics();

        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String line;
            int lineCount = 0;

            System.out.println("Чтение файла...");

            while ((line = reader.readLine()) != null) {
                lineCount++;

                if (line.trim().isEmpty()) {
                    continue;
                }

                LogEntry entry = new LogEntry(line);

                statistics.addEntry(entry);

                if (lineCount % 100 == 0) {
                    System.out.println("Обработано строк: " + lineCount);
                }
            }

            System.out.println("\nФайл прочитан. Всего строк: " + lineCount);

            statistics.printStatistics();

            System.out.println("\nПервые 5 записей для проверки:");
            printFirstEntries(filePath, 5);

        } catch (Exception e) {
            System.out.println("Ошибка: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private static void printFirstEntries(String filePath, int count) {
        try (BufferedReader reader = new BufferedReader(new FileReader(filePath))) {
            String line;
            int printed = 0;

            while ((line = reader.readLine()) != null && printed < count) {
                if (!line.trim().isEmpty()) {
                    LogEntry entry = new LogEntry(line);
                    System.out.println((printed + 1) + ". " + entry);
                    printed++;
                }
            }
        } catch (Exception e) {
            System.out.println("Не удалось прочитать записи: " + e.getMessage());
        }
    }
}