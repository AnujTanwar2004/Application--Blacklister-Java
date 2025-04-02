import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.util.*;
import java.util.List;

public class SoftwareBlocker {
    private JFrame frame;
    private JList<String> appList;
    private DefaultListModel<String> listModel;
    private Set<String> blockedApps = new HashSet<>();
    private javax.swing.Timer blockTimer;
    private String appPassword = "admin123";  // Hardcoded login password
    private String blockPassword = "1234";  // Password for blocking/unblocking apps
    private File blockListFile = new File("blocked_apps.txt");

    // Common applications to block
    private String[] commonApps = {
        "brave.exe", "firefox.exe", "spotify.exe", 
        "chrome.exe", "msedge.exe", "discord.exe"
    };

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            new SoftwareBlocker().showLoginScreen();
        });
    }

    private void showLoginScreen() {
        JPasswordField passwordField = new JPasswordField();
        Object[] message = {
            "Enter Password:", passwordField
        };

        int option = JOptionPane.showConfirmDialog(null, message, "Login", JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
        if (option == JOptionPane.OK_OPTION) {
            if (String.valueOf(passwordField.getPassword()).equals(appPassword)) {
                initialize();
                loadBlockedApps();
                startBlockingService();
                frame.setVisible(true);
            } else {
                JOptionPane.showMessageDialog(null, "Incorrect Password! Exiting...", "Error", JOptionPane.ERROR_MESSAGE);
                System.exit(0);
            }
        } else {
            System.exit(0);
        }
    }

    private void initialize() {
        frame = new JFrame();
        frame.setTitle("Software Blocker");
        frame.setBounds(100, 100, 500, 450);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.getContentPane().setBackground(new Color(30, 144, 255));

        JPanel mainPanel = new JPanel();
        mainPanel.setBackground(new Color(30, 144, 255));
        mainPanel.setLayout(new BorderLayout(10, 10));
        frame.getContentPane().add(mainPanel);

        JLabel titleLabel = new JLabel("Application Blocker");
        titleLabel.setFont(new Font("Arial", Font.BOLD, 20));
        titleLabel.setForeground(Color.WHITE);
        titleLabel.setHorizontalAlignment(SwingConstants.CENTER);
        mainPanel.add(titleLabel, BorderLayout.NORTH);

        listModel = new DefaultListModel<>();
        loadInstalledApps();

        appList = new JList<>(listModel);
        appList.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
        appList.setBackground(new Color(240, 248, 255));
        appList.setCellRenderer(new BlockedAppRenderer());

        JScrollPane scrollPane = new JScrollPane(appList);
        mainPanel.add(scrollPane, BorderLayout.CENTER);

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 10));
        buttonPanel.setBackground(new Color(30, 144, 255));

        JButton blockButton = createStyledButton("Block Selected", e -> blockSelectedApps());
        JButton unblockButton = createStyledButton("Unblock Selected", e -> unblockSelectedApps());
        JButton refreshButton = createStyledButton("Refresh List", e -> loadInstalledApps());
        JButton settingsButton = createStyledButton("Settings", e -> openSettings());

        buttonPanel.add(blockButton);
        buttonPanel.add(unblockButton);
        buttonPanel.add(refreshButton);
        buttonPanel.add(settingsButton);

        mainPanel.add(buttonPanel, BorderLayout.SOUTH);
    }

    private JButton createStyledButton(String text, ActionListener listener) {
        JButton button = new JButton(text);
        button.setBackground(new Color(0, 191, 255));
        button.setForeground(Color.WHITE);
        button.setFocusPainted(false);
        button.addActionListener(listener);
        return button;
    }

    private void loadInstalledApps() {
        listModel.clear();
        for (String app : commonApps) {
            listModel.addElement(app);
        }
    }

    private void loadBlockedApps() {
        if (blockListFile.exists()) {
            try (BufferedReader reader = new BufferedReader(new FileReader(blockListFile))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    blockedApps.add(line.trim());
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private void saveBlockedApps() {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(blockListFile))) {
            for (String app : blockedApps) {
                writer.write(app);
                writer.newLine();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void startBlockingService() {
        blockTimer = new javax.swing.Timer(2000, e -> enforceBlocking());
        blockTimer.start();
    }

    private void enforceBlocking() {
        if (blockedApps.isEmpty()) return;

        try {
            Process process = Runtime.getRuntime().exec("tasklist /fo csv /nh");
            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));

            String line;
            while ((line = reader.readLine()) != null) {
                String[] parts = line.split("\",\"");
                if (parts.length > 0) {
                    String exeName = parts[0].replace("\"", "").toLowerCase();

                    for (String blockedApp : blockedApps) {
                        if (exeName.equals(blockedApp.toLowerCase())) {
                            Runtime.getRuntime().exec("taskkill /f /im " + blockedApp);
                            break;
                        }
                    }
                }
            }
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    private void blockSelectedApps() {
        String input = JOptionPane.showInputDialog(frame, "Enter password to block:");
        if (input == null || !input.equals(blockPassword)) {
            JOptionPane.showMessageDialog(frame, "Incorrect password!", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        for (String app : appList.getSelectedValuesList()) {
            blockedApps.add(app);
            JOptionPane.showMessageDialog(frame, "Blocked: " + app, "Success", JOptionPane.INFORMATION_MESSAGE);
        }
        saveBlockedApps();
    }

    private void unblockSelectedApps() {
        String input = JOptionPane.showInputDialog(frame, "Enter password to unblock:");
        if (input == null || !input.equals(blockPassword)) {
            JOptionPane.showMessageDialog(frame, "Incorrect password!", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        for (String app : appList.getSelectedValuesList()) {
            blockedApps.remove(app);
            JOptionPane.showMessageDialog(frame, "Unblocked: " + app, "Success", JOptionPane.INFORMATION_MESSAGE);
        }
        saveBlockedApps();
    }

    private void openSettings() {
        JOptionPane.showMessageDialog(frame, "Settings feature coming soon!", "Info", JOptionPane.INFORMATION_MESSAGE);
    }

    // Custom renderer to highlight blocked apps
    class BlockedAppRenderer extends DefaultListCellRenderer {
        @Override
        public Component getListCellRendererComponent(JList<?> list, Object value, int index, 
                                                      boolean isSelected, boolean cellHasFocus) {
            Component c = super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);

            if (blockedApps.contains(value.toString())) {
                c.setBackground(Color.RED);
                c.setForeground(Color.WHITE);
            }
            return c;
        }
    }
}
