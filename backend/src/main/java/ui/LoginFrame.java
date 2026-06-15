package ui;

import javax.swing.*;
import java.awt.*;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.stream.Collectors;

public class LoginFrame extends JFrame {

    private final JTextField emailField;
    private final JPasswordField passwordField;
    private final JLabel statusLabel;

    public LoginFrame() {
        setTitle("Card Collector - Login");
        setSize(1000, 650);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        JPanel mainPanel = new JPanel(null);
        mainPanel.setBackground(new Color(115, 115, 115));

        JLabel titleLabel = new JLabel("Card Collector");
        titleLabel.setFont(new Font("Arial", Font.ITALIC, 54));
        titleLabel.setForeground(new Color(255, 165, 90));
        titleLabel.setBounds(330, 70, 500, 80);
        mainPanel.add(titleLabel);

        JLabel loginLabel = new JLabel("Login");
        loginLabel.setFont(new Font("Arial", Font.PLAIN, 42));
        loginLabel.setForeground(Color.WHITE);
        loginLabel.setBounds(450, 190, 200, 60);
        mainPanel.add(loginLabel);

        emailField = new JTextField();
        emailField.setFont(new Font("Arial", Font.PLAIN, 20));
        emailField.setBounds(355, 280, 300, 40);
        addPlaceholder(emailField, "Email");
        mainPanel.add(emailField);

        passwordField = new JPasswordField();
        passwordField.setFont(new Font("Arial", Font.PLAIN, 20));
        passwordField.setBounds(355, 360, 300, 40);
        addPasswordPlaceholder(passwordField, "Password");
        mainPanel.add(passwordField);

        statusLabel = new JLabel("");
        statusLabel.setFont(new Font("Arial", Font.PLAIN, 18));
        statusLabel.setForeground(Color.WHITE);
        statusLabel.setBounds(355, 415, 500, 30);
        mainPanel.add(statusLabel);

        JButton submitButton = new JButton("Submit");
        submitButton.setFont(new Font("Arial", Font.PLAIN, 34));
        submitButton.setBackground(new Color(45, 45, 45));
        submitButton.setForeground(Color.WHITE);
        submitButton.setFocusPainted(false);
        submitButton.setBounds(415, 460, 170, 70);
        submitButton.addActionListener(e -> loginUser());
        mainPanel.add(submitButton);

        JButton forgotButton = new JButton("Forgot password?");
        forgotButton.setFont(new Font("Arial", Font.PLAIN, 28));
        forgotButton.setBackground(new Color(45, 45, 45));
        forgotButton.setForeground(Color.WHITE);
        forgotButton.setFocusPainted(false);
        forgotButton.setBounds(330, 560, 350, 55);
        forgotButton.addActionListener(e ->
                statusLabel.setText("Password reset will be added later.")
        );
        mainPanel.add(forgotButton);

        JButton signupButton = new JButton("Sign up");
        signupButton.setFont(new Font("Arial", Font.PLAIN, 30));
        signupButton.setBackground(new Color(45, 45, 45));
        signupButton.setForeground(Color.WHITE);
        signupButton.setFocusPainted(false);
        signupButton.setBounds(780, 540, 160, 70);
        signupButton.addActionListener(e -> {
            new RegisterFrame().setVisible(true);
            dispose();
        });
        mainPanel.add(signupButton);

        add(mainPanel);
    }

    private void loginUser() {
        String email = emailField.getText().trim();
        char[] passwordChars = passwordField.getPassword();

        try {
            String password = new String(passwordChars);

            if (
                    email.isBlank() ||
                            password.isBlank() ||
                            email.equals("Email") ||
                            password.equals("Password")
            ) {
                statusLabel.setText("Please fill in all fields.");
                return;
            }

            URL url = new URL(AppConfig.endpoint("/login"));

            HttpURLConnection connection =
                    (HttpURLConnection) url.openConnection();

            connection.setRequestMethod("POST");
            connection.setRequestProperty(
                    "Content-Type",
                    "application/json"
            );
            connection.setDoOutput(true);

            String json = String.format(
                    "{\"email\":\"%s\",\"password\":\"%s\"}",
                    escapeJson(email),
                    escapeJson(password)
            );

            try (OutputStream os = connection.getOutputStream()) {
                byte[] input =
                        json.getBytes(StandardCharsets.UTF_8);

                os.write(input, 0, input.length);
            }

            int responseCode = connection.getResponseCode();
            String responseBody = readResponse(connection);

            if (responseCode >= 200 && responseCode < 300) {
                String token = extractJsonValue(responseBody, "token");
                String username = extractJsonValue(responseBody, "username");
                String userIdText = extractJsonValue(responseBody, "userId");
                String userEmail = extractJsonValue(responseBody, "email");

                if (
                        token.isBlank() ||
                                username.isBlank() ||
                                userIdText.isBlank() ||
                                userEmail.isBlank()
                ) {
                    statusLabel.setText("Invalid login response.");
                    return;
                }

                ClientSession session =
                        new ClientSession(
                                token,
                                Integer.parseInt(userIdText),
                                username,
                                userEmail
                        );

                new LoggedInFrame(session).setVisible(true);
                dispose();

            } else {
                statusLabel.setText("Email or password is incorrect.");
            }

            connection.disconnect();

        } catch (Exception ex) {
            statusLabel.setText("Could not reach backend.");
            ex.printStackTrace();

        } finally {
            Arrays.fill(passwordChars, '\0');
        }
    }

