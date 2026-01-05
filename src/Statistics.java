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

    private Set<String> notFoundPages;

    private Map<String, Integer> osCounts;

    private Map<String, Integer> browserCounts;

    public Statistics() {
        this.totalTraffic = 0;
        this.minTime = null;
        this.maxTime = null;
        this.entryCount = 0;
        this.existingPages = new HashSet<>();
        this.notFoundPages = new HashSet<>();
        this.osCounts = new HashMap<>();
        this.browserCounts = new HashMap<>();
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

        if (entry.getResponseCode() == 404) {
            notFoundPages.add(entry.getPath());
        }

        String os = entry.getUserAgent().getOsType();
        osCounts.put(os, osCounts.getOrDefault(os, 0) + 1);

        String browser = entry.getUserAgent().getBrowser();
        browserCounts.put(browser, browserCounts.getOrDefault(browser, 0) + 1);
    }

    public Set<String> getExistingPages() {
        return new HashSet<>(existingPages);
    }

    public Set<String> getNotFoundPages() {
        return new HashSet<>(notFoundPages);
    }


    public Map<String, Double> getOsStatistics() {
        Map<String, Double> result = new HashMap<>();

        if (entryCount == 0 || osCounts.isEmpty()) {
            return result;
        }

        int totalOsEntries = osCounts.values().stream().mapToInt(Integer::intValue).sum();

        for (Map.Entry<String, Integer> entry : osCounts.entrySet()) {
            double proportion = (double) entry.getValue() / totalOsEntries;
            result.put(entry.getKey(), proportion);
        }

        return result;
    }

    public Map<String, Double> getBrowserStatistics() {
        Map<String, Double> result = new HashMap<>();

        if (entryCount == 0 || browserCounts.isEmpty()) {
            return result;
        }

        int totalBrowserEntries = browserCounts.values().stream().mapToInt(Integer::intValue).sum();

        for (Map.Entry<String, Integer> entry : browserCounts.entrySet()) {
            double proportion = (double) entry.getValue() / totalBrowserEntries;
            result.put(entry.getKey(), proportion);
        }

        return result;
    }

    public Map<String, Integer> getOsRawStatistics() {
        return new HashMap<>(osCounts);
    }

    public Map<String, Integer> getBrowserRawStatistics() {
        return new HashMap<>(browserCounts);
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

    public int getNotFoundPagesCount() {
        return notFoundPages.size();
    }

    public boolean pageExists(String path) {
        return existingPages.contains(path);
    }

    public boolean pageNotFound(String path) {
        return notFoundPages.contains(path);
    }

    public void printResponseCodeStatistics() {
        System.out.println("=== СТАТИСТИКА КОДОВ ОТВЕТА ===");
        System.out.println("Существующие страницы (200): " + getExistingPagesCount());
        System.out.println("Несуществующие страницы (404): " + getNotFoundPagesCount());

        System.out.println("Всего уникальных страниц в логах: " +
                (existingPages.size() + notFoundPages.size()));
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

        printResponseCodeStatistics();

        System.out.println("=== СУЩЕСТВУЮЩИЕ СТРАНИЦЫ ===");
        if (!existingPages.isEmpty()) {
            System.out.println("Первые 5 страниц (всего " + existingPages.size() + "):");
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
        } else {
            System.out.println("Нет страниц с кодом 200");
        }

        System.out.println("=== НЕСУЩЕСТВУЮЩИЕ СТРАНИЦЫ ===");
        if (!notFoundPages.isEmpty()) {
            System.out.println("Первые 5 страниц (всего " + notFoundPages.size() + "):");
            int count = 0;
            for (String page : notFoundPages) {
                if (count++ < 5) {
                    System.out.println("  - " + page);
                } else {
                    break;
                }
            }
            if (notFoundPages.size() > 5) {
                System.out.println("  ... и еще " + (notFoundPages.size() - 5) + " страниц");
            }
        } else {
            System.out.println("Нет страниц с кодом 404");
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
        }

        System.out.println("\n=== СТАТИСТИКА БРАУЗЕРОВ ===");
        Map<String, Double> browserStats = getBrowserStatistics();
        if (!browserStats.isEmpty()) {
            System.out.println("Доли использования браузеров:");
            for (Map.Entry<String, Double> entry : browserStats.entrySet()) {
                System.out.printf("  %-10s: %.2f%% (%d запросов)%n",
                        entry.getKey(),
                        entry.getValue() * 100,
                        browserCounts.get(entry.getKey()));
            }
        }

        System.out.println("=".repeat(50));
    }
}