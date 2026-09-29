package com.workspace.management.ui;

import com.workspace.management.dao.BookingDAO;
import com.workspace.management.dao.DeskDAO;
import com.workspace.management.dao.impl.BookingDAOImpl;
import com.workspace.management.dao.impl.DeskDAOImpl;
import com.workspace.management.exception.BookingConflictException;
import com.workspace.management.model.Booking;
import com.workspace.management.model.Desk;
import com.workspace.management.model.User;
import com.workspace.management.service.BookingService;
import com.workspace.management.util.DateTimeUtil;
import com.workspace.management.util.UIUtils;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.RoundRectangle2D;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Interactive floor map using custom JPanel painting.
 * Desks are drawn as visual objects with status colors.
 * Click a desk to see details and book.
 * Positions come from the database.
 */
public class FloorMapPanel extends JPanel {
    private final User currentUser;
    private final DeskDAO deskDAO;
    private final BookingService bookingService;
    private List<Desk> desks = new ArrayList<>();
    private Desk hoveredDesk = null;
    private Desk selectedDesk = null;
    private FloorMapCanvas mapCanvas;
    private JPanel detailPanel;

    private static final int DESK_WIDTH = 60;
    private static final int DESK_HEIGHT = 40;

    public FloorMapPanel(User currentUser) {
        this.currentUser = currentUser;
        this.deskDAO = new DeskDAOImpl();
        this.bookingService = new BookingService();

        setLayout(new BorderLayout(16, 0));
        setBackground(UIUtils.BG_MAIN);
        setBorder(new EmptyBorder(24, 24, 24, 24));

        buildUI();
        refreshData();
    }

