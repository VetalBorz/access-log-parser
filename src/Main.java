import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.Map;
import java.util.Set;

public class Main {
    public static void main(String[] args) {
        // Путь к файлу
        String filePath = args.length > 0 ? args[0] : "access.log";

        // Проверяем файл
        File file = new File(filePath);
        if (!file.exists()) {
            System.out.println("Файл не найден: " + filePath);
            return;
        }

        // Создаем объект статистики
        Statistics statistics = new Statistics();

        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String line;
            int lineCount = 0;

            System.out.println("Чтение файла " + filePath + "...");

            while ((line = reader.readLine()) != null) {
                lineCount++;

                // Пропускаем пустые строки
                if (line.trim().isEmpty()) {
                    continue;
                }

                // Создаем объект LogEntry из строки
                LogEntry entry = new LogEntry(line);

                // Добавляем в статистику
                statistics.addEntry(entry);

                // Выводим прогресс каждые 1000 строк
                if (lineCount % 1000 == 0) {
                    System.out.println("Обработано строк: " + lineCount);
                }
            }

            System.out.println("\n" + "=".repeat(60));
            System.out.println("АНАЛИЗ ЗАВЕРШЕН");
            System.out.println("=".repeat(60));
            System.out.println("Всего строк в файле: " + lineCount);
            System.out.println("Обработано записей: " + statistics.getEntryCount());

            // Выводим полную статистику
            statistics.printStatistics();

            // Детальная статистика по новым методам
            System.out.println("\n" + "=".repeat(60));
            System.out.println("ДЕТАЛЬНАЯ СТАТИСТИКА COLLECTIONS");
            System.out.println("=".repeat(60));

            // 1. Несуществующие страницы
            System.out.println("\n1. НЕСУЩЕСТВУЮЩИЕ СТРАНИЦЫ (404)");
            Set<String> notFoundPages = statistics.getNotFoundPages();
            System.out.println("Всего несуществующих страниц: " + notFoundPages.size());

            if (!notFoundPages.isEmpty()) {
                System.out.println("Список несуществующих страниц:");
                int count = 1;
                for (String page : notFoundPages) {
                    System.out.printf("%3d. %s%n", count++, page);
                    if (count > 10 && notFoundPages.size() > 10) {
                        System.out.printf("... и еще %d страниц%n", notFoundPages.size() - 10);
                        break;
                    }
                }

                // Наиболее часто запрашиваемые несуществующие страницы
                System.out.println("\nРекомендация: проверьте эти URL на наличие опечаток или обновите ссылки");
            }

            // 2. Статистика браузеров
            System.out.println("\n2. СТАТИСТИКА БРАУЗЕРОВ");
            Map<String, Double> browserStats = statistics.getBrowserStatistics();

            if (!browserStats.isEmpty()) {
                System.out.println("Распределение браузеров среди пользователей:");
                System.out.println("Браузер         Доля      Процент");
                System.out.println("--------------- --------- ---------");

                // Сортируем по убыванию доли для наглядности
                browserStats.entrySet().stream()
                        .sorted((a, b) -> Double.compare(b.getValue(), a.getValue()))
                        .forEach(entry -> {
                            String browser = entry.getKey();
                            double proportion = entry.getValue();
                            System.out.printf("%-15s %.4f    %6.2f%%%n",
                                    browser, proportion, proportion * 100);
                        });

                // Наиболее популярный браузер
                String mostPopular = browserStats.entrySet().stream()
                        .max(Map.Entry.comparingByValue())
                        .map(Map.Entry::getKey)
                        .orElse("Не определен");

                System.out.println("\nСамый популярный браузер: " + mostPopular);
            }

            // 3. Сводная информация
            System.out.println("\n3. СВОДНАЯ ИНФОРМАЦИЯ");
            System.out.println("Успешные запросы (200): " + statistics.getExistingPagesCount() + " уникальных страниц");
            System.out.println("Ошибки 404: " + statistics.getNotFoundPagesCount() + " несуществующих страниц");

            double errorRate = statistics.getEntryCount() > 0 ?
                    (double) statistics.getNotFoundPagesCount() / statistics.getEntryCount() * 100 : 0;
            System.out.printf("Процент ошибок 404: %.2f%%%n", errorRate);

            if (errorRate > 5) {
                System.out.println("⚠ Внимание: высокий процент ошибок 404 (>5%)");
                System.out.println("   Рекомендуется проверить битые ссылки на сайте");
            }

        } catch (Exception e) {
            System.out.println("Ошибка: " + e.getMessage());
            e.printStackTrace();
        }

        System.out.println("\n" + "=".repeat(60));
        System.out.println("АНАЛИЗ ЗАВЕРШЕН УСПЕШНО");
        System.out.println("=".repeat(60));
    }
}