package com.peter.emulator;

import com.peter.Main;
import com.peter.emulator.components.ComponentBus;
import com.peter.emulator.components.MMU;
import com.peter.emulator.components.RAM;
import com.peter.emulator.components.TimerUnit;
import com.peter.emulator.gui.EmulatorGui;
import com.peter.emulator.peripherals.CharacterDisplay;
import com.peter.emulator.peripherals.ConsolePeripheral;
import com.peter.emulator.peripherals.KeyboardPeripheral;
import com.peter.emulator.peripherals.PeripheralManager;
import com.peter.emulator.peripherals.StoragePeripheral;

public class Emulator {

    public final ComponentBus componentBus = new ComponentBus();

    public final RAM kernalRam = new RAM(0, 1);

    public final RAM mainRam = new RAM(0x2_0000, 0x7e);
    public final MMU mmu = new MMU();
    public float tickSpeed = 480;

    public final CPU[] cores = new CPU[] {
        new CPU(0, componentBus, mmu)
    };
    public final PeripheralManager peripheralManager = new PeripheralManager(componentBus, cores[0]);
    public final EmulatorGui gui;
    public final TimerUnit timerUnit = new TimerUnit(PeripheralManager.PERIPHERAL_START + 0x200, cores[0]);
    public final ConsolePeripheral console = new ConsolePeripheral(PeripheralManager.PERIPHERAL_START + 0x300);
    public final KeyboardPeripheral keyboard = new KeyboardPeripheral(PeripheralManager.PERIPHERAL_START + 0x304);
    public final StoragePeripheral vd0 = new StoragePeripheral(Main.ROOT_PATH.resolve("devices/vd0"));
    public final CharacterDisplay charDisplay = new CharacterDisplay(40, 24);

    public Emulator() {
        componentBus.addComponent(kernalRam);
        componentBus.addComponent(peripheralManager);
        componentBus.addComponent(mainRam);

        peripheralManager.addPeripheral(timerUnit);
        peripheralManager.addPeripheral(console);
        peripheralManager.addPeripheral(keyboard);
        peripheralManager.addPeripheral(vd0);
        peripheralManager.addPeripheral(charDisplay);
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            stop();
        }));
        gui = new EmulatorGui(this);
    }

    protected void tick() {
        // System.out.println("tick");
        for (CPU cpu : cores) {
            cpu.tick();
        }
        peripheralManager.tick();
        gui.update();
    }

    protected boolean running = false;
    public boolean wait = true;
    public boolean waiting = true;
    protected Thread thread;

    public void run() {
        if (running)
            return;
        gui.show();
        running = true;
        System.out.println("Starting emulator . . .");
        cores[0].running = true;
        thread = new Thread(() -> {
            System.out.println("Emulator started\n");
            while (running) {
                if(wait) {
                    while(waiting) {
                        try {
                            Thread.sleep(100);
                        } catch (InterruptedException e) {
                        }
                    }
                } else if(tickSpeed > 0) {
                    try {
                        Thread.sleep((long)((1/tickSpeed)*1000));
                    } catch (InterruptedException e) {
                    }
                }
                if(!running)
                    break;
                try {
                    tick();
                } catch (Exception e) {
                    System.err.println("\n");
                    System.err.println("Exception in execution");
                    System.err.println(cores[0].lastInstruction);
                    if (cores[0].debugger != null) {
                        System.err.println(cores[0].debugger.printStack());
                    }
                    System.err.println(e);
                    e.printStackTrace();
                    running = false;
                }
                if(wait)
                    waiting = true;
            }
            System.out.println("\nEmulator stopped");
        }, "emulator-main");
        thread.start();
    }

    public void stop() {
        if (!running)
            return;
        running = false;
        thread.interrupt();
        thread = null;
        System.out.println("Stopping emulator . . .");
    }

    public boolean isRunning() {
        return running;
    }

    public void stopWaiting() {
        if(running && waiting) {
            waiting = false;
            thread.interrupt();
        }
    }
    public void setWait(boolean newWait) {
        if(newWait == wait || !running)
            return;
        wait = newWait;
        if(!wait && waiting) {
            waiting = false;
            thread.interrupt();
        }
    }

    public void reset() {
        for (CPU core : cores) {
            core.reset();
        }
    }
}
