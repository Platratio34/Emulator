package com.peter.emulator.gui;

import java.awt.Dimension;

import javax.swing.JFrame;
import javax.swing.JTextArea;

import com.peter.emulator.peripherals.CharacterDisplay;
import com.peter.emulator.peripherals.KeyboardPeripheral;

public class CharacterDisplayFrame extends JFrame {

    public final CharacterDisplay peripheral;

    protected JTextArea textArea;

    public CharacterDisplayFrame(CharacterDisplay peripheral, KeyboardPeripheral keyboardPeripheral) {
        super("Emulator - Character Display");
        this.peripheral = peripheral;
        peripheral.frame = this;

        textArea = new JTextArea(peripheral.height, peripheral.width);
        add(textArea);
        textArea.setFont(EmulatorGui.monFont);
        textArea.setEditable(false);

        updateDisplay();

        pack();

        if (keyboardPeripheral != null) {
            System.out.println("Adding keyboard");
            textArea.addKeyListener(keyboardPeripheral);
        }
    }

    public void updateDisplay() {
        textArea.setText(peripheral.getOut());
        pack();
    }

}
