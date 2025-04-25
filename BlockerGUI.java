import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import javax.swing.border.EmptyBorder;
import java.util.Set;
import java.util.function.Consumer;

public class BlockerGUI {
    private JFrame frame;
    private JList<String> appList;
    private DefaultListModel<String> listModel;

    private final String[] commonApps = {"brave.exe", "firefox.exe", "spotify.exe", "chrome.exe", "msedge.exe", "discord.exe"};

    public BlockerGUI(Set<String> blockedApps, Consumer<String> onBlock, Consumer<String> onUnblock) {
        initializeGUI(blockedApps, onBlock, onUnblock);
    }

    private void initializeGUI(Set<String> blockedApps, Consumer<String> onBlock, Consumer<String> onUnblock) {
        frame = new JFrame("Modern Software Blocker");
        frame.setSize(620, 500);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setLayout(new BorderLayout());
        frame.setLocationRelativeTo(null);
        frame.getContentPane().setBackground(new Color(25, 25, 25));

        JPanel sidePanel = new JPanel();
        sidePanel.setBackground(new Color(40, 40, 40));
        sidePanel.setPreferredSize(new Dimension(150, frame.getHeight()));
        frame.add(sidePanel, BorderLayout.WEST);

        JLabel titleLabel = new JLabel("\uD83D\uDD12 Blocker", SwingConstants.CENTER);
        titleLabel.setForeground(Color.WHITE);
        titleLabel.setFont(new Font("Arial", Font.BOLD, 18));
        titleLabel.setBorder(new EmptyBorder(10, 10, 10, 10));
        sidePanel.add(titleLabel);

        listModel = new DefaultListModel<>();
        for (String app : commonApps) listModel.addElement(app);

        appList = new JList<>(listModel);
        appList.setLayoutOrientation(JList.HORIZONTAL_WRAP);
        appList.setVisibleRowCount(0);
        appList.setFixedCellWidth(120);
        appList.setFixedCellHeight(100);
        appList.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
        appList.setBackground(new Color(50, 50, 50));
        appList.setForeground(Color.WHITE);
        appList.setCellRenderer(new ModernSoftwareBlocker.AppListRenderer());

        JScrollPane scrollPane = new JScrollPane(appList);
        scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        frame.add(scrollPane, BorderLayout.CENTER);

        JPanel buttonPanel = new JPanel(new GridLayout(1, 2, 10, 10));
        buttonPanel.setBackground(new Color(45, 45, 45));
        buttonPanel.setBorder(new EmptyBorder(10, 10, 10, 10));

        JButton blockBtn = createStyledButton("Block", "\uD83D\uDD12");
        JButton unblockBtn = createStyledButton("Unblock", "\uD83D\uDD13");

        blockBtn.addActionListener(e -> {
            for (String app : appList.getSelectedValuesList()) {
                onBlock.accept(app);
                JOptionPane.showMessageDialog(frame, "Blocked: " + app, "Success", JOptionPane.INFORMATION_MESSAGE);
            }
        });

        unblockBtn.addActionListener(e -> {
            for (String app : appList.getSelectedValuesList()) {
                onUnblock.accept(app);
                JOptionPane.showMessageDialog(frame, "Unblocked: " + app, "Success", JOptionPane.INFORMATION_MESSAGE);
            }
        });

        buttonPanel.add(blockBtn);
        buttonPanel.add(unblockBtn);
        frame.add(buttonPanel, BorderLayout.SOUTH);
    }

    private JButton createStyledButton(String text, String icon) {
        JButton button = new JButton(icon + " " + text) {
            @Override
            protected void paintComponent(Graphics g) {
                g.setColor(getModel().isPressed() ? new Color(30, 130, 230) : getBackground());
                g.fillRoundRect(0, 0, getWidth(), getHeight(), 30, 30);
                super.paintComponent(g);
            }

            @Override
            protected void paintBorder(Graphics g) {
                g.setColor(getBackground().darker());
                g.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 30, 30);
            }

            @Override
            public boolean isOpaque() {
                return false;
            }
        };

        button.setBackground(new Color(50, 150, 250));
        button.setForeground(Color.WHITE);
        button.setFocusPainted(false);
        button.setFont(new Font("Arial", Font.BOLD, 14));
        button.setContentAreaFilled(false);
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
        button.setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 20));
        return button;
    }

    public void show() {
        frame.setVisible(true);
    }
}
