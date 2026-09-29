package com.workspace.management.ui;

import com.workspace.management.model.Booking;
import com.workspace.management.model.MeetingRoomBooking;
import com.workspace.management.model.User;
import com.workspace.management.service.BookingService;
import com.workspace.management.service.CheckInService;
import com.workspace.management.service.MeetingRoomService;
import com.workspace.management.util.DateTimeUtil;
import com.workspace.management.util.UIUtils;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;
import java.util.stream.Collectors;

/**
 * My Bookings panel with tabbed view: Upcoming, Active, Completed, Cancelled.
 */
public class MyBookingsPanel extends JPanel {
    private final User currentUser;
    private final BookingService bookingService;
    private final MeetingRoomService roomService;
    private final CheckInService checkInService;
    private JTabbedPane tabbedPane;

    public MyBookingsPanel(User currentUser) {
        this.currentUser = currentUser;
        this.bookingService = new BookingService();
        this.roomService = new MeetingRoomService();
        this.checkInService = new CheckInService();
        setLayout(new BorderLayout());
        setBackground(UIUtils.BG_MAIN);
        setBorder(new EmptyBorder(24, 24, 24, 24));
        buildUI();
    }

    private void buildUI() {
        tabbedPane = new JTabbedPane();
        tabbedPane.setFont(UIUtils.FONT_HEADING);
        tabbedPane.addTab("Upcoming", createBookingTab("UPCOMING"));
        tabbedPane.addTab("Active", createBookingTab("ACTIVE"));
        tabbedPane.addTab("Completed", createBookingTab("COMPLETED"));
        tabbedPane.addTab("Cancelled", createBookingTab("CANCELLED"));
        tabbedPane.addTab("Room Bookings", createRoomBookingTab());

        tabbedPane.addChangeListener(e -> refreshData());
        add(tabbedPane, BorderLayout.CENTER);
    }

    private JPanel createBookingTab(String type) {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(UIUtils.BG_MAIN);
        panel.putClientProperty("type", type);
        return panel;
    }

