package ui;

import javax.swing.*;
import java.awt.*;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;

public class RegisterFrame extends JFrame {
    private final JTextField usernameField;
    private final JTextField emailField;
    private final JPasswordField passwordField;
    private final JLabel statusLabel;

    public RegisterFrame() {
        setTitle("Card Collector - Register");
        setSize(400, 260);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        JPanel panel = new JPanel(new GridLayout(5, 2, 10, 10));
        panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        usernameField = new JTextField();
        emailField = new JTextField();
        passwordField = new JPasswordField();
        statusLabel = new JLabel("");

        JButton registerButton = new JButton("Register");
        registerButton.addActionListener(e -> registerUser());

        panel.add(new JLabel("Username:"));
        panel.add(usernameField);

        panel.add(new JLabel("Email:"));
        panel.add(emailField);

        panel.add(new JLabel("Password:"));
        panel.add(passwordField);

        panel.add(new JLabel(""));
        panel.add(registerButton);

        panel.add(new JLabel("Status:"));
        panel.add(statusLabel);

        add(panel);
    }

    private void registerUser() {
        String username = usernameField.getText();
        String email = emailField.getText();
        String password = new String(passwordField.getPassword());

        if (username.isBlank() || email.isBlank() || password.isBlank()) {
            statusLabel.setText("Vul alle velden in.");
            return;
        }

        try {
            URL url = new URL("http://localhost:8080/register");
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();

            connection.setRequestMethod("POST");
            connection.setRequestProperty("Content-Type", "application/json");
            connection.setDoOutput(true);

            String json = String.format(
                    "{\"username\":\"%s\",\"email\":\"%s\",\"password\":\"%s\"}",
                    username,
                    email,
                    password
            );

            try (OutputStream os = connection.getOutputStream()) {
                byte[] input = json.getBytes(StandardCharsets.UTF_8);
                os.write(input, 0, input.length);
            }

            int responseCode = connection.getResponseCode();

            if (responseCode >= 200 && responseCode < 300) {
                statusLabel.setText("Registratie gelukt!");
            } else {
                statusLabel.setText("Fout: HTTP " + responseCode);
            }

            connection.disconnect();

        } catch (Exception ex) {
            statusLabel.setText("Kan backend niet bereiken.");
            ex.printStackTrace();
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            new RegisterFrame().setVisible(true);
        });
    }
}