package com.library;

import javax.swing.*;
import java.awt.*;

public class MemberDashboardFrame extends JFrame {
    private User user;

    public MemberDashboardFrame(User user) {
        this.user = user;
        initUI();
    }

    private void initUI() {
        setTitle("Member Dashboard - Library Management System");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(800, 600);
        setLocationRelativeTo(null);

        JTabbedPane tabbedPane = new JTabbedPane();
        tabbedPane.addTab("My Reservations", new ReservationsPanel(user));
        tabbedPane.addTab("My Fines", new FinesPanel(user));

        add(tabbedPane);
        setVisible(true);
    }
}