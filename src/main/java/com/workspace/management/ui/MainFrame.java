package com.workspace.management.ui;

import com.workspace.management.model.Role;
import com.workspace.management.model.User;
import com.workspace.management.service.AuthenticationService;
import com.workspace.management.service.AutoReleaseService;
import com.workspace.management.util.UIUtils;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Main application frame with sidebar navigation, top bar, and card-layout content area.
 * Hides menu items based on user role.
 */
public class MainFrame extends JFrame {
    private final User currentUser;
    private final CardLayout cardLayout;
    private final JPanel contentPanel;
    private final JPanel sidebarPanel;
    private final JLabel pageTitleLabel;
    private String activePanel = "Dashboard";
    private final Map<String, JButton> navButtons = new LinkedHashMap<>();
    private final AutoReleaseService autoReleaseService;

    // Panel instances (lazy loaded)
    private DashboardPanel dashboardPanel;
    private FloorMapPanel floorMapPanel;
    private BookingPanel bookingPanel;
    private MeetingRoomPanel meetingRoomPanel;
    private MyBookingsPanel myBookingsPanel;
    private CheckInPanel checkInPanel;
    private AnalyticsPanel analyticsPanel;
    private UserManagementPanel userManagementPanel;
    private DeskManagementPanel deskManagementPanel;
    private RoomManagementPanel roomManagementPanel;
    private EquipmentManagementPanel equipmentManagementPanel;

    public MainFrame(User user) {
        this.currentUser = user;
        AuthenticationService.setCurrentUser(user);
        this.cardLayout = new CardLayout();
        this.contentPanel = new JPanel(cardLayout);
        this.sidebarPanel = new JPanel();
        this.pageTitleLabel = new JLabel("Dashboard");
        this.autoReleaseService = new AutoReleaseService();

        initializeUI();
        startBackgroundServices();
    }

