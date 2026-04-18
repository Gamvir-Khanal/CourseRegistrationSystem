package com.university;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.sql.SQLException;
import java.util.List;

public class HomePanel extends JPanel {

    private final JFrame owner;

    // animation state
    private float[] orbX, orbY, orbDX, orbDY;
    private float[] orbR;
    private Color[] orbColors;
    private javax.swing.Timer animTimer;

    public HomePanel(JFrame owner) {
        this.owner = owner;
        setLayout(new GridBagLayout());
        setBackground(Theme.BG_DARK);
        initOrbs();
        startAnimation();
        buildUI();
    }

    // ── Orb animation ──────────────────────────────────────────────────────
    private void initOrbs() {
        int n = 6;
        orbX  = new float[n]; orbY  = new float[n];
        orbDX = new float[n]; orbDY = new float[n];
        orbR  = new float[n];
        orbColors = new Color[]{
            new Color(99,102,241,60), new Color(168,85,247,50),
            new Color(59,130,246,55), new Color(34,197,94,40),
            new Color(249,115,22,45), new Color(236,72,153,50)
        };
        java.util.Random rnd = new java.util.Random();
        for (int i = 0; i < n; i++) {
            orbX[i]  = rnd.nextFloat() * 900;
            orbY[i]  = rnd.nextFloat() * 600;
            orbDX[i] = (rnd.nextFloat() - 0.5f) * 1.2f;
            orbDY[i] = (rnd.nextFloat() - 0.5f) * 1.2f;
            orbR[i]  = 80 + rnd.nextFloat() * 120;
        }
    }

    private void startAnimation() {
        animTimer = new javax.swing.Timer(16, e -> {
            for (int i = 0; i < orbX.length; i++) {
                orbX[i] += orbDX[i]; orbY[i] += orbDY[i];
                if (orbX[i] < 0 || orbX[i] > getWidth())  orbDX[i] *= -1;
                if (orbY[i] < 0 || orbY[i] > getHeight()) orbDY[i] *= -1;
            }
            repaint();
        });
        animTimer.start();
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g;
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        for (int i = 0; i < orbX.length; i++) {
            int r = (int) orbR[i];
            g2.setColor(orbColors[i]);
            g2.fillOval((int) orbX[i] - r, (int) orbY[i] - r, r*2, r*2);
        }
    }

