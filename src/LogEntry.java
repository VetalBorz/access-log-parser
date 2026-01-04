import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

public class LogEntry {
    private final String ipAddress;
    private final LocalDateTime time;
    private final HttpMethod method;
    private final String path;
    private final int responseCode;
    private final int responseSize;
    private final String referer;
    private final UserAgent userAgent;

    public LogEntry(String logLine) {
        String ip = "0.0.0.0";
        LocalDateTime dt = LocalDateTime.now();
        HttpMethod m = HttpMethod.UNKNOWN;
        String p = "/";
        int code = 0;
        int size = 0;
        String ref = null;
        UserAgent ua = new UserAgent("");

        try {

            String cleanLine = logLine.trim();

            int firstSpace = cleanLine.indexOf(' ');
            if (firstSpace > 0) {
                ip = cleanLine.substring(0, firstSpace);
            }

            int dateStart = cleanLine.indexOf('[');
            int dateEnd = cleanLine.indexOf(']', dateStart);
            if (dateStart > 0 && dateEnd > dateStart) {
                String dateStr = cleanLine.substring(dateStart + 1, dateEnd);
                String[] dateParts = dateStr.split("\\s+");
                if (dateParts.length > 0) {
                    try {
                        dt = LocalDateTime.parse(dateParts[0],
                                DateTimeFormatter.ofPattern("dd/MMM/yyyy:HH:mm:ss"));
                    } catch (DateTimeParseException e) {
                    }
                }
            }

            int quoteStart = cleanLine.indexOf('"', dateEnd);
            int quoteEnd = cleanLine.indexOf('"', quoteStart + 1);
            if (quoteStart > 0 && quoteEnd > quoteStart) {
                String request = cleanLine.substring(quoteStart + 1, quoteEnd);
                String[] requestParts = request.split("\\s+");
                if (requestParts.length >= 2) {
                    m = parseHttpMethod(requestParts[0]);
                    p = requestParts[1];
                }
            }

            String afterRequest = cleanLine.substring(quoteEnd + 1).trim();
            String[] responseParts = afterRequest.split("\\s+");
            if (responseParts.length >= 2) {
                try {
                    code = Integer.parseInt(responseParts[0]);
                } catch (NumberFormatException e) {
                    code = 0;
                }

                try {
                    size = Integer.parseInt(responseParts[1]);
                } catch (NumberFormatException e) {
                    size = 0;
                }
            }

            int refStart = cleanLine.indexOf('"', quoteEnd + 1);
            int refEnd = cleanLine.indexOf('"', refStart + 1);
            int uaStart = cleanLine.indexOf('"', refEnd + 1);
            int uaEnd = cleanLine.indexOf('"', uaStart + 1);

            if (refStart > 0 && refEnd > refStart) {
                ref = cleanLine.substring(refStart + 1, refEnd);
                if (ref.equals("-")) {
                    ref = null;
                }
            }

            if (uaStart > 0 && uaEnd > uaStart) {
                String uaStr = cleanLine.substring(uaStart + 1, uaEnd);
                if (!uaStr.equals("-")) {
                    ua = new UserAgent(uaStr);
                }
            }

        } catch (Exception e) {
            System.err.println("Ошибка при парсинге строки: " + e.getMessage());
        }

        this.ipAddress = ip;
        this.time = dt;
        this.method = m;
        this.path = p;
        this.responseCode = code;
        this.responseSize = size;
        this.referer = ref;
        this.userAgent = ua;
    }

    private HttpMethod parseHttpMethod(String methodStr) {
        try {
            return HttpMethod.valueOf(methodStr.toUpperCase());
        } catch (IllegalArgumentException e) {
            return HttpMethod.UNKNOWN;
        }
    }

    public String getIpAddress() { return ipAddress; }
    public LocalDateTime getTime() { return time; }
    public HttpMethod getMethod() { return method; }
    public String getPath() { return path; }
    public int getResponseCode() { return responseCode; }
    public int getResponseSize() { return responseSize; }
    public String getReferer() { return referer; }
    public UserAgent getUserAgent() { return userAgent; }

    @Override
    public String toString() {
        return String.format("IP: %s, Time: %s, Method: %s, Size: %d",
                ipAddress, time, method, responseSize);
    }
}