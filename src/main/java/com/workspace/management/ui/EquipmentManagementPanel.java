package com.workspace.management.ui;

import com.workspace.management.dao.EquipmentDAO;
import com.workspace.management.dao.MeetingRoomDAO;
import com.workspace.management.dao.impl.EquipmentDAOImpl;
import com.workspace.management.dao.impl.MeetingRoomDAOImpl;
import com.workspace.management.model.Equipment;
import com.workspace.management.model.MeetingRoom;
import com.workspace.management.util.UIUtils;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;

/**
 * Equipment management panel.
 */
public class EquipmentManagementPanel extends JPanel {
    private final EquipmentDAO equipmentDAO;
    private final MeetingRoomDAO roomDAO;
    private JTable equipTable;
    private DefaultTableModel tableModel;

    public EquipmentManagementPanel() {
        this.equipmentDAO = new EquipmentDAOImpl();
        this.roomDAO = new MeetingRoomDAOImpl();
        setLayout(new BorderLayout(0, 12));
        setBackground(UIUtils.BG_MAIN);
        setBorder(new EmptyBorder(24, 24, 24, 24));
        buildUI();
        loadEquipment();
    }

    private void buildUI() {
        JPanel actionBar = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        actionBar.setOpaque(false);

        JButton addBtn = UIUtils.createPrimaryButton("Add Equipment");
        addBtn.addActionListener(e -> showAddEquipmentDialog());
        actionBar.add(addBtn);

        JButton editBtn = UIUtils.createSecondaryButton("Edit");
        editBtn.addActionListener(e -> editSelectedEquipment());
        actionBar.add(editBtn);

        JButton assignBtn = UIUtils.createSuccessButton("Assign to Room");
        assignBtn.addActionListener(e -> assignToRoom());
        actionBar.add(assignBtn);

        JButton removeBtn = UIUtils.createDangerButton("Remove from Room");
        removeBtn.addActionListener(e -> removeFromRoom());
        actionBar.add(removeBtn);

        JButton refreshBtn = UIUtils.createSecondaryButton("Refresh");
        refreshBtn.addActionListener(e -> loadEquipment());
        actionBar.add(refreshBtn);

        add(actionBar, BorderLayout.NORTH);

        String[] cols = {"ID", "Name", "Category", "Description", "Status"};
        tableModel = new DefaultTableModel(cols, 0) { @Override public boolean isCellEditable(int r, int c) { return false; } };
        equipTable = UIUtils.createStyledTable();
        equipTable.setModel(tableModel);
        add(UIUtils.createScrollPane(equipTable), BorderLayout.CENTER);
    }

    private void loadEquipment() {
        new SwingWorker<List<Equipment>, Void>() {
            @Override protected List<Equipment> doInBackground() { return equipmentDAO.findAll(); }
            @Override protected void done() {
                try {
                    tableModel.setRowCount(0);
                    for (Equipment e : get()) {
                        tableModel.addRow(new Object[]{e.getId(), e.getName(), e.getCategory(), e.getDescription(), e.getStatus()});
                    }
                } catch (Exception ignored) {}
            }
        }.execute();
    }