    private void initializeUI() {
        setTitle("Enterprise Workspace Management System");
        setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        setSize(1400, 850);
        setMinimumSize(new Dimension(1100, 700));
        setLocationRelativeTo(null);

        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                handleLogout();
            }
        });

        JPanel mainPanel = new JPanel(new BorderLayout());

        // === SIDEBAR ===
        buildSidebar();
        mainPanel.add(sidebarPanel, BorderLayout.WEST);

        // === RIGHT SIDE (Top Bar + Content) ===
        JPanel rightPanel = new JPanel(new BorderLayout());

        // Top Bar
        JPanel topBar = buildTopBar();
        rightPanel.add(topBar, BorderLayout.NORTH);

        // Content Area
        contentPanel.setBackground(UIUtils.BG_MAIN);
        buildContentPanels();
        rightPanel.add(contentPanel, BorderLayout.CENTER);

        mainPanel.add(rightPanel, BorderLayout.CENTER);
        setContentPane(mainPanel);

        // Show dashboard by default
        showPanel("Dashboard");
    }

    private void buildSidebar() {
        sidebarPanel.setLayout(new BoxLayout(sidebarPanel, BoxLayout.Y_AXIS));
        sidebarPanel.setBackground(UIUtils.BG_SIDEBAR);
        sidebarPanel.setPreferredSize(new Dimension(UIUtils.SIDEBAR_WIDTH, 0));
        sidebarPanel.setBorder(new EmptyBorder(0, 0, 0, 0));

        // Logo area
        JPanel logoPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 16, 16));
        logoPanel.setBackground(UIUtils.BG_SIDEBAR);
        logoPanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 70));
        JLabel logo = new JLabel("\u2B21 EWMS");
        logo.setFont(new Font("Segoe UI", Font.BOLD, 20));
        logo.setForeground(Color.WHITE);
        logoPanel.add(logo);
        sidebarPanel.add(logoPanel);

        // Separator
        JSeparator sep = new JSeparator();
        sep.setMaximumSize(new Dimension(Integer.MAX_VALUE, 1));
        sep.setForeground(new Color(55, 65, 81));
        sidebarPanel.add(sep);
        sidebarPanel.add(Box.createVerticalStrut(8));

        // Navigation items - visibility based on role
        addNavItem("\u2302  Dashboard", "Dashboard", true);
        addNavItem("\u25A6  Floor Map", "FloorMap", true);
        addNavItem("\u2316  Desk Booking", "Booking", true);
        addNavItem("\u25A3  Meeting Rooms", "MeetingRooms", true);
        addNavItem("\u2630  My Bookings", "MyBookings", true);
        addNavItem("\u2713  Check-In", "CheckIn", true);

        // Manager + Admin items
        boolean isManagerOrAdmin = currentUser.hasRole(Role.FACILITY_MANAGER);
        if (isManagerOrAdmin) {
            sidebarPanel.add(Box.createVerticalStrut(10));
            JLabel mgmtLabel = new JLabel("   MANAGEMENT");
            mgmtLabel.setFont(new Font("Segoe UI", Font.BOLD, 10));
            mgmtLabel.setForeground(new Color(107, 114, 128));
            mgmtLabel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 20));
            sidebarPanel.add(mgmtLabel);
            sidebarPanel.add(Box.createVerticalStrut(4));

            addNavItem("\u2261  Analytics", "Analytics", true);
            addNavItem("\u2338  Desk Mgmt", "DeskMgmt", true);
            addNavItem("\u2616  Room Mgmt", "RoomMgmt", true);
            addNavItem("\u2699  Equipment", "EquipmentMgmt", true);
        }

        // Admin only items
        if (currentUser.isAdmin()) {
            addNavItem("\u263A  Users", "Users", true);
        }

        // Spacer
        sidebarPanel.add(Box.createVerticalGlue());

        // Logout
        sidebarPanel.add(Box.createVerticalStrut(8));
        JSeparator sep2 = new JSeparator();
        sep2.setMaximumSize(new Dimension(Integer.MAX_VALUE, 1));
        sep2.setForeground(new Color(55, 65, 81));
        sidebarPanel.add(sep2);

        addNavItem("\u2190  Logout", "Logout", true);
        sidebarPanel.add(Box.createVerticalStrut(8));
    }

    private void addNavItem(String label, String panelName, boolean visible) {
        if (!visible) return;
        JButton btn = UIUtils.createSidebarButton(label, false);
        btn.addActionListener(e -> {
            if ("Logout".equals(panelName)) {
                handleLogout();
            } else {
                showPanel(panelName);
            }
        });
        navButtons.put(panelName, btn);
        sidebarPanel.add(btn);
    }

    private JPanel buildTopBar() {
        JPanel topBar = new JPanel(new BorderLayout());
        topBar.setBackground(Color.WHITE);
        topBar.setPreferredSize(new Dimension(0, UIUtils.TOPBAR_HEIGHT));
        topBar.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 0, 1, 0, UIUtils.BORDER),
                new EmptyBorder(0, 24, 0, 24)
        ));

        // Page title
        pageTitleLabel.setFont(UIUtils.FONT_SUBTITLE);
        pageTitleLabel.setForeground(UIUtils.TEXT_PRIMARY);
        topBar.add(pageTitleLabel, BorderLayout.WEST);

        // User info
        JPanel userPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 12, 0));
        userPanel.setOpaque(false);

        JLabel roleBadge = UIUtils.createStatusBadge(
                currentUser.getRole().getDisplayName(),
                currentUser.isAdmin() ? UIUtils.DANGER :
                        currentUser.isManager() ? UIUtils.WARNING : UIUtils.PRIMARY
        );
        userPanel.add(roleBadge);

        JLabel userLabel = new JLabel(currentUser.getFullName());
        userLabel.setFont(UIUtils.FONT_HEADING);
        userLabel.setForeground(UIUtils.TEXT_PRIMARY);
        userPanel.add(userLabel);

        topBar.add(userPanel, BorderLayout.EAST);
        return topBar;
    }

    private void buildContentPanels() {
        // Initialize all panels
        dashboardPanel = new DashboardPanel();
        floorMapPanel = new FloorMapPanel(currentUser);
        bookingPanel = new BookingPanel(currentUser);
        meetingRoomPanel = new MeetingRoomPanel(currentUser);
        myBookingsPanel = new MyBookingsPanel(currentUser);
        checkInPanel = new CheckInPanel(currentUser);

        contentPanel.add(dashboardPanel, "Dashboard");
        contentPanel.add(floorMapPanel, "FloorMap");
        contentPanel.add(bookingPanel, "Booking");
        contentPanel.add(meetingRoomPanel, "MeetingRooms");
        contentPanel.add(myBookingsPanel, "MyBookings");
        contentPanel.add(checkInPanel, "CheckIn");

        if (currentUser.hasRole(Role.FACILITY_MANAGER)) {
            analyticsPanel = new AnalyticsPanel();
            deskManagementPanel = new DeskManagementPanel();
            roomManagementPanel = new RoomManagementPanel();
            equipmentManagementPanel = new EquipmentManagementPanel();
            contentPanel.add(analyticsPanel, "Analytics");
            contentPanel.add(deskManagementPanel, "DeskMgmt");
            contentPanel.add(roomManagementPanel, "RoomMgmt");
            contentPanel.add(equipmentManagementPanel, "EquipmentMgmt");
        }

        if (currentUser.isAdmin()) {
            userManagementPanel = new UserManagementPanel();
            contentPanel.add(userManagementPanel, "Users");
        }
    }

    /**
     * Show a content panel by name and update navigation state.
     */
    public void showPanel(String panelName) {
        activePanel = panelName;
        cardLayout.show(contentPanel, panelName);

        // Update page title
        String title = switch (panelName) {
            case "Dashboard" -> "Dashboard";
            case "FloorMap" -> "Interactive Floor Map";
            case "Booking" -> "Desk Booking";
            case "MeetingRooms" -> "Meeting Rooms";
            case "MyBookings" -> "My Bookings";
            case "CheckIn" -> "Check-In";
            case "Analytics" -> "Analytics Dashboard";
            case "DeskMgmt" -> "Desk Management";
            case "RoomMgmt" -> "Room Management";
            case "EquipmentMgmt" -> "Equipment Management";
            case "Users" -> "User Management";
            default -> panelName;
        };
        pageTitleLabel.setText(title);

        // Update sidebar button states
        navButtons.forEach((name, btn) -> {
            boolean isActive = name.equals(panelName);
            btn.setBackground(isActive ? UIUtils.BG_SIDEBAR_ACTIVE : UIUtils.BG_SIDEBAR);
            btn.setForeground(isActive ? Color.WHITE : new Color(156, 163, 175));
            btn.setFont(isActive ? UIUtils.FONT_SIDEBAR_ACTIVE : UIUtils.FONT_SIDEBAR);
        });

        // Refresh panel data
        refreshActivePanel();
    }

    private void refreshActivePanel() {
        switch (activePanel) {
            case "Dashboard" -> { if (dashboardPanel != null) dashboardPanel.refreshData(); }
            case "FloorMap" -> { if (floorMapPanel != null) floorMapPanel.refreshData(); }
            case "MyBookings" -> { if (myBookingsPanel != null) myBookingsPanel.refreshData(); }
            case "CheckIn" -> { if (checkInPanel != null) checkInPanel.refreshData(); }
        }
    }

    private void startBackgroundServices() {
        autoReleaseService.start(() -> {
            // UI refresh callback - runs on EDT via SwingUtilities.invokeLater
            refreshActivePanel();
        });
    }

    private void handleLogout() {
        int confirm = JOptionPane.showConfirmDialog(this,
                "Are you sure you want to logout?", "Confirm Logout",
                JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);
        if (confirm == JOptionPane.YES_OPTION) {
            autoReleaseService.stop();
            new AuthenticationService().logout();
            dispose();
            SwingUtilities.invokeLater(() -> new LoginFrame().setVisible(true));
        }
    }
}
