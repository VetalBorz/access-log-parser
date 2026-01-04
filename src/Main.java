import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;


class LineTooLongException extends RuntimeException {
    public LineTooLongException(String message) {
        super(message);
    }
}


public class Main {
    public static void main(String[] args) {
        String path = "access.log";
        File file = new File(path);

        if (!file.exists() || !file.isFile()) {
            System.err.println("Файл не найден: " + path);
            return;
        }

        int totalLines = 0;
        int googleBotCount = 0;
        int yandexBotCount = 0;

        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String line;

            System.out.println("Анализирую " + path + "...");

            while ((line = reader.readLine()) != null) {
                totalLines++;

                if (line.length() > 1024) {
                    throw new LineTooLongException(
                            "Строка #" + totalLines + " превышает 1024 символа"
                    );
                }

                String userAgent = extractUserAgent(line);
                if (userAgent != null) {
                    String botName = extractBotNameFromUserAgent(userAgent);

                    if (botName != null) {
                        if (botName.equalsIgnoreCase("Googlebot") ||
                                botName.equalsIgnoreCase("GoogleBot") ||
                                botName.equalsIgnoreCase("Google")) {
                            googleBotCount++;
                        } else if (botName.equalsIgnoreCase("YandexBot") ||
                                botName.equalsIgnoreCase("Yandex")) {
                            yandexBotCount++;
                        }
                    }
                }
            }

            System.out.println("=== РЕЗУЛЬТАТЫ ===");
            System.out.println("Всего строк: " + totalLines);

            if (totalLines > 0) {
                System.out.println("GoogleBot:");
                System.out.println("  Запросов: " + googleBotCount);
                System.out.println("  Доля: " + String.format("%.2f%%",
                        (double)googleBotCount / totalLines * 100));

                System.out.println("YandexBot:");
                System.out.println("  Запросов: " + yandexBotCount);
                System.out.println("  Доля: " + String.format("%.2f%%",
                        (double)yandexBotCount / totalLines * 100));
            }

        } catch (IOException e) {
            System.err.println("Ошибка: " + e.getMessage());
        } catch (LineTooLongException e) {
            System.err.println("ОШИБКА: " + e.getMessage());
        }
    }

    private static String extractUserAgent(String logLine) {

        int lastQuoteIndex = logLine.lastIndexOf('"');
        if (lastQuoteIndex == -1) return null;

        int secondLastQuoteIndex = logLine.lastIndexOf('"', lastQuoteIndex - 1);
        if (secondLastQuoteIndex == -1) return null;

        return logLine.substring(secondLastQuoteIndex + 1, lastQuoteIndex);
    }

    private static String extractBotNameFromUserAgent(String userAgent) {
        try {
            int start = userAgent.indexOf('(');
            int end = userAgent.indexOf(')', start);

            if (start == -1 || end == -1) {
                return null;
            }

            String content = userAgent.substring(start + 1, end);
            String[] parts = content.split(";");

            if (parts.length >= 2) {
                String fragment = parts[1].trim();
                int slashIndex = fragment.indexOf('/');
                if (slashIndex != -1) {
                    return fragment.substring(0, slashIndex).trim();
                }
                return fragment;
            }
        } catch (Exception e) {
        }
        return null;
    }
}
