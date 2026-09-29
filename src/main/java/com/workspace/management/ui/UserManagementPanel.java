package com.workspace.management.ui;

import com.workspace.management.dao.UserDAO;
import com.workspace.management.dao.impl.UserDAOImpl;
import com.workspace.management.model.Role;
import com.workspace.management.model.User;
import com.workspace.management.service.AnalyticsService;
import com.workspace.management.util.PasswordUtil;
import com.workspace.management.util.UIUtils;
import com.workspace.management.util.ValidationUtil;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;

/**
 * User management panel for System Administrators.
 */
public class UserManagementPanel extends JPanel {
    private final UserDAO userDAO;
    private final AnalyticsService analyticsService;
    private JTable userTable;
    private DefaultTableModel tableModel;

    public UserManagementPanel() {
        this.userDAO = new UserDAOImpl();
        this.analyticsService = new AnalyticsService();
        setLayout(new BorderLayout(0, 12));
        setBackground(UIUtils.BG_MAIN);
        setBorder(new EmptyBorder(24, 24, 24, 24));
        buildUI();
        loadUsers();
    }

    private void buildUI() {
        // Actions bar
        JPanel actionBar = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        actionBar.setOpaque(false);

        JButton addBtn = UIUtils.createPrimaryButton("Add User");
        addBtn.addActionListener(e -> showAddUserDialog());
        actionBar.add(addBtn);

        JButton editBtn = UIUtils.createSecondaryButton("Edit");
        editBtn.addActionListener(e -> editSelectedUser());
        actionBar.add(editBtn);

        JButton disableBtn = UIUtils.createDangerButton("Disable");
        disableBtn.addActionListener(e -> toggleUserStatus("INACTIVE"));
        actionBar.add(disableBtn);

        JButton enableBtn = UIUtils.createSuccessButton("Enable");
        enableBtn.addActionListener(e -> toggleUserStatus("ACTIVE"));
        actionBar.add(enableBtn);

        JButton resetPwdBtn = UIUtils.createSecondaryButton("Reset Password");
        resetPwdBtn.addActionListener(e -> resetPassword());
        actionBar.add(resetPwdBtn);

        JButton refreshBtn = UIUtils.createSecondaryButton("Refresh");
        refreshBtn.addActionListener(e -> loadUsers());
        actionBar.add(refreshBtn);

        add(actionBar, BorderLayout.NORTH);

        // Table
        String[] columns = {"ID", "Name", "Email", "Role", "Department", "Status", "Created"};
        tableModel = new DefaultTableModel(columns, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        userTable = UIUtils.createStyledTable();
        userTable.setModel(tableModel);
        add(UIUtils.createScrollPane(userTable), BorderLayout.CENTER);
    }

    private void loadUsers() {
        new SwingWorker<List<User>, Void>() {
            @Override protected List<User> doInBackground() { return userDAO.findAll(); }
            @Override protected void done() {
                try {
                    List<User> users = get();
                    tableModel.setRowCount(0);
                    for (User u : users) {
                        tableModel.addRow(new Object[]{
                            u.getId(), u.getFullName(), u.getEmail(),
                            u.getRole() != null ? u.getRole().getDisplayName() : "N/A",
                            u.getDepartment(), u.getStatus(),
                            u.getCreatedAt() != null ? u.getCreatedAt().toLocalDate().toString() : ""
                        });
                    }
                } catch (Exception ignored) {}
            }
        }.execute();
    }

    private void showAddUserDialog() {
        JPanel form = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(6, 6, 6, 6);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        JTextField firstNameField = UIUtils.createTextField("First name");
        JTextField lastNameField = UIUtils.createTextField("Last name");
        JTextField emailField = UIUtils.createTextField("Email");
        JPasswordField passwordField = UIUtils.createPasswordField("Password");
        JTextField deptField = UIUtils.createTextField("Department");
        JTextField phoneField = UIUtils.createTextField("Phone");

        JComboBox<String> roleCombo = UIUtils.createComboBox();
        roleCombo.addItem("Employee");
        roleCombo.addItem("Facility Manager");
        roleCombo.addItem("System Administrator");

        UIUtils.addFormRow(form, "First Name:", firstNameField, gbc, 0);
        UIUtils.addFormRow(form, "Last Name:", lastNameField, gbc, 1);
        UIUtils.addFormRow(form, "Email:", emailField, gbc, 2);
        UIUtils.addFormRow(form, "Password:", passwordField, gbc, 3);
        UIUtils.addFormRow(form, "Role:", roleCombo, gbc, 4);
        UIUtils.addFormRow(form, "Department:", deptField, gbc, 5);
        UIUtils.addFormRow(form, "Phone:", phoneField, gbc, 6);

        int result = JOptionPane.showConfirmDialog(this, form, "Add New User", JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
        if (result == JOptionPane.OK_OPTION) {
            String firstName = firstNameField.getText().trim();
            String lastName = lastNameField.getText().trim();
            String email = emailField.getText().trim();
            String password = new String(passwordField.getPassword());

            if (!ValidationUtil.isValidName(firstName) || !ValidationUtil.isValidName(lastName)) {
                UIUtils.showError(this, "Please enter valid first and last names."); return;
            }
            if (!ValidationUtil.isValidEmail(email)) {
                UIUtils.showError(this, "Please enter a valid email address."); return;
            }
            if (!ValidationUtil.isValidPassword(password)) {
                UIUtils.showError(this, "Password must be at least 6 characters."); return;
            }

            int roleId = roleCombo.getSelectedIndex() == 2 ? 1 : roleCombo.getSelectedIndex() == 1 ? 2 : 3;

            new SwingWorker<Integer, Void>() {
                @Override protected Integer doInBackground() {
                    User user = new User(firstName, lastName, email, PasswordUtil.hashPassword(password), roleId);
                    user.setDepartment(deptField.getText().trim());
                    user.setPhone(phoneField.getText().trim());
                    int id = userDAO.create(user);
                    if (id > 0) {
                        analyticsService.logAudit(com.workspace.management.service.AuthenticationService.getCurrentUser().getId(),
                                "USER_CREATED", "USER", id, "Created user " + email);
                    }
                    return id;
                }
                @Override protected void done() {
                    try {
                        int id = get();
                        if (id > 0) { UIUtils.showSuccess(UserManagementPanel.this, "User created successfully!"); loadUsers(); }
                        else UIUtils.showError(UserManagementPanel.this, "Failed to create user. Email may already exist.");
                    } catch (Exception e) { UIUtils.showError(UserManagementPanel.this, "Error creating user."); }
                }
            }.execute();
        }
    }

    private void editSelectedUser() {
        int row = userTable.getSelectedRow();
        if (row < 0) { UIUtils.showWarning(this, "Please select a user to edit."); return; }
        int userId = (int) tableModel.getValueAt(row, 0);

        new SwingWorker<User, Void>() {
            @Override protected User doInBackground() { return userDAO.findById(userId).orElse(null); }
            @Override protected void done() {
                try {
                    User user = get();
                    if (user == null) { UIUtils.showError(UserManagementPanel.this, "User not found."); return; }

                    JPanel form = new JPanel(new GridBagLayout());
                    GridBagConstraints gbc = new GridBagConstraints();
                    gbc.insets = new Insets(6, 6, 6, 6);
                    gbc.fill = GridBagConstraints.HORIZONTAL;

                    JTextField firstName = UIUtils.createTextField(""); firstName.setText(user.getFirstName());
                    JTextField lastName = UIUtils.createTextField(""); lastName.setText(user.getLastName());
                    JTextField email = UIUtils.createTextField(""); email.setText(user.getEmail());
                    JTextField dept = UIUtils.createTextField(""); dept.setText(user.getDepartment());
                    JTextField phone = UIUtils.createTextField(""); phone.setText(user.getPhone());
                    JComboBox<String> roleCombo = UIUtils.createComboBox();
                    roleCombo.addItem("Employee"); roleCombo.addItem("Facility Manager"); roleCombo.addItem("System Administrator");
                    roleCombo.setSelectedIndex(user.getRoleId() == 1 ? 2 : user.getRoleId() == 2 ? 1 : 0);

                    UIUtils.addFormRow(form, "First Name:", firstName, gbc, 0);
                    UIUtils.addFormRow(form, "Last Name:", lastName, gbc, 1);
                    UIUtils.addFormRow(form, "Email:", email, gbc, 2);
                    UIUtils.addFormRow(form, "Role:", roleCombo, gbc, 3);
                    UIUtils.addFormRow(form, "Department:", dept, gbc, 4);
                    UIUtils.addFormRow(form, "Phone:", phone, gbc, 5);

                    int result = JOptionPane.showConfirmDialog(UserManagementPanel.this, form, "Edit User", JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
                    if (result == JOptionPane.OK_OPTION) {
                        user.setFirstName(firstName.getText().trim());
                        user.setLastName(lastName.getText().trim());
                        user.setEmail(email.getText().trim());
                        user.setRoleId(roleCombo.getSelectedIndex() == 2 ? 1 : roleCombo.getSelectedIndex() == 1 ? 2 : 3);
                        user.setDepartment(dept.getText().trim());
                        user.setPhone(phone.getText().trim());

                        new SwingWorker<Boolean, Void>() {
                            @Override protected Boolean doInBackground() { return userDAO.update(user); }
                            @Override protected void done() {
                                try { if(get()){UIUtils.showSuccess(UserManagementPanel.this,"User updated.");loadUsers();}else{UIUtils.showError(UserManagementPanel.this,"Update failed.");} }
                                catch(Exception ex){UIUtils.showError(UserManagementPanel.this,"Error.");}
                            }
                        }.execute();
                    }
                } catch (Exception ex) { UIUtils.showError(UserManagementPanel.this, "Error loading user."); }
            }
        }.execute();
    }

    private void toggleUserStatus(String status) {
        int row = userTable.getSelectedRow();
        if (row < 0) { UIUtils.showWarning(this, "Please select a user."); return; }
        int userId = (int) tableModel.getValueAt(row, 0);
        String action = "ACTIVE".equals(status) ? "enable" : "disable";

        if (UIUtils.showConfirmDialog(this, "Confirm", "Are you sure you want to " + action + " this user?")) {
            new SwingWorker<Boolean, Void>() {
                @Override protected Boolean doInBackground() { return userDAO.updateStatus(userId, status); }
                @Override protected void done() {
                    try { if(get()){UIUtils.showSuccess(UserManagementPanel.this,"User "+action+"d.");loadUsers();}else{UIUtils.showError(UserManagementPanel.this,"Failed.");} }
                    catch(Exception e){UIUtils.showError(UserManagementPanel.this,"Error.");}
                }
            }.execute();
        }
    }

    private void resetPassword() {
        int row = userTable.getSelectedRow();
        if (row < 0) { UIUtils.showWarning(this, "Please select a user."); return; }
        int userId = (int) tableModel.getValueAt(row, 0);

        String newPassword = JOptionPane.showInputDialog(this, "Enter new password:", "Reset Password", JOptionPane.PLAIN_MESSAGE);
        if (newPassword != null && !newPassword.isEmpty()) {
            if (!ValidationUtil.isValidPassword(newPassword)) {
                UIUtils.showError(this, "Password must be at least 6 characters."); return;
            }
            new SwingWorker<Boolean, Void>() {
                @Override protected Boolean doInBackground() { return userDAO.updatePassword(userId, PasswordUtil.hashPassword(newPassword)); }
                @Override protected void done() {
                    try { if(get()) UIUtils.showSuccess(UserManagementPanel.this,"Password reset."); else UIUtils.showError(UserManagementPanel.this,"Failed."); }
                    catch(Exception e){UIUtils.showError(UserManagementPanel.this,"Error.");}
                }
            }.execute();
        }
    }
}
