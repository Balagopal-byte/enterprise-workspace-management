package com.workspace.management.ui;

import com.workspace.management.exception.AuthenticationException;
import com.workspace.management.model.User;
import com.workspace.management.service.AuthenticationService;
import com.workspace.management.util.UIUtils;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.geom.RoundRectangle2D;

/**
 * Professional login screen with split layout:
 * Left: Branding panel with gradient
 * Right: Login form card
 */
public class LoginFrame extends JFrame {
    private JTextField emailField;
    private JPasswordField passwordField;
    private JCheckBox showPasswordCheck;
    private JButton loginButton;
    private JLabel errorLabel;
    private JLabel loadingLabel;
    private final AuthenticationService authService;

    public LoginFrame() {
        authService = new AuthenticationService();
        initializeUI();
    }

    private void initializeUI() {
        setTitle("Enterprise Workspace Management System — Login");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1000, 600);
        setMinimumSize(new Dimension(800, 500));
        setLocationRelativeTo(null);
        setResizable(true);

        JPanel mainPanel = new JPanel(new GridLayout(1, 2));

        // === LEFT PANEL: Branding ===
        JPanel leftPanel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                // Gradient background
                GradientPaint gradient = new GradientPaint(0, 0, new Color(17, 24, 39),
                        getWidth(), getHeight(), new Color(37, 99, 235));
                g2.setPaint(gradient);
                g2.fillRect(0, 0, getWidth(), getHeight());

                // Draw decorative elements
                g2.setColor(new Color(255, 255, 255, 15));
                g2.fillOval(-50, -50, 300, 300);
                g2.fillOval(getWidth() - 150, getHeight() - 200, 350, 350);

                // Grid pattern
                g2.setColor(new Color(255, 255, 255, 8));
                for (int i = 0; i < getWidth(); i += 40) {
                    g2.drawLine(i, 0, i, getHeight());
                }
                for (int i = 0; i < getHeight(); i += 40) {
                    g2.drawLine(0, i, getWidth(), i);
                }

                // Draw desk icons
                g2.setColor(new Color(255, 255, 255, 30));
                drawDeskIcon(g2, 80, 200, 50);
                drawDeskIcon(g2, 200, 250, 40);
                drawDeskIcon(g2, 150, 350, 45);
                drawDeskIcon(g2, 300, 180, 35);
                drawDeskIcon(g2, 350, 320, 55);

