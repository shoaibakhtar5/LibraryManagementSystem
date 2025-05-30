package com.library;

import javax.swing.*;
import java.awt.*;
import java.awt.geom.RoundRectangle2D;

public class RoundedButton extends JButton {
    private Color hoverBackground;
    private Color normalBackground;
    private String fullText; // Store full button text

    public RoundedButton(String text, Color bg, Color fg) {
        super(text);
        this.fullText = text;
        this.normalBackground = bg;
        this.hoverBackground = bg.brighter();
        setForeground(fg);
        setBackground(normalBackground);
        setContentAreaFilled(false);
        setFocusPainted(false);
        setBorderPainted(false);
        setFont(new Font("Roboto", Font.BOLD, 16));

        addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                setBackground(hoverBackground);
            }

            public void mouseExited(java.awt.event.MouseEvent evt) {
                setBackground(normalBackground);
            }
        });
    }

    public RoundedButton(String text, Color bg, Color fg, String iconPath) {
        this(text, bg, fg);
        if (iconPath != null) {
            try {
                ImageIcon icon = new ImageIcon(getClass().getResource("/resources/" + iconPath));
                setIcon(new ImageIcon(icon.getImage().getScaledInstance(24, 24, Image.SCALE_SMOOTH)));
            } catch (Exception e) {
                System.out.println("Icon not found: " + iconPath);
            }
        }
    }

    // Method to show/hide full text
    public void setFullTextVisible(boolean visible) {
        super.setText(visible ? fullText : "");
    }

    public String getFullText() {
        return fullText;
    }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        Shape shape = new RoundRectangle2D.Float(0, 0, getWidth() - 1, getHeight() - 1, 30, 30);
        g2.setColor(getBackground());
        g2.fill(shape);
        g2.setColor(new Color(0, 0, 0, 50));
        g2.draw(shape);
        super.paintComponent(g2);
        g2.dispose();
    }

    @Override
    public Dimension getPreferredSize() {
        Dimension d = super.getPreferredSize();
        return new Dimension(d.width + 20, d.height + 10);
    }
}
