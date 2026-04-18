package com.university;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.SQLException;
import java.util.Set;

public class StudentPanel extends JPanel {

    private final String rollNumber;
    private final int    studentId;
    private final String studentName;
    private final JFrame owner;

    // All Courses tab
    private JTable       allTable;
    private DefaultTableModel allModel;

    // My Courses tab
    private JTable       myTable;
    private DefaultTableModel myModel;

    // Status bar
    private JLabel statusBar;

    public StudentPanel(JFrame owner, String rollNumber, String studentName, int studentId) {
        this.owner       = owner;
        this.rollNumber  = rollNumber;
        this.studentName = studentName;
        this.studentId   = studentId;

        setLayout(new BorderLayout());
        setBackground(Theme.BG_DARK);

        add(buildHeader(),  BorderLayout.NORTH);
        add(buildTabs(),    BorderLayout.CENTER);
        add(buildStatus(),  BorderLayout.SOUTH);

        refreshAll();
    }

    // ── Header ─────────────────────────────────────────────────────────────
    private JPanel buildHeader() {
        JPanel p = new JPanel(new BorderLayout());
        p.setBackground(Theme.BG_CARD);
        p.setBorder(BorderFactory.createEmptyBorder(14, 20, 14, 20));

        JLabel title = Theme.label("🎓  Student Portal — " + studentName + "  (" + rollNumber + ")",
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

    // ── Tabs ───────────────────────────────────────────────────────────────
    private JTabbedPane buildTabs() {
        JTabbedPane tabs = new JTabbedPane();
        tabs.setBackground(Theme.BG_DARK);
        tabs.setForeground(Color.BLACK);
        tabs.setFont(Theme.FONT_HEADER);

        JLabel tab1 = new JLabel("📚  All Courses");
tab1.setFont(new Font("Segoe UI Emoji", Font.BOLD, 14));

JLabel tab2 = new JLabel("✅  My Courses");
tab2.setFont(new Font("Segoe UI Emoji", Font.BOLD, 14));

tabs.addTab(null, buildAllCoursesTab());
tabs.setTabComponentAt(0, tab1);

tabs.addTab(null, buildMyCoursesTab());
tabs.setTabComponentAt(1, tab2);
        return tabs;
    }

    // ── All Courses Tab ────────────────────────────────────────────────────
    private JPanel buildAllCoursesTab() {
        JPanel p = new JPanel(new BorderLayout(0, 8));
        p.setBackground(Theme.BG_DARK);
        p.setBorder(BorderFactory.createEmptyBorder(12, 12, 12, 12));

        String[] cols = {"ID", "Course Name", "Faculty", "Enrolled", "Capacity", "Prerequisites"};
        allModel = new DefaultTableModel(cols, 0) {
            public boolean isCellEditable(int r, int c) { return false; }
        };
        allTable = new JTable(allModel);
        Theme.styleTable(allTable);
        allTable.getColumnModel().getColumn(0).setMaxWidth(40);
        allTable.getColumnModel().getColumn(3).setMaxWidth(70);
        allTable.getColumnModel().getColumn(4).setMaxWidth(70);
        p.add(Theme.darkScroll(allTable), BorderLayout.CENTER);

        // Button row
        JPanel btn = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        btn.setBackground(Theme.BG_DARK);

        JButton enroll = Theme.pillButton("➕  Enroll", Theme.SUCCESS);
        enroll.addActionListener(e -> enrollSelected());

        JButton refresh = Theme.pillButton("🔄  Refresh", Theme.ACCENT);
        refresh.addActionListener(e -> refreshAll());

        btn.add(enroll);
        btn.add(refresh);
        p.add(btn, BorderLayout.SOUTH);
        return p;
    }

    // ── My Courses Tab ─────────────────────────────────────────────────────
    private JPanel buildMyCoursesTab() {
        JPanel p = new JPanel(new BorderLayout(0, 8));
        p.setBackground(Theme.BG_DARK);
        p.setBorder(BorderFactory.createEmptyBorder(12, 12, 12, 12));

        String[] cols = {"ID", "Course Name", "Faculty", "Prerequisites"};
        myModel = new DefaultTableModel(cols, 0) {
            public boolean isCellEditable(int r, int c) { return false; }
        };
        myTable = new JTable(myModel);
        Theme.styleTable(myTable);
        myTable.getColumnModel().getColumn(0).setMaxWidth(40);
        p.add(Theme.darkScroll(myTable), BorderLayout.CENTER);

        JPanel btn = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        btn.setBackground(Theme.BG_DARK);

        JButton drop = Theme.pillButton("❌  Drop Course", Theme.DANGER);
        drop.addActionListener(e -> dropSelected());

        JButton refresh = Theme.pillButton("🔄  Refresh", Theme.ACCENT);
        refresh.addActionListener(e -> refreshAll());

        btn.add(drop);
        btn.add(refresh);
        p.add(btn, BorderLayout.SOUTH);
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

    private void enrollSelected() {
        int row = allTable.getSelectedRow();
        if (row < 0) { setStatus("⚠  Select a course first.", Theme.WARNING); return; }

        int courseId   = (int) allModel.getValueAt(row, 0);
        String cName   = (String) allModel.getValueAt(row, 1);

        int confirm = JOptionPane.showConfirmDialog(owner,
            "Enroll in: " + cName + "?", "Confirm Enroll",
            JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);
        if (confirm != JOptionPane.YES_OPTION) return;

        try {
            String result = DatabaseManager.enrollStudent(studentId, courseId);
            switch (result) {
                case "OK"              -> { setStatus("✅  Enrolled in " + cName + "!", Theme.SUCCESS); refreshAll(); }
                case "ALREADY_ENROLLED"-> setStatus("ℹ  Already enrolled in " + cName + ".", Theme.ACCENT);
                case "FULL"            -> setStatus("❌  " + cName + " is full.", Theme.DANGER);
            }
        } catch (SQLException ex) {
            setStatus("DB Error: " + ex.getMessage(), Theme.DANGER);
        }
    }

    private void dropSelected() {
        int row = myTable.getSelectedRow();
        if (row < 0) { setStatus("⚠  Select a course to drop.", Theme.WARNING); return; }

        int courseId = (int) myModel.getValueAt(row, 0);
        String cName = (String) myModel.getValueAt(row, 1);

        int confirm = JOptionPane.showConfirmDialog(owner,
            "Drop: " + cName + "?\nThis cannot be undone.", "Confirm Drop",
            JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
        if (confirm != JOptionPane.YES_OPTION) return;

        try {
            if (DatabaseManager.dropCourse(studentId, courseId)) {
                setStatus("🗑  Dropped " + cName + ".", Theme.WARNING);
                refreshAll();
            }
        } catch (SQLException ex) {
            setStatus("DB Error: " + ex.getMessage(), Theme.DANGER);
        }
    }

    // ── Refresh ────────────────────────────────────────────────────────────
    private void refreshAll() {
        try {
            // All courses
            allModel.setRowCount(0);
            Set<Integer> enrolled = DatabaseManager.getEnrolledCourseIds(studentId);
            for (Object[] row : DatabaseManager.getAllCoursesTable()) {
                allModel.addRow(row);
            }

            // Highlight enrolled rows
            allTable.setDefaultRenderer(Object.class, new javax.swing.table.DefaultTableCellRenderer() {
                @Override
                public Component getTableCellRendererComponent(JTable t, Object v,
                        boolean sel, boolean foc, int r, int c) {
                    super.getTableCellRendererComponent(t, v, sel, foc, r, c);
                    int id = (int) allModel.getValueAt(r, 0);
                    if (!sel) {
                        setBackground(enrolled.contains(id) ? new Color(30, 80, 50) : (r%2==0 ? Theme.BG_CARD : Theme.BG_ROW_ALT));
                        setForeground(Theme.TEXT_PRIMARY);
                    }
                    setBorder(BorderFactory.createEmptyBorder(0,8,0,8));
                    return this;
                }
            });

            // My courses
            myModel.setRowCount(0);
            for (Object[] row : DatabaseManager.getStudentEnrollments(studentId)) {
                myModel.addRow(row);
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