                g2.dispose();
            }

            private void drawDeskIcon(Graphics2D g2, int x, int y, int size) {
                g2.fill(new RoundRectangle2D.Float(x, y, size, size * 0.6f, 6, 6));
                g2.fillRect(x + 5, y + (int)(size * 0.6f), size - 10, 4);
            }
        };
        leftPanel.setLayout(new GridBagLayout());
        GridBagConstraints lGbc = new GridBagConstraints();
        lGbc.gridx = 0; lGbc.gridy = 0;
        lGbc.insets = new Insets(0, 40, 10, 40);
        lGbc.anchor = GridBagConstraints.WEST;

        // App icon / logo text
        JLabel logoLabel = new JLabel("\u2B21");
        logoLabel.setFont(new Font("Segoe UI", Font.PLAIN, 48));
        logoLabel.setForeground(new Color(96, 165, 250));
        leftPanel.add(logoLabel, lGbc);

        lGbc.gridy = 1;
        lGbc.insets = new Insets(0, 40, 5, 40);
        JLabel titleLabel = new JLabel("Enterprise Workspace");
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 28));
        titleLabel.setForeground(Color.WHITE);
        leftPanel.add(titleLabel, lGbc);

        lGbc.gridy = 2;
        lGbc.insets = new Insets(0, 40, 20, 40);
        JLabel titleLabel2 = new JLabel("Management System");
        titleLabel2.setFont(new Font("Segoe UI", Font.BOLD, 28));
        titleLabel2.setForeground(new Color(96, 165, 250));
        leftPanel.add(titleLabel2, lGbc);

        lGbc.gridy = 3;
        lGbc.insets = new Insets(0, 40, 10, 40);
        JLabel descLabel = new JLabel("<html><div style='width:280px;color:rgb(148,163,184);font-size:12px;'>" +
                "Streamline your workspace management with intelligent desk booking, " +
                "meeting room scheduling, and real-time occupancy tracking.</div></html>");
        leftPanel.add(descLabel, lGbc);

        lGbc.gridy = 4;
        lGbc.insets = new Insets(20, 40, 0, 40);
        JLabel featureLabel = new JLabel("<html><div style='color:rgb(148,163,184);font-size:11px;'>" +
                "\u2713  Interactive Floor Maps<br>" +
                "\u2713  Smart Desk Booking<br>" +
                "\u2713  Meeting Room Scheduling<br>" +
                "\u2713  Real-time Occupancy Analytics</div></html>");
        leftPanel.add(featureLabel, lGbc);

        // === RIGHT PANEL: Login Form ===
        JPanel rightPanel = new JPanel(new GridBagLayout());
        rightPanel.setBackground(UIUtils.BG_MAIN);

        JPanel loginCard = new JPanel();
        loginCard.setLayout(new BoxLayout(loginCard, BoxLayout.Y_AXIS));
        loginCard.setBackground(Color.WHITE);
        loginCard.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(UIUtils.BORDER, 1),
                new EmptyBorder(40, 40, 40, 40)
        ));
        loginCard.setPreferredSize(new Dimension(380, 420));
        loginCard.setMaximumSize(new Dimension(380, 420));

        // Welcome text
        JLabel welcomeLabel = new JLabel("Welcome back");
        welcomeLabel.setFont(UIUtils.FONT_TITLE);
        welcomeLabel.setForeground(UIUtils.TEXT_PRIMARY);
        welcomeLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        loginCard.add(welcomeLabel);

        loginCard.add(Box.createVerticalStrut(4));

        JLabel subLabel = new JLabel("Sign in to your workspace account");
        subLabel.setFont(UIUtils.FONT_BODY);
        subLabel.setForeground(UIUtils.TEXT_SECONDARY);
        subLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        loginCard.add(subLabel);

        loginCard.add(Box.createVerticalStrut(30));

        // Email field
        JLabel emailLabel = new JLabel("Email Address");
        emailLabel.setFont(UIUtils.FONT_HEADING);
        emailLabel.setForeground(UIUtils.TEXT_PRIMARY);
        emailLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        loginCard.add(emailLabel);
        loginCard.add(Box.createVerticalStrut(6));

        emailField = UIUtils.createTextField("you@example.com");
        emailField.setMaximumSize(new Dimension(Integer.MAX_VALUE, 42));
        emailField.setAlignmentX(Component.LEFT_ALIGNMENT);
        loginCard.add(emailField);

        loginCard.add(Box.createVerticalStrut(16));

        // Password field
        JLabel passLabel = new JLabel("Password");
        passLabel.setFont(UIUtils.FONT_HEADING);
        passLabel.setForeground(UIUtils.TEXT_PRIMARY);
        passLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        loginCard.add(passLabel);
        loginCard.add(Box.createVerticalStrut(6));

        passwordField = UIUtils.createPasswordField("Enter your password");
        passwordField.setMaximumSize(new Dimension(Integer.MAX_VALUE, 42));
        passwordField.setAlignmentX(Component.LEFT_ALIGNMENT);
        loginCard.add(passwordField);

        loginCard.add(Box.createVerticalStrut(8));

        // Show password
        showPasswordCheck = new JCheckBox("Show password");
        showPasswordCheck.setFont(UIUtils.FONT_SMALL);
        showPasswordCheck.setForeground(UIUtils.TEXT_SECONDARY);
        showPasswordCheck.setBackground(Color.WHITE);
        showPasswordCheck.setAlignmentX(Component.LEFT_ALIGNMENT);
        showPasswordCheck.addActionListener(e -> {
            passwordField.setEchoChar(showPasswordCheck.isSelected() ? (char) 0 : '\u2022');
        });
        loginCard.add(showPasswordCheck);

        loginCard.add(Box.createVerticalStrut(10));

        // Error label
        errorLabel = new JLabel(" ");
        errorLabel.setFont(UIUtils.FONT_SMALL);
        errorLabel.setForeground(UIUtils.DANGER);
        errorLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        loginCard.add(errorLabel);

        loginCard.add(Box.createVerticalStrut(6));

        // Login button
        loginButton = UIUtils.createPrimaryButton("Sign In");
        loginButton.setMaximumSize(new Dimension(Integer.MAX_VALUE, 44));
        loginButton.setAlignmentX(Component.LEFT_ALIGNMENT);
        loginButton.addActionListener(this::performLogin);
        loginCard.add(loginButton);

        loginCard.add(Box.createVerticalStrut(10));

        // Loading label
        loadingLabel = new JLabel("Authenticating...");
        loadingLabel.setFont(UIUtils.FONT_SMALL);
        loadingLabel.setForeground(UIUtils.TEXT_SECONDARY);
        loadingLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        loadingLabel.setVisible(false);
        loginCard.add(loadingLabel);

        rightPanel.add(loginCard);

        mainPanel.add(leftPanel);
        mainPanel.add(rightPanel);
        setContentPane(mainPanel);

        // Enter key support
        KeyAdapter enterKey = new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_ENTER) {
                    performLogin(null);
                }
            }
        };
        emailField.addKeyListener(enterKey);
        passwordField.addKeyListener(enterKey);

        // Default focus
        SwingUtilities.invokeLater(() -> emailField.requestFocusInWindow());
    }

    /**
     * Perform login using SwingWorker to avoid freezing the UI.
     */
    private void performLogin(ActionEvent e) {
        String email = emailField.getText().trim();
        String password = new String(passwordField.getPassword());

        errorLabel.setText(" ");
        loginButton.setEnabled(false);
        loadingLabel.setVisible(true);

        new SwingWorker<User, Void>() {
            @Override
            protected User doInBackground() throws Exception {
                return authService.authenticate(email, password);
            }

            @Override
            protected void done() {
                loadingLabel.setVisible(false);
                loginButton.setEnabled(true);
                try {
                    User user = get();
                    // Login successful - open main frame
                    dispose();
                    SwingUtilities.invokeLater(() -> {
                        MainFrame mainFrame = new MainFrame(user);
                        mainFrame.setVisible(true);
                    });
                } catch (Exception ex) {
                    Throwable cause = ex.getCause();
                    if (cause instanceof AuthenticationException) {
                        errorLabel.setText(cause.getMessage());
                    } else {
                        errorLabel.setText("Unable to connect to the workspace database. Please check your connection.");
                    }
                }
            }
        }.execute();
    }
}
