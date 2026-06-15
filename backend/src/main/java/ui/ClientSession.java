package ui;

public class ClientSession {

    private final String token;
    private final int userId;
    private final String username;
    private final String email;

    public ClientSession(
            String token,
            int userId,
            String username,
            String email
    ) {
        this.token = token;
        this.userId = userId;
        this.username = username;
        this.email = email;
    }

    public String getToken() {
        return token;
    }

    public int getUserId() {
        return userId;
    }

    public String getUsername() {
        return username;
    }

    public String getEmail() {
        return email;
    }
}