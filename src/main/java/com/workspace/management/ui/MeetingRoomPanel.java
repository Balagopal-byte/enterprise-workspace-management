package com.workspace.management.ui;

import com.workspace.management.dao.MeetingRoomDAO;
import com.workspace.management.dao.EquipmentDAO;
import com.workspace.management.dao.impl.MeetingRoomDAOImpl;
import com.workspace.management.dao.impl.EquipmentDAOImpl;
import com.workspace.management.exception.BookingConflictException;
import com.workspace.management.model.*;
import com.workspace.management.service.MeetingRoomService;
import com.workspace.management.util.DateTimeUtil;
import com.workspace.management.util.UIUtils;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

/**
 * Meeting room booking interface with room cards showing capacity and equipment.
 */
public class MeetingRoomPanel extends JPanel {
    private final User currentUser;
    private final MeetingRoomDAO roomDAO;
    private final EquipmentDAO equipmentDAO;
    private final MeetingRoomService roomService;
    private JPanel roomsGrid;
    private JLabel statusLabel;
    private final LocalTime[] timeSlots;

    public MeetingRoomPanel(User currentUser) {
        this.currentUser = currentUser;
        this.roomDAO = new MeetingRoomDAOImpl();
        this.equipmentDAO = new EquipmentDAOImpl();
        this.roomService = new MeetingRoomService();
        this.timeSlots = DateTimeUtil.generateTimeSlots();
        setLayout(new BorderLayout(0, 16));
        setBackground(UIUtils.BG_MAIN);
        setBorder(new EmptyBorder(24, 24, 24, 24));
        buildUI();
        loadRooms();
    }

    private void buildUI() {
        JLabel header = UIUtils.createSectionHeader("Available Meeting Rooms");
        add(header, BorderLayout.NORTH);

        roomsGrid = new JPanel(new GridLayout(0, 3, 16, 16));
        roomsGrid.setOpaque(false);

        JScrollPane scrollPane = new JScrollPane(roomsGrid);
        scrollPane.setBorder(null);
        scrollPane.getViewport().setBackground(UIUtils.BG_MAIN);
        add(scrollPane, BorderLayout.CENTER);

        statusLabel = new JLabel("Loading rooms...");
        statusLabel.setFont(UIUtils.FONT_SMALL);
        statusLabel.setForeground(UIUtils.TEXT_SECONDARY);
        add(statusLabel, BorderLayout.SOUTH);
    }

    private void loadRooms() {
        new SwingWorker<List<MeetingRoom>, Void>() {
            @Override
            protected List<MeetingRoom> doInBackground() {
                List<MeetingRoom> rooms = roomDAO.findAll();
                for (MeetingRoom room : rooms) {
                    room.setEquipmentList(equipmentDAO.findByRoom(room.getId()));
                }
                return rooms;
            }

            @Override
            protected void done() {
                try {
                    List<MeetingRoom> rooms = get();
                    roomsGrid.removeAll();
                    if (rooms.isEmpty()) {
                        roomsGrid.add(UIUtils.createEmptyStatePanel("No Meeting Rooms", "Rooms will appear here when added."));
                    } else {
                        for (MeetingRoom room : rooms) {
                            roomsGrid.add(createRoomCard(room));
                        }
                    }
                    statusLabel.setText(rooms.size() + " meeting room(s) available");
                    roomsGrid.revalidate();
                    roomsGrid.repaint();
                } catch (Exception e) {
                    statusLabel.setText("Error loading rooms.");
                }
            }
        }.execute();
    }

