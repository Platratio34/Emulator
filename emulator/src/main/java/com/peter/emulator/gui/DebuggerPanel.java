package com.peter.emulator.gui;

import java.awt.GridLayout;
import java.util.HashMap;

import javax.swing.JLabel;
import javax.swing.JPanel;

import com.peter.emulator.CPU;
import com.peter.emulator.assembly.SymbolFile.StackVarSymbol;
import com.peter.emulator.debug.Debugger;

public class DebuggerPanel extends JPanel {

    public final CPU cpu;
    protected Debugger debugger;

    // protected final JLabel lineLbl;
    protected final JLabel funcLbl;
    protected final JPanel sVarPanel;
    protected final JPanel varPanel;
    protected final HashMap<String, VarDisplay> varDisplays = new HashMap<>();

    public DebuggerPanel(CPU cpu) {
        super(new GridLayout(-1, 1));
        this.cpu = cpu;

        // this.lineLbl = new JLabel();
        // add(lineLbl);

        this.funcLbl = new JLabel();
        add(funcLbl);

        sVarPanel = new JPanel(new GridLayout(0, 3));
        add(sVarPanel);

        varPanel = new JPanel(new GridLayout(0, 3));
        add(varPanel);
    }

    public void update() {
        if (debugger != cpu.debugger) {
            debugger = cpu.debugger;
            for (VarDisplay vd : varDisplays.values()) {
                varPanel.remove(vd);
            }
            for (String v : debugger.getVars()) {
                VarDisplay vd = new VarDisplay(v);
                varPanel.add(vd);
                varDisplays.put(v, vd);
            }
        }
        // String str = debugger.printStack()
        // lineLbl.setText();
        funcLbl.setText(String.format("<html>%s<br/>%s</html>", debugger.getLine(cpu, ""), debugger.printStack().replace("\n","<br>")));
        for (VarDisplay vd : varDisplays.values()) {
            vd.update();
        }

        sVarPanel.removeAll();
        for (StackVarSymbol sv : debugger.activeStackVars) {
            JLabel lbl = new JLabel();
            lbl.setText(String.format("%s: %s", sv.name, debugger.readVar(cpu, sv)));
            sVarPanel.add(lbl);
        }
    }

    protected class StackVarDisplay extends JLabel {
        public final String name;

        public StackVarDisplay(String name) {
            this.name = name;
            update();
        }

        public void update() {
            setText(String.format("%s: %s", name, debugger.getVar(cpu, name)));
        }
    }

    protected class VarDisplay extends JLabel {
        public final String name;

        public VarDisplay(String name) {
            this.name = name;
            update();
        }

        public void update() {
            setText(String.format("%s: %s", name, debugger.getVar(cpu, name)));
        }
    }
}
