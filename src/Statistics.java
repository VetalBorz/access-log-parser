import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

public class Statistics {
    private int totalTraffic;
    private LocalDateTime minTime;
    private LocalDateTime maxTime;
    private int entryCount;

    private Set<String> existingPages;
    private Set<String> notFoundPages;

    private Map<String, Integer> osCounts;
    private Map<String, Integer> browserCounts;

    private Set<LogEntry> allEntries;
    private int errorRequestsCount;
    private int nonBotRequestsCount;
    private Set<String> uniqueNonBotIPs;

    public Statistics() {
        this.totalTraffic = 0;
        this.minTime = null;
        this.maxTime = null;
        this.entryCount = 0;
        this.existingPages = new HashSet<>();
        this.notFoundPages = new HashSet<>();
        this.osCounts = new HashMap<>();
        this.browserCounts = new HashMap<>();
        this.allEntries = new HashSet<>();
        this.errorRequestsCount = 0;
        this.nonBotRequestsCount = 0;
        this.uniqueNonBotIPs = new HashSet<>();
    }

    public void addEntry(LogEntry entry) {
        totalTraffic += entry.getResponseSize();
        entryCount++;

        allEntries.add(entry);

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

        if (entry.getResponseCode() >= 400 && entry.getResponseCode() < 600) {
            errorRequestsCount++;
        }

        if (!entry.getUserAgent().isBot()) {
            nonBotRequestsCount++;
            uniqueNonBotIPs.add(entry.getIpAddress());
        }

        String os = entry.getUserAgent().getOsType();
        osCounts.put(os, osCounts.getOrDefault(os, 0) + 1);

        String browser = entry.getUserAgent().getBrowser();
        browserCounts.put(browser, browserCounts.getOrDefault(browser, 0) + 1);
    }

    public double getAverageVisitsPerHour() {
        if (minTime == null || maxTime == null || nonBotRequestsCount == 0) {
            return 0.0;
        }

        long hoursBetween = ChronoUnit.HOURS.between(minTime, maxTime);
        if (hoursBetween < 1) {
            hoursBetween = 1;
        }

        return (double) nonBotRequestsCount / hoursBetween;
    }

    public double getAverageErrorRequestsPerHour() {
        if (minTime == null || maxTime == null || errorRequestsCount == 0) {
            return 0.0;
        }

        long hoursBetween = ChronoUnit.HOURS.between(minTime, maxTime);
        if (hoursBetween < 1) {
            hoursBetween = 1;
        }

        return (double) errorRequestsCount / hoursBetween;
    }

    public double getAverageVisitsPerUser() {
        if (uniqueNonBotIPs.isEmpty() || nonBotRequestsCount == 0) {
            return 0.0;
        }

        return (double) nonBotRequestsCount / uniqueNonBotIPs.size();
    }

    public double getAverageVisitsPerUserStream() {
        if (allEntries.isEmpty()) {
            return 0.0;
        }

        long nonBotCount = allEntries.stream()
                .filter(entry -> !entry.getUserAgent().isBot())
                .count();

        long uniqueNonBotIPsCount = allEntries.stream()
                .filter(entry -> !entry.getUserAgent().isBot())
                .map(LogEntry::getIpAddress)
                .distinct()
                .count();

        if (uniqueNonBotIPsCount == 0) {
            return 0.0;
        }

        return (double) nonBotCount / uniqueNonBotIPsCount;
    }

    public Map<Integer, Long> getResponseCodeStatistics() {
        return allEntries.stream()
                .collect(Collectors.groupingBy(
                        LogEntry::getResponseCode,
                        Collectors.counting()
                ));
    }