    private void showAddEquipmentDialog() {
        JPanel form = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(6, 6, 6, 6); gbc.fill = GridBagConstraints.HORIZONTAL;

        JTextField nameField = UIUtils.createTextField("Equipment name");
        JTextField catField = UIUtils.createTextField("Category");
        JTextField descField = UIUtils.createTextField("Description");

        UIUtils.addFormRow(form, "Name:", nameField, gbc, 0);
        UIUtils.addFormRow(form, "Category:", catField, gbc, 1);
        UIUtils.addFormRow(form, "Description:", descField, gbc, 2);

        int result = JOptionPane.showConfirmDialog(this, form, "Add Equipment", JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
        if (result == JOptionPane.OK_OPTION) {
            Equipment eq = new Equipment(nameField.getText().trim(), catField.getText().trim(), descField.getText().trim());
            new SwingWorker<Integer, Void>() {
                @Override protected Integer doInBackground() { return equipmentDAO.create(eq); }
                @Override protected void done() {
                    try { if(get()>0){UIUtils.showSuccess(EquipmentManagementPanel.this,"Added!");loadEquipment();}else UIUtils.showError(EquipmentManagementPanel.this,"Failed."); }
                    catch(Exception e) { UIUtils.showError(EquipmentManagementPanel.this,"Error."); }
                }
            }.execute();
        }
    }

    private void editSelectedEquipment() {
        int row = equipTable.getSelectedRow();
        if (row < 0) { UIUtils.showWarning(this, "Select equipment."); return; }
        int eqId = (int) tableModel.getValueAt(row, 0);

        new SwingWorker<Equipment, Void>() {
            @Override protected Equipment doInBackground() { return equipmentDAO.findById(eqId).orElse(null); }
            @Override protected void done() {
                try {
                    Equipment eq = get();
                    if (eq == null) return;
                    JPanel form = new JPanel(new GridBagLayout());
                    GridBagConstraints gbc = new GridBagConstraints();
                    gbc.insets = new Insets(6,6,6,6); gbc.fill = GridBagConstraints.HORIZONTAL;

                    JTextField nameField = UIUtils.createTextField(""); nameField.setText(eq.getName());
                    JTextField catField = UIUtils.createTextField(""); catField.setText(eq.getCategory());
                    JTextField descField = UIUtils.createTextField(""); descField.setText(eq.getDescription());
                    JComboBox<String> statusCombo = UIUtils.createComboBox();
                    statusCombo.addItem("ACTIVE"); statusCombo.addItem("INACTIVE"); statusCombo.addItem("MAINTENANCE");
                    statusCombo.setSelectedItem(eq.getStatus());

                    UIUtils.addFormRow(form, "Name:", nameField, gbc, 0);
                    UIUtils.addFormRow(form, "Category:", catField, gbc, 1);
                    UIUtils.addFormRow(form, "Description:", descField, gbc, 2);
                    UIUtils.addFormRow(form, "Status:", statusCombo, gbc, 3);

                    int result = JOptionPane.showConfirmDialog(EquipmentManagementPanel.this, form, "Edit Equipment", JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
                    if (result == JOptionPane.OK_OPTION) {
                        eq.setName(nameField.getText().trim());
                        eq.setCategory(catField.getText().trim());
                        eq.setDescription(descField.getText().trim());
                        eq.setStatus((String) statusCombo.getSelectedItem());
                        new SwingWorker<Boolean,Void>() {
                            @Override protected Boolean doInBackground() { return equipmentDAO.update(eq); }
                            @Override protected void done() { try{if(get()){UIUtils.showSuccess(EquipmentManagementPanel.this,"Updated.");loadEquipment();}} catch(Exception ignored){} }
                        }.execute();
                    }
                } catch (Exception ignored) {}
            }
        }.execute();
    }

    private void assignToRoom() {
        int row = equipTable.getSelectedRow();
        if (row < 0) { UIUtils.showWarning(this, "Select equipment."); return; }
        int eqId = (int) tableModel.getValueAt(row, 0);

        new SwingWorker<List<MeetingRoom>, Void>() {
            @Override protected List<MeetingRoom> doInBackground() { return roomDAO.findAll(); }
            @Override protected void done() {
                try {
                    List<MeetingRoom> rooms = get();
                    if (rooms.isEmpty()) { UIUtils.showWarning(EquipmentManagementPanel.this, "No rooms available."); return; }
                    String[] roomNames = rooms.stream().map(r -> r.getId() + " - " + r.getRoomName()).toArray(String[]::new);
                    String selected = (String) JOptionPane.showInputDialog(EquipmentManagementPanel.this,
                            "Select room:", "Assign Equipment", JOptionPane.PLAIN_MESSAGE, null, roomNames, roomNames[0]);
                    if (selected != null) {
                        int roomId = Integer.parseInt(selected.split(" - ")[0]);
                        new SwingWorker<Boolean,Void>() {
                            @Override protected Boolean doInBackground() { return equipmentDAO.addToRoom(roomId, eqId, 1); }
                            @Override protected void done() { try{if(get()) UIUtils.showSuccess(EquipmentManagementPanel.this,"Assigned!");} catch(Exception ignored){} }
                        }.execute();
                    }
                } catch (Exception ignored) {}
            }
        }.execute();
    }

    private void removeFromRoom() {
        int row = equipTable.getSelectedRow();
        if (row < 0) { UIUtils.showWarning(this, "Select equipment."); return; }
        int eqId = (int) tableModel.getValueAt(row, 0);

        new SwingWorker<List<MeetingRoom>, Void>() {
            @Override protected List<MeetingRoom> doInBackground() { return roomDAO.findAll(); }
            @Override protected void done() {
                try {
                    List<MeetingRoom> rooms = get();
                    String[] roomNames = rooms.stream().map(r -> r.getId() + " - " + r.getRoomName()).toArray(String[]::new);
                    String selected = (String) JOptionPane.showInputDialog(EquipmentManagementPanel.this,
                            "Select room:", "Remove Equipment", JOptionPane.PLAIN_MESSAGE, null, roomNames, roomNames[0]);
                    if (selected != null) {
                        int roomId = Integer.parseInt(selected.split(" - ")[0]);
                        new SwingWorker<Boolean,Void>() {
                            @Override protected Boolean doInBackground() { return equipmentDAO.removeFromRoom(roomId, eqId); }
                            @Override protected void done() { try{if(get()) UIUtils.showSuccess(EquipmentManagementPanel.this,"Removed!");} catch(Exception ignored){} }
                        }.execute();
                    }
                } catch (Exception ignored) {}
            }
        }.execute();
    }
}
