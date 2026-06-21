package com.peter.emulator.gui;

import java.awt.GridLayout;

import javax.swing.JFrame;
import javax.swing.JTextArea;
import javax.swing.JTextField;

import com.peter.emulator.peripherals.ConsolePeripheral;

public class ConsoleFrame extends JFrame {

    public final ConsolePeripheral console;

    protected JTextArea textArea;
    protected JTextField inputField;

    protected String[] lines = new String[8];
    

    public ConsoleFrame(ConsolePeripheral console) {
        super("Emulator - Console");
        this.console = console;
        console.outConsumer = this::onOutput;
        setLayout(new GridLayout(0, 1));

        textArea = new JTextArea();
        add(textArea);

        inputField = new JTextField();
        add(inputField);
        inputField.addActionListener((action) -> {
            String str = inputField.getText();
            for (int i = 0; i < str.length(); i++) {
                console.write(str.charAt(i));
            }
            console.write('\n');
        });

        for (int i = 0; i < lines.length; i++) {
            lines[i] = "";
        }
        updateLines();

        setSize(800, 500);
    }

    public void onOutput(char c) {
        if (c == '\n') {
            for (int i = 1; i < lines.length; i++) {
                lines[i - 1] = lines[i];
            }
            lines[lines.length - 1] = "";
            return;
        }
        lines[lines.length - 1] += c;

        updateLines();
    }

    protected void updateLines() {
        String str = "";
        for (int i = 0; i < lines.length; i++) {
            if (i > 0)
                str += "\n";
            str += lines[i];
        }
        textArea.setText(str);
    }
}
