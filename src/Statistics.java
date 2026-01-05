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
    private Map<Long, Integer> visitsPerSecond;
    private Set<String> refererDomains;
    private Map<String, Integer> visitsPerIP;

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
        this.visitsPerSecond = new HashMap<>();
        this.refererDomains = new HashSet<>();
        this.visitsPerIP = new HashMap<>();
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

            long secondsSinceEpoch = entryTime.toEpochSecond(java.time.ZoneOffset.UTC);
            visitsPerSecond.put(secondsSinceEpoch,
                    visitsPerSecond.getOrDefault(secondsSinceEpoch, 0) + 1);

            visitsPerIP.put(entry.getIpAddress(),
                    visitsPerIP.getOrDefault(entry.getIpAddress(), 0) + 1);
        }

        if (entry.getReferer() != null && !entry.getReferer().isEmpty()) {
            String domain = extractDomainFromReferer(entry.getReferer());
            if (domain != null && !domain.isEmpty()) {
                refererDomains.add(domain);
            }
        }

        String os = entry.getUserAgent().getOsType();
        osCounts.put(os, osCounts.getOrDefault(os, 0) + 1);

        String browser = entry.getUserAgent().getBrowser();
        browserCounts.put(browser, browserCounts.getOrDefault(browser, 0) + 1);
    }

    private String extractDomainFromReferer(String referer) {
        try {
            String url = referer.toLowerCase();
            if (url.startsWith("http://")) {
                url = url.substring(7);
            } else if (url.startsWith("https://")) {
                url = url.substring(8);
            }

            int slashIndex = url.indexOf('/');
            if (slashIndex != -1) {
                url = url.substring(0, slashIndex);
            }

            int colonIndex = url.indexOf(':');
            if (colonIndex != -1) {
                url = url.substring(0, colonIndex);
            }

            return url.trim();
        } catch (Exception e) {
            return null;
        }
    }

    public int getPeakVisitsPerSecond() {
        if (visitsPerSecond.isEmpty()) {
            return 0;
        }

        return visitsPerSecond.values().stream()
                .max(Integer::compare)
                .orElse(0);
    }

    public LocalDateTime getPeakVisitsTime() {
        if (visitsPerSecond.isEmpty()) {
            return null;
        }

        long peakSecond = visitsPerSecond.entrySet().stream()
                .max(Map.Entry.comparingByValue())
                .map(Map.Entry::getKey)
                .orElse(0L);

        return LocalDateTime.ofEpochSecond(peakSecond, 0, java.time.ZoneOffset.UTC);
    }

    public Set<String> getRefererDomains() {
        return new HashSet<>(refererDomains);
    }

    public int getRefererDomainsCount() {
        return refererDomains.size();
    }

    public int getMaxVisitsBySingleUser() {
        if (visitsPerIP.isEmpty()) {
            return 0;
        }

        return visitsPerIP.values().stream()
                .max(Integer::compare)
                .orElse(0);
    }

    public String getMostActiveUserIP() {
        if (visitsPerIP.isEmpty()) {
            return null;
        }

        return visitsPerIP.entrySet().stream()
                .max(Map.Entry.comparingByValue())
                .map(Map.Entry::getKey)
                .orElse(null);
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

    public double getBotPercentage() {
        if (entryCount == 0) {
            return 0.0;
        }

        int botCount = entryCount - nonBotRequestsCount;
        return (double) botCount / entryCount * 100;
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
        System.out.println("=".repeat(80));
        System.out.println("ПОЛНАЯ СТАТИСТИКА АНАЛИЗА ЛОГ-ФАЙЛА");
        System.out.println("=".repeat(80));

        System.out.println("ОСНОВНЫЕ ПОКАЗАТЕЛИ:");
        System.out.printf("Всего записей: %,d%n", entryCount);
        if (minTime != null && maxTime != null) {
            System.out.printf("Период: %s - %s%n", minTime, maxTime);
            long hours = ChronoUnit.HOURS.between(minTime, maxTime);
            System.out.printf("Продолжительность: %d часов%n", hours);
        }

        System.out.println("ПОКАЗАТЕЛИ STREAM API #2:");

        int peakVisits = getPeakVisitsPerSecond();
        LocalDateTime peakTime = getPeakVisitsTime();
        System.out.printf("Пиковая посещаемость: %d посещений/секунду%n", peakVisits);
        if (peakTime != null) {
            System.out.printf("Время пиковой посещаемости: %s%n", peakTime);
        }

        int refererCount = getRefererDomainsCount();
        System.out.printf("Сайтов-источников трафика: %,d%n", refererCount);

        int maxUserVisits = getMaxVisitsBySingleUser();
        String mostActiveIP = getMostActiveUserIP();
        System.out.printf("Максимальная посещаемость одним пользователем: %d запросов%n", maxUserVisits);
        if (mostActiveIP != null) {
            System.out.printf("Самый активный пользователь: %s%n", mostActiveIP);
        }

        System.out.println("СТАТИСТИКА:");
        System.out.printf("Среднее количество посещений в час: %.2f%n", getAverageVisitsPerHour());
        System.out.printf("Среднее количество ошибок в час: %.2f%n", getAverageErrorRequestsPerHour());
        System.out.printf("Средняя посещаемость на пользователя: %.2f%n", getAverageVisitsPerUser());

        System.out.printf("Боты: %.2f%% от всех запросов%n", getBotPercentage());

        System.out.println("ДОПОЛНИТЕЛЬНАЯ АНАЛИТИКА:");

        if (!visitsPerSecond.isEmpty()) {
            double avgVisitsPerSec = visitsPerSecond.values().stream()
                    .mapToInt(Integer::intValue).average().orElse(0);
            System.out.printf("Средняя посещаемость в секунду: %.2f%n", avgVisitsPerSec);
            if (avgVisitsPerSec > 0) {
                System.out.printf("Пик/Среднее: %.2f раз%n", peakVisits / avgVisitsPerSec);
            }
        }

        System.out.println("ТОП-5 САМЫХ АКТИВНЫХ ПОЛЬЗОВАТЕЛЕЙ:");
        Map<String, Long> topIPs = getTopActiveIPs(5);
        if (!topIPs.isEmpty()) {
            topIPs.forEach((ip, count) ->
                    System.out.printf("  %s: %,d запросов%n", ip, count));
        }

        System.out.println("=".repeat(80));
    }
}