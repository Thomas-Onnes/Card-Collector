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
import java.util.stream.Collectors;

public class RegisterFrame extends JFrame {

    private final JTextField usernameField;
    private final JTextField emailField;
    private final JPasswordField passwordField;
    private final JPasswordField confirmPasswordField;
    private final JLabel statusLabel;

    public RegisterFrame() {
        setTitle("Card Collector - Sign up");
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

        JLabel signupLabel = new JLabel("Sign up");
        signupLabel.setFont(new Font("Arial", Font.PLAIN, 42));
        signupLabel.setForeground(Color.WHITE);
        signupLabel.setBounds(430, 190, 250, 60);
        mainPanel.add(signupLabel);

        usernameField = new JTextField();
        usernameField.setFont(new Font("Arial", Font.PLAIN, 20));
        usernameField.setBounds(365, 270, 300, 40);
        addPlaceholder(usernameField, "Username");
        mainPanel.add(usernameField);

        emailField = new JTextField();
        emailField.setFont(new Font("Arial", Font.PLAIN, 20));
        emailField.setBounds(365, 340, 300, 40);
        addPlaceholder(emailField, "Email");
        mainPanel.add(emailField);

        passwordField = new JPasswordField();
        passwordField.setFont(new Font("Arial", Font.PLAIN, 20));
        passwordField.setBounds(365, 410, 300, 40);
        addPasswordPlaceholder(passwordField, "Password");
        mainPanel.add(passwordField);

        confirmPasswordField = new JPasswordField();
        confirmPasswordField.setFont(new Font("Arial", Font.PLAIN, 20));
        confirmPasswordField.setBounds(365, 480, 300, 40);
        addPasswordPlaceholder(confirmPasswordField, "Confirm Password");
        mainPanel.add(confirmPasswordField);

        statusLabel = new JLabel("");
        statusLabel.setFont(new Font("Arial", Font.PLAIN, 18));
        statusLabel.setForeground(Color.WHITE);
        statusLabel.setBounds(365, 525, 430, 25);
        mainPanel.add(statusLabel);

        JButton submitButton = new JButton("Submit");
        submitButton.setFont(new Font("Arial", Font.PLAIN, 30));
        submitButton.setBackground(new Color(45, 45, 45));
        submitButton.setForeground(Color.WHITE);
        submitButton.setFocusPainted(false);
        submitButton.setBounds(420, 550, 170, 65);
        submitButton.addActionListener(e -> registerUser());
        mainPanel.add(submitButton);

        JButton backButton = new JButton("Back");
        backButton.setFont(new Font("Arial", Font.PLAIN, 30));
        backButton.setBackground(new Color(45, 45, 45));
        backButton.setForeground(Color.WHITE);
        backButton.setFocusPainted(false);
        backButton.setBounds(40, 540, 160, 70);
        backButton.addActionListener(e -> {
            new LoginFrame().setVisible(true);
            dispose();
        });
        mainPanel.add(backButton);

        add(mainPanel);
    }

    private void registerUser() {
        String username = usernameField.getText();
        String email = emailField.getText();
        String password =
                new String(passwordField.getPassword());

        String confirmPassword =
                new String(confirmPasswordField.getPassword());

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
            statusLabel.setText("Fill in all fields.");
            return;
        }

        if (!password.equals(confirmPassword)) {
            statusLabel.setText("Password does not match.");
            return;
        }

        if (password.length() < 8) {
            statusLabel.setText("Password must be at least 8 characters.");
            return;
        }

        try {
            URL url = new URL("http://localhost:8080/register");
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
                byte[] input =
                        json.getBytes(StandardCharsets.UTF_8);

                os.write(input, 0, input.length);
            }

            int responseCode = connection.getResponseCode();
            String responseBody = readResponse(connection);

            if (responseCode >= 200 && responseCode < 300) {
                statusLabel.setText("Registrated Succesfully!");

                JOptionPane.showMessageDialog(
                        this,
                        "Account has been made.."
                );

                new LoginFrame().setVisible(true);
                dispose();

            } else {
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

            connection.disconnect();

        } catch (Exception ex) {
            statusLabel.setText("Cannot reach backend.");
            ex.printStackTrace();
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
                .replace("\"", "\\\"");
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
                String password = new String(field.getPassword());

                if (password.equals(placeholder)) {
                    field.setText("");
                    field.setForeground(Color.BLACK);
                    field.setEchoChar('•');
                }
            }

            @Override
            public void focusLost(FocusEvent e) {
                String password = new String(field.getPassword());

                if (password.isBlank()) {
                    field.setEchoChar((char) 0);
                    field.setText(placeholder);
                    field.setForeground(Color.GRAY);
                }
            }
        });
    }
}