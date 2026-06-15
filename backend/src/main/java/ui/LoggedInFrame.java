package ui;

import javax.swing.*;
import java.awt.*;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;

public class LoggedInFrame extends JFrame {

    private final ClientSession session;

    public LoggedInFrame(ClientSession session) {
        this.session = session;

        setTitle("Card Collector - Logged in");
        setSize(1000, 650);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        JPanel mainPanel = new JPanel(null);
        mainPanel.setBackground(new Color(115, 115, 115));

        JLabel titleLabel = new JLabel("Card Collector");
        titleLabel.setFont(new Font("Arial", Font.ITALIC, 54));
        titleLabel.setForeground(new Color(255, 165, 90));
        titleLabel.setBounds(330, 90, 500, 80);
        mainPanel.add(titleLabel);

        JLabel loggedInLabel = new JLabel("Logged in");
        loggedInLabel.setFont(new Font("Arial", Font.PLAIN, 44));
        loggedInLabel.setForeground(Color.WHITE);
        loggedInLabel.setBounds(400, 240, 300, 70);
        mainPanel.add(loggedInLabel);

        JLabel usernameLabel =
                new JLabel("User: " + session.getUsername());

        usernameLabel.setFont(new Font("Arial", Font.PLAIN, 28));
        usernameLabel.setForeground(Color.WHITE);
        usernameLabel.setBounds(390, 320, 400, 50);
        mainPanel.add(usernameLabel);

        JButton logoutButton = new JButton("Logout");
        logoutButton.setFont(new Font("Arial", Font.PLAIN, 30));
        logoutButton.setBackground(new Color(45, 45, 45));
        logoutButton.setForeground(Color.WHITE);
        logoutButton.setFocusPainted(false);
        logoutButton.setBounds(410, 430, 180, 70);

        logoutButton.addActionListener(e -> {
            logout();
            new LoginFrame().setVisible(true);
            dispose();
        });

        mainPanel.add(logoutButton);

        add(mainPanel);
    }

    private void logout() {
        try {
            URL url =
                    new URL(
                            AppConfig.endpoint("/logout")
                    );

            HttpURLConnection connection =
                    (HttpURLConnection) url.openConnection();

            connection.setRequestMethod("POST");
            connection.setRequestProperty(
                    "Authorization",
                    "Bearer " + session.getToken()
            );

            connection.setDoOutput(true);

            try (OutputStream os = connection.getOutputStream()) {
                os.write(new byte[0]);
            }

            connection.getResponseCode();
            connection.disconnect();

        } catch (Exception ignored) {
            // Even if backend logout fails, local screen returns to login.
        }
    }
}