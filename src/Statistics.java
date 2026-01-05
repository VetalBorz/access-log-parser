import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class Statistics {
    private int totalTraffic;
    private LocalDateTime minTime;
    private LocalDateTime maxTime;
    private int entryCount;

    private Set<String> existingPages;

    private Map<String, Integer> osCounts;

    public Statistics() {
        this.totalTraffic = 0;
        this.minTime = null;
        this.maxTime = null;
        this.entryCount = 0;
        this.existingPages = new HashSet<>();
        this.osCounts = new HashMap<>();
    }

    public void addEntry(LogEntry entry) {
        totalTraffic += entry.getResponseSize();
        entryCount++;

        LocalDateTime entryTime = entry.getTime();

        if (minTime == null || entryTime.isBefore(minTime)) {
            minTime = entryTime;
        }

        if (maxTime == null || entryTime.isAfter(maxTime)) {
            maxTime = entryTime;
        }

        if (entry.getResponseCode() == 200) {
            existingPages.add(entry.getPath());
        }

        String os = entry.getUserAgent().getOsType();
        osCounts.put(os, osCounts.getOrDefault(os, 0) + 1);
    }


    public Set<String> getExistingPages() {
        return new HashSet<>(existingPages);
    }

    public Map<String, Double> getOsStatistics() {
        Map<String, Double> result = new HashMap<>();

        if (entryCount == 0) {
            return result;
        }

        int totalOsEntries = 0;
        for (int count : osCounts.values()) {
            totalOsEntries += count;
        }

        for (Map.Entry<String, Integer> entry : osCounts.entrySet()) {
            double proportion = (double) entry.getValue() / totalOsEntries;
            result.put(entry.getKey(), proportion);
        }

        return result;
    }


    public Map<String, Integer> getOsRawStatistics() {
        return new HashMap<>(osCounts);
    }

    public double getTrafficRate() {
        if (minTime == null || maxTime == null || entryCount == 0) {
            return 0.0;
        }

        long hoursBetween = ChronoUnit.HOURS.between(minTime, maxTime);

        if (hoursBetween < 1) {
            hoursBetween = 1;
        }

        return (double) totalTraffic / hoursBetween;
    }

    public int getTotalTraffic() {
        return totalTraffic;
    }

    public LocalDateTime getMinTime() {
        return minTime;
    }

    public LocalDateTime getMaxTime() {
        return maxTime;
    }

    public int getEntryCount() {
        return entryCount;
    }


    public int getExistingPagesCount() {
        return existingPages.size();
    }


    public boolean pageExists(String path) {
        return existingPages.contains(path);
    }

    public void printStatistics() {
        System.out.println("=== ОСНОВНАЯ СТАТИСТИКА ===");
        System.out.println("Всего записей: " + entryCount);
        System.out.println("Общий трафик: " + totalTraffic + " байт");

        if (minTime != null && maxTime != null) {
            System.out.println("Период: с " + minTime + " по " + maxTime);
            long hours = ChronoUnit.HOURS.between(minTime, maxTime);
            System.out.println("Продолжительность: " + hours + " часов");
            System.out.println("Средний трафик в час: " + String.format("%.2f", getTrafficRate()) + " байт/час");
        }

        System.out.println("=== СУЩЕСТВУЮЩИЕ СТРАНИЦЫ ===");
        System.out.println("Количество страниц с кодом 200: " + getExistingPagesCount());

        if (!existingPages.isEmpty()) {
            System.out.println("Первые 5 страниц:");
            int count = 0;
            for (String page : existingPages) {
                if (count++ < 5) {
                    System.out.println("  - " + page);
                } else {
                    break;
                }
            }
            if (existingPages.size() > 5) {
                System.out.println("  ... и еще " + (existingPages.size() - 5) + " страниц");
            }
        }

        System.out.println("=== СТАТИСТИКА ОПЕРАЦИОННЫХ СИСТЕМ ===");
        Map<String, Double> osStats = getOsStatistics();
        if (!osStats.isEmpty()) {
            System.out.println("Доли использования ОС:");
            for (Map.Entry<String, Double> entry : osStats.entrySet()) {
                System.out.printf("  %-10s: %.2f%% (%d запросов)%n",
                        entry.getKey(),
                        entry.getValue() * 100,
                        osCounts.get(entry.getKey()));
            }

            double sum = osStats.values().stream().mapToDouble(Double::doubleValue).sum();
            System.out.printf("Сумма долей: %.4f (должно быть 1.0000)%n", sum);
        }

        System.out.println("=".repeat(50));
    }
}
