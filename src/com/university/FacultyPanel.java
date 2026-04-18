package com.university;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.SQLException;

public class FacultyPanel extends JPanel {

    private final String facultyName;
    private final JFrame owner;

    private JTable         courseTable;
    private DefaultTableModel courseModel;
    private JTable         rosterTable;
    private DefaultTableModel rosterModel;
    private JLabel         rosterTitle;
    private JLabel         statusBar;

    public FacultyPanel(JFrame owner, String facultyName) {
        this.owner       = owner;
        this.facultyName = facultyName;

        setLayout(new BorderLayout());
        setBackground(Theme.BG_DARK);

        add(buildHeader(),  BorderLayout.NORTH);
        add(buildBody(),    BorderLayout.CENTER);
        add(buildStatus(),  BorderLayout.SOUTH);

        refreshCourses();
    }

    // ── Header ─────────────────────────────────────────────────────────────
    private JPanel buildHeader() {
        JPanel p = new JPanel(new BorderLayout());
        p.setBackground(Theme.BG_CARD);
        p.setBorder(BorderFactory.createEmptyBorder(14, 20, 14, 20));

        JLabel title = Theme.label("🏫  Faculty Portal — " + facultyName,
                Theme.FONT_TITLE, Theme.TEXT_PRIMARY);
                title.setFont(new Font("Segoe UI Emoji", Font.BOLD, 22));
        p.add(title, BorderLayout.WEST);

        JButton back = Theme.pillButton("⬅  Logout", Theme.DANGER);
        back.addActionListener(e -> {
            owner.getContentPane().removeAll();
            owner.getContentPane().add(new HomePanel(owner));
            owner.revalidate(); owner.repaint();
        });
        p.add(back, BorderLayout.EAST);
        return p;
    }