    private JPanel createRoomCard(MeetingRoom room) {
        JPanel card = UIUtils.createCard();
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setPreferredSize(new Dimension(300, 280));

        // Room name
        JLabel nameLabel = new JLabel(room.getRoomName());
        nameLabel.setFont(UIUtils.FONT_SUBTITLE);
        nameLabel.setForeground(UIUtils.TEXT_PRIMARY);
        nameLabel.setAlignmentX(LEFT_ALIGNMENT);
        card.add(nameLabel);
        card.add(Box.createVerticalStrut(4));

        // Status badge
        Color statusColor = switch (room.getStatus()) {
            case AVAILABLE -> UIUtils.SUCCESS;
            case BOOKED -> UIUtils.PRIMARY;
            case MAINTENANCE -> UIUtils.WARNING;
            case INACTIVE -> UIUtils.DESK_MAINTENANCE;
        };
        JLabel badge = UIUtils.createStatusBadge(room.getStatus().getDisplayName(), statusColor);
        badge.setAlignmentX(LEFT_ALIGNMENT);
        card.add(badge);
        card.add(Box.createVerticalStrut(12));

        // Details
        JLabel capLabel = new JLabel("\u263A Capacity: " + room.getCapacity() + " people");
        capLabel.setFont(UIUtils.FONT_SMALL);
        capLabel.setForeground(UIUtils.TEXT_SECONDARY);
        capLabel.setAlignmentX(LEFT_ALIGNMENT);
        card.add(capLabel);
        card.add(Box.createVerticalStrut(2));

        JLabel locLabel = new JLabel("\u2316 " + room.getLocation());
        locLabel.setFont(UIUtils.FONT_SMALL);
        locLabel.setForeground(UIUtils.TEXT_SECONDARY);
        locLabel.setAlignmentX(LEFT_ALIGNMENT);
        card.add(locLabel);
        card.add(Box.createVerticalStrut(8));

        // Equipment
        JLabel eqLabel = new JLabel("Equipment:");
        eqLabel.setFont(new Font("Segoe UI", Font.BOLD, 11));
        eqLabel.setForeground(UIUtils.TEXT_PRIMARY);
        eqLabel.setAlignmentX(LEFT_ALIGNMENT);
        card.add(eqLabel);
        card.add(Box.createVerticalStrut(2));

        String eqText = room.getEquipmentSummary();
        JLabel eqValLabel = new JLabel("<html><div style='width:220px;'>" + eqText + "</div></html>");
        eqValLabel.setFont(UIUtils.FONT_SMALL);
        eqValLabel.setForeground(UIUtils.TEXT_SECONDARY);
        eqValLabel.setAlignmentX(LEFT_ALIGNMENT);
        card.add(eqValLabel);

        card.add(Box.createVerticalGlue());

        // Book button
        if (room.getStatus() == MeetingRoom.RoomStatus.AVAILABLE || room.getStatus() == MeetingRoom.RoomStatus.BOOKED) {
            JButton bookBtn = UIUtils.createPrimaryButton("Book Room");
            bookBtn.setMaximumSize(new Dimension(Integer.MAX_VALUE, 36));
            bookBtn.setAlignmentX(LEFT_ALIGNMENT);
            bookBtn.addActionListener(e -> showBookRoomDialog(room));
            card.add(bookBtn);
        }

        return card;
    }

    private void showBookRoomDialog(MeetingRoom room) {
        JPanel form = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(6, 6, 6, 6);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        JTextField titleField = UIUtils.createTextField("Meeting title");
        JTextArea descArea = new JTextArea(2, 20);
        descArea.setBorder(BorderFactory.createLineBorder(UIUtils.BORDER));

        JSpinner dateSpinner = new JSpinner(new SpinnerDateModel());
        dateSpinner.setEditor(new JSpinner.DateEditor(dateSpinner, "yyyy-MM-dd"));

        JComboBox<String> startCombo = UIUtils.createComboBox();
        JComboBox<String> endCombo = UIUtils.createComboBox();
        for (LocalTime t : timeSlots) {
            startCombo.addItem(DateTimeUtil.formatTime(t));
            endCombo.addItem(DateTimeUtil.formatTime(t));
        }
        startCombo.setSelectedIndex(6); // 10:00
        endCombo.setSelectedIndex(8);   // 11:00

        JSpinner attendeeSpinner = new JSpinner(new SpinnerNumberModel(2, 1, room.getCapacity(), 1));

        UIUtils.addFormRow(form, "Title:", titleField, gbc, 0);
        UIUtils.addFormRow(form, "Date:", dateSpinner, gbc, 1);
        UIUtils.addFormRow(form, "Start:", startCombo, gbc, 2);
        UIUtils.addFormRow(form, "End:", endCombo, gbc, 3);
        UIUtils.addFormRow(form, "Attendees:", attendeeSpinner, gbc, 4);
        UIUtils.addFormRow(form, "Description:", new JScrollPane(descArea), gbc, 5);

        int result = JOptionPane.showConfirmDialog(this, form,
                "Book " + room.getRoomName(), JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);

        if (result == JOptionPane.OK_OPTION) {
            java.util.Date selDate = (java.util.Date) dateSpinner.getValue();
            LocalDate date = selDate.toInstant().atZone(java.time.ZoneId.systemDefault()).toLocalDate();
            LocalTime start = timeSlots[startCombo.getSelectedIndex()];
            LocalTime end = timeSlots[endCombo.getSelectedIndex()];

            new SwingWorker<MeetingRoomBooking, Void>() {
                @Override protected MeetingRoomBooking doInBackground() throws Exception {
                    return roomService.createBooking(currentUser.getId(), room.getId(), date, start, end,
                            titleField.getText().trim(), descArea.getText().trim(), (int) attendeeSpinner.getValue());
                }
                @Override protected void done() {
                    try {
                        MeetingRoomBooking b = get();
                        UIUtils.showSuccess(MeetingRoomPanel.this, room.getRoomName() + " booked successfully! ID: " + b.getId());
                        loadRooms();
                    } catch (Exception ex) {
                        Throwable cause = ex.getCause();
                        if (cause instanceof BookingConflictException) {
                            UIUtils.showWarning(MeetingRoomPanel.this, cause.getMessage());
                        } else {
                            UIUtils.showError(MeetingRoomPanel.this, "Room booking failed. Please try again.");
                        }
                    }
                }
            }.execute();
        }
    }
}
