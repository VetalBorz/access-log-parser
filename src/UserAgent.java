public class UserAgent {
    private final String osType;
    private final String browser;
    private final boolean isBot;

    public UserAgent(String userAgentString) {
        this.osType = parseOsType(userAgentString);
        this.browser = parseBrowser(userAgentString);
        this.isBot = checkIfBot(userAgentString);
    }

    private String parseOsType(String userAgent) {
        if (userAgent == null) return "Other";

        userAgent = userAgent.toLowerCase();

        if (userAgent.contains("windows")) {
            return "Windows";
        } else if (userAgent.contains("mac os") || userAgent.contains("macos")) {
            return "macOS";
        } else if (userAgent.contains("linux")) {
            return "Linux";
        } else if (userAgent.contains("android")) {
            return "Android";
        } else if (userAgent.contains("ios") || userAgent.contains("iphone")) {
            return "iOS";
        } else {
            return "Other";
        }
    }

    private String parseBrowser(String userAgent) {
        if (userAgent == null) return "Other";

        userAgent = userAgent.toLowerCase();

        if (userAgent.contains("edge")) {
            return "Edge";
        } else if (userAgent.contains("firefox")) {
            return "Firefox";
        } else if (userAgent.contains("chrome") && !userAgent.contains("chromium")) {
            return "Chrome";
        } else if (userAgent.contains("opera") || userAgent.contains("opr/")) {
            return "Opera";
        } else if (userAgent.contains("safari") && !userAgent.contains("chrome")) {
            return "Safari";
        } else {
            return "Other";
        }
    }

    private boolean checkIfBot(String userAgent) {
        if (userAgent == null) return false;

        userAgent = userAgent.toLowerCase();

        return userAgent.contains("bot") ||
                userAgent.contains("crawler") ||
                userAgent.contains("spider") ||
                userAgent.contains("googlebot") ||
                userAgent.contains("yandexbot") ||
                userAgent.contains("bingbot") ||
                userAgent.contains("slurp") ||
                userAgent.contains("duckduckbot");
    }

    public String getOsType() {
        return osType;
    }

    public String getBrowser() {
        return browser;
    }

    public boolean isBot() {
        return isBot;
    }

    @Override
    public String toString() {
        return String.format("OS: %s, Browser: %s, Bot: %b", osType, browser, isBot);
    }
}