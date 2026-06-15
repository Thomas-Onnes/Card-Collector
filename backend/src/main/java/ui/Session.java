package ui;

public final class Session {

    private static String authToken;

    private Session() {
    }

    public static void setAuthToken(String token) {
        authToken = token;
    }

    public static String getAuthToken() {
        return authToken;
    }

    public static boolean isAuthenticated() {
        return authToken != null && !authToken.isBlank();
    }

    public static void clear() {
        authToken = null;
    }
}