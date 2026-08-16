package com.peter.emulator.gui;

import javax.swing.JFrame;
import javax.swing.JTextArea;

import com.peter.emulator.peripherals.CharacterDisplay;

public class CharacterDisplayFrame extends JFrame {

    public final CharacterDisplay peripheral;

    protected JTextArea textArea;

    public CharacterDisplayFrame(CharacterDisplay peripheral) {
        super("Emulator - Character Display");
        this.peripheral = peripheral;
        peripheral.frame = this;

        textArea = new JTextArea();
        add(textArea);
        textArea.setFont(EmulatorGui.monFont);

        updateDisplay();

        setSize(800, 500);
    }

    public void updateDisplay() {
        textArea.setText(peripheral.getOut());
    }

}
