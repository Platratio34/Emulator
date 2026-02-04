package com.peter.emulator.gui;

import java.awt.BorderLayout;
import java.awt.GridLayout;

import javax.swing.*;

import com.peter.emulator.Emulator;

public class EmulatorGui {

    protected JFrame frame;
    protected CPUPanel cpuPanel;
    protected MemoryPanel stackPanel;
    protected MemoryPanel kernalPanel;

    protected JPanel buttonGrid;
    protected JButton pauseBtn;
    protected JButton resumeBtn;
    protected JButton tickBtn;

    public EmulatorGui(Emulator emulator) {
        SwingUtilities.invokeLater(() -> {
            frame = new JFrame("Emulator");
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            JPanel panel = new JPanel();

            JLabel label = new JLabel("Emulator");
            panel.add(label);

            frame.getContentPane().add(panel, BorderLayout.CENTER);

            buttonGrid = new JPanel();
            buttonGrid.setLayout(new GridLayout(2,2));
            frame.getContentPane().add(buttonGrid, BorderLayout.CENTER);
            
            pauseBtn = new JButton("Pause");
            pauseBtn.addActionListener((event) -> {
                emulator.setWait(true);
            });
            buttonGrid.add(pauseBtn);

            resumeBtn = new JButton("Resume");
            resumeBtn.addActionListener((event) -> {
                emulator.setWait(false);
            });
            buttonGrid.add(resumeBtn);

            tickBtn = new JButton("Tick");
            tickBtn.addActionListener((event) -> {
                emulator.stopWaiting();
            });
            buttonGrid.add(tickBtn);

            cpuPanel = new CPUPanel(emulator.cores[0], "cpu0");
            frame.getContentPane().add(cpuPanel, BorderLayout.WEST);

            JPanel p = new JPanel();
            p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS));
            frame.getContentPane().add(p, BorderLayout.EAST);

            stackPanel = new MemoryPanel(emulator.cores[0], 0x1000, 8);
            stackPanel.stack = true;
            p.add(stackPanel);

            kernalPanel = new MemoryPanel(emulator.cores[0], 0, 10);
            p.add(kernalPanel);

            frame.setSize(1500, 500);
            
            frame.setVisible(true);

        });
    }

    boolean updating = false;
    public void update() {
        if(updating)
            return;
        updating = true;
        SwingUtilities.invokeLater(() -> {
            cpuPanel.update();
            stackPanel.update();
            kernalPanel.update();
            updating = false;
        });
    }

    public static String toHex(int num) {
        String str = String.format("%x", num);
        while (str.length() < 8) {
            str = "0" + str;
        }
        return str.substring(0,4)+"_"+str.substring(4);
    }
}