    private String readResponse(HttpURLConnection connection)
            throws Exception {

        InputStream stream;

        if (
                connection.getResponseCode() >= 200 &&
                        connection.getResponseCode() < 300
        ) {
            stream = connection.getInputStream();
        } else {
            stream = connection.getErrorStream();
        }

        if (stream == null) {
            return "";
        }

        try (
                BufferedReader reader =
                        new BufferedReader(
                                new InputStreamReader(
                                        stream,
                                        StandardCharsets.UTF_8
                                )
                        )
        ) {
            return reader.lines()
                    .collect(Collectors.joining("\n"));
        }
    }

    private String escapeJson(String value) {
        return value
                .replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("\n", "\\n")
                .replace("\r", "\\r");
    }

    private String extractJsonValue(String json, String key) {
        String searchKey = "\"" + key + "\":";

        int keyIndex = json.indexOf(searchKey);

        if (keyIndex == -1) {
            return "";
        }

        int valueStart = keyIndex + searchKey.length();

        while (
                valueStart < json.length() &&
                        Character.isWhitespace(json.charAt(valueStart))
        ) {
            valueStart++;
        }

        if (valueStart >= json.length()) {
            return "";
        }

        if (json.charAt(valueStart) == '"') {
            valueStart++;

            StringBuilder result = new StringBuilder();
            boolean escaping = false;

            for (int i = valueStart; i < json.length(); i++) {
                char c = json.charAt(i);

                if (escaping) {
                    result.append(c);
                    escaping = false;
                } else if (c == '\\') {
                    escaping = true;
                } else if (c == '"') {
                    return result.toString();
                } else {
                    result.append(c);
                }
            }

            return "";
        }

        int valueEnd = valueStart;

        while (
                valueEnd < json.length() &&
                        json.charAt(valueEnd) != ',' &&
                        json.charAt(valueEnd) != '}'
        ) {
            valueEnd++;
        }

        return json.substring(valueStart, valueEnd).trim();
    }

    private void addPlaceholder(JTextField field, String placeholder) {
        field.setText(placeholder);
        field.setForeground(Color.GRAY);

        field.addFocusListener(new FocusAdapter() {
            @Override
            public void focusGained(FocusEvent e) {
                if (field.getText().equals(placeholder)) {
                    field.setText("");
                    field.setForeground(Color.BLACK);
                }
            }

            @Override
            public void focusLost(FocusEvent e) {
                if (field.getText().isBlank()) {
                    field.setText(placeholder);
                    field.setForeground(Color.GRAY);
                }
            }
        });
    }

    private void addPasswordPlaceholder(JPasswordField field, String placeholder) {
        field.setEchoChar((char) 0);
        field.setText(placeholder);
        field.setForeground(Color.GRAY);

        field.addFocusListener(new FocusAdapter() {
            @Override
            public void focusGained(FocusEvent e) {
                char[] passwordChars = field.getPassword();
                String password = new String(passwordChars);

                try {
                    if (password.equals(placeholder)) {
                        field.setText("");
                        field.setForeground(Color.BLACK);
                        field.setEchoChar('•');
                    }
                } finally {
                    Arrays.fill(passwordChars, '\0');
                }
            }

            @Override
            public void focusLost(FocusEvent e) {
                char[] passwordChars = field.getPassword();
                String password = new String(passwordChars);

                try {
                    if (password.isBlank()) {
                        field.setEchoChar((char) 0);
                        field.setText(placeholder);
                        field.setForeground(Color.GRAY);
                    }
                } finally {
                    Arrays.fill(passwordChars, '\0');
                }
            }
        });
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            new LoginFrame().setVisible(true);
        });
    }
}