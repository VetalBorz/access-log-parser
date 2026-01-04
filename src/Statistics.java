import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

public class Statistics {
    private int totalTraffic;
    private LocalDateTime minTime;
    private LocalDateTime maxTime;
    private int entryCount;

    public Statistics() {
        this.totalTraffic = 0;
        this.minTime = null;
        this.maxTime = null;
        this.entryCount = 0;
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

    public void printStatistics() {
        System.out.println("=== СТАТИСТИКА ===");
        System.out.println("Всего записей: " + entryCount);
        System.out.println("Общий трафик: " + totalTraffic + " байт");

        if (minTime != null && maxTime != null) {
            System.out.println("Период: с " + minTime + " по " + maxTime);
            long hours = ChronoUnit.HOURS.between(minTime, maxTime);
            System.out.println("Продолжительность: " + hours + " часов");
            System.out.println("Средний трафик в час: " + String.format("%.2f", getTrafficRate()) + " байт/час");
        }
    }
}

