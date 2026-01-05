import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.Map;
import java.util.Set;

public class Main {
    public static void main(String[] args) {
        String filePath = args.length > 0 ? args[0] : "access.log";

        File file = new File(filePath);
        if (!file.exists()) {
            System.out.println("Файл не найден: " + filePath);
            return;
        }

        Statistics statistics = new Statistics();

        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String line;
            int lineCount = 0;

            System.out.println("Чтение файла " + filePath + "...");

            while ((line = reader.readLine()) != null) {
                lineCount++;

                if (line.trim().isEmpty()) {
                    continue;
                }

                LogEntry entry = new LogEntry(line);

                statistics.addEntry(entry);

                if (lineCount % 1000 == 0) {
                    System.out.println("Обработано строк: " + lineCount);
                }
            }

            System.out.println("=== АНАЛИЗ ЗАВЕРШЕН ===");
            System.out.println("Всего строк в файле: " + lineCount);

            statistics.printStatistics();

            System.out.println("=== РЕЗУЛЬТАТЫ COLLECTIONS ===");

            Set<String> existingPages = statistics.getExistingPages();
            System.out.println("1. Существующие страницы (всего " + existingPages.size() + "):");
            int pageCounter = 1;
            for (String page : existingPages) {
                System.out.printf("%3d. %s%n", pageCounter++, page);
                if (pageCounter > 20 && existingPages.size() > 20) {
                    System.out.printf("... и еще %d страниц%n", existingPages.size() - 20);
                    break;
                }
            }

            Map<String, Double> osStats = statistics.getOsStatistics();
            System.out.println("2. Статистика операционных систем:");
            if (!osStats.isEmpty()) {
                System.out.println("ОС                Доля      Процент   Запросов");
                System.out.println("---------------- --------- --------- ---------");

                for (Map.Entry<String, Double> entry : osStats.entrySet()) {
                    String os = entry.getKey();
                    double proportion = entry.getValue();
                    int count = statistics.getOsRawStatistics().get(os);

                    System.out.printf("%-16s %.6f %8.2f%% %9d%n",
                            os, proportion, proportion * 100, count);
                }

                double totalProportion = osStats.values().stream()
                        .mapToDouble(Double::doubleValue).sum();
                int totalRequests = statistics.getOsRawStatistics().values().stream()
                        .mapToInt(Integer::intValue).sum();

                System.out.println("---------------- --------- --------- ---------");
                System.out.printf("ИТОГО:           %.6f %8.2f%% %9d%n",
                        totalProportion, totalProportion * 100, totalRequests);
            }

        } catch (Exception e) {
            System.out.println("Ошибка: " + e.getMessage());
            e.printStackTrace();
        }
    }
}