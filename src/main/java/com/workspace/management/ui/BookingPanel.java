package com.workspace.management.ui;

import com.workspace.management.dao.DeskDAO;
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
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

/**
 * Desk booking panel with date/time selection and available desk display.
 */
public class BookingPanel extends JPanel {
    private final User currentUser;
    private final BookingService bookingService;
    private final DeskDAO deskDAO;
    private JComboBox<String> dateCombo;
    private JComboBox<String> startTimeCombo;
    private JComboBox<String> endTimeCombo;
    private JComboBox<String> zoneCombo;
    private JPanel desksGrid;
    private JLabel statusLabel;
    private final LocalTime[] timeSlots;

    public BookingPanel(User currentUser) {
        this.currentUser = currentUser;
        this.bookingService = new BookingService();
        this.deskDAO = new DeskDAOImpl();
        this.timeSlots = DateTimeUtil.generateTimeSlots();
        setLayout(new BorderLayout(16, 16));
        setBackground(UIUtils.BG_MAIN);
        setBorder(new EmptyBorder(24, 24, 24, 24));
        buildUI();
    }

    private void buildUI() {
        // Filter panel
        JPanel filterCard = UIUtils.createCard();
        filterCard.setLayout(new FlowLayout(FlowLayout.LEFT, 16, 8));
        filterCard.setPreferredSize(new Dimension(0, 70));

        // Date
        dateCombo = UIUtils.createComboBox();
        LocalDate today = LocalDate.now();
        for (int i = 0; i < 14; i++) {
            LocalDate d = today.plusDays(i);
            dateCombo.addItem(DateTimeUtil.getRelativeDateString(d) + " (" + DateTimeUtil.formatDate(d) + ")");
        }

        // Start time
        startTimeCombo = UIUtils.createComboBox();
        endTimeCombo = UIUtils.createComboBox();
        for (LocalTime t : timeSlots) {
            startTimeCombo.addItem(DateTimeUtil.formatTime(t));
            endTimeCombo.addItem(DateTimeUtil.formatTime(t));
        }
        startTimeCombo.setSelectedIndex(4); // 09:00
        endTimeCombo.setSelectedIndex(20); // 17:00

        // Zone filter
        zoneCombo = UIUtils.createComboBox();
        zoneCombo.addItem("All Zones");
        new SwingWorker<List<String>, Void>() {
            @Override protected List<String> doInBackground() { return deskDAO.findAllZones(); }
            @Override protected void done() {
                try { for (String z : get()) zoneCombo.addItem(z); } catch (Exception ignored) {}
            }
        }.execute();

        JButton searchBtn = UIUtils.createPrimaryButton("Find Desks");
        searchBtn.addActionListener(e -> searchAvailableDesks());

        filterCard.add(new JLabel("Date:"));
        filterCard.add(dateCombo);
        filterCard.add(new JLabel("From:"));
        filterCard.add(startTimeCombo);
        filterCard.add(new JLabel("To:"));
        filterCard.add(endTimeCombo);
        filterCard.add(new JLabel("Zone:"));
        filterCard.add(zoneCombo);
        filterCard.add(searchBtn);

        add(filterCard, BorderLayout.NORTH);

        // Desks grid
        desksGrid = new JPanel(new GridLayout(0, 4, 12, 12));
        desksGrid.setOpaque(false);
        JScrollPane scrollPane = new JScrollPane(desksGrid);
        scrollPane.setBorder(null);
        scrollPane.getViewport().setBackground(UIUtils.BG_MAIN);
        add(scrollPane, BorderLayout.CENTER);

        // Status
        statusLabel = new JLabel("Select date and time, then click 'Find Desks'");
        statusLabel.setFont(UIUtils.FONT_BODY);
        statusLabel.setForeground(UIUtils.TEXT_SECONDARY);
        add(statusLabel, BorderLayout.SOUTH);
    }

