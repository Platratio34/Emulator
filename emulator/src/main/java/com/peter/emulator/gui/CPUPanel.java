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

    protected final JLabel[] regLabels = new JLabel[32];
    protected final JLabel pgmPtrLbl;
    protected final JLabel stackPtrLbl;
    protected final JLabel pidLbl;
    protected final JLabel memTblLbl;
    protected final JLabel intpCdeLbl;
    protected final JLabel inptRspLbl;
    protected final JLabel pmLbl;
    protected final JLabel pgmPtrLbl_I;
    protected final JLabel stackPtrLbl_I;
    protected final JLabel pidLbl_I;
    protected final JLabel memTblLbl_I;
    protected final JLabel pmLbl_I;

    protected final JLabel instrLbl;

    protected final DebuggerPanel debuggerPanel;

    public CPUPanel(CPU cpu, String name) {
        this.cpu = cpu;
        this.cpuName = name;

        setLayout(new GridLayout(0, 1));

        nameLbl = new JLabel(name);
        add(nameLbl);

        regPanelOuter = new JPanel();
        add(regPanelOuter);
        regPanel = new JPanel();
        regPanel.setLayout(new GridLayout(12,8, 5, 0));
        Dimension d = regPanel.getPreferredSize();
        setSize(d);
        regPanelOuter.add(regPanel);
        for(int i = 0; i < 8; i++) {
            JLabel l0 = new JLabel(MachineCode.translateReg(i));
            regPanel.add(l0);
            l0.setFont(EmulatorGui.monFont);
        }
        for(int i = 0; i < 8; i++) {
            regLabels[i] = new JLabel(EmulatorGui.toHex(cpu.registers[i]));
            regPanel.add(regLabels[i]).setFont(EmulatorGui.monFont);
        }
        for(int i = 0; i < 8; i++) {
            JLabel l0 = new JLabel(MachineCode.translateReg(i+8));
            regPanel.add(l0);
            l0.setFont(EmulatorGui.monFont);
        }
        for (int i = 0; i < 8; i++) {
            regLabels[i + 8] = new JLabel(EmulatorGui.toHex(cpu.registers[i + 8]));
            regPanel.add(regLabels[i + 8]).setFont(EmulatorGui.monFont);
        }
        
        regPanel.add(new JLabel(MachineCode.translateReg(MachineCode.REG_PGM_PNTR))).setFont(EmulatorGui.monFont);
        regPanel.add(new JLabel(MachineCode.translateReg(MachineCode.REG_STACK_PNTR))).setFont(EmulatorGui.monFont);

        regPanel.add(new JLabel(MachineCode.translateReg(MachineCode.REG_PID))).setFont(EmulatorGui.monFont);
        regPanel.add(new JLabel(MachineCode.translateReg(MachineCode.REG_MEM_TABLE))).setFont(EmulatorGui.monFont);

        regPanel.add(new JLabel(MachineCode.translateReg(MachineCode.REG_INTERRUPT))).setFont(EmulatorGui.monFont);
        regPanel.add(new JLabel(MachineCode.translateReg(MachineCode.REG_INTR_HANDLER))).setFont(EmulatorGui.monFont);

        regPanel.add(new JLabel(MachineCode.translateReg(MachineCode.REG_PRIVILEGED_MODE))).setFont(EmulatorGui.monFont);
        regPanel.add(new JLabel("")).setFont(EmulatorGui.monFont);

        pgmPtrLbl = new JLabel(EmulatorGui.toHex(cpu.pgmPtr));
        pgmPtrLbl.setFont(EmulatorGui.monFont);
        regPanel.add(pgmPtrLbl);
        stackPtrLbl = (JLabel)regPanel.add(new JLabel(EmulatorGui.toHex(cpu.stackPtr)));
        stackPtrLbl.setFont(EmulatorGui.monFont);

        pidLbl = (JLabel)regPanel.add(new JLabel(EmulatorGui.toHex(cpu.pid)));
        pidLbl.setFont(EmulatorGui.monFont);
        memTblLbl = (JLabel)regPanel.add(new JLabel(EmulatorGui.toHex(cpu.memTablePtr)));
        memTblLbl.setFont(EmulatorGui.monFont);

        intpCdeLbl = (JLabel)regPanel.add(new JLabel(EmulatorGui.toHex(cpu.interruptCode)));
        intpCdeLbl.setFont(EmulatorGui.monFont);
        inptRspLbl = (JLabel)regPanel.add(new JLabel(EmulatorGui.toHex(cpu.interruptHandler)));
        inptRspLbl.setFont(EmulatorGui.monFont);

        pmLbl = (JLabel)regPanel.add(new JLabel(EmulatorGui.toHex(cpu.privilegeMode ? 1 : 0)));
        pmLbl.setFont(EmulatorGui.monFont);
        regPanel.add(new JLabel("")).setFont(EmulatorGui.monFont);
        
        
        for(int i = 0; i < 8; i++) {
            JLabel l0 = new JLabel(MachineCode.translateReg(i+16));
            regPanel.add(l0);
            l0.setFont(EmulatorGui.monFont);
        }
        for(int i = 0; i < 8; i++) {
            regLabels[i + 16] = new JLabel(EmulatorGui.toHex(cpu.getReg(i + 16)));
            regPanel.add(regLabels[i + 16]).setFont(EmulatorGui.monFont);
        }
        for(int i = 0; i < 8; i++) {
            JLabel l0 = new JLabel(MachineCode.translateReg(i+24));
            regPanel.add(l0);
            l0.setFont(EmulatorGui.monFont);
        }
        for (int i = 0; i < 8; i++) {
            regLabels[i + 24] = new JLabel(EmulatorGui.toHex(cpu.getReg(i + 24)));
            regPanel.add(regLabels[i + 24]).setFont(EmulatorGui.monFont);
        }
        
        regPanel.add(new JLabel(MachineCode.translateReg(MachineCode.REG_PGM_PNTR_I))).setFont(EmulatorGui.monFont);
        regPanel.add(new JLabel(MachineCode.translateReg(MachineCode.REG_STACK_PNTR_I))).setFont(EmulatorGui.monFont);

        regPanel.add(new JLabel(MachineCode.translateReg(MachineCode.REG_PID_I))).setFont(EmulatorGui.monFont);
        regPanel.add(new JLabel(MachineCode.translateReg(MachineCode.REG_MEM_TABLE_I))).setFont(EmulatorGui.monFont);

        regPanel.add(new JLabel(MachineCode.translateReg(MachineCode.REG_PRIVILEGED_MODE_I))).setFont(EmulatorGui.monFont);
        regPanel.add(new JLabel("")).setFont(EmulatorGui.monFont);
        regPanel.add(new JLabel("")).setFont(EmulatorGui.monFont);
        regPanel.add(new JLabel("")).setFont(EmulatorGui.monFont);

        pgmPtrLbl_I = new JLabel(EmulatorGui.toHex(cpu.pgmPtrI));
        pgmPtrLbl_I.setFont(EmulatorGui.monFont);
        regPanel.add(pgmPtrLbl_I);
        stackPtrLbl_I = (JLabel)regPanel.add(new JLabel(EmulatorGui.toHex(cpu.stackPtrI)));
        stackPtrLbl_I.setFont(EmulatorGui.monFont);

        pidLbl_I = (JLabel)regPanel.add(new JLabel(EmulatorGui.toHex(cpu.pidI)));
        pidLbl_I.setFont(EmulatorGui.monFont);
        memTblLbl_I = (JLabel)regPanel.add(new JLabel(EmulatorGui.toHex(cpu.memTablePtrI)));
        memTblLbl_I.setFont(EmulatorGui.monFont);

        pmLbl_I = (JLabel)regPanel.add(new JLabel(EmulatorGui.toHex(cpu.privilegeModeI ? 1 : 0)));
        pmLbl_I.setFont(EmulatorGui.monFont);

        regPanel.add(new JLabel("")).setFont(EmulatorGui.monFont);
        regPanel.add(new JLabel("")).setFont(EmulatorGui.monFont);
        regPanel.add(new JLabel("")).setFont(EmulatorGui.monFont);

        instrLbl = new JLabel(MachineCode.translate(cpu.readMem(cpu.pgmPtr), cpu.readMem(cpu.pgmPtr+1)));
        instrLbl.setFont(EmulatorGui.monFont);
        add(instrLbl);

        debuggerPanel = new DebuggerPanel(cpu);
        add(debuggerPanel);

        update();
    }

    public void update() {
        for(int i = 0; i < 32; i++) {
            regLabels[i].setText(EmulatorGui.toHex(cpu.getReg(i)));
        }
        pgmPtrLbl.setText(EmulatorGui.toHex(cpu.pgmPtr));
        stackPtrLbl.setText(EmulatorGui.toHex(cpu.stackPtr));

        pidLbl.setText(EmulatorGui.toHex(cpu.pid));
        memTblLbl.setText(EmulatorGui.toHex(cpu.memTablePtr));

        intpCdeLbl.setText(EmulatorGui.toHex(cpu.interruptCode));
        inptRspLbl.setText(EmulatorGui.toHex(cpu.interruptHandler));

        pmLbl.setText(EmulatorGui.toHex(cpu.privilegeMode ? 1 : 0));
        
        pgmPtrLbl_I.setText(EmulatorGui.toHex(cpu.pgmPtrI));
        stackPtrLbl_I.setText(EmulatorGui.toHex(cpu.stackPtrI));

        pidLbl_I.setText(EmulatorGui.toHex(cpu.pidI));
        memTblLbl_I.setText(EmulatorGui.toHex(cpu.memTablePtrI));

        pmLbl_I.setText(EmulatorGui.toHex(cpu.privilegeModeI ? 1 : 0));

        instrLbl.setText(MachineCode.translate(cpu.instr, cpu.instrb) + "(" + EmulatorGui.toHex(cpu.instr) + " "
                + EmulatorGui.toHex(cpu.instrb) + ")");
        
        debuggerPanel.update();
    }
}