    private JPanel createRoomBookingTab() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(UIUtils.BG_MAIN);
        panel.putClientProperty("type", "ROOM");
        return panel;
    }

    public void refreshData() {
        int idx = tabbedPane.getSelectedIndex();
        JPanel panel = (JPanel) tabbedPane.getComponentAt(idx);
        String type = (String) panel.getClientProperty("type");

        if ("ROOM".equals(type)) {
            loadRoomBookings(panel);
        } else {
            loadDeskBookings(panel, type);
        }
    }

    private void loadDeskBookings(JPanel panel, String type) {
        panel.removeAll();
        panel.add(UIUtils.createLoadingPanel("Loading bookings"), BorderLayout.CENTER);
        panel.revalidate();

        new SwingWorker<List<Booking>, Void>() {
            @Override
            protected List<Booking> doInBackground() {
                List<Booking> all = bookingService.getUserBookings(currentUser.getId());
                return switch (type) {
                    case "UPCOMING" -> all.stream()
                            .filter(b -> b.getStatus() == Booking.BookingStatus.CONFIRMED || b.getStatus() == Booking.BookingStatus.PENDING)
                            .collect(Collectors.toList());
                    case "ACTIVE" -> all.stream()
                            .filter(b -> b.getStatus() == Booking.BookingStatus.CHECKED_IN)
                            .collect(Collectors.toList());
                    case "COMPLETED" -> all.stream()
                            .filter(b -> b.getStatus() == Booking.BookingStatus.COMPLETED || b.getStatus() == Booking.BookingStatus.AUTO_RELEASED)
                            .collect(Collectors.toList());
                    case "CANCELLED" -> all.stream()
                            .filter(b -> b.getStatus() == Booking.BookingStatus.CANCELLED)
                            .collect(Collectors.toList());
                    default -> all;
                };
            }

            @Override
            protected void done() {
                try {
                    List<Booking> bookings = get();
                    panel.removeAll();
                    if (bookings.isEmpty()) {
                        panel.add(UIUtils.createEmptyStatePanel("No Bookings", "You don't have any " + type.toLowerCase() + " bookings."), BorderLayout.CENTER);
                    } else {
                        String[] cols = {"ID", "Desk", "Zone", "Date", "Time", "Status", "Actions"};
                        Object[][] data = new Object[bookings.size()][7];
                        for (int i = 0; i < bookings.size(); i++) {
                            Booking b = bookings.get(i);
                            data[i] = new Object[]{
                                b.getId(), b.getDeskCode(), b.getDeskZone(),
                                DateTimeUtil.formatDate(b.getBookingDate()),
                                DateTimeUtil.formatTimeRange(b.getStartTime(), b.getEndTime()),
                                b.getStatus().getDisplayName(), ""
                            };
                        }
                        DefaultTableModel model = new DefaultTableModel(data, cols) {
                            @Override public boolean isCellEditable(int r, int c) { return false; }
                        };
                        JTable table = UIUtils.createStyledTable();
                        table.setModel(model);

                        // Action buttons panel
                        JPanel actionPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
                        actionPanel.setOpaque(false);

                        if ("UPCOMING".equals(type)) {
                            JButton cancelBtn = UIUtils.createDangerButton("Cancel Selected");
                            cancelBtn.addActionListener(e -> {
                                int row = table.getSelectedRow();
                                if (row >= 0) {
                                    int bookingId = (int) table.getValueAt(row, 0);
                                    if (UIUtils.showConfirmDialog(panel, "Cancel Booking", "Are you sure you want to cancel this booking?")) {
                                        new SwingWorker<Boolean, Void>() {
                                            @Override protected Boolean doInBackground() { return bookingService.cancelBooking(bookingId, currentUser.getId()); }
                                            @Override protected void done() {
                                                try {
                                                    if (get()) { UIUtils.showSuccess(panel, "Booking cancelled."); refreshData(); }
                                                    else UIUtils.showError(panel, "Could not cancel booking.");
                                                } catch (Exception ex) { UIUtils.showError(panel, "Error cancelling booking."); }
                                            }
                                        }.execute();
                                    }
                                } else { UIUtils.showWarning(panel, "Please select a booking to cancel."); }
                            });
                            actionPanel.add(cancelBtn);

                            JButton checkInBtn = UIUtils.createSuccessButton("Check In");
                            checkInBtn.addActionListener(e -> {
                                int row = table.getSelectedRow();
                                if (row >= 0) {
                                    int bookingId = (int) table.getValueAt(row, 0);
                                    new SwingWorker<Boolean, Void>() {
                                        @Override protected Boolean doInBackground() { return checkInService.checkIn(bookingId, currentUser.getId()); }
                                        @Override protected void done() {
                                            try {
                                                if (get()) { UIUtils.showSuccess(panel, "Check-in successful!"); refreshData(); }
                                                else UIUtils.showWarning(panel, "Check-in not available. Please check the time window.");
                                            } catch (Exception ex) { UIUtils.showError(panel, "Check-in failed."); }
                                        }
                                    }.execute();
                                } else { UIUtils.showWarning(panel, "Please select a booking to check in."); }
                            });
                            actionPanel.add(checkInBtn);
                        }

                        panel.add(UIUtils.createScrollPane(table), BorderLayout.CENTER);
                        panel.add(actionPanel, BorderLayout.SOUTH);
                    }
                    panel.revalidate();
                    panel.repaint();
                } catch (Exception e) {
                    panel.removeAll();
                    panel.add(UIUtils.createEmptyStatePanel("Error", "Failed to load bookings."), BorderLayout.CENTER);
                    panel.revalidate();
                }
            }
        }.execute();
    }

    private void loadRoomBookings(JPanel panel) {
        panel.removeAll();
        panel.add(UIUtils.createLoadingPanel("Loading room bookings"), BorderLayout.CENTER);
        panel.revalidate();

        new SwingWorker<List<MeetingRoomBooking>, Void>() {
            @Override protected List<MeetingRoomBooking> doInBackground() { return roomService.getUserBookings(currentUser.getId()); }
            @Override protected void done() {
                try {
                    List<MeetingRoomBooking> bookings = get();
                    panel.removeAll();
                    if (bookings.isEmpty()) {
                        panel.add(UIUtils.createEmptyStatePanel("No Room Bookings", "You haven't booked any meeting rooms."), BorderLayout.CENTER);
                    } else {
                        String[] cols = {"ID", "Room", "Title", "Date", "Time", "Attendees", "Status"};
                        Object[][] data = new Object[bookings.size()][7];
                        for (int i = 0; i < bookings.size(); i++) {
                            MeetingRoomBooking b = bookings.get(i);
                            data[i] = new Object[]{ b.getId(), b.getRoomName(), b.getTitle(),
                                DateTimeUtil.formatDate(b.getBookingDate()),
                                DateTimeUtil.formatTimeRange(b.getStartTime(), b.getEndTime()),
                                b.getAttendeeCount(), b.getStatus().getDisplayName() };
                        }
                        JTable table = UIUtils.createStyledTable();
                        table.setModel(new DefaultTableModel(data, cols) { @Override public boolean isCellEditable(int r, int c) { return false; } });

                        JPanel actionPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
                        actionPanel.setOpaque(false);
                        JButton cancelBtn = UIUtils.createDangerButton("Cancel Selected");
                        cancelBtn.addActionListener(e -> {
                            int row = table.getSelectedRow();
                            if (row >= 0) {
                                int id = (int) table.getValueAt(row, 0);
                                if (UIUtils.showConfirmDialog(panel, "Cancel", "Cancel this room booking?")) {
                                    new SwingWorker<Boolean, Void>() {
                                        @Override protected Boolean doInBackground() { return roomService.cancelBooking(id, currentUser.getId()); }
                                        @Override protected void done() { try { if(get()){UIUtils.showSuccess(panel,"Cancelled.");refreshData();}else{UIUtils.showError(panel,"Failed.");} } catch(Exception ex){UIUtils.showError(panel,"Error.");} }
                                    }.execute();
                                }
                            }
                        });
                        actionPanel.add(cancelBtn);

                        panel.add(UIUtils.createScrollPane(table), BorderLayout.CENTER);
                        panel.add(actionPanel, BorderLayout.SOUTH);
                    }
                    panel.revalidate();
                    panel.repaint();
                } catch (Exception ignored) {}
            }
        }.execute();
    }
}
