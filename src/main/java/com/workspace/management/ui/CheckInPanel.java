package com.workspace.management.ui;

import com.workspace.management.model.Booking;
import com.workspace.management.model.User;
import com.workspace.management.service.BookingService;
import com.workspace.management.service.CheckInService;
import com.workspace.management.util.DateTimeUtil;
import com.workspace.management.util.UIUtils;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Check-In panel showing eligible bookings with check-in buttons.
 */
public class CheckInPanel extends JPanel {
    private final User currentUser;
    private final CheckInService checkInService;
    private final BookingService bookingService;
    private JPanel bookingsContainer;

    public CheckInPanel(User currentUser) {
        this.currentUser = currentUser;
        this.checkInService = new CheckInService();
        this.bookingService = new BookingService();
        setLayout(new BorderLayout(0, 16));
        setBackground(UIUtils.BG_MAIN);
        setBorder(new EmptyBorder(24, 24, 24, 24));
        buildUI();
        refreshData();
    }

    private void buildUI() {
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setOpaque(false);
        JLabel header = UIUtils.createSectionHeader("Check-In to Your Workspace");
        headerPanel.add(header, BorderLayout.WEST);

        JLabel graceInfo = new JLabel("Grace period: " + checkInService.getGracePeriodMinutes() + " minutes");
        graceInfo.setFont(UIUtils.FONT_SMALL);
        graceInfo.setForeground(UIUtils.TEXT_SECONDARY);
        headerPanel.add(graceInfo, BorderLayout.EAST);

        add(headerPanel, BorderLayout.NORTH);

        bookingsContainer = new JPanel();
        bookingsContainer.setLayout(new BoxLayout(bookingsContainer, BoxLayout.Y_AXIS));
        bookingsContainer.setOpaque(false);

        JScrollPane scrollPane = new JScrollPane(bookingsContainer);
        scrollPane.setBorder(null);
        scrollPane.getViewport().setBackground(UIUtils.BG_MAIN);
        add(scrollPane, BorderLayout.CENTER);
    }

    public void refreshData() {
        bookingsContainer.removeAll();
        bookingsContainer.add(UIUtils.createLoadingPanel("Loading check-in eligible bookings"));
        bookingsContainer.revalidate();

        new SwingWorker<List<Booking>, Void>() {
            @Override
            protected List<Booking> doInBackground() {
                List<Booking> upcoming = bookingService.getUpcomingBookings(currentUser.getId());
                List<Booking> active = bookingService.getActiveBookings(currentUser.getId());
                return Stream.concat(active.stream(), upcoming.stream())
                        .filter(b -> b.getStatus() == Booking.BookingStatus.CONFIRMED || b.getStatus() == Booking.BookingStatus.CHECKED_IN)
                        .collect(Collectors.toList());
            }

            @Override
            protected void done() {
                try {
                    List<Booking> bookings = get();
                    bookingsContainer.removeAll();

                    if (bookings.isEmpty()) {
                        bookingsContainer.add(UIUtils.createEmptyStatePanel(
                                "No Bookings to Check In",
                                "You don't have any active or upcoming desk reservations today."));
                    } else {
                        for (Booking booking : bookings) {
                            bookingsContainer.add(createCheckInCard(booking));
                            bookingsContainer.add(Box.createVerticalStrut(12));
                        }
                    }
                    bookingsContainer.add(Box.createVerticalGlue());
                    bookingsContainer.revalidate();
                    bookingsContainer.repaint();
                } catch (Exception e) {
                    bookingsContainer.removeAll();
                    bookingsContainer.add(UIUtils.createEmptyStatePanel("Error", "Failed to load bookings."));
                    bookingsContainer.revalidate();
                }
            }
        }.execute();
    }

