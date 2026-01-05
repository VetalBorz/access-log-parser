import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;

public class Main {
    public static void main(String[] args) {
        String filePath = "access.log";
        File file = new File(filePath);

        if (!file.exists()) {
            System.out.println("Файл не найден: " + filePath);
            System.out.println("Создайте тестовый файл или укажите путь к существующему лог-файлу.");
            return;
        }

        System.out.println("=".repeat(80));
        System.out.println("АНАЛИЗ ЛОГ-ФАЙЛА С ПОМОЩЬЮ STREAM API");
        System.out.println("=".repeat(80));
        System.out.println("Файл: " + filePath);
        System.out.println("Размер: " + file.length() + " байт");

        Statistics stats = new Statistics();
        long startTime = System.currentTimeMillis();
        int lineCount = 0;

        try (BufferedReader reader = new BufferedReader(new FileReader(filePath))) {
            String line;

            System.out.println("Загрузка и анализ данных...");

            while ((line = reader.readLine()) != null) {
                lineCount++;

                if (line.trim().isEmpty()) {
                    continue;
                }

                LogEntry entry = new LogEntry(line);
                stats.addEntry(entry);

                if (lineCount % 5000 == 0) {
                    System.out.printf("Обработано строк: %,d", lineCount);
                }
            }

            long endTime = System.currentTimeMillis();
            long processingTime = endTime - startTime;

            System.out.printf("Загрузка завершена. Обработано строк: %,d%n", lineCount);
            System.out.printf("Время обработки: %,d мс%n", processingTime);

            if (lineCount > 0) {
                System.out.printf("Скорость обработки: %.2f строк/сек%n",
                        lineCount / (processingTime / 1000.0));
            }

            printResults(stats);

        } catch (Exception e) {
            System.out.println("Ошибка при обработке файла: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private static void printResults(Statistics stats) {
        System.out.println(" " + "=".repeat(80));
        System.out.println("РЕЗУЛЬТАТЫ АНАЛИЗА");
        System.out.println("=".repeat(80));

        System.out.println("КЛЮЧЕВЫЕ МЕТРИКИ:");
        System.out.printf("Всего запросов: %,d%n", stats.getEntryCount());
        System.out.printf("Запросов от реальных пользователей: %,d%n", stats.getNonBotRequestsCount());
        System.out.printf("Запросов от ботов: %,d%n",
                stats.getEntryCount() - stats.getNonBotRequestsCount());
        System.out.printf("Процент ботов: %.2f%%%n", stats.getBotPercentage());

        System.out.println("МЕТРИКИ STREAM API #2:");
        System.out.printf("Пиковая посещаемость: %d запросов/секунду%n",
                stats.getPeakVisitsPerSecond());
        System.out.printf("Максимальная активность пользователя: %d запросов%n",
                stats.getMaxVisitsBySingleUser());
        System.out.printf("Источников трафика (referer домены): %,d%n",
                stats.getRefererDomainsCount());

        System.out.println("МЕТРИКИ STREAM API #1:");
        System.out.printf("Средняя посещаемость в час: %.2f запросов/час%n",
                stats.getAverageVisitsPerHour());
        System.out.printf("Среднее количество ошибок в час: %.2f ошибок/час%n",
                stats.getAverageErrorRequestsPerHour());
        System.out.printf("Средняя активность пользователя: %.2f запросов/пользователь%n",
                stats.getAverageVisitsPerUser());

        System.out.println("ДОПОЛНИТЕЛЬНАЯ ИНФОРМАЦИЯ:");
        System.out.printf("Уникальных пользователей: %,d%n", stats.getUniqueNonBotUsersCount());
        System.out.printf("Существующих страниц (200): %,d%n", stats.getExistingPagesCount());
        System.out.printf("Несуществующих страниц (404): %,d%n", stats.getNotFoundPagesCount());
        System.out.printf("Ошибочных запросов (4xx/5xx): %,d%n", stats.getErrorRequestsCount());

        if (stats.getPeakVisitsTime() != null) {
            System.out.printf("Время пиковой нагрузки: %s%n", stats.getPeakVisitsTime());
        }

        String mostActiveIP = stats.getMostActiveUserIP();
        if (mostActiveIP != null) {
            System.out.printf("Самый активный пользователь: %s (%d запросов)%n",
                    mostActiveIP, stats.getMaxVisitsBySingleUser());
        }

        System.out.println("РЕКОМЕНДАЦИИ:");

        if (stats.getBotPercentage() > 30) {
            System.out.println("Высокий процент ботов - рассмотрите внедрение защиты от ботов");
        }

        if (stats.getPeakVisitsPerSecond() > 100) {
            System.out.println("Высокая пиковая нагрузка - рассмотрите масштабирование сервера");
        }

        if (stats.getAverageErrorRequestsPerHour() > 10) {
            System.out.println("Высокий уровень ошибок - проверьте стабильность работы сайта");
        }

        if (stats.getRefererDomainsCount() < 3) {
            System.out.println("Мало источников трафика - улучшите SEO и внешние ссылки");
        }

        System.out.println(" " + "=".repeat(80));
        System.out.println("АНАЛИЗ ЗАВЕРШЕН");
        System.out.println("=".repeat(80));
    }
}