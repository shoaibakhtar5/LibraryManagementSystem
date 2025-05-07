package com.library;

import javax.swing.*;
import java.awt.*;
import java.awt.geom.RoundRectangle2D;

public class RoundedButton extends JButton {
    private Color hoverBackground;
    private Color normalBackground;

    public RoundedButton(String text, Color bg, Color fg, String iconPath) {
        super(text);
        this.normalBackground = bg;
        this.hoverBackground = bg.brighter();
        setForeground(fg);
        setBackground(normalBackground);
        setContentAreaFilled(false);
        setFocusPainted(false);
        setBorderPainted(false);
        setFont(new Font("Segoe UI", Font.BOLD, 14));
        setOpaque(false);

        if (iconPath != null) {
            try {
                ImageIcon icon = new ImageIcon(getClass().getResource("/resources/" + iconPath));
                setIcon(new ImageIcon(icon.getImage().getScaledInstance(20, 20, Image.SCALE_SMOOTH)));
            } catch (Exception e) {
                System.out.println("Icon not found: " + iconPath);
            }
        }

        addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                setBackground(hoverBackground);
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                setBackground(normalBackground);
            }
        });
    }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        Shape shape = new RoundRectangle2D.Float(0, 0, getWidth() - 1, getHeight() - 1, 10, 10);
        g2.setColor(getBackground());
        g2.fill(shape);
        g2.setColor(new Color(200, 200, 200));
        g2.draw(shape);
        super.paintComponent(g2);
        g2.dispose();
    }

    @Override
    public void setToolTipText(String text) {
        super.setToolTipText(text);
    }
}