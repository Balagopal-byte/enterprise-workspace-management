package com.workspace.management.ui;

import com.workspace.management.service.AnalyticsService;
import com.workspace.management.service.BookingService;
import com.workspace.management.model.Booking;
import com.workspace.management.util.DateTimeUtil;
import com.workspace.management.util.UIUtils;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.util.List;
import java.util.Map;

/**
 * Professional dashboard with stat cards and charts.
 * All data comes from the database via AnalyticsService.
 */
public class DashboardPanel extends JPanel {
    private final AnalyticsService analyticsService;
    private final BookingService bookingService;
    private JPanel statsPanel;
    private JPanel chartsPanel;
    private JPanel activityPanel;
    private JLabel loadingLabel;

    public DashboardPanel() {
        this.analyticsService = new AnalyticsService();
        this.bookingService = new BookingService();
        setLayout(new BorderLayout());
        setBackground(UIUtils.BG_MAIN);
        setBorder(new EmptyBorder(24, 24, 24, 24));
        buildUI();
        refreshData();
    }

    private void buildUI() {
        JPanel content = new JPanel();
        content.setLayout(new BoxLayout(content, BoxLayout.Y_AXIS));
        content.setOpaque(false);

        // Stats cards row
        statsPanel = new JPanel(new GridLayout(1, 6, 16, 0));
        statsPanel.setOpaque(false);
        statsPanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 130));
        statsPanel.setAlignmentX(LEFT_ALIGNMENT);
        content.add(statsPanel);
        content.add(Box.createVerticalStrut(20));

        // Charts row
        chartsPanel = new JPanel(new GridLayout(1, 2, 16, 0));
        chartsPanel.setOpaque(false);
        chartsPanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 300));
        chartsPanel.setAlignmentX(LEFT_ALIGNMENT);
        content.add(chartsPanel);
        content.add(Box.createVerticalStrut(20));

        // Recent activity
        activityPanel = new JPanel(new BorderLayout());
        activityPanel.setOpaque(false);
        activityPanel.setAlignmentX(LEFT_ALIGNMENT);
        content.add(activityPanel);

        // Loading
        loadingLabel = new JLabel("Loading dashboard...");
        loadingLabel.setFont(UIUtils.FONT_BODY);
        loadingLabel.setForeground(UIUtils.TEXT_SECONDARY);
        loadingLabel.setAlignmentX(LEFT_ALIGNMENT);

        JScrollPane scrollPane = new JScrollPane(content);
        scrollPane.setBorder(null);
        scrollPane.getViewport().setBackground(UIUtils.BG_MAIN);
        scrollPane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        add(scrollPane, BorderLayout.CENTER);
    }

    /**
     * Refresh dashboard data using SwingWorker.
     */
    public void refreshData() {
        new SwingWorker<int[], Void>() {
            @Override
            protected int[] doInBackground() {
                return new int[]{
                    analyticsService.getTotalDesks(),
                    analyticsService.getAvailableDesks(),
                    analyticsService.getOccupiedDesks(),
                    analyticsService.getTodayBookingCount(),
                    analyticsService.getAvailableRooms(),
                    (int) Math.round(analyticsService.getCurrentOccupancy())
                };
            }

            @Override
            protected void done() {
                try {
                    int[] data = get();
                    updateStats(data);
                    updateCharts();
                    updateActivity();
                } catch (Exception e) {
                    showError();
                }
            }
        }.execute();
    }

    private void updateStats(int[] data) {
        statsPanel.removeAll();
        statsPanel.add(UIUtils.createStatCard("Total Desks", String.valueOf(data[0]), UIUtils.PRIMARY));
        statsPanel.add(UIUtils.createStatCard("Available", String.valueOf(data[1]), UIUtils.SUCCESS));
        statsPanel.add(UIUtils.createStatCard("Occupied", String.valueOf(data[2]), UIUtils.DANGER));
        statsPanel.add(UIUtils.createStatCard("Today's Bookings", String.valueOf(data[3]), UIUtils.WARNING));
        statsPanel.add(UIUtils.createStatCard("Rooms Available", String.valueOf(data[4]), UIUtils.PRIMARY));
        statsPanel.add(UIUtils.createStatCard("Occupancy %", data[5] + "%", data[5] > 70 ? UIUtils.DANGER : UIUtils.SUCCESS));
        statsPanel.revalidate();
        statsPanel.repaint();
    }

    private void updateCharts() {
        chartsPanel.removeAll();

        // Occupancy by hour chart
        JPanel hourChart = UIUtils.createCard();
        hourChart.setLayout(new BorderLayout());
        JLabel hourTitle = new JLabel("  Occupancy by Hour (Today)");
        hourTitle.setFont(UIUtils.FONT_HEADING);
        hourTitle.setForeground(UIUtils.TEXT_PRIMARY);
        hourTitle.setBorder(new EmptyBorder(0, 0, 8, 0));
        hourChart.add(hourTitle, BorderLayout.NORTH);

        new SwingWorker<Map<Integer, Integer>, Void>() {
            @Override
            protected Map<Integer, Integer> doInBackground() {
                return analyticsService.getOccupancyByHour(java.time.LocalDate.now());
            }

            @Override
            protected void done() {
                try {
                    Map<Integer, Integer> data = get();
                    hourChart.add(new SimpleBarChart(data), BorderLayout.CENTER);
                    hourChart.revalidate();
                } catch (Exception ignored) {}
            }
        }.execute();
        chartsPanel.add(hourChart);

        // Desk utilization by zone
        JPanel zoneChart = UIUtils.createCard();
        zoneChart.setLayout(new BorderLayout());
        JLabel zoneTitle = new JLabel("  Desk Utilization by Zone");
        zoneTitle.setFont(UIUtils.FONT_HEADING);
        zoneTitle.setForeground(UIUtils.TEXT_PRIMARY);
        zoneTitle.setBorder(new EmptyBorder(0, 0, 8, 0));
        zoneChart.add(zoneTitle, BorderLayout.NORTH);

        new SwingWorker<Map<String, Integer>, Void>() {
            @Override
            protected Map<String, Integer> doInBackground() {
                return analyticsService.getDeskUtilizationByZone(
                        java.time.LocalDate.now().minusDays(7), java.time.LocalDate.now());
            }

            @Override
            protected void done() {
                try {
                    Map<String, Integer> data = get();
                    zoneChart.add(new SimpleHorizontalBarChart(data), BorderLayout.CENTER);
                    zoneChart.revalidate();
                } catch (Exception ignored) {}
            }
        }.execute();
        chartsPanel.add(zoneChart);

        chartsPanel.revalidate();
        chartsPanel.repaint();
    }

    private void updateActivity() {
        activityPanel.removeAll();
        JPanel card = UIUtils.createCard();
        card.setLayout(new BorderLayout());

        JLabel title = new JLabel("  Today's Bookings");
        title.setFont(UIUtils.FONT_HEADING);
        title.setForeground(UIUtils.TEXT_PRIMARY);
        title.setBorder(new EmptyBorder(0, 0, 8, 0));
        card.add(title, BorderLayout.NORTH);

        new SwingWorker<List<Booking>, Void>() {
            @Override
            protected List<Booking> doInBackground() {
                return bookingService.getTodaysBookings();
            }

            @Override
            protected void done() {
                try {
                    List<Booking> bookings = get();
                    if (bookings.isEmpty()) {
                        card.add(UIUtils.createEmptyStatePanel("No bookings today", "The workspace is quiet today"), BorderLayout.CENTER);
                    } else {
                        String[] cols = {"Desk", "User", "Time", "Status"};
                        Object[][] data = new Object[Math.min(bookings.size(), 10)][4];
                        for (int i = 0; i < data.length; i++) {
                            Booking b = bookings.get(i);
                            data[i] = new Object[]{
                                b.getDeskCode(),
                                b.getUserName(),
                                DateTimeUtil.formatTimeRange(b.getStartTime(), b.getEndTime()),
                                b.getStatus().getDisplayName()
                            };
                        }
                        JTable table = UIUtils.createStyledTable();
                        table.setModel(new javax.swing.table.DefaultTableModel(data, cols) {
                            @Override public boolean isCellEditable(int r, int c) { return false; }
                        });
                        card.add(UIUtils.createScrollPane(table), BorderLayout.CENTER);
                    }
                    card.revalidate();
                    card.repaint();
                } catch (Exception ignored) {}
            }
        }.execute();

        activityPanel.add(card);
        activityPanel.revalidate();
    }

    private void showError() {
        statsPanel.removeAll();
        JLabel err = new JLabel("Unable to load dashboard data. Check database connection.");
        err.setForeground(UIUtils.DANGER);
        err.setFont(UIUtils.FONT_BODY);
        statsPanel.add(err);
        statsPanel.revalidate();
    }

    // === SIMPLE CHART COMPONENTS ===

    /**
     * Simple bar chart drawn with Swing Graphics2D.
     */
    private static class SimpleBarChart extends JPanel {
        private final Map<Integer, Integer> data;

        SimpleBarChart(Map<Integer, Integer> data) {
            this.data = data;
            setOpaque(false);
            setPreferredSize(new Dimension(400, 200));
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            if (data == null || data.isEmpty()) return;
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            int w = getWidth() - 60;
            int h = getHeight() - 50;
            int x0 = 40, y0 = 10;
            int barCount = data.size();
            int maxVal = data.values().stream().mapToInt(Integer::intValue).max().orElse(1);
            if (maxVal == 0) maxVal = 1;
            int barWidth = Math.max(8, (w / barCount) - 6);

            int i = 0;
            for (Map.Entry<Integer, Integer> entry : data.entrySet()) {
                int barHeight = (int) ((entry.getValue() / (double) maxVal) * h);
                int bx = x0 + i * (barWidth + 6);
                int by = y0 + h - barHeight;

                g2.setColor(UIUtils.PRIMARY);
                g2.fillRoundRect(bx, by, barWidth, barHeight, 4, 4);

                g2.setColor(UIUtils.TEXT_SECONDARY);
                g2.setFont(UIUtils.FONT_SMALL);
                String label = entry.getKey() + ":00";
                if (i % 2 == 0) {
                    g2.drawString(label, bx - 2, y0 + h + 15);
                }
                i++;
            }
            g2.dispose();
        }
    }

    /**
     * Simple horizontal bar chart.
     */
    private static class SimpleHorizontalBarChart extends JPanel {
        private final Map<String, Integer> data;

        SimpleHorizontalBarChart(Map<String, Integer> data) {
            this.data = data;
            setOpaque(false);
            setPreferredSize(new Dimension(400, 200));
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            if (data == null || data.isEmpty()) return;
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            int maxVal = data.values().stream().mapToInt(Integer::intValue).max().orElse(1);
            if (maxVal == 0) maxVal = 1;
            int barHeight = Math.min(30, (getHeight() - 20) / Math.max(data.size(), 1));
            int maxBarWidth = getWidth() - 180;
            Color[] colors = {UIUtils.PRIMARY, UIUtils.SUCCESS, UIUtils.WARNING, UIUtils.DANGER, new Color(139, 92, 246)};

            int y = 15;
            int ci = 0;
            for (Map.Entry<String, Integer> entry : data.entrySet()) {
                int bw = (int) ((entry.getValue() / (double) maxVal) * maxBarWidth);
                bw = Math.max(bw, 4);

                g2.setColor(UIUtils.TEXT_PRIMARY);
                g2.setFont(UIUtils.FONT_SMALL);
                g2.drawString(entry.getKey(), 5, y + barHeight / 2 + 4);

                g2.setColor(colors[ci % colors.length]);
                g2.fillRoundRect(140, y, bw, barHeight - 4, 6, 6);

                g2.setColor(UIUtils.TEXT_SECONDARY);
                g2.drawString(String.valueOf(entry.getValue()), 145 + bw, y + barHeight / 2 + 4);

                y += barHeight + 4;
                ci++;
            }
            g2.dispose();
        }
    }
}
