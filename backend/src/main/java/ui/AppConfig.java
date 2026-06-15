package ui;

public final class AppConfig {

    private static final String DEFAULT_API_BASE_URL = "http://localhost:8080";

    private AppConfig() {
    }

    public static String endpoint(String path) {
        String baseUrl = System.getProperty(
                "cardcollector.apiBaseUrl",
                DEFAULT_API_BASE_URL
        );

        if (!isAllowedBaseUrl(baseUrl)) {
            throw new IllegalStateException(
                    "API base URL must use HTTPS, except for localhost development."
            );
        }

        if (!path.startsWith("/")) {
            path = "/" + path;
        }

        return baseUrl + path;
    }

    private static boolean isAllowedBaseUrl(String baseUrl) {
        return baseUrl.startsWith("https://") ||
                baseUrl.startsWith("http://localhost") ||
                baseUrl.startsWith("http://127.0.0.1");
    }
}