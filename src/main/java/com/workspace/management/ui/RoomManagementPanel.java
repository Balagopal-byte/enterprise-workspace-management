package com.workspace.management.ui;

import com.workspace.management.dao.MeetingRoomDAO;
import com.workspace.management.dao.impl.MeetingRoomDAOImpl;
import com.workspace.management.model.MeetingRoom;
import com.workspace.management.util.UIUtils;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;

/**
 * Room management panel for Facility Managers and System Admins.
 */
public class RoomManagementPanel extends JPanel {
    private final MeetingRoomDAO roomDAO;
    private JTable roomTable;
    private DefaultTableModel tableModel;

    public RoomManagementPanel() {
        this.roomDAO = new MeetingRoomDAOImpl();
        setLayout(new BorderLayout(0, 12));
        setBackground(UIUtils.BG_MAIN);
        setBorder(new EmptyBorder(24, 24, 24, 24));
        buildUI();
        loadRooms();
    }

    private void buildUI() {
        JPanel actionBar = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        actionBar.setOpaque(false);

        JButton addBtn = UIUtils.createPrimaryButton("Add Room");
        addBtn.addActionListener(e -> showAddRoomDialog());
        actionBar.add(addBtn);

        JButton editBtn = UIUtils.createSecondaryButton("Edit");
        editBtn.addActionListener(e -> editSelectedRoom());
        actionBar.add(editBtn);

        JButton maintenanceBtn = UIUtils.createButton("Maintenance", UIUtils.WARNING);
        maintenanceBtn.addActionListener(e -> setRoomStatus("MAINTENANCE"));
        actionBar.add(maintenanceBtn);

        JButton activateBtn = UIUtils.createSuccessButton("Activate");
        activateBtn.addActionListener(e -> setRoomStatus("AVAILABLE"));
        actionBar.add(activateBtn);

        JButton disableBtn = UIUtils.createDangerButton("Disable");
        disableBtn.addActionListener(e -> setRoomStatus("INACTIVE"));
        actionBar.add(disableBtn);

        JButton refreshBtn = UIUtils.createSecondaryButton("Refresh");
        refreshBtn.addActionListener(e -> loadRooms());
        actionBar.add(refreshBtn);

        add(actionBar, BorderLayout.NORTH);

        String[] cols = {"ID", "Name", "Floor", "Capacity", "Location", "Status", "Description"};
        tableModel = new DefaultTableModel(cols, 0) { @Override public boolean isCellEditable(int r, int c) { return false; } };
        roomTable = UIUtils.createStyledTable();
        roomTable.setModel(tableModel);
        add(UIUtils.createScrollPane(roomTable), BorderLayout.CENTER);
    }

    private void loadRooms() {
        new SwingWorker<List<MeetingRoom>, Void>() {
            @Override protected List<MeetingRoom> doInBackground() { return roomDAO.findAll(); }
            @Override protected void done() {
                try {
                    tableModel.setRowCount(0);
                    for (MeetingRoom r : get()) {
                        tableModel.addRow(new Object[]{r.getId(), r.getRoomName(), r.getFloor(), r.getCapacity(), r.getLocation(), r.getStatus().name(), r.getDescription()});
                    }
                } catch (Exception ignored) {}
            }
        }.execute();
    }