    // ── Body: split pane ───────────────────────────────────────────────────
    private JSplitPane buildBody() {
        JSplitPane split = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT,
                buildCoursePanel(), buildRosterPanel());
        split.setDividerLocation(620);
        split.setBackground(Theme.BG_DARK);
        split.setBorder(null);
        return split;
    }

    // ── Left: Course Table ─────────────────────────────────────────────────
    private JPanel buildCoursePanel() {
        JPanel p = new JPanel(new BorderLayout(0, 8));
        p.setBackground(Theme.BG_DARK);
        p.setBorder(BorderFactory.createEmptyBorder(12, 12, 12, 6));

        JLabel heading = Theme.label("Your Courses", Theme.FONT_HEADER, Theme.TEXT_PRIMARY);
        p.add(heading, BorderLayout.NORTH);

        String[] cols = {"ID", "Course Name", "Enrolled", "Capacity", "Prerequisites"};
        courseModel = new DefaultTableModel(cols, 0) {
            public boolean isCellEditable(int r, int c) { return false; }
        };
        courseTable = new JTable(courseModel);
        Theme.styleTable(courseTable);
        courseTable.getColumnModel().getColumn(0).setMaxWidth(40);
        courseTable.getColumnModel().getColumn(2).setMaxWidth(70);
        courseTable.getColumnModel().getColumn(3).setMaxWidth(70);

        // Capacity color renderer
        courseTable.getColumnModel().getColumn(2).setCellRenderer(
            new javax.swing.table.DefaultTableCellRenderer() {
                @Override
                public Component getTableCellRendererComponent(JTable t, Object v,
                        boolean sel, boolean foc, int r, int c) {
                    super.getTableCellRendererComponent(t, v, sel, foc, r, c);
                    if (!sel && v instanceof Integer enrolled) {
                        int cap = (Integer) courseModel.getValueAt(r, 3);
                        double pct = cap == 0 ? 0 : (double) enrolled / cap;
                        setForeground(pct >= 1.0 ? Theme.DANGER : pct >= 0.8 ? Theme.WARNING : Theme.SUCCESS);
                    }
                    setBorder(BorderFactory.createEmptyBorder(0,8,0,8));
                    return this;
                }
            });

        courseTable.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) showRoster();
        });

        p.add(Theme.darkScroll(courseTable), BorderLayout.CENTER);

        // Buttons
        JPanel btn = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        btn.setBackground(Theme.BG_DARK);

        JButton editCap  = Theme.pillButton("✏  Edit Capacity",   Theme.ACCENT);
        JButton editPre  = Theme.pillButton("📋  Edit Prerequisites", new Color(99, 190, 120));
        JButton refresh  = Theme.pillButton("🔄  Refresh",         Theme.BG_CARD);

        editCap.addActionListener(e  -> editCapacity());
        editPre.addActionListener(e  -> editPrereqs());
        refresh.addActionListener(e  -> { refreshCourses(); setStatus("Refreshed.", Theme.TEXT_MUTED); });

        btn.add(editCap); btn.add(editPre); btn.add(refresh);
        p.add(btn, BorderLayout.SOUTH);
        return p;
    }

    // ── Right: Roster ──────────────────────────────────────────────────────
    private JPanel buildRosterPanel() {
        JPanel p = new JPanel(new BorderLayout(0, 8));
        p.setBackground(Theme.BG_DARK);
        p.setBorder(BorderFactory.createEmptyBorder(12, 6, 12, 12));

        rosterTitle = Theme.label("Select a course to view roster", Theme.FONT_HEADER, Theme.TEXT_MUTED);
        p.add(rosterTitle, BorderLayout.NORTH);

        String[] cols = {"Roll No.", "Name", "Email"};
        rosterModel = new DefaultTableModel(cols, 0) {
            public boolean isCellEditable(int r, int c) { return false; }
        };
        rosterTable = new JTable(rosterModel);
        Theme.styleTable(rosterTable);
        p.add(Theme.darkScroll(rosterTable), BorderLayout.CENTER);
        return p;
    }

    // ── Status Bar ─────────────────────────────────────────────────────────
    private JPanel buildStatus() {
        JPanel p = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 4));
        p.setBackground(new Color(22, 22, 40));
        statusBar = Theme.label("Ready.", Theme.FONT_SMALL, Theme.TEXT_MUTED);
        p.add(statusBar);
        return p;
    }

    // ── Actions ────────────────────────────────────────────────────────────

    private void editCapacity() {
        int row = courseTable.getSelectedRow();
        if (row < 0) { setStatus("⚠  Select a course first.", Theme.WARNING); return; }

        int courseId = (int) courseModel.getValueAt(row, 0);
        int curCap   = (int) courseModel.getValueAt(row, 3);

        String input = JOptionPane.showInputDialog(owner,
                "New capacity for \"" + courseModel.getValueAt(row, 1) + "\":",
                String.valueOf(curCap));
        if (input == null || input.isBlank()) return;

        try {
            int newCap = Integer.parseInt(input.trim());
            if (newCap < 1) { setStatus("❌  Capacity must be ≥ 1.", Theme.DANGER); return; }
            boolean ok = DatabaseManager.updateCourseCapacity(courseId, newCap);
            if (ok) {
                setStatus("✅  Capacity updated to " + newCap + ".", Theme.SUCCESS);
                refreshCourses();
            } else {
                setStatus("❌  Cannot set capacity below current enrollment.", Theme.DANGER);
            }
        } catch (NumberFormatException ex) {
            setStatus("❌  Please enter a valid number.", Theme.DANGER);
        } catch (SQLException ex) {
            setStatus("DB Error: " + ex.getMessage(), Theme.DANGER);
        }
    }

    private void editPrereqs() {
        int row = courseTable.getSelectedRow();
        if (row < 0) { setStatus("⚠  Select a course first.", Theme.WARNING); return; }

        int    courseId = (int)    courseModel.getValueAt(row, 0);
        String curPre   = (String) courseModel.getValueAt(row, 4);

        String input = JOptionPane.showInputDialog(owner,
                "Prerequisites for \"" + courseModel.getValueAt(row, 1) + "\"\n(enter 'None' if none):",
                curPre);
        if (input == null) return;

        try {
            DatabaseManager.updateCoursePrerequisites(courseId, input.trim());
            setStatus("✅  Prerequisites updated.", Theme.SUCCESS);
            refreshCourses();
        } catch (SQLException ex) {
            setStatus("DB Error: " + ex.getMessage(), Theme.DANGER);
        }
    }

    private void showRoster() {
        int row = courseTable.getSelectedRow();
        if (row < 0) return;

        int    courseId = (int)    courseModel.getValueAt(row, 0);
        String cName    = (String) courseModel.getValueAt(row, 1);

        try {
            rosterModel.setRowCount(0);
            for (Object[] r : DatabaseManager.getCourseRoster(courseId)) {
                rosterModel.addRow(r);
            }
            rosterTitle.setText("Roster: " + cName + "  (" + rosterModel.getRowCount() + " students)");
            rosterTitle.setForeground(Theme.TEXT_PRIMARY);
        } catch (SQLException ex) {
            setStatus("DB Error: " + ex.getMessage(), Theme.DANGER);
        }
    }

    // ── Refresh ────────────────────────────────────────────────────────────
    private void refreshCourses() {
        try {
            courseModel.setRowCount(0);
            for (Object[] row : DatabaseManager.getCoursesForFaculty(facultyName)) {
                courseModel.addRow(row);
            }
        } catch (SQLException ex) {
            setStatus("DB Error: " + ex.getMessage(), Theme.DANGER);
        }
    }

    private void setStatus(String msg, Color color) {
        statusBar.setText(msg);
        statusBar.setForeground(color);
    }
}