    // ── UI ─────────────────────────────────────────────────────────────────
    private void buildUI() {
        JPanel card = new JPanel();
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setBackground(new Color(28, 28, 46, 220));
        card.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(Theme.BORDER, 1),
            BorderFactory.createEmptyBorder(32, 40, 32, 40)));
        card.setOpaque(true);

        // Title
        JLabel title = Theme.label("Course Registration System", Theme.FONT_TITLE, Theme.TEXT_PRIMARY);
        title.setAlignmentX(CENTER_ALIGNMENT);
        JLabel sub = Theme.label("University Portal", Theme.FONT_BODY, Theme.TEXT_MUTED);
        sub.setAlignmentX(CENTER_ALIGNMENT);

        card.add(title);
        card.add(Box.createVerticalStrut(4));
        card.add(sub);
        card.add(Box.createVerticalStrut(30));

        // Student login
        JLabel sLabel = Theme.label("Student Login", Theme.FONT_HEADER, Theme.TEXT_PRIMARY);
        sLabel.setAlignmentX(CENTER_ALIGNMENT);
        card.add(sLabel);
        card.add(Box.createVerticalStrut(8));

        JTextField rollField = Theme.darkField(20);
        rollField.setMaximumSize(new Dimension(280, 36));
        rollField.setAlignmentX(CENTER_ALIGNMENT);
        rollField.setToolTipText("Enter Roll Number (e.g. S001)");
        JLabel rollHint = Theme.label("Roll Number (S001 / S002 / S003)", Theme.FONT_SMALL, Theme.TEXT_MUTED);
        rollHint.setAlignmentX(CENTER_ALIGNMENT);
        card.add(rollField);
        card.add(Box.createVerticalStrut(2));
        card.add(rollHint);
        card.add(Box.createVerticalStrut(10));

        JButton studentBtn = Theme.pillButton("Enter Student Portal", Theme.ACCENT);
        studentBtn.setMaximumSize(new Dimension(280, 40));
        studentBtn.setAlignmentX(CENTER_ALIGNMENT);
        card.add(studentBtn);

        card.add(Box.createVerticalStrut(24));
        JSeparator sep = new JSeparator();
        sep.setMaximumSize(new Dimension(280, 1));
        sep.setForeground(Theme.BORDER);
        card.add(sep);
        card.add(Box.createVerticalStrut(24));

        // Faculty login
        JLabel fLabel = Theme.label("Faculty Login", Theme.FONT_HEADER, Theme.TEXT_PRIMARY);
        fLabel.setAlignmentX(CENTER_ALIGNMENT);
        card.add(fLabel);
        card.add(Box.createVerticalStrut(8));

        // Faculty combo
        String[] facultyArr = loadFacultyNames();
        JComboBox<String> facultyCombo = new JComboBox<>(facultyArr);
        facultyCombo.setBackground(new Color(40, 40, 65));
        facultyCombo.setForeground(Color.BLACK);
        facultyCombo.setFont(Theme.FONT_BODY);
        facultyCombo.setMaximumSize(new Dimension(280, 36));
        facultyCombo.setAlignmentX(CENTER_ALIGNMENT);

        JPasswordField passField = Theme.darkPasswordField(20);
        passField.setMaximumSize(new Dimension(280, 36));
        passField.setAlignmentX(CENTER_ALIGNMENT);
        JLabel passHint = Theme.label("Password (e.g. smith123)", Theme.FONT_SMALL, Theme.TEXT_MUTED);
        passHint.setAlignmentX(CENTER_ALIGNMENT);

        card.add(facultyCombo);
        card.add(Box.createVerticalStrut(8));
        card.add(passField);
        card.add(Box.createVerticalStrut(2));
        card.add(passHint);
        card.add(Box.createVerticalStrut(10));

        JButton facultyBtn = Theme.pillButton("Enter Faculty Portal", new Color(99, 190, 120));
        facultyBtn.setMaximumSize(new Dimension(280, 40));
        facultyBtn.setAlignmentX(CENTER_ALIGNMENT);
        card.add(facultyBtn);

        // Status
        card.add(Box.createVerticalStrut(16));
        JLabel status = Theme.label("", Theme.FONT_SMALL, Theme.DANGER);
        status.setAlignmentX(CENTER_ALIGNMENT);
        card.add(status);

        // ── Listeners ──
        studentBtn.addActionListener(e -> {
            String roll = rollField.getText().trim();
            if (roll.isEmpty()) { status.setText("Enter your roll number."); return; }
            try {
                String name = DatabaseManager.loginStudent(roll);
                if (name == null) { status.setText("Roll number not found."); return; }
                int sid = DatabaseManager.getStudentId(roll);
                animTimer.stop();
                owner.getContentPane().removeAll();
                owner.getContentPane().add(new StudentPanel(owner, roll.toUpperCase(), name, sid));
                owner.revalidate(); owner.repaint();
            } catch (SQLException ex) {
                status.setText("DB Error: " + ex.getMessage());
            }
        });

        facultyBtn.addActionListener(e -> {
            String name = (String) facultyCombo.getSelectedItem();
            String pass = new String(passField.getPassword()).trim();
            if (pass.isEmpty()) { status.setText("Enter password."); return; }
            try {
                if (!DatabaseManager.loginFaculty(name, pass)) {
                    status.setText("Incorrect password.");
                    return;
                }
                animTimer.stop();
                owner.getContentPane().removeAll();
                owner.getContentPane().add(new FacultyPanel(owner, name));
                owner.revalidate(); owner.repaint();
            } catch (SQLException ex) {
                status.setText("DB Error: " + ex.getMessage());
            }
        });

        // Enter key on roll field
        rollField.addActionListener(e -> studentBtn.doClick());
        passField.addActionListener(e -> facultyBtn.doClick());

        add(card);
    }

    private String[] loadFacultyNames() {
        try {
            List<String> names = DatabaseManager.getFacultyNames();
            return names.toArray(new String[0]);
        } catch (SQLException e) {
            return new String[]{"Dr. Smith","Dr. Patel","Dr. Lee","Dr. Rao","Dr. Chen"};
        }
    }
}