    private void showAddRoomDialog() {
        JPanel form = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(6, 6, 6, 6); gbc.fill = GridBagConstraints.HORIZONTAL;

        JTextField nameField = UIUtils.createTextField("Room name");
        JSpinner floorSpinner = new JSpinner(new SpinnerNumberModel(1, 1, 10, 1));
        JSpinner capSpinner = new JSpinner(new SpinnerNumberModel(4, 1, 100, 1));
        JTextField locField = UIUtils.createTextField("Location");
        JTextField descField = UIUtils.createTextField("Description");

        UIUtils.addFormRow(form, "Name:", nameField, gbc, 0);
        UIUtils.addFormRow(form, "Floor:", floorSpinner, gbc, 1);
        UIUtils.addFormRow(form, "Capacity:", capSpinner, gbc, 2);
        UIUtils.addFormRow(form, "Location:", locField, gbc, 3);
        UIUtils.addFormRow(form, "Description:", descField, gbc, 4);

        int result = JOptionPane.showConfirmDialog(this, form, "Add Meeting Room", JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
        if (result == JOptionPane.OK_OPTION) {
            MeetingRoom room = new MeetingRoom(nameField.getText().trim(), (int) floorSpinner.getValue(),
                    (int) capSpinner.getValue(), locField.getText().trim());
            room.setDescription(descField.getText().trim());

            new SwingWorker<Integer, Void>() {
                @Override protected Integer doInBackground() { return roomDAO.create(room); }
                @Override protected void done() {
                    try { if(get()>0){UIUtils.showSuccess(RoomManagementPanel.this,"Room added!");loadRooms();}else{UIUtils.showError(RoomManagementPanel.this,"Failed.");} }
                    catch(Exception e){UIUtils.showError(RoomManagementPanel.this,"Error.");}
                }
            }.execute();
        }
    }

    private void editSelectedRoom() {
        int row = roomTable.getSelectedRow();
        if (row < 0) { UIUtils.showWarning(this, "Select a room."); return; }
        int roomId = (int) tableModel.getValueAt(row, 0);

        new SwingWorker<MeetingRoom, Void>() {
            @Override protected MeetingRoom doInBackground() { return roomDAO.findById(roomId).orElse(null); }
            @Override protected void done() {
                try {
                    MeetingRoom room = get();
                    if (room == null) return;
                    JPanel form = new JPanel(new GridBagLayout());
                    GridBagConstraints gbc = new GridBagConstraints();
                    gbc.insets = new Insets(6,6,6,6); gbc.fill = GridBagConstraints.HORIZONTAL;

                    JTextField nameField = UIUtils.createTextField(""); nameField.setText(room.getRoomName());
                    JSpinner capSpinner = new JSpinner(new SpinnerNumberModel(room.getCapacity(), 1, 100, 1));
                    JTextField locField = UIUtils.createTextField(""); locField.setText(room.getLocation());
                    JTextField descField = UIUtils.createTextField(""); descField.setText(room.getDescription());

                    UIUtils.addFormRow(form, "Name:", nameField, gbc, 0);
                    UIUtils.addFormRow(form, "Capacity:", capSpinner, gbc, 1);
                    UIUtils.addFormRow(form, "Location:", locField, gbc, 2);
                    UIUtils.addFormRow(form, "Description:", descField, gbc, 3);

                    int result = JOptionPane.showConfirmDialog(RoomManagementPanel.this, form, "Edit Room", JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
                    if (result == JOptionPane.OK_OPTION) {
                        room.setRoomName(nameField.getText().trim());
                        room.setCapacity((int) capSpinner.getValue());
                        room.setLocation(locField.getText().trim());
                        room.setDescription(descField.getText().trim());
                        new SwingWorker<Boolean,Void>() {
                            @Override protected Boolean doInBackground() { return roomDAO.update(room); }
                            @Override protected void done() { try{if(get()){UIUtils.showSuccess(RoomManagementPanel.this,"Updated.");loadRooms();}} catch(Exception ignored){} }
                        }.execute();
                    }
                } catch (Exception ignored) {}
            }
        }.execute();
    }

    private void setRoomStatus(String status) {
        int row = roomTable.getSelectedRow();
        if (row < 0) { UIUtils.showWarning(this, "Select a room."); return; }
        int roomId = (int) tableModel.getValueAt(row, 0);
        new SwingWorker<Boolean, Void>() {
            @Override protected Boolean doInBackground() { return roomDAO.updateStatus(roomId, status); }
            @Override protected void done() { try{if(get()){UIUtils.showSuccess(RoomManagementPanel.this,"Status updated.");loadRooms();}} catch(Exception ignored){} }
        }.execute();
    }
}
