package com.peter.emulator;

import com.peter.emulator.components.MMU;
import com.peter.emulator.components.RAM;
import com.peter.emulator.gui.EmulatorGui;
import com.peter.emulator.peripherals.ConsolePeripheral;
import com.peter.emulator.peripherals.PeripheralManager;
import com.peter.emulator.peripherals.StoragePeripheral;

public class Emulator {

    public final RAM ram = new RAM();
    public final MMU mmu = new MMU();
    public float tickSpeed = 60;

    public final CPU[] cores = new CPU[] {
        new CPU(ram, mmu)
    };
    public PeripheralManager peripheralManager = new PeripheralManager(ram);
    public final EmulatorGui gui;

    public Emulator() {
        peripheralManager.addPeripheral(new ConsolePeripheral());
        peripheralManager.addPeripheral(new StoragePeripheral());
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            stop();
        }));
        gui = new EmulatorGui(this);
    }

    protected void tick() {
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

    public void setProgram(int[] data) {
        setProgram(data, 0);
    }

    public void setProgram(int[] data, int position) {
        ram.copy(data, position);
        cores[0].setPtr(position);
        cores[0].running = true;
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
        if(!wait) {
            waiting = false;
            thread.interrupt();
        }
    }
}