    private void searchAvailableDesks() {
        LocalDate selectedDate = LocalDate.now().plusDays(dateCombo.getSelectedIndex());
        LocalTime startTime = timeSlots[startTimeCombo.getSelectedIndex()];
        LocalTime endTime = timeSlots[endTimeCombo.getSelectedIndex()];
        String zone = zoneCombo.getSelectedIndex() == 0 ? null : (String) zoneCombo.getSelectedItem();

        if (!endTime.isAfter(startTime)) {
            UIUtils.showWarning(this, "End time must be after start time.");
            return;
        }

        statusLabel.setText("Searching available desks...");
        desksGrid.removeAll();

        new SwingWorker<List<Desk>, Void>() {
            @Override
            protected List<Desk> doInBackground() {
                List<Desk> allDesks = zone != null ? deskDAO.findByZone(zone) : deskDAO.findAll();
                // Filter available desks
                var bookingDAO = new com.workspace.management.dao.impl.BookingDAOImpl();
                allDesks.removeIf(d -> d.getStatus() == Desk.DeskStatus.MAINTENANCE || d.getStatus() == Desk.DeskStatus.INACTIVE);
                // Check each desk for conflicts
                allDesks.removeIf(d -> {
                    try {
                        var conn = com.workspace.management.config.DatabaseConfig.getInstance().getConnection();
                        boolean hasConflict = bookingDAO.hasOverlappingBooking(conn, d.getId(), selectedDate, startTime, endTime, 0);
                        com.workspace.management.config.DatabaseConfig.getInstance().releaseConnection(conn);
                        return hasConflict;
                    } catch (Exception e) { return true; }
                });
                return allDesks;
            }

            @Override
            protected void done() {
                try {
                    List<Desk> available = get();
                    desksGrid.removeAll();
                    if (available.isEmpty()) {
                        statusLabel.setText("No desks available for the selected time slot.");
                        desksGrid.add(UIUtils.createEmptyStatePanel("No Available Desks",
                                "Try a different time or zone"));
                    } else {
                        statusLabel.setText(available.size() + " desk(s) available");
                        for (Desk desk : available) {
                            desksGrid.add(createDeskCard(desk, selectedDate, startTime, endTime));
                        }
                    }
                    desksGrid.revalidate();
                    desksGrid.repaint();
                } catch (Exception e) {
                    statusLabel.setText("Error searching desks.");
                }
            }
        }.execute();
    }

    private JPanel createDeskCard(Desk desk, LocalDate date, LocalTime start, LocalTime end) {
        JPanel card = UIUtils.createCard();
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setPreferredSize(new Dimension(200, 180));

        JLabel codeLabel = new JLabel(desk.getDeskCode());
        codeLabel.setFont(UIUtils.FONT_SUBTITLE);
        codeLabel.setForeground(UIUtils.TEXT_PRIMARY);
        codeLabel.setAlignmentX(LEFT_ALIGNMENT);
        card.add(codeLabel);
        card.add(Box.createVerticalStrut(4));

        JLabel statusBadge = UIUtils.createStatusBadge("Available", UIUtils.SUCCESS);
        statusBadge.setAlignmentX(LEFT_ALIGNMENT);
        card.add(statusBadge);
        card.add(Box.createVerticalStrut(8));

        JLabel zoneLabel = new JLabel(desk.getZone());
        zoneLabel.setFont(UIUtils.FONT_SMALL);
        zoneLabel.setForeground(UIUtils.TEXT_SECONDARY);
        zoneLabel.setAlignmentX(LEFT_ALIGNMENT);
        card.add(zoneLabel);

        JLabel typeLabel = new JLabel(desk.getDeskType().getDisplayName());
        typeLabel.setFont(UIUtils.FONT_SMALL);
        typeLabel.setForeground(UIUtils.TEXT_SECONDARY);
        typeLabel.setAlignmentX(LEFT_ALIGNMENT);
        card.add(typeLabel);

        card.add(Box.createVerticalGlue());

        JButton bookBtn = UIUtils.createSuccessButton("Book");
        bookBtn.setAlignmentX(LEFT_ALIGNMENT);
        bookBtn.setMaximumSize(new Dimension(Integer.MAX_VALUE, 36));
        bookBtn.addActionListener(e -> {
            new SwingWorker<Booking, Void>() {
                @Override protected Booking doInBackground() throws Exception {
                    return bookingService.createBooking(currentUser.getId(), desk.getId(), date, start, end, "");
                }
                @Override protected void done() {
                    try {
                        Booking b = get();
                        UIUtils.showSuccess(BookingPanel.this, "Desk " + desk.getDeskCode() + " booked successfully! ID: " + b.getId());
                        searchAvailableDesks();
                    } catch (Exception ex) {
                        Throwable cause = ex.getCause();
                        if (cause instanceof BookingConflictException) {
                            UIUtils.showWarning(BookingPanel.this, cause.getMessage());
                        } else {
                            UIUtils.showError(BookingPanel.this, "Booking failed. Please try again.");
                        }
                        searchAvailableDesks();
                    }
                }
            }.execute();
        });
        card.add(bookBtn);

        return card;
    }
}
