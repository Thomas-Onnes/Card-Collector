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
        statusLabel.setBounds(355, 415, 400, 30);
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
                statusLabel.setText("Password reset komt later.")
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
        String email = emailField.getText();
        String password = new String(passwordField.getPassword());

        if (
                email.isBlank() ||
                        password.isBlank() ||
                        email.equals("Email") ||
                        password.equals("Password")
        ) {
            statusLabel.setText("Vul alle velden in.");
            return;
        }

        try {
            URL url = new URL("http://localhost:8080/login");
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
                new LoggedInFrame().setVisible(true);
                dispose();
            } else {
                statusLabel.setText("Email of wachtwoord is onjuist.");
            }

            connection.disconnect();

        } catch (Exception ex) {
            statusLabel.setText("Kan backend niet bereiken.");
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

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            new LoginFrame().setVisible(true);
        });
    }
}