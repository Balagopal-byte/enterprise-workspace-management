package com.workspace.management.ui;

import com.workspace.management.service.AnalyticsService;
import com.workspace.management.util.UIUtils;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.time.LocalDate;
import java.util.Map;

/**
 * Analytics dashboard with date filtering and charts.
 */
public class AnalyticsPanel extends JPanel {
    private final AnalyticsService analyticsService;
    private JPanel statsPanel;
    private JPanel chartsPanel;
    private JComboBox<String> periodCombo;
    private LocalDate startDate;
    private LocalDate endDate;

    public AnalyticsPanel() {
        this.analyticsService = new AnalyticsService();
        setLayout(new BorderLayout(0, 16));
        setBackground(UIUtils.BG_MAIN);
        setBorder(new EmptyBorder(24, 24, 24, 24));
        this.startDate = LocalDate.now().minusDays(7);
        this.endDate = LocalDate.now();
        buildUI();
        loadData();
    }

    private void buildUI() {
        // Filter bar
        JPanel filterPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 12, 4));
        filterPanel.setOpaque(false);
        JLabel filterLabel = new JLabel("Period:");
        filterLabel.setFont(UIUtils.FONT_HEADING);
        filterPanel.add(filterLabel);

        periodCombo = UIUtils.createComboBox();
        periodCombo.addItem("Today");
        periodCombo.addItem("This Week");
        periodCombo.addItem("This Month");
        periodCombo.addItem("Last 30 Days");
        periodCombo.setSelectedIndex(1);
        periodCombo.addActionListener(e -> {
            updateDateRange();
            loadData();
        });
        filterPanel.add(periodCombo);

        JButton refreshBtn = UIUtils.createPrimaryButton("Refresh");
        refreshBtn.addActionListener(e -> loadData());
        filterPanel.add(refreshBtn);

        add(filterPanel, BorderLayout.NORTH);

        // Main content
        JPanel content = new JPanel();
        content.setLayout(new BoxLayout(content, BoxLayout.Y_AXIS));
        content.setOpaque(false);

        statsPanel = new JPanel(new GridLayout(1, 5, 12, 0));
        statsPanel.setOpaque(false);
        statsPanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 130));
        statsPanel.setAlignmentX(LEFT_ALIGNMENT);
        content.add(statsPanel);
        content.add(Box.createVerticalStrut(16));

        chartsPanel = new JPanel(new GridLayout(2, 2, 16, 16));
        chartsPanel.setOpaque(false);
        chartsPanel.setAlignmentX(LEFT_ALIGNMENT);
        content.add(chartsPanel);

        JScrollPane scrollPane = new JScrollPane(content);
        scrollPane.setBorder(null);
        scrollPane.getViewport().setBackground(UIUtils.BG_MAIN);
        add(scrollPane, BorderLayout.CENTER);
    }

    private void updateDateRange() {
        int idx = periodCombo.getSelectedIndex();
        endDate = LocalDate.now();
        startDate = switch (idx) {
            case 0 -> LocalDate.now();
            case 1 -> LocalDate.now().minusDays(7);
            case 2 -> LocalDate.now().withDayOfMonth(1);
            case 3 -> LocalDate.now().minusDays(30);
            default -> LocalDate.now().minusDays(7);
        };
    }

    private void loadData() {
        new SwingWorker<Void, Void>() {
            int bookings, cancellations, autoReleases, checkIns;
            double checkInRate, avgOccupancy, peakOccupancy;
            Map<String, Integer> zoneUtil, roomUtil, statusDist;
            Map<Integer, Integer> hourlyOccupancy;

            @Override
            protected Void doInBackground() {
                bookings = analyticsService.getBookingCount(startDate, endDate);
                cancellations = analyticsService.getCancellationCount(startDate, endDate);
                autoReleases = analyticsService.getAutoReleaseCount(startDate, endDate);
                checkIns = analyticsService.getCheckInCount(startDate, endDate);
                checkInRate = analyticsService.getCheckInRate(startDate, endDate);
                avgOccupancy = analyticsService.getAverageOccupancy(startDate, endDate);
                peakOccupancy = analyticsService.getPeakOccupancy(startDate, endDate);
                zoneUtil = analyticsService.getDeskUtilizationByZone(startDate, endDate);
                roomUtil = analyticsService.getRoomUtilization(startDate, endDate);
                statusDist = analyticsService.getBookingsByStatus(startDate, endDate);
                hourlyOccupancy = analyticsService.getOccupancyByHour(LocalDate.now());
                return null;
            }

            @Override
            protected void done() {
                // Stats
                statsPanel.removeAll();
                statsPanel.add(UIUtils.createStatCard("Total Bookings", String.valueOf(bookings), UIUtils.PRIMARY));
                statsPanel.add(UIUtils.createStatCard("Check-Ins", String.valueOf(checkIns), UIUtils.SUCCESS));
                statsPanel.add(UIUtils.createStatCard("Cancellations", String.valueOf(cancellations), UIUtils.WARNING));
                statsPanel.add(UIUtils.createStatCard("Auto-Released", String.valueOf(autoReleases), UIUtils.DANGER));
                statsPanel.add(UIUtils.createStatCard("Check-In Rate", String.format("%.0f%%", checkInRate), UIUtils.PRIMARY));
                statsPanel.revalidate();

                // Charts
                chartsPanel.removeAll();

                // 1. Zone utilization
                JPanel zoneCard = UIUtils.createCard();
                zoneCard.setLayout(new BorderLayout());
                zoneCard.add(makeChartLabel("Desk Utilization by Zone"), BorderLayout.NORTH);
                zoneCard.add(new HBarChart(zoneUtil), BorderLayout.CENTER);
                chartsPanel.add(zoneCard);

                // 2. Room utilization
                JPanel roomCard = UIUtils.createCard();
                roomCard.setLayout(new BorderLayout());
                roomCard.add(makeChartLabel("Room Utilization"), BorderLayout.NORTH);
                roomCard.add(new HBarChart(roomUtil), BorderLayout.CENTER);
                chartsPanel.add(roomCard);

                // 3. Booking status distribution
                JPanel statusCard = UIUtils.createCard();
                statusCard.setLayout(new BorderLayout());
                statusCard.add(makeChartLabel("Bookings by Status"), BorderLayout.NORTH);
                statusCard.add(new PieChart(statusDist), BorderLayout.CENTER);
                chartsPanel.add(statusCard);

                // 4. Hourly occupancy
                JPanel hourCard = UIUtils.createCard();
                hourCard.setLayout(new BorderLayout());
                hourCard.add(makeChartLabel("Today's Hourly Occupancy"), BorderLayout.NORTH);
                hourCard.add(new VBarChart(hourlyOccupancy), BorderLayout.CENTER);
                chartsPanel.add(hourCard);

                chartsPanel.revalidate();
                chartsPanel.repaint();
            }
        }.execute();
    }

    private JLabel makeChartLabel(String text) {
        JLabel l = new JLabel("  " + text);
        l.setFont(UIUtils.FONT_HEADING);
        l.setForeground(UIUtils.TEXT_PRIMARY);
        l.setBorder(new EmptyBorder(0, 0, 6, 0));
        return l;
    }

    // --- Chart Components ---

    private static class HBarChart extends JPanel {
        private final Map<String, Integer> data;
        HBarChart(Map<String, Integer> data) { this.data = data; setOpaque(false); }
        @Override protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            if (data == null || data.isEmpty()) return;
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            int max = data.values().stream().mapToInt(Integer::intValue).max().orElse(1);
            if (max == 0) max = 1;
            int bh = Math.min(28, (getHeight() - 10) / Math.max(data.size(), 1));
            int mw = getWidth() - 180;
            Color[] colors = {UIUtils.PRIMARY, UIUtils.SUCCESS, UIUtils.WARNING, UIUtils.DANGER, new Color(139, 92, 246)};
            int y = 8, ci = 0;
            for (var e : data.entrySet()) {
                int bw = Math.max(4, (int)((e.getValue() / (double) max) * mw));
                g2.setFont(UIUtils.FONT_SMALL); g2.setColor(UIUtils.TEXT_PRIMARY);
                g2.drawString(e.getKey(), 5, y + bh / 2 + 4);
                g2.setColor(colors[ci++ % colors.length]);
                g2.fillRoundRect(130, y, bw, bh - 4, 6, 6);
                g2.setColor(UIUtils.TEXT_SECONDARY);
                g2.drawString(String.valueOf(e.getValue()), 135 + bw, y + bh / 2 + 4);
                y += bh + 2;
            }
            g2.dispose();
        }
    }

    private static class VBarChart extends JPanel {
        private final Map<Integer, Integer> data;
        VBarChart(Map<Integer, Integer> data) { this.data = data; setOpaque(false); }
        @Override protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            if (data == null || data.isEmpty()) return;
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            int w = getWidth() - 60, h = getHeight() - 40;
            int max = data.values().stream().mapToInt(Integer::intValue).max().orElse(1);
            if (max == 0) max = 1;
            int barWidth = Math.max(6, (w / data.size()) - 4);
            int i = 0;
            for (var e : data.entrySet()) {
                int bh = (int) ((e.getValue() / (double) max) * h);
                int bx = 40 + i * (barWidth + 4);
                g2.setColor(UIUtils.PRIMARY);
                g2.fillRoundRect(bx, 10 + h - bh, barWidth, bh, 4, 4);
                g2.setColor(UIUtils.TEXT_SECONDARY); g2.setFont(UIUtils.FONT_SMALL);
                if (i % 2 == 0) g2.drawString(e.getKey() + "h", bx, h + 25);
                i++;
            }
            g2.dispose();
        }
    }

    private static class PieChart extends JPanel {
        private final Map<String, Integer> data;
        PieChart(Map<String, Integer> data) { this.data = data; setOpaque(false); }
        @Override protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            if (data == null || data.isEmpty()) return;
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            int total = data.values().stream().mapToInt(Integer::intValue).sum();
            if (total == 0) return;
            int size = Math.min(getWidth(), getHeight()) - 40;
            int cx = 20, cy = (getHeight() - size) / 2;
            Color[] colors = {UIUtils.PRIMARY, UIUtils.SUCCESS, UIUtils.WARNING, UIUtils.DANGER, new Color(139,92,246), new Color(236,72,153)};
            int startAngle = 0;
            int ci = 0;
            int legendY = 15;
            for (var e : data.entrySet()) {
                int arc = (int) Math.round((e.getValue() / (double) total) * 360);
                g2.setColor(colors[ci % colors.length]);
                g2.fillArc(cx, cy, size, size, startAngle, arc);
                // Legend
                g2.fillRect(cx + size + 20, legendY, 12, 12);
                g2.setColor(UIUtils.TEXT_PRIMARY); g2.setFont(UIUtils.FONT_SMALL);
                g2.drawString(e.getKey() + " (" + e.getValue() + ")", cx + size + 38, legendY + 10);
                startAngle += arc;
                legendY += 20;
                ci++;
            }
            g2.dispose();
        }
    }
}
