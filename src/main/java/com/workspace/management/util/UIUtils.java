package com.workspace.management.util;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.JTableHeader;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.RoundRectangle2D;

/**
 * UI styling utility providing the modern enterprise look and feel.
 * Contains the color system, component factories, and custom renderers.
 */
public final class UIUtils {

    // === COLOR SYSTEM ===
    public static final Color PRIMARY = new Color(37, 99, 235);       // #2563EB
    public static final Color PRIMARY_DARK = new Color(29, 78, 216);
    public static final Color PRIMARY_LIGHT = new Color(219, 234, 254);
    public static final Color SUCCESS = new Color(22, 163, 74);       // #16A34A
    public static final Color SUCCESS_LIGHT = new Color(220, 252, 231);
    public static final Color WARNING = new Color(245, 158, 11);      // #F59E0B
    public static final Color WARNING_LIGHT = new Color(254, 243, 199);
    public static final Color DANGER = new Color(220, 38, 38);        // #DC2626
    public static final Color DANGER_LIGHT = new Color(254, 226, 226);
    public static final Color BG_MAIN = new Color(248, 250, 252);     // #F8FAFC
    public static final Color BG_SIDEBAR = new Color(17, 24, 39);     // #111827
    public static final Color BG_SIDEBAR_HOVER = new Color(31, 41, 55);
    public static final Color BG_SIDEBAR_ACTIVE = new Color(55, 65, 81);
    public static final Color CARD_BG = Color.WHITE;
    public static final Color TEXT_PRIMARY = new Color(17, 24, 39);    // #111827
    public static final Color TEXT_SECONDARY = new Color(100, 116, 139); // #64748B
    public static final Color TEXT_LIGHT = new Color(148, 163, 184);
    public static final Color TEXT_WHITE = Color.WHITE;
    public static final Color BORDER = new Color(226, 232, 240);
    public static final Color TABLE_STRIPE = new Color(249, 250, 251);
    public static final Color TABLE_HOVER = new Color(239, 246, 255);

    // Desk status colors
    public static final Color DESK_AVAILABLE = new Color(34, 197, 94);
    public static final Color DESK_RESERVED = new Color(59, 130, 246);
    public static final Color DESK_OCCUPIED = new Color(239, 68, 68);
    public static final Color DESK_MAINTENANCE = new Color(156, 163, 175);

    // === FONTS ===
    public static final Font FONT_TITLE = new Font("Segoe UI", Font.BOLD, 24);
    public static final Font FONT_SUBTITLE = new Font("Segoe UI", Font.BOLD, 18);
    public static final Font FONT_HEADING = new Font("Segoe UI", Font.BOLD, 14);
    public static final Font FONT_BODY = new Font("Segoe UI", Font.PLAIN, 14);
    public static final Font FONT_SMALL = new Font("Segoe UI", Font.PLAIN, 12);
    public static final Font FONT_BUTTON = new Font("Segoe UI", Font.BOLD, 13);
    public static final Font FONT_SIDEBAR = new Font("Segoe UI", Font.PLAIN, 14);
    public static final Font FONT_SIDEBAR_ACTIVE = new Font("Segoe UI", Font.BOLD, 14);
    public static final Font FONT_STAT = new Font("Segoe UI", Font.BOLD, 28);

    // === DIMENSIONS ===
    public static final int SIDEBAR_WIDTH = 240;
    public static final int TOPBAR_HEIGHT = 60;
    public static final int CARD_RADIUS = 12;
    public static final int BUTTON_RADIUS = 8;

    private UIUtils() {}

    // === COMPONENT FACTORIES ===

