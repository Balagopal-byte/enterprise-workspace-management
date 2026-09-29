package com.workspace.management.ui;

import com.workspace.management.dao.DeskDAO;
import com.workspace.management.dao.impl.DeskDAOImpl;
import com.workspace.management.model.Desk;
import com.workspace.management.util.UIUtils;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;

/**
 * Desk management panel for Facility Managers and System Admins.
 */
public class DeskManagementPanel extends JPanel {
    private final DeskDAO deskDAO;
    private JTable deskTable;
    private DefaultTableModel tableModel;

    public DeskManagementPanel() {
        this.deskDAO = new DeskDAOImpl();
        setLayout(new BorderLayout(0, 12));
        setBackground(UIUtils.BG_MAIN);
        setBorder(new EmptyBorder(24, 24, 24, 24));
        buildUI();
        loadDesks();
    }

    private void buildUI() {
        JPanel actionBar = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        actionBar.setOpaque(false);

        JButton addBtn = UIUtils.createPrimaryButton("Add Desk");
        addBtn.addActionListener(e -> showAddDeskDialog());
        actionBar.add(addBtn);

        JButton editBtn = UIUtils.createSecondaryButton("Edit");
        editBtn.addActionListener(e -> editSelectedDesk());
        actionBar.add(editBtn);

        JButton maintenanceBtn = UIUtils.createButton("Maintenance", UIUtils.WARNING);
        maintenanceBtn.addActionListener(e -> setDeskStatus("MAINTENANCE"));
        actionBar.add(maintenanceBtn);

        JButton activateBtn = UIUtils.createSuccessButton("Activate");
        activateBtn.addActionListener(e -> setDeskStatus("AVAILABLE"));
        actionBar.add(activateBtn);

        JButton disableBtn = UIUtils.createDangerButton("Disable");
        disableBtn.addActionListener(e -> setDeskStatus("INACTIVE"));
        actionBar.add(disableBtn);

        JButton refreshBtn = UIUtils.createSecondaryButton("Refresh");
        refreshBtn.addActionListener(e -> loadDesks());
        actionBar.add(refreshBtn);

        add(actionBar, BorderLayout.NORTH);

        String[] cols = {"ID", "Code", "Floor", "Zone", "Type", "Pos X", "Pos Y", "Status", "Monitor", "Phone"};
        tableModel = new DefaultTableModel(cols, 0) { @Override public boolean isCellEditable(int r, int c) { return false; } };
        deskTable = UIUtils.createStyledTable();
        deskTable.setModel(tableModel);
        add(UIUtils.createScrollPane(deskTable), BorderLayout.CENTER);
    }

    private void loadDesks() {
        new SwingWorker<List<Desk>, Void>() {
            @Override protected List<Desk> doInBackground() { return deskDAO.findAll(); }
            @Override protected void done() {
                try {
                    tableModel.setRowCount(0);
                    for (Desk d : get()) {
                        tableModel.addRow(new Object[]{d.getId(), d.getDeskCode(), d.getFloor(), d.getZone(),
                            d.getDeskType().name(), d.getPositionX(), d.getPositionY(), d.getStatus().name(),
                            d.isHasMonitor() ? "Yes" : "No", d.isHasPhone() ? "Yes" : "No"});
                    }
                } catch (Exception ignored) {}
            }
        }.execute();
    }