    public Map<String, Long> getTopActiveIPs(int limit) {
        return allEntries.stream()
                .filter(entry -> !entry.getUserAgent().isBot())
                .collect(Collectors.groupingBy(
                        LogEntry::getIpAddress,
                        Collectors.counting()
                ))
                .entrySet().stream()
                .sorted((a, b) -> Long.compare(b.getValue(), a.getValue()))
                .limit(limit)
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        Map.Entry::getValue
                ));
    }

    public Map<String, Long> getPopularPages(int limit) {
        return allEntries.stream()
                .filter(entry -> !entry.getUserAgent().isBot())
                .collect(Collectors.groupingBy(
                        LogEntry::getPath,
                        Collectors.counting()
                ))
                .entrySet().stream()
                .sorted((a, b) -> Long.compare(b.getValue(), a.getValue()))
                .limit(limit)
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        Map.Entry::getValue
                ));
    }

    public double getBotPercentage() {
        if (allEntries.isEmpty()) {
            return 0.0;
        }

        long botCount = allEntries.stream()
                .filter(entry -> entry.getUserAgent().isBot())
                .count();

        return (double) botCount / allEntries.size() * 100;
    }

    public Map<Integer, Long> getHourlyDistribution() {
        return allEntries.stream()
                .collect(Collectors.groupingBy(
                        entry -> entry.getTime().getHour(),
                        Collectors.counting()
                ));
    }

    public Set<String> getExistingPages() {
        return new HashSet<>(existingPages);
    }

    public Set<String> getNotFoundPages() {
        return new HashSet<>(notFoundPages);
    }

    public Map<String, Double> getOsStatistics() {
        return calculateProportions(osCounts);
    }

    public Map<String, Double> getBrowserStatistics() {
        return calculateProportions(browserCounts);
    }

    private Map<String, Double> calculateProportions(Map<String, Integer> counts) {
        Map<String, Double> result = new HashMap<>();

        if (counts.isEmpty()) {
            return result;
        }

        int total = counts.values().stream().mapToInt(Integer::intValue).sum();

        for (Map.Entry<String, Integer> entry : counts.entrySet()) {
            double proportion = (double) entry.getValue() / total;
            result.put(entry.getKey(), proportion);
        }

        return result;
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

    public int getTotalTraffic() { return totalTraffic; }
    public LocalDateTime getMinTime() { return minTime; }
    public LocalDateTime getMaxTime() { return maxTime; }
    public int getEntryCount() { return entryCount; }
    public int getExistingPagesCount() { return existingPages.size(); }
    public int getNotFoundPagesCount() { return notFoundPages.size(); }
    public int getErrorRequestsCount() { return errorRequestsCount; }
    public int getNonBotRequestsCount() { return nonBotRequestsCount; }
    public int getUniqueNonBotUsersCount() { return uniqueNonBotIPs.size(); }

    public void printStatistics() {
        System.out.println("=".repeat(70));
        System.out.println("ПОЛНАЯ СТАТИСТИКА АНАЛИЗА ЛОГ-ФАЙЛА");
        System.out.println("=".repeat(70));

        System.out.println("ОСНОВНЫЕ ПОКАЗАТЕЛИ:");
        System.out.printf("Всего записей: %,d%n", entryCount);
        System.out.printf("Период: %s - %s%n",
                minTime != null ? minTime : "N/A",
                maxTime != null ? maxTime : "N/A");

        if (minTime != null && maxTime != null) {
            long hours = ChronoUnit.HOURS.between(minTime, maxTime);
            System.out.printf("Продолжительность: %d часов%n", hours);
        }

        System.out.println("STREAM API СТАТИСТИКА:");
        System.out.printf("Среднее количество посещений в час: %.2f%n", getAverageVisitsPerHour());
        System.out.printf("Среднее количество ошибок в час: %.2f%n", getAverageErrorRequestsPerHour());
        System.out.printf("Средняя посещаемость на пользователя: %.2f запросов%n", getAverageVisitsPerUser());
        System.out.printf("Процент ботов: %.2f%%%n", getBotPercentage());

        System.out.println("СТАТИСТИКА ПОЛЬЗОВАТЕЛЕЙ:");
        System.out.printf("Всего уникальных пользователей (не ботов): %,d%n", uniqueNonBotIPs.size());
        System.out.printf("Запросов от обычных пользователей: %,d%n", nonBotRequestsCount);
        System.out.printf("Запросов от ботов: %,d%n", entryCount - nonBotRequestsCount);

        System.out.println("СТАТИСТИКА ОШИБОК:");
        System.out.printf("Всего ошибочных запросов (4xx, 5xx): %,d%n", errorRequestsCount);
        System.out.printf("Процент ошибок: %.2f%%%n",
                entryCount > 0 ? (double)errorRequestsCount / entryCount * 100 : 0);

        System.out.println("ТОП-5 САМЫХ АКТИВНЫХ ПОЛЬЗОВАТЕЛЕЙ:");
        Map<String, Long> topIPs = getTopActiveIPs(5);
        if (!topIPs.isEmpty()) {
            topIPs.forEach((ip, count) ->
                    System.out.printf("  %s: %,d запросов%n", ip, count));
        }

        System.out.println("ТОП-5 САМЫХ ПОПУЛЯРНЫХ СТРАНИЦ:");
        Map<String, Long> popularPages = getPopularPages(5);
        if (!popularPages.isEmpty()) {
            popularPages.forEach((page, count) ->
                    System.out.printf("  %s: %,d посещений%n", page, count));
        }

        System.out.println("РАСПРЕДЕЛЕНИЕ ЗАПРОСОВ ПО ЧАСАМ:");
        Map<Integer, Long> hourlyDist = getHourlyDistribution();
        hourlyDist.entrySet().stream()
                .sorted(Map.Entry.comparingByKey())
                .forEach(entry ->
                        System.out.printf("  %02d:00 - %02d:59: %,d запросов%n",
                                entry.getKey(), entry.getKey(), entry.getValue()));

        System.out.println("=".repeat(70));
    }
}