    private JPanel createCheckInCard(Booking booking) {
        JPanel card = UIUtils.createCard();
        card.setLayout(new BorderLayout(16, 0));
        card.setMaximumSize(new Dimension(Integer.MAX_VALUE, 120));
        card.setAlignmentX(LEFT_ALIGNMENT);

        // Left: Desk info
        JPanel infoPanel = new JPanel();
        infoPanel.setLayout(new BoxLayout(infoPanel, BoxLayout.Y_AXIS));
        infoPanel.setOpaque(false);

        JLabel deskLabel = new JLabel("Desk " + booking.getDeskCode());
        deskLabel.setFont(UIUtils.FONT_SUBTITLE);
        deskLabel.setForeground(UIUtils.TEXT_PRIMARY);
        infoPanel.add(deskLabel);

        JLabel dateLabel = new JLabel(DateTimeUtil.getRelativeDateString(booking.getBookingDate()));
        dateLabel.setFont(UIUtils.FONT_BODY);
        dateLabel.setForeground(UIUtils.TEXT_SECONDARY);
        infoPanel.add(dateLabel);

        JLabel timeLabel = new JLabel(DateTimeUtil.formatTimeRange(booking.getStartTime(), booking.getEndTime()));
        timeLabel.setFont(UIUtils.FONT_HEADING);
        timeLabel.setForeground(UIUtils.PRIMARY);
        infoPanel.add(timeLabel);

        if (booking.getDeskZone() != null) {
            JLabel zoneLabel = new JLabel(booking.getDeskZone());
            zoneLabel.setFont(UIUtils.FONT_SMALL);
            zoneLabel.setForeground(UIUtils.TEXT_LIGHT);
            infoPanel.add(zoneLabel);
        }

        card.add(infoPanel, BorderLayout.CENTER);

        // Right: Status + Action
        JPanel actionPanel = new JPanel();
        actionPanel.setLayout(new BoxLayout(actionPanel, BoxLayout.Y_AXIS));
        actionPanel.setOpaque(false);

        // Status
        Color statusColor = UIUtils.getBookingStatusColor(booking.getStatus().name());
        JLabel statusBadge = UIUtils.createStatusBadge(booking.getStatus().getDisplayName(), statusColor);
        statusBadge.setAlignmentX(RIGHT_ALIGNMENT);
        actionPanel.add(statusBadge);
        actionPanel.add(Box.createVerticalStrut(8));

        if (booking.getStatus() == Booking.BookingStatus.CONFIRMED) {
            boolean eligible = checkInService.isWithinCheckInWindow(booking);
            JButton checkInBtn = UIUtils.createSuccessButton(eligible ? "CHECK IN" : "Not Yet");
            checkInBtn.setEnabled(eligible);
            checkInBtn.setAlignmentX(RIGHT_ALIGNMENT);

            if (eligible) {
                checkInBtn.addActionListener(e -> {
                    checkInBtn.setEnabled(false);
                    checkInBtn.setText("Checking in...");
                    new SwingWorker<Boolean, Void>() {
                        @Override protected Boolean doInBackground() { return checkInService.checkIn(booking.getId(), currentUser.getId()); }
                        @Override protected void done() {
                            try {
                                if (get()) {
                                    UIUtils.showSuccess(CheckInPanel.this, "Check-in successful for Desk " + booking.getDeskCode() + "!");
                                    refreshData();
                                } else {
                                    UIUtils.showWarning(CheckInPanel.this, "Check-in failed. The check-in window may have passed.");
                                    refreshData();
                                }
                            } catch (Exception ex) {
                                UIUtils.showError(CheckInPanel.this, "Check-in error. Please try again.");
                                checkInBtn.setEnabled(true);
                                checkInBtn.setText("CHECK IN");
                            }
                        }
                    }.execute();
                });
            }
            actionPanel.add(checkInBtn);
        } else if (booking.getStatus() == Booking.BookingStatus.CHECKED_IN) {
            JLabel checkedIn = new JLabel("\u2713 Checked In");
            checkedIn.setFont(UIUtils.FONT_HEADING);
            checkedIn.setForeground(UIUtils.SUCCESS);
            checkedIn.setAlignmentX(RIGHT_ALIGNMENT);
            actionPanel.add(checkedIn);
        }

        card.add(actionPanel, BorderLayout.EAST);
        return card;
    }
}
