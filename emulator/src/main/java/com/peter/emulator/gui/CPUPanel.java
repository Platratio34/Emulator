package com.peter.emulator.gui;

import java.awt.Dimension;
import java.awt.GridLayout;

import javax.swing.BoxLayout;
import javax.swing.JLabel;
import javax.swing.JPanel;

import com.peter.emulator.CPU;
import com.peter.emulator.MachineCode;

public class CPUPanel extends JPanel {

    protected final CPU cpu;
    protected final String cpuName;

    protected final JLabel nameLbl;
    protected final JPanel regPanel;
    protected final JPanel regPanelOuter;

    protected final JLabel[] regLabels = new JLabel[16];
    protected final JLabel pgmPtrLbl;
    protected final JLabel stackPtrLbl;
    protected final JLabel pidLbl;
    protected final JLabel memTblLbl;
    protected final JLabel intpCdeLbl;
    protected final JLabel inptRspLbl;
    protected final JLabel pmLbl;

    protected final JLabel instrLbl;

    protected final DebuggerPanel debuggerPanel;

    public CPUPanel(CPU cpu, String name) {
        this.cpu = cpu;
        this.cpuName = name;

        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));

        nameLbl = new JLabel(name);
        add(nameLbl);

        regPanelOuter = new JPanel();
        add(regPanelOuter);
        regPanel = new JPanel();
        regPanel.setLayout(new GridLayout(6,8, 5, 0));
        Dimension d = regPanel.getPreferredSize();
        setSize(d);
        regPanelOuter.add(regPanel);
        for(int i = 0; i < 8; i++) {
            JLabel l0 = new JLabel(MachineCode.translateReg(i));
            regPanel.add(l0);
        }
        for(int i = 0; i < 8; i++) {
            regLabels[i] = new JLabel(EmulatorGui.toHex(cpu.registers[i]));
            regPanel.add(regLabels[i]);
        }
        for(int i = 0; i < 8; i++) {
            JLabel l0 = new JLabel(MachineCode.translateReg(i+8));
            regPanel.add(l0);
        }
        for(int i = 0; i < 8; i++) {
            regLabels[i+8] = new JLabel(EmulatorGui.toHex(cpu.registers[i+8]));
            regPanel.add(regLabels[i+8]);
        }
        regPanel.add(new JLabel(MachineCode.translateReg(MachineCode.REG_PGM_PNTR)));
        regPanel.add(new JLabel(MachineCode.translateReg(MachineCode.REG_STACK_PNTR)));

        regPanel.add(new JLabel(MachineCode.translateReg(MachineCode.REG_PID)));
        regPanel.add(new JLabel(MachineCode.translateReg(MachineCode.REG_MEM_TABLE)));

        regPanel.add(new JLabel(MachineCode.translateReg(MachineCode.REG_INTERRUPT)));
        regPanel.add(new JLabel(MachineCode.translateReg(MachineCode.REG_INTR_RSP)));

        regPanel.add(new JLabel(MachineCode.translateReg(MachineCode.REG_PRIVILEGED_MODE)));
        regPanel.add(new JLabel(""));

        pgmPtrLbl = new JLabel(EmulatorGui.toHex(cpu.pgmPtr));
        regPanel.add(pgmPtrLbl);
        stackPtrLbl = (JLabel)regPanel.add(new JLabel(EmulatorGui.toHex(cpu.stackPtr)));

        pidLbl = (JLabel)regPanel.add(new JLabel(EmulatorGui.toHex(cpu.pid)));
        memTblLbl = (JLabel)regPanel.add(new JLabel(EmulatorGui.toHex(cpu.memTablePtr)));

        intpCdeLbl = (JLabel)regPanel.add(new JLabel(EmulatorGui.toHex(cpu.interruptCode)));
        inptRspLbl = (JLabel)regPanel.add(new JLabel(EmulatorGui.toHex(cpu.interruptRsp)));

        pmLbl = (JLabel)regPanel.add(new JLabel(EmulatorGui.toHex(cpu.privilegeMode ? 1 : 0)));
        regPanel.add(new JLabel(""));

        instrLbl = new JLabel(MachineCode.translate(cpu.readMem(cpu.pgmPtr), cpu.readMem(cpu.pgmPtr+1)));
        add(instrLbl);

        debuggerPanel = new DebuggerPanel(cpu);
        add(debuggerPanel);
    }

    public void update() {
        for(int i = 0; i < 16; i++) {
            regLabels[i].setText(EmulatorGui.toHex(cpu.registers[i]));
        }
        pgmPtrLbl.setText(EmulatorGui.toHex(cpu.pgmPtr));
        stackPtrLbl.setText(EmulatorGui.toHex(cpu.stackPtr));

        pidLbl.setText(EmulatorGui.toHex(cpu.pid));
        memTblLbl.setText(EmulatorGui.toHex(cpu.memTablePtr));

        intpCdeLbl.setText(EmulatorGui.toHex(cpu.interruptCode));
        inptRspLbl.setText(EmulatorGui.toHex(cpu.interruptRsp));

        pmLbl.setText(EmulatorGui.toHex(cpu.privilegeMode ? 1 : 0));

        instrLbl.setText(MachineCode.translate(cpu.instr, cpu.instrb) + "(" + EmulatorGui.toHex(cpu.instr) + " "
                + EmulatorGui.toHex(cpu.instrb) + ")");
        
        debuggerPanel.update();
    }
}
