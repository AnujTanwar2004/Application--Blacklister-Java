import javax.swing.*;
import java.io.*;
import java.util.*;
import java.awt.image.BufferedImage;
import java.awt.*;
import javax.swing.border.EmptyBorder;


public class ModernSoftwareBlocker {
    private Set<String> blockedApps = new HashSet<>();
    private javax.swing.Timer blockTimer;
    private String appPassword = "admin123";
    private File blockListFile = new File("blocked_apps.txt");

 
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new ModernSoftwareBlocker().showLoginScreen());
    }

    private void showLoginScreen() {
        JPasswordField passwordField = new JPasswordField();
        Object[] message = {"Enter Password:", passwordField};
        int option = JOptionPane.showConfirmDialog(null, message, "Login", JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
        if (option == JOptionPane.OK_OPTION) {
            if (String.valueOf(passwordField.getPassword()).equals(appPassword)) {
                loadBlockedApps();
                startBlockingService();

                BlockerGUI gui = new BlockerGUI(
                    blockedApps,
                    app -> {
                        blockedApps.add(app);
                        saveBlockedApps();
                    },
                    app -> {
                        blockedApps.remove(app);
                        saveBlockedApps();
                    }
                );

                gui.show();
            } else {
                JOptionPane.showMessageDialog(null, "Incorrect Password! Exiting...", "Error", JOptionPane.ERROR_MESSAGE);
                System.exit(0);
            }
        } else {
            System.exit(0);
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
                System.out.println(line);
                String[] parts = line.split(",");
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

    public static class AppListRenderer extends DefaultListCellRenderer {
        @Override
        public Component getListCellRendererComponent(JList<?> list, Object value, int index,
                                                      boolean isSelected, boolean cellHasFocus) {
            JLabel label = (JLabel) super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
            String appName = (String) value;
            label.setIcon(loadAppIcon(appName));
            label.setText("<html><center>" + appName + "</center></html>");
            label.setHorizontalTextPosition(SwingConstants.CENTER);
            label.setVerticalTextPosition(SwingConstants.BOTTOM);
            label.setIconTextGap(5);
            label.setBorder(new EmptyBorder(5, 5, 5, 5));
            label.setHorizontalAlignment(SwingConstants.CENTER);
            return label;
        }

        private static ImageIcon loadAppIcon(String appName) {
            String basePath = "C:\\Users\\Anuj\\OneDrive\\Desktop\\JAVA PROJECT\\icons/";
            String iconName = appName.replace(".exe", "").toLowerCase();

            File folder = new File(basePath);
            File[] files = folder.listFiles();
            if (files != null) {
                for (File file : files) {
                    String fileName = file.getName().toLowerCase();
                    if (fileName.startsWith(iconName) && fileName.endsWith(".png")) {
                        ImageIcon icon = new ImageIcon(file.getAbsolutePath());
                        Image scaled = icon.getImage().getScaledInstance(48, 48, Image.SCALE_SMOOTH);
                        return new ImageIcon(scaled);
                    }
                }
            }

            BufferedImage placeholder = new BufferedImage(48, 48, BufferedImage.TYPE_INT_ARGB);
            Graphics2D g = placeholder.createGraphics();
            g.setColor(Color.GRAY);
            g.fillOval(8, 8, 32, 32);
            g.dispose();
            return new ImageIcon(placeholder);
        }
    }
}
