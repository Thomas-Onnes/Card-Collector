package ui;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class LoggedInFrame extends JFrame {

    private static final int MAX_COLLECTIONS_PER_ROW = 5;

    private final ClientSession session;
    private final JPanel collectionsGridPanel;
    private final List<CollectionItem> collections = new ArrayList<>();

    public LoggedInFrame(ClientSession session) {
        this.session = session;

        setTitle("Card Collector - Collections");
        setMinimumSize(new Dimension(900, 600));
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        JPanel rootPanel = new JPanel(new BorderLayout());
        rootPanel.setBackground(new Color(115, 115, 115));

        rootPanel.add(createTopBar(), BorderLayout.NORTH);

        collectionsGridPanel = new JPanel(new GridLayout(0, MAX_COLLECTIONS_PER_ROW, 25, 55));
        collectionsGridPanel.setBackground(new Color(115, 115, 115));
        collectionsGridPanel.setBorder(BorderFactory.createEmptyBorder(25, 25, 25, 25));

        JScrollPane scrollPane = new JScrollPane(collectionsGridPanel);
        scrollPane.setBorder(null);
        scrollPane.getViewport().setBackground(new Color(115, 115, 115));
        scrollPane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        scrollPane.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED);

        rootPanel.add(scrollPane, BorderLayout.CENTER);
        rootPanel.add(createBottomBar(), BorderLayout.SOUTH);

        add(rootPanel);

        setSize(1000, 650);
        loadCollections();
    }

    private JPanel createTopBar() {
        JPanel topBar = new JPanel(new BorderLayout());
        topBar.setBackground(new Color(95, 95, 95));
        topBar.setBorder(BorderFactory.createLineBorder(Color.BLACK, 2));
        topBar.setPreferredSize(new Dimension(1000, 55));

        JLabel welcomeLabel = new JLabel("Welcome: " + session.getUsername());
        welcomeLabel.setFont(new Font("Arial", Font.PLAIN, 22));
        welcomeLabel.setForeground(Color.WHITE);
        welcomeLabel.setBorder(BorderFactory.createEmptyBorder(0, 15, 0, 0));

        JButton signOutButton = new JButton("Sign out");
        signOutButton.setFont(new Font("Arial", Font.PLAIN, 20));
        signOutButton.setBackground(new Color(45, 45, 45));
        signOutButton.setForeground(Color.WHITE);
        signOutButton.setFocusPainted(false);
        signOutButton.setPreferredSize(new Dimension(130, 45));

        signOutButton.addActionListener(e -> {
            logout();
            new LoginFrame().setVisible(true);
            dispose();
        });

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 5));
        buttonPanel.setOpaque(false);
        buttonPanel.add(signOutButton);

        topBar.add(welcomeLabel, BorderLayout.WEST);
        topBar.add(buttonPanel, BorderLayout.EAST);

        return topBar;
    }

    private JPanel createBottomBar() {
        JPanel bottomBar = new JPanel(new BorderLayout());
        bottomBar.setBackground(new Color(115, 115, 115));
        bottomBar.setBorder(BorderFactory.createEmptyBorder(15, 25, 20, 25));

        JButton addButton = new JButton("Add Collection");
        addButton.setFont(new Font("Arial", Font.PLAIN, 28));
        addButton.setBackground(new Color(45, 45, 45));
        addButton.setForeground(Color.WHITE);
        addButton.setFocusPainted(false);
        addButton.setPreferredSize(new Dimension(250, 60));

        addButton.addActionListener(e -> showCreateCollectionPopup());

        JPanel leftPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        leftPanel.setOpaque(false);
        leftPanel.add(addButton);

        bottomBar.add(leftPanel, BorderLayout.WEST);

        return bottomBar;
    }

    private void loadCollections() {
        try {
            URL url = new URL(AppConfig.endpoint("/collections"));

            HttpURLConnection connection =
                    (HttpURLConnection) url.openConnection();

            connection.setRequestMethod("GET");
            connection.setRequestProperty(
                    "Authorization",
                    "Bearer " + session.getToken()
            );

            int responseCode = connection.getResponseCode();
            String responseBody = readResponse(connection);

            if (responseCode == 200) {
                collections.clear();
                collections.addAll(parseCollections(responseBody));
                renderCollections();

            } else if (responseCode == 401) {
                JOptionPane.showMessageDialog(
                        this,
                        "Your session expired. Please login again."
                );

                new LoginFrame().setVisible(true);
                dispose();

            } else {
                JOptionPane.showMessageDialog(
                        this,
                        "Could not load collections."
                );
            }

            connection.disconnect();

        } catch (Exception e) {
            JOptionPane.showMessageDialog(
                    this,
                    "Could not reach backend."
            );

            e.printStackTrace();
        }
    }

    private void renderCollections() {
        collectionsGridPanel.removeAll();

        for (CollectionItem collection : collections) {
            JPanel wrapper = new JPanel(new BorderLayout());
            wrapper.setOpaque(false);

            JPanel cardPanel = createCollectionCard(collection);

            JButton deleteButton = new JButton("Delete");
            deleteButton.setFont(new Font("Arial", Font.PLAIN, 15));
            deleteButton.setBackground(new Color(255, 120, 120));
            deleteButton.setForeground(Color.BLACK);
            deleteButton.setFocusPainted(false);
            deleteButton.setPreferredSize(new Dimension(90, 35));

            deleteButton.addActionListener(e ->
                    confirmDeleteCollection(collection)
            );

            JPanel deletePanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 0, 10));
            deletePanel.setOpaque(false);
            deletePanel.add(deleteButton);

            wrapper.add(cardPanel, BorderLayout.CENTER);
            wrapper.add(deletePanel, BorderLayout.SOUTH);

            collectionsGridPanel.add(wrapper);
        }

        collectionsGridPanel.revalidate();
        collectionsGridPanel.repaint();
    }

    private JPanel createCollectionCard(CollectionItem collection) {
        JPanel cardPanel = new JPanel(new BorderLayout());
        cardPanel.setPreferredSize(new Dimension(160, 250));
        cardPanel.setBorder(BorderFactory.createLineBorder(Color.BLACK, 2));
        cardPanel.setBackground(new Color(175, 210, 245));
        cardPanel.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

        JPanel emptyArea = new JPanel();
        emptyArea.setBackground(new Color(175, 210, 245));

        JLabel nameLabel = new JLabel(collection.getCollectionName());
        nameLabel.setFont(new Font("Arial", Font.PLAIN, 18));
        nameLabel.setOpaque(true);
        nameLabel.setBackground(new Color(170, 180, 190));
        nameLabel.setForeground(Color.BLACK);
        nameLabel.setBorder(BorderFactory.createEmptyBorder(8, 8, 8, 8));

        cardPanel.add(emptyArea, BorderLayout.CENTER);
        cardPanel.add(nameLabel, BorderLayout.SOUTH);

        cardPanel.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                new CollectionDetailFrame(
                        session,
                        collection
                ).setVisible(true);

                dispose();
            }
        });

        return cardPanel;
    }

    private void showCreateCollectionPopup() {
        JDialog dialog = new JDialog(this, "Add Collection", true);
        dialog.setMinimumSize(new Dimension(380, 260));
        dialog.setLocationRelativeTo(this);

        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBackground(new Color(160, 160, 160));
        panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(6, 6, 6, 6);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1.0;

        JTextField nameField = new JTextField();
        nameField.setFont(new Font("Arial", Font.PLAIN, 20));
        nameField.setPreferredSize(new Dimension(280, 38));

        JLabel nameLabel = new JLabel("Collection name:");
        nameLabel.setFont(new Font("Arial", Font.PLAIN, 18));

        JLabel typeLabel = new JLabel("Collection type:");
        typeLabel.setFont(new Font("Arial", Font.PLAIN, 18));

        JComboBox<String> typeBox =
                new JComboBox<>(
                        new String[]{
                                "Pokemon",
                                "MTG"
                        }
                );

        typeBox.setFont(new Font("Arial", Font.PLAIN, 20));
        typeBox.setPreferredSize(new Dimension(180, 38));

        JLabel statusLabel = new JLabel("");
        statusLabel.setFont(new Font("Arial", Font.PLAIN, 13));
        statusLabel.setForeground(Color.RED);

        JButton createButton = new JButton("Create");
        createButton.setFont(new Font("Arial", Font.PLAIN, 20));
        createButton.setBackground(new Color(45, 45, 45));
        createButton.setForeground(Color.WHITE);
        createButton.setFocusPainted(false);

        gbc.gridx = 0;
        gbc.gridy = 0;
        panel.add(nameLabel, gbc);

        gbc.gridy = 1;
        panel.add(nameField, gbc);

        gbc.gridy = 2;
        panel.add(typeLabel, gbc);

        gbc.gridy = 3;
        panel.add(typeBox, gbc);

        gbc.gridy = 4;
        panel.add(statusLabel, gbc);

        gbc.gridy = 5;
        gbc.anchor = GridBagConstraints.EAST;
        gbc.fill = GridBagConstraints.NONE;
        panel.add(createButton, gbc);

        createButton.addActionListener(e -> {
            String collectionName = nameField.getText().trim();
            String selectedType = (String) typeBox.getSelectedItem();

            if (collectionName.isBlank()) {
                statusLabel.setText("Collection name is required.");
                return;
            }

            if (collectionName.length() < 3) {
                statusLabel.setText("Name must be at least 3 characters.");
                return;
            }

            if (collectionName.length() > 50) {
                statusLabel.setText("Name may not be longer than 50 characters.");
                return;
            }

            if (!collectionName.matches("[\\p{L}0-9 _-]+")) {
                statusLabel.setText("Name contains invalid characters.");
                return;
            }

            String gameType =
                    "MTG".equals(selectedType)
                            ? "mtg"
                            : "pokemon";

            boolean created =
                    createCollection(
                            collectionName,
                            gameType
                    );

            if (created) {
                dialog.dispose();
                loadCollections();
            } else {
                statusLabel.setText("Could not create collection.");
            }
        });

        dialog.add(panel);
        dialog.pack();
        dialog.setLocationRelativeTo(this);
        dialog.setVisible(true);
    }

    private boolean createCollection(
            String collectionName,
            String gameType
    ) {
        try {
            URL url = new URL(AppConfig.endpoint("/collections"));

            HttpURLConnection connection =
                    (HttpURLConnection) url.openConnection();

            connection.setRequestMethod("POST");
            connection.setRequestProperty(
                    "Content-Type",
                    "application/json"
            );
            connection.setRequestProperty(
                    "Authorization",
                    "Bearer " + session.getToken()
            );
            connection.setDoOutput(true);

            String json =
                    String.format(
                            "{\"collectionName\":\"%s\",\"gameType\":\"%s\"}",
                            escapeJson(collectionName),
                            escapeJson(gameType)
                    );

            try (OutputStream outputStream = connection.getOutputStream()) {
                byte[] input = json.getBytes(StandardCharsets.UTF_8);
                outputStream.write(input, 0, input.length);
            }

            int responseCode = connection.getResponseCode();

            readResponse(connection);
            connection.disconnect();

            return responseCode >= 200 && responseCode < 300;

        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    private void confirmDeleteCollection(CollectionItem collection) {
        int result =
                JOptionPane.showConfirmDialog(
                        this,
                        "Are you sure you want to delete "
                                + collection.getCollectionName()
                                + "? Deleting this means all the added cards in the collection will be deleted aswell.",
                        "Delete collection",
                        JOptionPane.YES_NO_OPTION,
                        JOptionPane.WARNING_MESSAGE
                );

        if (result == JOptionPane.YES_OPTION) {
            boolean deleted =
                    deleteCollection(
                            collection.getCollectionId()
                    );

            if (deleted) {
                loadCollections();
            } else {
                JOptionPane.showMessageDialog(
                        this,
                        "Could not delete collection."
                );
            }
        }
    }

    private boolean deleteCollection(int collectionId) {
        try {
            URL url =
                    new URL(
                            AppConfig.endpoint(
                                    "/collections/" + collectionId
                            )
                    );

            HttpURLConnection connection =
                    (HttpURLConnection) url.openConnection();

            connection.setRequestMethod("DELETE");
            connection.setRequestProperty(
                    "Authorization",
                    "Bearer " + session.getToken()
            );

            int responseCode = connection.getResponseCode();

            readResponse(connection);
            connection.disconnect();

            return responseCode >= 200 && responseCode < 300;

        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    private void logout() {
        try {
            URL url = new URL(AppConfig.endpoint("/logout"));

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
        }
    }

    private List<CollectionItem> parseCollections(String json) {
        List<CollectionItem> result = new ArrayList<>();

        String collectionJson = json;

        int collectionsKeyIndex = json.indexOf("\"collections\"");
        if (collectionsKeyIndex != -1) {
            int arrayStart = json.indexOf("[", collectionsKeyIndex);
            int arrayEnd = json.lastIndexOf("]");

            if (arrayStart != -1 && arrayEnd != -1 && arrayEnd > arrayStart) {
                collectionJson = json.substring(arrayStart, arrayEnd + 1);
            }
        }

        List<String> objects = extractJsonObjects(collectionJson);

        for (String object : objects) {
            String idText =
                    extractJsonValue(
                            object,
                            "collectionId"
                    );

            String collectionName =
                    extractJsonValue(
                            object,
                            "collectionName"
                    );

            String gameType =
                    extractJsonValue(
                            object,
                            "gameType"
                    );

            if (
                    !idText.isBlank() &&
                            !collectionName.isBlank() &&
                            !gameType.isBlank()
            ) {
                result.add(
                        new CollectionItem(
                                Integer.parseInt(idText),
                                collectionName,
                                gameType
                        )
                );
            }
        }

        return result;
    }

    private List<String> extractJsonObjects(String json) {
        List<String> objects = new ArrayList<>();

        boolean inString = false;
        boolean escaping = false;
        int objectDepth = 0;
        int objectStart = -1;

        for (int i = 0; i < json.length(); i++) {
            char c = json.charAt(i);

            if (escaping) {
                escaping = false;
                continue;
            }

            if (c == '\\') {
                escaping = true;
                continue;
            }

            if (c == '"') {
                inString = !inString;
                continue;
            }

            if (!inString) {
                if (c == '{') {
                    if (objectDepth == 0) {
                        objectStart = i;
                    }

                    objectDepth++;

                } else if (c == '}') {
                    objectDepth--;

                    if (objectDepth == 0 && objectStart != -1) {
                        objects.add(
                                json.substring(
                                        objectStart,
                                        i + 1
                                )
                        );

                        objectStart = -1;
                    }
                }
            }
        }

        return objects;
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
}