    private void buildUI() {
        // Left: Floor map
        JPanel mapPanel = UIUtils.createCard();
        mapPanel.setLayout(new BorderLayout());

        // Legend
        JPanel legendPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 20, 4));
        legendPanel.setOpaque(false);
        legendPanel.add(createLegendItem("Available", UIUtils.DESK_AVAILABLE));
        legendPanel.add(createLegendItem("Reserved", UIUtils.DESK_RESERVED));
        legendPanel.add(createLegendItem("Occupied", UIUtils.DESK_OCCUPIED));
        legendPanel.add(createLegendItem("Maintenance", UIUtils.DESK_MAINTENANCE));

        JLabel floorLabel = new JLabel("  Floor 1 — Office Layout");
        floorLabel.setFont(UIUtils.FONT_HEADING);
        floorLabel.setForeground(UIUtils.TEXT_PRIMARY);

        JPanel topRow = new JPanel(new BorderLayout());
        topRow.setOpaque(false);
        topRow.add(floorLabel, BorderLayout.WEST);
        topRow.add(legendPanel, BorderLayout.EAST);
        mapPanel.add(topRow, BorderLayout.NORTH);

        mapCanvas = new FloorMapCanvas();
        mapPanel.add(mapCanvas, BorderLayout.CENTER);

        // Right: Detail panel
        detailPanel = UIUtils.createCard();
        detailPanel.setLayout(new BorderLayout());
        detailPanel.setPreferredSize(new Dimension(300, 0));
        showDefaultDetail();

        add(mapPanel, BorderLayout.CENTER);
        add(detailPanel, BorderLayout.EAST);
    }

    private JPanel createLegendItem(String label, Color color) {
        JPanel item = new JPanel(new FlowLayout(FlowLayout.LEFT, 4, 0));
        item.setOpaque(false);
        JPanel dot = new JPanel() {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(color);
                g2.fillOval(2, 2, 10, 10);
                g2.dispose();
            }
        };
        dot.setOpaque(false);
        dot.setPreferredSize(new Dimension(14, 14));
        item.add(dot);
        JLabel lbl = new JLabel(label);
        lbl.setFont(UIUtils.FONT_SMALL);
        lbl.setForeground(UIUtils.TEXT_SECONDARY);
        item.add(lbl);
        return item;
    }

    public void refreshData() {
        new SwingWorker<List<Desk>, Void>() {
            @Override
            protected List<Desk> doInBackground() {
                List<Desk> allDesks = deskDAO.findAll();
                // Update desk status based on current bookings
                BookingDAO bookingDAO = new BookingDAOImpl();
                List<Booking> todayBookings = bookingDAO.findByDate(LocalDate.now());
                LocalTime now = LocalTime.now();
                for (Desk desk : allDesks) {
                    if (desk.getStatus() == Desk.DeskStatus.MAINTENANCE ||
                        desk.getStatus() == Desk.DeskStatus.INACTIVE) continue;

                    boolean isBooked = false;
                    for (Booking b : todayBookings) {
                        if (b.getDeskId() == desk.getId() && b.isActive() &&
                            b.getStartTime().isBefore(now) && b.getEndTime().isAfter(now)) {
                            isBooked = true;
                            desk.setCurrentBookingUser(b.getUserName());
                            if (b.getStatus() == Booking.BookingStatus.CHECKED_IN) {
                                desk.setStatus(Desk.DeskStatus.OCCUPIED);
                            } else {
                                desk.setStatus(Desk.DeskStatus.RESERVED);
                            }
                            break;
                        }
                    }
                    if (!isBooked && desk.getStatus() != Desk.DeskStatus.MAINTENANCE) {
                        desk.setStatus(Desk.DeskStatus.AVAILABLE);
                    }
                }
                return allDesks;
            }

            @Override
            protected void done() {
                try {
                    desks = get();
                    mapCanvas.repaint();
                } catch (Exception e) {
                    UIUtils.showError(FloorMapPanel.this, "Failed to load floor map data.");
                }
            }
        }.execute();
    }

    private void showDefaultDetail() {
        detailPanel.removeAll();
        detailPanel.add(UIUtils.createEmptyStatePanel("Select a Desk", "Click on a desk to view details and book"), BorderLayout.CENTER);
        detailPanel.revalidate();
        detailPanel.repaint();
    }

    private void showDeskDetail(Desk desk) {
        detailPanel.removeAll();

        JPanel content = new JPanel();
        content.setLayout(new BoxLayout(content, BoxLayout.Y_AXIS));
        content.setOpaque(false);
        content.setBorder(new EmptyBorder(8, 8, 8, 8));

        // Desk header
        JLabel header = new JLabel("Desk " + desk.getDeskCode());
        header.setFont(UIUtils.FONT_SUBTITLE);
        header.setForeground(UIUtils.TEXT_PRIMARY);
        header.setAlignmentX(LEFT_ALIGNMENT);
        content.add(header);
        content.add(Box.createVerticalStrut(4));

        // Status badge
        JLabel statusBadge = UIUtils.createStatusBadge(
                desk.getStatus().getDisplayName(),
                UIUtils.getDeskStatusColor(desk.getStatus().name()));
        statusBadge.setAlignmentX(LEFT_ALIGNMENT);
        content.add(statusBadge);
        content.add(Box.createVerticalStrut(16));

        // Details
        addDetailRow(content, "Zone", desk.getZone());
        addDetailRow(content, "Type", desk.getDeskType().getDisplayName());
        addDetailRow(content, "Floor", String.valueOf(desk.getFloor()));
        addDetailRow(content, "Monitor", desk.isHasMonitor() ? "Yes" : "No");
        addDetailRow(content, "Phone", desk.isHasPhone() ? "Yes" : "No");
        addDetailRow(content, "Power", desk.isHasPowerOutlet() ? "Yes" : "No");

        if (desk.getCurrentBookingUser() != null) {
            content.add(Box.createVerticalStrut(8));
            addDetailRow(content, "Booked by", desk.getCurrentBookingUser());
        }

        content.add(Box.createVerticalStrut(20));

        // Book button
        if (desk.getStatus() == Desk.DeskStatus.AVAILABLE) {
            JButton bookBtn = UIUtils.createPrimaryButton("Book This Desk");
            bookBtn.setAlignmentX(LEFT_ALIGNMENT);
            bookBtn.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));
            bookBtn.addActionListener(e -> showBookingDialog(desk));
            content.add(bookBtn);
        } else if (desk.getStatus() == Desk.DeskStatus.MAINTENANCE) {
            JLabel msg = new JLabel("This desk is under maintenance");
            msg.setFont(UIUtils.FONT_SMALL);
            msg.setForeground(UIUtils.WARNING);
            msg.setAlignmentX(LEFT_ALIGNMENT);
            content.add(msg);
        } else {
            JLabel msg = new JLabel("Currently unavailable");
            msg.setFont(UIUtils.FONT_SMALL);
            msg.setForeground(UIUtils.DANGER);
            msg.setAlignmentX(LEFT_ALIGNMENT);
            content.add(msg);
        }

        content.add(Box.createVerticalGlue());
        detailPanel.add(new JScrollPane(content), BorderLayout.CENTER);
        detailPanel.revalidate();
        detailPanel.repaint();
    }

    private void addDetailRow(JPanel panel, String label, String value) {
        JPanel row = new JPanel(new BorderLayout());
        row.setOpaque(false);
        row.setMaximumSize(new Dimension(Integer.MAX_VALUE, 24));
        row.setAlignmentX(LEFT_ALIGNMENT);
        JLabel lbl = new JLabel(label);
        lbl.setFont(UIUtils.FONT_SMALL);
        lbl.setForeground(UIUtils.TEXT_SECONDARY);
        JLabel val = new JLabel(value);
        val.setFont(UIUtils.FONT_SMALL);
        val.setForeground(UIUtils.TEXT_PRIMARY);
        row.add(lbl, BorderLayout.WEST);
        row.add(val, BorderLayout.EAST);
        panel.add(row);
        panel.add(Box.createVerticalStrut(6));
    }

    private void showBookingDialog(Desk desk) {
        JPanel formPanel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(6, 6, 6, 6);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        // Date picker (using JSpinner with date model)
        JSpinner dateSpinner = new JSpinner(new SpinnerDateModel());
        JSpinner.DateEditor dateEditor = new JSpinner.DateEditor(dateSpinner, "yyyy-MM-dd");
        dateSpinner.setEditor(dateEditor);
        dateSpinner.setValue(java.util.Date.from(LocalDate.now().atStartOfDay(java.time.ZoneId.systemDefault()).toInstant()));

        // Time combo boxes
        LocalTime[] timeSlots = DateTimeUtil.generateTimeSlots();
        JComboBox<String> startCombo = new JComboBox<>();
        JComboBox<String> endCombo = new JComboBox<>();
        for (LocalTime t : timeSlots) {
            startCombo.addItem(DateTimeUtil.formatTime(t));
            endCombo.addItem(DateTimeUtil.formatTime(t));
        }
        startCombo.setSelectedIndex(4); // 09:00
        endCombo.setSelectedIndex(20); // 17:00

        JTextArea notesArea = new JTextArea(2, 20);
        notesArea.setBorder(BorderFactory.createLineBorder(UIUtils.BORDER));

        UIUtils.addFormRow(formPanel, "Date:", dateSpinner, gbc, 0);
        UIUtils.addFormRow(formPanel, "Start Time:", startCombo, gbc, 1);
        UIUtils.addFormRow(formPanel, "End Time:", endCombo, gbc, 2);
        UIUtils.addFormRow(formPanel, "Notes:", new JScrollPane(notesArea), gbc, 3);

        int result = JOptionPane.showConfirmDialog(this, formPanel,
                "Book Desk " + desk.getDeskCode(), JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);

        if (result == JOptionPane.OK_OPTION) {
            // Parse selections
            java.util.Date selectedDate = (java.util.Date) dateSpinner.getValue();
            LocalDate date = selectedDate.toInstant().atZone(java.time.ZoneId.systemDefault()).toLocalDate();
            LocalTime startTime = timeSlots[startCombo.getSelectedIndex()];
            LocalTime endTime = timeSlots[endCombo.getSelectedIndex()];
            String notes = notesArea.getText().trim();

            // Book using SwingWorker
            new SwingWorker<Booking, Void>() {
                @Override
                protected Booking doInBackground() throws Exception {
                    return bookingService.createBooking(
                            currentUser.getId(), desk.getId(), date, startTime, endTime, notes);
                }

                @Override
                protected void done() {
                    try {
                        Booking booking = get();
                        UIUtils.showSuccess(FloorMapPanel.this,
                                "Desk " + desk.getDeskCode() + " booked successfully!\n" +
                                "Booking ID: " + booking.getId() + "\n" +
                                "Date: " + DateTimeUtil.formatDate(date) + "\n" +
                                "Time: " + DateTimeUtil.formatTimeRange(startTime, endTime));
                        refreshData();
                    } catch (Exception e) {
                        Throwable cause = e.getCause();
                        if (cause instanceof BookingConflictException) {
                            UIUtils.showWarning(FloorMapPanel.this, cause.getMessage());
                        } else {
                            UIUtils.showError(FloorMapPanel.this,
                                    "Unable to complete booking. Please try again.");
                        }
                    }
                }
            }.execute();
        }
    }

    /**
     * Custom canvas that draws the office floor map.
     */
    private class FloorMapCanvas extends JPanel {
        FloorMapCanvas() {
            setOpaque(false);
            setBackground(new Color(249, 250, 251));

            addMouseMotionListener(new MouseMotionAdapter() {
                @Override
                public void mouseMoved(MouseEvent e) {
                    Desk newHover = getDeskAt(e.getX(), e.getY());
                    if (newHover != hoveredDesk) {
                        hoveredDesk = newHover;
                        setCursor(hoveredDesk != null ?
                                new Cursor(Cursor.HAND_CURSOR) : Cursor.getDefaultCursor());
                        if (hoveredDesk != null) {
                            setToolTipText("<html><b>" + hoveredDesk.getDeskCode() + "</b><br>" +
                                    "Zone: " + hoveredDesk.getZone() + "<br>" +
                                    "Status: " + hoveredDesk.getStatus().getDisplayName() + "<br>" +
                                    "Type: " + hoveredDesk.getDeskType().getDisplayName() + "</html>");
                        } else {
                            setToolTipText(null);
                        }
                        repaint();
                    }
                }
            });

            addMouseListener(new MouseAdapter() {
                @Override
                public void mouseClicked(MouseEvent e) {
                    Desk clicked = getDeskAt(e.getX(), e.getY());
                    if (clicked != null) {
                        selectedDesk = clicked;
                        showDeskDetail(clicked);
                        repaint();
                    }
                }
            });
        }

        private Desk getDeskAt(int mx, int my) {
            int w = getWidth();
            int h = getHeight();
            for (Desk desk : desks) {
                int dx = (int) (desk.getPositionX() / 100.0 * w);
                int dy = (int) (desk.getPositionY() / 100.0 * h);
                if (mx >= dx && mx <= dx + DESK_WIDTH && my >= dy && my <= dy + DESK_HEIGHT) {
                    return desk;
                }
            }
            return null;
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            int w = getWidth();
            int h = getHeight();

            // Floor background
            g2.setColor(new Color(249, 250, 251));
            g2.fillRect(0, 0, w, h);

            // Grid pattern
            g2.setColor(new Color(0, 0, 0, 8));
            for (int x = 0; x < w; x += 30) g2.drawLine(x, 0, x, h);
            for (int y = 0; y < h; y += 30) g2.drawLine(0, y, w, y);

            // Zone labels
            g2.setColor(new Color(0, 0, 0, 20));
            g2.setFont(new Font("Segoe UI", Font.BOLD, 12));
            g2.drawString("OPEN WORKSPACE", (int)(0.05 * w), (int)(0.07 * h));
            g2.drawString("QUIET ZONE", (int)(0.05 * w), (int)(0.50 * h));
            g2.drawString("COLLABORATIVE ZONE", (int)(0.05 * w), (int)(0.70 * h));
            g2.drawString("MANAGER ZONE", (int)(0.50 * w), (int)(0.07 * h));
            g2.drawString("MEETING ROOMS", (int)(0.65 * w), (int)(0.07 * h));

            // Zone boundaries
            g2.setColor(new Color(0, 0, 0, 15));
            g2.setStroke(new BasicStroke(1, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND, 0, new float[]{6, 4}, 0));
            g2.drawLine((int)(0.48 * w), 0, (int)(0.48 * w), (int)(0.45 * h)); // Vertical separator
            g2.drawLine(0, (int)(0.45 * h), (int)(0.48 * w), (int)(0.45 * h)); // Horizontal separator
            g2.drawLine(0, (int)(0.65 * h), (int)(0.48 * w), (int)(0.65 * h)); // Zone separator
            g2.setStroke(new BasicStroke(1));

            // Lounge area
            g2.setColor(new Color(0, 0, 0, 10));
            g2.fill(new RoundRectangle2D.Float((int)(0.48 * w), (int)(0.70 * h), (int)(0.48 * w), (int)(0.25 * h), 12, 12));
            g2.setColor(new Color(0, 0, 0, 20));
            g2.setFont(new Font("Segoe UI", Font.BOLD, 14));
            g2.drawString("LOUNGE AREA", (int)(0.60 * w), (int)(0.85 * h));

            // Draw desks
            for (Desk desk : desks) {
                drawDesk(g2, desk, w, h);
            }

            g2.dispose();
        }

        private void drawDesk(Graphics2D g2, Desk desk, int totalW, int totalH) {
            int dx = (int) (desk.getPositionX() / 100.0 * totalW);
            int dy = (int) (desk.getPositionY() / 100.0 * totalH);

            Color statusColor = UIUtils.getDeskStatusColor(desk.getStatus().name());
            boolean isHovered = desk == hoveredDesk;
            boolean isSelected = desk == selectedDesk;

            // Shadow
            if (isHovered || isSelected) {
                g2.setColor(new Color(0, 0, 0, 20));
                g2.fill(new RoundRectangle2D.Float(dx + 2, dy + 2, DESK_WIDTH, DESK_HEIGHT, 8, 8));
            }

            // Desk body
            g2.setColor(isHovered ? statusColor.brighter() : statusColor);
            g2.fill(new RoundRectangle2D.Float(dx, dy, DESK_WIDTH, DESK_HEIGHT, 8, 8));

            // Border
            if (isSelected) {
                g2.setColor(UIUtils.TEXT_PRIMARY);
                g2.setStroke(new BasicStroke(2));
                g2.draw(new RoundRectangle2D.Float(dx, dy, DESK_WIDTH, DESK_HEIGHT, 8, 8));
                g2.setStroke(new BasicStroke(1));
            }

            // Desk label
            g2.setColor(Color.WHITE);
            g2.setFont(new Font("Segoe UI", Font.BOLD, 11));
            FontMetrics fm = g2.getFontMetrics();
            String label = desk.getDeskCode();
            int tw = fm.stringWidth(label);
            g2.drawString(label, dx + (DESK_WIDTH - tw) / 2, dy + DESK_HEIGHT / 2 + 4);

            // Type indicator
            if (desk.getDeskType() == Desk.DeskType.STANDING) {
                g2.setColor(new Color(255, 255, 255, 180));
                g2.setFont(new Font("Segoe UI", Font.PLAIN, 8));
                g2.drawString("SIT/STAND", dx + 6, dy + DESK_HEIGHT - 4);
            }
        }
    }
}
