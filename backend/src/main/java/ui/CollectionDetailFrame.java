package ui;

import javax.swing.*;
import java.awt.*;

public class CollectionDetailFrame extends JFrame {

    private final ClientSession session;
    private final CollectionItem collection;

    public CollectionDetailFrame(
            ClientSession session,
            CollectionItem collection
    ) {
        this.session = session;
        this.collection = collection;

        setTitle("Card Collector - " + collection.getCollectionName());
        setMinimumSize(new Dimension(900, 600));
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        JPanel rootPanel = new JPanel(new BorderLayout());
        rootPanel.setBackground(new Color(115, 115, 115));

        rootPanel.add(createTopBar(), BorderLayout.NORTH);
        rootPanel.add(createCenterPanel(), BorderLayout.CENTER);

        add(rootPanel);

        setSize(1000, 650);
    }

    private JPanel createTopBar() {
        JPanel topBar = new JPanel(new BorderLayout());
        topBar.setBackground(new Color(95, 95, 95));
        topBar.setBorder(BorderFactory.createLineBorder(Color.BLACK, 2));
        topBar.setPreferredSize(new Dimension(1000, 55));

        JButton backButton = new JButton("Back");
        backButton.setFont(new Font("Arial", Font.PLAIN, 20));
        backButton.setBackground(new Color(45, 45, 45));
        backButton.setForeground(Color.WHITE);
        backButton.setFocusPainted(false);
        backButton.setPreferredSize(new Dimension(120, 45));

        backButton.addActionListener(e -> {
            new LoggedInFrame(session).setVisible(true);
            dispose();
        });

        JLabel welcomeLabel = new JLabel("Welcome: " + session.getUsername());
        welcomeLabel.setFont(new Font("Arial", Font.PLAIN, 22));
        welcomeLabel.setForeground(Color.WHITE);
        welcomeLabel.setBorder(BorderFactory.createEmptyBorder(0, 15, 0, 0));

        JPanel leftPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 5));
        leftPanel.setOpaque(false);
        leftPanel.add(backButton);
        leftPanel.add(welcomeLabel);

        topBar.add(leftPanel, BorderLayout.WEST);

        return topBar;
    }

    private JPanel createCenterPanel() {
        JPanel centerPanel = new JPanel(new GridBagLayout());
        centerPanel.setBackground(new Color(115, 115, 115));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);
        gbc.gridx = 0;
        gbc.fill = GridBagConstraints.HORIZONTAL;

        JLabel collectionNameLabel =
                new JLabel(
                        collection.getCollectionName(),
                        SwingConstants.CENTER
                );

        collectionNameLabel.setFont(new Font("Arial", Font.PLAIN, 46));
        collectionNameLabel.setForeground(Color.WHITE);

        JLabel typeLabel =
                new JLabel(
                        "Type: " + displayGameType(collection.getGameType()),
                        SwingConstants.CENTER
                );

        typeLabel.setFont(new Font("Arial", Font.PLAIN, 28));
        typeLabel.setForeground(Color.WHITE);

        JLabel placeholderLabel =
                new JLabel(
                        "Cards will be added here later.",
                        SwingConstants.CENTER
                );

        placeholderLabel.setFont(new Font("Arial", Font.PLAIN, 22));
        placeholderLabel.setForeground(Color.LIGHT_GRAY);

        gbc.gridy = 0;
        centerPanel.add(collectionNameLabel, gbc);

        gbc.gridy = 1;
        centerPanel.add(typeLabel, gbc);

        gbc.gridy = 2;
        centerPanel.add(placeholderLabel, gbc);

        return centerPanel;
    }

    private String displayGameType(String gameType) {
        if ("mtg".equalsIgnoreCase(gameType)) {
            return "Magic: The Gathering";
        }

        return "Pokemon";
    }
}