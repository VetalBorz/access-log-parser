import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.Map;

public class Main {
    public static void main(String[] args) {
        String filePath = args.length > 0 ? args[0] : "access.log";

        File file = new File(filePath);
        if (!file.exists()) {
            System.out.println("Файл не найден: " + filePath);
            return;
        }

        System.out.println("=".repeat(70));
        System.out.println("АНАЛИЗ ЛОГ-ФАЙЛА С ИСПОЛЬЗОВАНИЕМ STREAM API");
        System.out.println("=".repeat(70));
        System.out.println("Файл: " + filePath);
        System.out.println("Размер: " + file.length() + " байт");

        Statistics statistics = new Statistics();

        long startTime = System.currentTimeMillis();

        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String line;
            int lineCount = 0;

            System.out.println("Чтение и анализ файла...");

            while ((line = reader.readLine()) != null) {
                lineCount++;

                if (line.trim().isEmpty()) {
                    continue;
                }

                LogEntry entry = new LogEntry(line);

                statistics.addEntry(entry);

                if (lineCount % 5000 == 0) {
                    System.out.printf("Обработано строк: %,d%n", lineCount);
                }
            }

            long endTime = System.currentTimeMillis();
            long processingTime = endTime - startTime;

            System.out.println(" " + "=".repeat(70));
            System.out.println("АНАЛИЗ ЗАВЕРШЕН");
            System.out.println("=".repeat(70));
            System.out.printf("Время обработки: %,d мс%n", processingTime);
            System.out.printf("Скорость обработки: %.2f строк/сек%n",
                    lineCount / (processingTime / 1000.0));

            statistics.printStatistics();

            printAdditionalReports(statistics);

        } catch (Exception e) {
            System.out.println("Ошибка: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private static void printAdditionalReports(Statistics stats) {
        System.out.println(" " + "=".repeat(70));
        System.out.println("ДОПОЛНИТЕЛЬНЫЕ ОТЧЕТЫ");
        System.out.println("=".repeat(70));

        System.out.println("ОТЧЕТ ПО ЭФФЕКТИВНОСТИ САЙТА:");

        double avgVisitsPerHour = stats.getAverageVisitsPerHour();
        double avgErrorsPerHour = stats.getAverageErrorRequestsPerHour();
        double avgVisitsPerUser = stats.getAverageVisitsPerUser();

        System.out.printf("Средняя посещаемость в час: %.2f%n", avgVisitsPerHour);
        System.out.printf("Среднее количество ошибок в час: %.2f%n", avgErrorsPerHour);
        System.out.printf("Средняя активность пользователя: %.2f запросов%n", avgVisitsPerUser);

        System.out.println("ОЦЕНКА ЭФФЕКТИВНОСТИ:");
        if (avgVisitsPerHour > 100) {
            System.out.println("Высокая посещаемость сайта");
        } else if (avgVisitsPerHour > 10) {
            System.out.println("Средняя посещаемость сайта");
        } else {
            System.out.println("Низкая посещаемость сайта");
        }

        if (avgErrorsPerHour < 1) {
            System.out.println("Низкий уровень ошибок");
        } else if (avgErrorsPerHour < 5) {
            System.out.println("Средний уровень ошибок");
        } else {
            System.out.println("Высокий уровень ошибок - требуется проверка");
        }

        if (avgVisitsPerUser > 3) {
            System.out.println("Пользователи активно взаимодействуют с сайтом");
        } else if (avgVisitsPerUser > 1) {
            System.out.println("Средняя активность пользователей");
        } else {
            System.out.println("Низкая вовлеченность пользователей");
        }

        double botPercentage = stats.getBotPercentage();
        System.out.println("СТАТИСТИКА БОТОВ:");
        System.out.printf("Процент ботов: %.2f%%%n", botPercentage);
        if (botPercentage > 50) {
            System.out.println("Высокий процент ботов - возможно, требуется защита от сканирования");
        } else if (botPercentage > 20) {
            System.out.println("Заметное присутствие ботов");
        } else {
            System.out.println("Нормальный уровень активности ботов");
        }

        System.out.println("ТОП-10 САМЫХ ПОПУЛЯРНЫХ СТРАНИЦ:");
        Map<String, Long> popularPages = stats.getPopularPages(10);
        if (!popularPages.isEmpty()) {
            int rank = 1;
            for (Map.Entry<String, Long> entry : popularPages.entrySet()) {
                System.out.printf("%2d. %-40s %,d посещений%n",
                        rank++, entry.getKey(), entry.getValue());
            }
        }

        System.out.println("=".repeat(70));
    }
}