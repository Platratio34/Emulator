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
    protected final JLabel[] rowLabels;
    protected final JLabel[] labels;

    public MemoryPanel(CPU cpu, int start, int rows) {
        this.cpu = cpu;
        this.start = start;

        panelOuter = new JPanel();
        add(panelOuter);

        panel = new JPanel();
        panelOuter.add(panel);
        panel.setLayout(new GridLayout(rows, 9, 10, 0));

        rowLabels = new JLabel[rows];
        labels = new JLabel[8*rows];
        for (int i = 0; i < 8 * rows; i++) {
            if (i % 8 == 0) {
                int rI = i / 8;
                rowLabels[rI] = new JLabel(EmulatorGui.toHex((i * 4)));
                rowLabels[rI].setFont(EmulatorGui.monFont);
                panel.add(rowLabels[rI]);
                rowLabels[rI].setForeground(Color.BLUE);
            }
            labels[i] = new JLabel(EmulatorGui.toHex(cpu.readMem((i*4) + start)));
            labels[i].setFont(EmulatorGui.monFont);
            panel.add(labels[i]);
            if(stack) {
                labels[i].setForeground(cpu.stackPtr == ((i*4)+start) ? Color.red : Color.black);
            } else {
                labels[i].setForeground(cpu.pgmPtr == ((i*4)+start) ? Color.red : Color.black);
            }
        }
    }

    public void update() {
        for(int i = 0; i < labels.length; i++) {
            labels[i].setText(EmulatorGui.toHex(cpu.readMem((i*4)+start)));
            if(stack) {
                labels[i].setForeground(cpu.stackPtr == ((i*4)+start) ? Color.red : Color.black);
            } else {
                labels[i].setForeground(cpu.pgmPtr == ((i*4)+start) ? Color.red : Color.black);
            }
        }
    }

}
