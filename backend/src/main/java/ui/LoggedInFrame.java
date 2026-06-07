package ui;

import javax.swing.*;
import java.awt.*;

public class LoggedInFrame extends JFrame {

    public LoggedInFrame() {
        setTitle("Card Collector - Logged in");
        setSize(1000, 650);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        JPanel mainPanel = new JPanel(null);
        mainPanel.setBackground(new Color(115, 115, 115));

        JLabel titleLabel = new JLabel("Card Collector");
        titleLabel.setFont(new Font("Arial", Font.ITALIC, 54));
        titleLabel.setForeground(new Color(255, 165, 90));
        titleLabel.setBounds(330, 100, 500, 80);
        mainPanel.add(titleLabel);

        JLabel loggedInLabel = new JLabel("Logged in");
        loggedInLabel.setFont(new Font("Arial", Font.PLAIN, 44));
        loggedInLabel.setForeground(Color.WHITE);
        loggedInLabel.setBounds(400, 260, 300, 70);
        mainPanel.add(loggedInLabel);

        JButton logoutButton = new JButton("Logout");
        logoutButton.setFont(new Font("Arial", Font.PLAIN, 30));
        logoutButton.setBackground(new Color(45, 45, 45));
        logoutButton.setForeground(Color.WHITE);
        logoutButton.setFocusPainted(false);
        logoutButton.setBounds(410, 390, 180, 70);

        logoutButton.addActionListener(e -> {
            new LoginFrame().setVisible(true);
            dispose();
        });

        mainPanel.add(logoutButton);

        add(mainPanel);
    }
}