    /**
     * Create a modern styled button with hover effects.
     */
    public static JButton createButton(String text, Color bgColor) {
        JButton button = new JButton(text) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                if (getModel().isPressed()) {
                    g2.setColor(bgColor.darker());
                } else if (getModel().isRollover()) {
                    g2.setColor(bgColor.brighter());
                } else {
                    g2.setColor(bgColor);
                }
                g2.fill(new RoundRectangle2D.Float(0, 0, getWidth(), getHeight(), BUTTON_RADIUS, BUTTON_RADIUS));
                g2.dispose();
                super.paintComponent(g);
            }
        };
        button.setFont(FONT_BUTTON);
        button.setForeground(Color.WHITE);
        button.setBackground(bgColor);
        button.setBorderPainted(false);
        button.setFocusPainted(false);
        button.setContentAreaFilled(false);
        button.setOpaque(false);
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
        button.setPreferredSize(new Dimension(140, 38));
        button.setBorder(new EmptyBorder(8, 20, 8, 20));
        return button;
    }

    /**
     * Create a primary colored button.
     */
    public static JButton createPrimaryButton(String text) {
        return createButton(text, PRIMARY);
    }

    /**
     * Create a success (green) colored button.
     */
    public static JButton createSuccessButton(String text) {
        return createButton(text, SUCCESS);
    }

    /**
     * Create a danger (red) colored button.
     */
    public static JButton createDangerButton(String text) {
        return createButton(text, DANGER);
    }

    /**
     * Create a secondary (outline-style) button.
     */
    public static JButton createSecondaryButton(String text) {
        JButton button = new JButton(text);
        button.setFont(FONT_BUTTON);
        button.setForeground(TEXT_PRIMARY);
        button.setBackground(CARD_BG);
        button.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDER, 1),
                new EmptyBorder(8, 20, 8, 20)
        ));
        button.setFocusPainted(false);
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
        button.setPreferredSize(new Dimension(140, 38));
        button.addMouseListener(new MouseAdapter() {
            public void mouseEntered(MouseEvent e) { button.setBackground(BG_MAIN); }
            public void mouseExited(MouseEvent e) { button.setBackground(CARD_BG); }
        });
        return button;
    }

    /**
     * Create a rounded card panel with shadow-like border.
     */
    public static JPanel createCard() {
        JPanel card = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                // Shadow
                g2.setColor(new Color(0, 0, 0, 10));
                g2.fill(new RoundRectangle2D.Float(2, 2, getWidth() - 2, getHeight() - 2, CARD_RADIUS, CARD_RADIUS));
                // Card background
                g2.setColor(CARD_BG);
                g2.fill(new RoundRectangle2D.Float(0, 0, getWidth() - 2, getHeight() - 2, CARD_RADIUS, CARD_RADIUS));
                // Border
                g2.setColor(BORDER);
                g2.draw(new RoundRectangle2D.Float(0, 0, getWidth() - 3, getHeight() - 3, CARD_RADIUS, CARD_RADIUS));
                g2.dispose();
            }
        };
        card.setOpaque(false);
        card.setBorder(new EmptyBorder(16, 16, 16, 16));
        card.setLayout(new BorderLayout());
        return card;
    }

    /**
     * Create a styled text field.
     */
    public static JTextField createTextField(String placeholder) {
        JTextField field = new JTextField() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                if (getText().isEmpty() && !isFocusOwner()) {
                    Graphics2D g2 = (Graphics2D) g.create();
                    g2.setColor(TEXT_LIGHT);
                    g2.setFont(FONT_BODY);
                    Insets insets = getInsets();
                    g2.drawString(placeholder, insets.left, getHeight() / 2 + g2.getFontMetrics().getAscent() / 2 - 2);
                    g2.dispose();
                }
            }
        };
        field.setFont(FONT_BODY);
        field.setForeground(TEXT_PRIMARY);
        field.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDER, 1),
                new EmptyBorder(8, 12, 8, 12)
        ));
        field.setPreferredSize(new Dimension(250, 40));
        return field;
    }

    /**
     * Create a styled password field.
     */
    public static JPasswordField createPasswordField(String placeholder) {
        JPasswordField field = new JPasswordField() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                if (getPassword().length == 0 && !isFocusOwner()) {
                    Graphics2D g2 = (Graphics2D) g.create();
                    g2.setColor(TEXT_LIGHT);
                    g2.setFont(FONT_BODY);
                    Insets insets = getInsets();
                    g2.drawString(placeholder, insets.left, getHeight() / 2 + g2.getFontMetrics().getAscent() / 2 - 2);
                    g2.dispose();
                }
            }
        };
        field.setFont(FONT_BODY);
        field.setForeground(TEXT_PRIMARY);
        field.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDER, 1),
                new EmptyBorder(8, 12, 8, 12)
        ));
        field.setPreferredSize(new Dimension(250, 40));
        return field;
    }

    /**
     * Create a modern styled JTable.
     */
    public static JTable createStyledTable() {
        JTable table = new JTable();
        table.setFont(FONT_BODY);
        table.setRowHeight(44);
        table.setShowGrid(false);
        table.setIntercellSpacing(new Dimension(0, 0));
        table.setSelectionBackground(PRIMARY_LIGHT);
        table.setSelectionForeground(TEXT_PRIMARY);
        table.setFillsViewportHeight(true);
        table.setBackground(CARD_BG);

        // Header styling
        JTableHeader header = table.getTableHeader();
        header.setFont(FONT_HEADING);
        header.setBackground(new Color(249, 250, 251));
        header.setForeground(TEXT_SECONDARY);
        header.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, BORDER));
        header.setPreferredSize(new Dimension(0, 44));

        // Alternating row colors
        table.setDefaultRenderer(Object.class, new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value,
                    boolean isSelected, boolean hasFocus, int row, int column) {
                Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                if (!isSelected) {
                    c.setBackground(row % 2 == 0 ? CARD_BG : TABLE_STRIPE);
                }
                setBorder(new EmptyBorder(0, 12, 0, 12));
                return c;
            }
        });

        return table;
    }

    /**
     * Create a styled combo box.
     */
    public static <T> JComboBox<T> createComboBox() {
        JComboBox<T> combo = new JComboBox<>();
        combo.setFont(FONT_BODY);
        combo.setPreferredSize(new Dimension(200, 40));
        combo.setBackground(CARD_BG);
        return combo;
    }

    /**
     * Create a status badge (colored label).
     */
    public static JLabel createStatusBadge(String text, Color bgColor) {
        JLabel badge = new JLabel(text, SwingConstants.CENTER) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                // Light background
                g2.setColor(new Color(bgColor.getRed(), bgColor.getGreen(), bgColor.getBlue(), 30));
                g2.fill(new RoundRectangle2D.Float(0, 0, getWidth(), getHeight(), 16, 16));
                g2.dispose();
                super.paintComponent(g);
            }
        };
        badge.setFont(new Font("Segoe UI", Font.BOLD, 11));
        badge.setForeground(bgColor);
        badge.setOpaque(false);
        badge.setBorder(new EmptyBorder(4, 12, 4, 12));
        badge.setPreferredSize(new Dimension(100, 26));
        return badge;
    }

    /**
     * Create a section header label.
     */
    public static JLabel createSectionHeader(String text) {
        JLabel label = new JLabel(text);
        label.setFont(FONT_SUBTITLE);
        label.setForeground(TEXT_PRIMARY);
        label.setBorder(new EmptyBorder(0, 0, 10, 0));
        return label;
    }

    /**
     * Create a stat card for dashboards (number + label).
     */
    public static JPanel createStatCard(String title, String value, Color accentColor) {
        JPanel card = createCard();
        card.setLayout(new BorderLayout(0, 8));
        card.setPreferredSize(new Dimension(200, 120));

        // Accent stripe at top
        JPanel accent = new JPanel();
        accent.setBackground(accentColor);
        accent.setPreferredSize(new Dimension(0, 4));
        accent.setOpaque(true);

        JLabel valueLabel = new JLabel(value);
        valueLabel.setFont(FONT_STAT);
        valueLabel.setForeground(TEXT_PRIMARY);

        JLabel titleLabel = new JLabel(title);
        titleLabel.setFont(FONT_SMALL);
        titleLabel.setForeground(TEXT_SECONDARY);

        JPanel content = new JPanel(new BorderLayout(0, 4));
        content.setOpaque(false);
        content.add(valueLabel, BorderLayout.CENTER);
        content.add(titleLabel, BorderLayout.SOUTH);

        card.add(accent, BorderLayout.NORTH);
        card.add(content, BorderLayout.CENTER);
        return card;
    }

    /**
     * Create a loading panel with spinner text.
     */
    public static JPanel createLoadingPanel(String message) {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setOpaque(false);
        JLabel label = new JLabel(message + "...");
        label.setFont(FONT_BODY);
        label.setForeground(TEXT_SECONDARY);
        label.setIcon(UIManager.getIcon("OptionPane.informationIcon"));
        panel.add(label);
        return panel;
    }

    /**
     * Create an empty state panel.
     */
    public static JPanel createEmptyStatePanel(String title, String subtitle) {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setOpaque(false);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0; gbc.gridy = 0;
        gbc.insets = new Insets(10, 10, 5, 10);

        JLabel titleLabel = new JLabel(title);
        titleLabel.setFont(FONT_SUBTITLE);
        titleLabel.setForeground(TEXT_SECONDARY);
        panel.add(titleLabel, gbc);

        gbc.gridy = 1;
        gbc.insets = new Insets(0, 10, 10, 10);
        JLabel subLabel = new JLabel(subtitle);
        subLabel.setFont(FONT_BODY);
        subLabel.setForeground(TEXT_LIGHT);
        panel.add(subLabel, gbc);

        return panel;
    }

    /**
     * Show a styled confirmation dialog.
     */
    public static boolean showConfirmDialog(Component parent, String title, String message) {
        int result = JOptionPane.showConfirmDialog(parent, message, title,
                JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);
        return result == JOptionPane.YES_OPTION;
    }

    /**
     * Show a success notification.
     */
    public static void showSuccess(Component parent, String message) {
        JOptionPane.showMessageDialog(parent, message, "Success", JOptionPane.INFORMATION_MESSAGE);
    }

    /**
     * Show an error notification.
     */
    public static void showError(Component parent, String message) {
        JOptionPane.showMessageDialog(parent, message, "Error", JOptionPane.ERROR_MESSAGE);
    }

    /**
     * Show a warning notification.
     */
    public static void showWarning(Component parent, String message) {
        JOptionPane.showMessageDialog(parent, message, "Warning", JOptionPane.WARNING_MESSAGE);
    }

    /**
     * Wrap a JTable in a styled scroll pane.
     */
    public static JScrollPane createScrollPane(JTable table) {
        JScrollPane scrollPane = new JScrollPane(table);
        scrollPane.setBorder(BorderFactory.createLineBorder(BORDER, 1));
        scrollPane.getViewport().setBackground(CARD_BG);
        return scrollPane;
    }

    /**
     * Create a sidebar navigation button.
     */
    public static JButton createSidebarButton(String text, boolean isActive) {
        JButton button = new JButton(text);
        button.setFont(isActive ? FONT_SIDEBAR_ACTIVE : FONT_SIDEBAR);
        button.setForeground(isActive ? TEXT_WHITE : new Color(156, 163, 175));
        button.setBackground(isActive ? BG_SIDEBAR_ACTIVE : BG_SIDEBAR);
        button.setBorderPainted(false);
        button.setFocusPainted(false);
        button.setHorizontalAlignment(SwingConstants.LEFT);
        button.setBorder(new EmptyBorder(12, 20, 12, 20));
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
        button.setMaximumSize(new Dimension(Integer.MAX_VALUE, 44));

        if (!isActive) {
            button.addMouseListener(new MouseAdapter() {
                public void mouseEntered(MouseEvent e) { button.setBackground(BG_SIDEBAR_HOVER); }
                public void mouseExited(MouseEvent e) { button.setBackground(BG_SIDEBAR); }
            });
        }
        return button;
    }

    /**
     * Add a label-field pair to a form panel using GridBagLayout.
     */
    public static void addFormRow(JPanel panel, String label, JComponent field,
                                   GridBagConstraints gbc, int row) {
        gbc.gridx = 0; gbc.gridy = row;
        gbc.anchor = GridBagConstraints.WEST;
        gbc.fill = GridBagConstraints.NONE;
        gbc.weightx = 0;
        JLabel lbl = new JLabel(label);
        lbl.setFont(FONT_HEADING);
        lbl.setForeground(TEXT_PRIMARY);
        panel.add(lbl, gbc);

        gbc.gridx = 1;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1.0;
        panel.add(field, gbc);
    }

    /**
     * Get color for desk status.
     */
    public static Color getDeskStatusColor(String status) {
        if (status == null) return DESK_MAINTENANCE;
        return switch (status.toUpperCase()) {
            case "AVAILABLE" -> DESK_AVAILABLE;
            case "RESERVED" -> DESK_RESERVED;
            case "OCCUPIED" -> DESK_OCCUPIED;
            case "MAINTENANCE", "INACTIVE" -> DESK_MAINTENANCE;
            default -> DESK_MAINTENANCE;
        };
    }

    /**
     * Get color for booking status.
     */
    public static Color getBookingStatusColor(String status) {
        if (status == null) return TEXT_SECONDARY;
        return switch (status.toUpperCase()) {
            case "CONFIRMED", "PENDING" -> PRIMARY;
            case "CHECKED_IN" -> SUCCESS;
            case "COMPLETED" -> TEXT_SECONDARY;
            case "CANCELLED" -> DANGER;
            case "AUTO_RELEASED" -> WARNING;
            default -> TEXT_SECONDARY;
        };
    }
}
