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

public class RegisterFrame extends JFrame {

    private static final Color BACKGROUND = new Color(115, 115, 115);
    private static final Color BUTTON_COLOR = new Color(45, 45, 45);
    private static final Color TITLE_COLOR = new Color(255, 165, 90);

    private JTextField usernameField;
    private JTextField emailField;
    private JPasswordField passwordField;
    private JPasswordField confirmPasswordField;
    private JLabel statusLabel;

    public RegisterFrame() {
        setTitle("Card Collector - Sign up");
        setMinimumSize(new Dimension(800, 650));
        setSize(1000, 700);
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
        formPanel.setBorder(BorderFactory.createEmptyBorder(25, 40, 25, 40));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(8, 10, 8, 10);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.gridx = 0;
        gbc.weightx = 1.0;

        JLabel titleLabel = new JLabel("Card Collector", SwingConstants.CENTER);
        titleLabel.setFont(new Font("Arial", Font.ITALIC, 54));
        titleLabel.setForeground(TITLE_COLOR);

        JLabel registerLabel = new JLabel("Sign up", SwingConstants.CENTER);
        registerLabel.setFont(new Font("Arial", Font.PLAIN, 42));
        registerLabel.setForeground(Color.WHITE);

        usernameField = new JTextField();
        usernameField.setFont(new Font("Arial", Font.PLAIN, 20));
        usernameField.setPreferredSize(new Dimension(340, 42));
        addPlaceholder(usernameField, "Username");

        emailField = new JTextField();
        emailField.setFont(new Font("Arial", Font.PLAIN, 20));
        emailField.setPreferredSize(new Dimension(340, 42));
        addPlaceholder(emailField, "Email");

        passwordField = new JPasswordField();
        passwordField.setFont(new Font("Arial", Font.PLAIN, 20));
        passwordField.setPreferredSize(new Dimension(340, 42));
        addPasswordPlaceholder(passwordField, "Password");

        confirmPasswordField = new JPasswordField();
        confirmPasswordField.setFont(new Font("Arial", Font.PLAIN, 20));
        confirmPasswordField.setPreferredSize(new Dimension(340, 42));
        addPasswordPlaceholder(confirmPasswordField, "Confirm Password");

        statusLabel = new JLabel("", SwingConstants.CENTER);
        statusLabel.setFont(new Font("Arial", Font.PLAIN, 16));
        statusLabel.setForeground(Color.WHITE);

        JButton submitButton = createButton("Create account", 28);
        submitButton.setPreferredSize(new Dimension(250, 65));
        submitButton.addActionListener(e -> registerUser());

        gbc.gridy = 0;
        formPanel.add(titleLabel, gbc);

        gbc.gridy = 1;
        gbc.insets = new Insets(18, 10, 8, 10);
        formPanel.add(registerLabel, gbc);

        gbc.gridy = 2;
        gbc.insets = new Insets(14, 10, 8, 10);
        formPanel.add(usernameField, gbc);

        gbc.gridy = 3;
        formPanel.add(emailField, gbc);

        gbc.gridy = 4;
        formPanel.add(passwordField, gbc);

        gbc.gridy = 5;
        formPanel.add(confirmPasswordField, gbc);

        gbc.gridy = 6;
        gbc.insets = new Insets(5, 10, 5, 10);
        formPanel.add(statusLabel, gbc);

        gbc.gridy = 7;
        gbc.insets = new Insets(12, 10, 10, 10);
        gbc.fill = GridBagConstraints.NONE;
        formPanel.add(submitButton, gbc);

        centerPanel.add(formPanel);

        return centerPanel;
    }

    private JPanel createBottomPanel() {
        JPanel bottomPanel = new JPanel(new BorderLayout());
        bottomPanel.setBackground(BACKGROUND);
        bottomPanel.setBorder(BorderFactory.createEmptyBorder(10, 25, 25, 25));

        JButton loginButton = createButton("Login", 24);
        loginButton.setPreferredSize(new Dimension(150, 55));
        loginButton.addActionListener(e -> {
            new LoginFrame().setVisible(true);
            dispose();
        });

        JPanel rightPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        rightPanel.setOpaque(false);
        rightPanel.add(loginButton);

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

    private void registerUser() {
        String username = usernameField.getText().trim();
        String email = emailField.getText().trim();

        char[] passwordChars = passwordField.getPassword();
        char[] confirmPasswordChars = confirmPasswordField.getPassword();

        try {
            String password = new String(passwordChars);
            String confirmPassword = new String(confirmPasswordChars);

            if (
                    username.isBlank() ||
                            email.isBlank() ||
                            password.isBlank() ||
                            confirmPassword.isBlank() ||
                            username.equals("Username") ||
                            email.equals("Email") ||
                            password.equals("Password") ||
                            confirmPassword.equals("Confirm Password")
            ) {
                statusLabel.setText("Please fill in all fields.");
                return;
            }

            if (!password.equals(confirmPassword)) {
                statusLabel.setText("Passwords do not match.");
                return;
            }

            URL url = new URL(AppConfig.endpoint("/register"));

            HttpURLConnection connection =
                    (HttpURLConnection) url.openConnection();

            connection.setRequestMethod("POST");
            connection.setRequestProperty(
                    "Content-Type",
                    "application/json"
            );
            connection.setDoOutput(true);

            String json = String.format(
                    "{\"username\":\"%s\",\"email\":\"%s\",\"password\":\"%s\"}",
                    escapeJson(username),
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
                JOptionPane.showMessageDialog(
                        this,
                        "Account created successfully. You can now login."
                );

                new LoginFrame().setVisible(true);
                dispose();

            } else {
                showRegisterError(responseBody);
            }

            connection.disconnect();

        } catch (Exception ex) {
            statusLabel.setText("Could not reach backend.");
            ex.printStackTrace();

        } finally {
            Arrays.fill(passwordChars, '\0');
            Arrays.fill(confirmPasswordChars, '\0');
        }
    }

    private void showRegisterError(String responseBody) {
        if (responseBody.contains("Username is required")) {
            statusLabel.setText("Username is required.");
        } else if (responseBody.contains("Username must be at least 3 characters")) {
            statusLabel.setText("Username must be at least 3 characters.");
        } else if (responseBody.contains("Username may not be longer than 30 characters")) {
            statusLabel.setText("Username may not be longer than 30 characters.");
        } else if (responseBody.contains("Username may only contain letters, numbers and _")) {
            statusLabel.setText("Username may only contain letters, numbers and _.");
        } else if (responseBody.contains("Email is required")) {
            statusLabel.setText("Email is required.");
        } else if (responseBody.contains("Email is too long")) {
            statusLabel.setText("Email is too long.");
        } else if (responseBody.contains("Invalid email format")) {
            statusLabel.setText("Please enter a valid email address.");
        } else if (responseBody.contains("Password must be at least 8 characters")) {
            statusLabel.setText("Password must be at least 8 characters.");
        } else if (responseBody.contains("Password may not be longer than 128 characters")) {
            statusLabel.setText("Password may not be longer than 128 characters.");
        } else if (responseBody.contains("Username already exists")) {
            statusLabel.setText("Username already exists.");
        } else if (responseBody.contains("Email already exists")) {
            statusLabel.setText("Email already exists.");
        } else {
            statusLabel.setText("Registration failed.");
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
}