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

    private static final Color BACKGROUND = new Color(115, 115, 115);
    private static final Color BUTTON_COLOR = new Color(45, 45, 45);
    private static final Color TITLE_COLOR = new Color(255, 165, 90);

    private JTextField emailField;
    private JPasswordField passwordField;
    private JLabel statusLabel;

    public LoginFrame() {
        setTitle("Card Collector - Login");
        setMinimumSize(new Dimension(800, 550));
        setSize(1000, 650);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        JPanel rootPanel = new JPanel(new BorderLayout());
        rootPanel.setBackground(BACKGROUND);

        rootPanel.add(createCenterPanel(), BorderLayout.CENTER);
        rootPanel.add(createBottomPanel(), BorderLayout.SOUTH);

        add(rootPanel);
    }

    private JPanel createCenterPanel() {
        JPanel centerPanel = new JPanel(new GridBagLayout());
        centerPanel.setBackground(BACKGROUND);

        JPanel formPanel = new JPanel(new GridBagLayout());
        formPanel.setOpaque(false);
        formPanel.setBorder(BorderFactory.createEmptyBorder(30, 40, 30, 40));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.gridx = 0;
        gbc.weightx = 1.0;

        JLabel titleLabel = new JLabel("Card Collector", SwingConstants.CENTER);
        titleLabel.setFont(new Font("Arial", Font.ITALIC, 54));
        titleLabel.setForeground(TITLE_COLOR);

        JLabel loginLabel = new JLabel("Login", SwingConstants.CENTER);
        loginLabel.setFont(new Font("Arial", Font.PLAIN, 42));
        loginLabel.setForeground(Color.WHITE);

        emailField = new JTextField();
        emailField.setFont(new Font("Arial", Font.PLAIN, 20));
        emailField.setPreferredSize(new Dimension(340, 42));
        addPlaceholder(emailField, "Email");

        passwordField = new JPasswordField();
        passwordField.setFont(new Font("Arial", Font.PLAIN, 20));
        passwordField.setPreferredSize(new Dimension(340, 42));
        addPasswordPlaceholder(passwordField, "Password");

        statusLabel = new JLabel("", SwingConstants.CENTER);
        statusLabel.setFont(new Font("Arial", Font.PLAIN, 17));
        statusLabel.setForeground(Color.WHITE);

        JButton submitButton = createButton("Submit", 32);
        submitButton.setPreferredSize(new Dimension(190, 65));
        submitButton.addActionListener(e -> loginUser());

        JButton forgotButton = createButton("Forgot password?", 24);
        forgotButton.setPreferredSize(new Dimension(280, 50));
        forgotButton.addActionListener(e ->
                statusLabel.setText("Password reset will be added later.")
        );

        gbc.gridy = 0;
        formPanel.add(titleLabel, gbc);

        gbc.gridy = 1;
        gbc.insets = new Insets(25, 10, 10, 10);
        formPanel.add(loginLabel, gbc);

        gbc.gridy = 2;
        gbc.insets = new Insets(20, 10, 10, 10);
        formPanel.add(emailField, gbc);

        gbc.gridy = 3;
        formPanel.add(passwordField, gbc);

        gbc.gridy = 4;
        gbc.insets = new Insets(5, 10, 5, 10);
        formPanel.add(statusLabel, gbc);

        gbc.gridy = 5;
        gbc.insets = new Insets(15, 10, 10, 10);
        gbc.fill = GridBagConstraints.NONE;
        formPanel.add(submitButton, gbc);

        gbc.gridy = 6;
        gbc.insets = new Insets(15, 10, 10, 10);
        formPanel.add(forgotButton, gbc);

        centerPanel.add(formPanel);

        return centerPanel;
    }

    private JPanel createBottomPanel() {
        JPanel bottomPanel = new JPanel(new BorderLayout());
        bottomPanel.setBackground(BACKGROUND);
        bottomPanel.setBorder(BorderFactory.createEmptyBorder(10, 25, 25, 25));

        JButton signupButton = createButton("Sign up", 24);
        signupButton.setPreferredSize(new Dimension(150, 55));
        signupButton.addActionListener(e -> {
            new RegisterFrame().setVisible(true);
            dispose();
        });

        JPanel rightPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        rightPanel.setOpaque(false);
        rightPanel.add(signupButton);

        bottomPanel.add(rightPanel, BorderLayout.EAST);

        return bottomPanel;
    }

    private JButton createButton(String text, int fontSize) {
        JButton button = new JButton(text);
        button.setFont(new Font("Arial", Font.PLAIN, fontSize));
        button.setBackground(BUTTON_COLOR);
        button.setForeground(Color.WHITE);
        button.setFocusPainted(false);
        return button;
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
                byte[] input = json.getBytes(StandardCharsets.UTF_8);
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

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            new LoginFrame().setVisible(true);
        });
    }
}