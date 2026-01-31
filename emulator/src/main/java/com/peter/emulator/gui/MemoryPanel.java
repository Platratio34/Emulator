package com.peter.emulator.gui;

import java.awt.Color;
import java.awt.GridLayout;

import javax.swing.JLabel;
import javax.swing.JPanel;

import com.peter.emulator.CPU;

public class MemoryPanel extends JPanel {

    protected final CPU cpu;
    public boolean stack = false;
    protected final int start;

    protected final JPanel panel;
    protected final JPanel panelOuter;
    protected final JLabel[] labels = new JLabel[8*8];

    public MemoryPanel(CPU cpu, int start) {
        this.cpu = cpu;
        this.start = start;

        panelOuter = new JPanel();
        add(panelOuter);

        panel = new JPanel();
        panelOuter.add(panel);
        panel.setLayout(new GridLayout(8,8,10,0));

        for(int i = 0; i < 8*8; i++) {
            labels[i] = new JLabel(EmulatorGui.toHex(cpu.readMem(i+start)));
            panel.add(labels[i]);
            if(stack) {
                labels[i].setForeground(cpu.stackPtr == (i+start) ? Color.red : Color.black);
            }
        }
    }

    public void update() {
        for(int i = 0; i < 8*8; i++) {
            labels[i].setText(EmulatorGui.toHex(cpu.readMem(i+start)));
            if(stack) {
                labels[i].setForeground(cpu.stackPtr == (i+start) ? Color.red : Color.black);
            }
        }
    }

}