    private void showAddDeskDialog() {
        JPanel form = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(6, 6, 6, 6); gbc.fill = GridBagConstraints.HORIZONTAL;

        JTextField codeField = UIUtils.createTextField("e.g. D23");
        JSpinner floorSpinner = new JSpinner(new SpinnerNumberModel(1, 1, 10, 1));
        JTextField zoneField = UIUtils.createTextField("e.g. Open Workspace");
        JSpinner posXSpinner = new JSpinner(new SpinnerNumberModel(50, 0, 100, 5));
        JSpinner posYSpinner = new JSpinner(new SpinnerNumberModel(50, 0, 100, 5));
        JComboBox<String> typeCombo = UIUtils.createComboBox();
        for (Desk.DeskType t : Desk.DeskType.values()) typeCombo.addItem(t.name());
        JCheckBox monitorCheck = new JCheckBox("Monitor", true);
        JCheckBox phoneCheck = new JCheckBox("Phone");
        JTextField descField = UIUtils.createTextField("Description");

        UIUtils.addFormRow(form, "Code:", codeField, gbc, 0);
        UIUtils.addFormRow(form, "Floor:", floorSpinner, gbc, 1);
        UIUtils.addFormRow(form, "Zone:", zoneField, gbc, 2);
        UIUtils.addFormRow(form, "Position X %:", posXSpinner, gbc, 3);
        UIUtils.addFormRow(form, "Position Y %:", posYSpinner, gbc, 4);
        UIUtils.addFormRow(form, "Type:", typeCombo, gbc, 5);
        UIUtils.addFormRow(form, "Monitor:", monitorCheck, gbc, 6);
        UIUtils.addFormRow(form, "Phone:", phoneCheck, gbc, 7);
        UIUtils.addFormRow(form, "Description:", descField, gbc, 8);

        int result = JOptionPane.showConfirmDialog(this, form, "Add New Desk", JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
        if (result == JOptionPane.OK_OPTION) {
            Desk desk = new Desk(codeField.getText().trim(), (int) floorSpinner.getValue(),
                    zoneField.getText().trim(), (int) posXSpinner.getValue(), (int) posYSpinner.getValue());
            desk.setDeskType((String) typeCombo.getSelectedItem());
            desk.setHasMonitor(monitorCheck.isSelected());
            desk.setHasPhone(phoneCheck.isSelected());
            desk.setDescription(descField.getText().trim());

            new SwingWorker<Integer, Void>() {
                @Override protected Integer doInBackground() { return deskDAO.create(desk); }
                @Override protected void done() {
                    try { if(get()>0){UIUtils.showSuccess(DeskManagementPanel.this,"Desk added!");loadDesks();}else{UIUtils.showError(DeskManagementPanel.this,"Failed. Code may already exist.");} }
                    catch(Exception e){UIUtils.showError(DeskManagementPanel.this,"Error adding desk.");}
                }
            }.execute();
        }
    }

    private void editSelectedDesk() {
        int row = deskTable.getSelectedRow();
        if (row < 0) { UIUtils.showWarning(this, "Select a desk to edit."); return; }
        int deskId = (int) tableModel.getValueAt(row, 0);

        new SwingWorker<Desk, Void>() {
            @Override protected Desk doInBackground() { return deskDAO.findById(deskId).orElse(null); }
            @Override protected void done() {
                try {
                    Desk desk = get();
                    if (desk == null) return;
                    JPanel form = new JPanel(new GridBagLayout());
                    GridBagConstraints gbc = new GridBagConstraints();
                    gbc.insets = new Insets(6,6,6,6); gbc.fill = GridBagConstraints.HORIZONTAL;

                    JTextField codeField = UIUtils.createTextField(""); codeField.setText(desk.getDeskCode());
                    JTextField zoneField = UIUtils.createTextField(""); zoneField.setText(desk.getZone());
                    JSpinner posX = new JSpinner(new SpinnerNumberModel(desk.getPositionX(), 0, 100, 5));
                    JSpinner posY = new JSpinner(new SpinnerNumberModel(desk.getPositionY(), 0, 100, 5));

                    UIUtils.addFormRow(form, "Code:", codeField, gbc, 0);
                    UIUtils.addFormRow(form, "Zone:", zoneField, gbc, 1);
                    UIUtils.addFormRow(form, "Position X:", posX, gbc, 2);
                    UIUtils.addFormRow(form, "Position Y:", posY, gbc, 3);

                    int result = JOptionPane.showConfirmDialog(DeskManagementPanel.this, form, "Edit Desk", JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
                    if (result == JOptionPane.OK_OPTION) {
                        desk.setDeskCode(codeField.getText().trim());
                        desk.setZone(zoneField.getText().trim());
                        desk.setPositionX((int) posX.getValue());
                        desk.setPositionY((int) posY.getValue());
                        new SwingWorker<Boolean,Void>() {
                            @Override protected Boolean doInBackground() { return deskDAO.update(desk); }
                            @Override protected void done() { try{if(get()){UIUtils.showSuccess(DeskManagementPanel.this,"Updated.");loadDesks();}} catch(Exception ignored){} }
                        }.execute();
                    }
                } catch (Exception ignored) {}
            }
        }.execute();
    }

    private void setDeskStatus(String status) {
        int row = deskTable.getSelectedRow();
        if (row < 0) { UIUtils.showWarning(this, "Select a desk."); return; }
        int deskId = (int) tableModel.getValueAt(row, 0);
        new SwingWorker<Boolean, Void>() {
            @Override protected Boolean doInBackground() { return deskDAO.updateStatus(deskId, status); }
            @Override protected void done() { try{if(get()){UIUtils.showSuccess(DeskManagementPanel.this,"Status updated.");loadDesks();}} catch(Exception ignored){} }
        }.execute();
    }
}
