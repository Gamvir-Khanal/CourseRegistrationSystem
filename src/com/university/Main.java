package com.university;

import javax.swing.*;
import java.awt.*;
import java.sql.SQLException;

public class Main {

    public static void main(String[] args) {
        // Set system look for native dialogs=
        
        try { UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName()); }
        catch (Exception ignored) {}

        SwingUtilities.invokeLater(() -> {
            // ── Connect to DB and seed ────────────────────────────────────
            try {
                DatabaseManager.connect();
                DatabaseManager.seedIfEmpty();
            } catch (SQLException ex) {
                JOptionPane.showMessageDialog(null,
                    "Cannot connect to MySQL!\n\n" + ex.getMessage() +
                    "\n\nMake sure MySQL is running and the credentials in\n" +
                    "DatabaseManager.java (USER/PASS) are correct.",
                    "Database Connection Error", JOptionPane.ERROR_MESSAGE);
                System.exit(1);
            }

            // ── Build main window ─────────────────────────────────────────
            JFrame frame = new JFrame("Course Registration System");
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            frame.setMinimumSize(new Dimension(900, 620));
            frame.setPreferredSize(new Dimension(1100, 720));

            frame.getContentPane().setBackground(Theme.BG_DARK);
            frame.getContentPane().setLayout(new BorderLayout());
            frame.getContentPane().add(new HomePanel(frame));

            frame.pack();
            frame.setLocationRelativeTo(null);
            frame.setVisible(true);

            // Graceful DB disconnect on close
            frame.addWindowListener(new java.awt.event.WindowAdapter() {
                @Override public void windowClosing(java.awt.event.WindowEvent e) {
                    DatabaseManager.disconnect();
                }
            });
        });
    }
}
