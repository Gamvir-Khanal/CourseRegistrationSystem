package com.university;

import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.table.*;
import java.awt.*;

public class Theme {

    // ── Palette ────────────────────────────────────────────────────────────
    public static final Color BG_DARK      = new Color(18, 18, 30);
    public static final Color BG_CARD      = new Color(28, 28, 46);
    public static final Color BG_ROW_ALT   = new Color(35, 35, 58);
    public static final Color ACCENT       = new Color(99, 102, 241);   // indigo
    public static final Color ACCENT_HOVER = new Color(129, 140, 248);
    public static final Color SUCCESS      = new Color(34, 197, 94);
    public static final Color DANGER       = new Color(239, 68, 68);
    public static final Color WARNING      = new Color(234, 179, 8);
    public static final Color TEXT_PRIMARY = new Color(241, 245, 249);
    public static final Color TEXT_MUTED   = new Color(148, 163, 184);
    public static final Color BORDER       = new Color(55, 65, 81);

    // ── Fonts ──────────────────────────────────────────────────────────────
    public static final Font FONT_TITLE  = new Font("Segoe UI", Font.BOLD,  22);
    public static final Font FONT_HEADER = new Font("Segoe UI", Font.BOLD,  14);
    public static final Font FONT_BODY   = new Font("Segoe UI", Font.PLAIN, 13);
    public static final Font FONT_SMALL  = new Font("Segoe UI", Font.PLAIN, 11);

    // ── Pill Button ────────────────────────────────────────────────────────
    public static JButton pillButton(String text, Color bg) {
        JButton btn = new JButton(text) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(getModel().isRollover() ? bg.brighter() : bg);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 24, 24);
                g2.dispose();
                super.paintComponent(g);
            }
        };
        btn.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 13));
        btn.setForeground(Color.WHITE);
        btn.setContentAreaFilled(false);
        btn.setBorderPainted(false);
        btn.setFocusPainted(false);
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btn.setPreferredSize(new Dimension(150, 36));
        return btn;
    }

    // ── Dark Table ─────────────────────────────────────────────────────────
    public static void styleTable(JTable table) {
        table.setBackground(BG_CARD);
        table.setForeground(Color.BLACK);
        table.setGridColor(BORDER);
        table.setRowHeight(28);
        table.setFont(FONT_BODY);
        table.setSelectionBackground(ACCENT);
        table.setSelectionForeground(Color.WHITE);
        table.setShowHorizontalLines(true);
        table.setShowVerticalLines(false);
        table.setIntercellSpacing(new Dimension(0, 1));

        JTableHeader header = table.getTableHeader();
        header.setOpaque(true); 
        header.setBackground(new Color(45, 45, 70));
        header.setForeground(Color.BLACK);
        header.setFont(FONT_HEADER);
        header.setPreferredSize(new Dimension(0, 34));

        // Alternating rows renderer
        table.setDefaultRenderer(Object.class, new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable t, Object v,
                    boolean sel, boolean foc, int row, int col) {
                super.getTableCellRendererComponent(t, v, sel, foc, row, col);
                if (!sel) {
                    setBackground(row % 2 == 0 ? BG_CARD : BG_ROW_ALT);
                    setForeground(TEXT_PRIMARY);
                }
                setBorder(BorderFactory.createEmptyBorder(0, 8, 0, 8));
                return this;
            }
        });
    }

    // ── Dark TextField ─────────────────────────────────────────────────────
    public static JTextField darkField(int cols) {
        JTextField tf = new JTextField(cols);
        tf.setBackground(new Color(40, 40, 65));
        tf.setForeground(TEXT_PRIMARY);
        tf.setCaretColor(TEXT_PRIMARY);
        tf.setFont(FONT_BODY);
        tf.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(BORDER, 1),
            BorderFactory.createEmptyBorder(4, 8, 4, 8)));
        return tf;
    }

    // ── Dark PasswordField ─────────────────────────────────────────────────
    public static JPasswordField darkPasswordField(int cols) {
        JPasswordField pf = new JPasswordField(cols);
        pf.setBackground(new Color(40, 40, 65));
        pf.setForeground(TEXT_PRIMARY);
        pf.setCaretColor(TEXT_PRIMARY);
        pf.setFont(FONT_BODY);
        pf.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(BORDER, 1),
            BorderFactory.createEmptyBorder(4, 8, 4, 8)));
        return pf;
    }

    // ── Dark Label ─────────────────────────────────────────────────────────
    public static JLabel label(String text, Font font, Color color) {
        JLabel lbl = new JLabel(text);
        lbl.setFont(font);
        lbl.setForeground(color);
        return lbl;
    }

    // ── ScrollPane ─────────────────────────────────────────────────────────
    public static JScrollPane darkScroll(Component c) {
        JScrollPane sp = new JScrollPane(c);
        sp.setBackground(BG_CARD);
        sp.getViewport().setBackground(BG_CARD);
        sp.setBorder(BorderFactory.createLineBorder(BORDER, 1));
        sp.getVerticalScrollBar().setBackground(BG_DARK);
        return sp;
    }
}
