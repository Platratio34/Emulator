package com.peter.emulator.assembly;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.HashMap;

import com.peter.emulator.machinecode.ConditionalOperator;
import com.peter.emulator.machinecode.Goto;
import com.peter.emulator.machinecode.Instruction;
import com.peter.emulator.machinecode.Load;
import com.peter.emulator.machinecode.Goto.Mode;

public class Disassembler {

    private InputStream inStream;
    private BufferedWriter outWriter;

    private int readWord() throws IOException {
        int w = inStream.read() << 24;
        w |= inStream.read() << 16;
        w |= inStream.read() << 8;
        w |= inStream.read();
        return w;
    }

    private boolean hasWord() throws IOException {
        return inStream.available() >= 4;
    }

    protected static record Line(int addr, String line) {
        protected static Line format(int addr, String line, Object... args) {
            return new Line(addr, String.format(line, args));
        }
    };

    public Disassembler(Path inPath, Path outPath) throws IOException {
        inStream = Files.newInputStream(inPath);
        if (!hasWord()) {
            inStream.close();
            return;
        }
        int v1 = readWord();
        HashMap<Integer, Define> defines = new HashMap<>();
        ArrayList<Define> defOrder = new ArrayList<>();
        HashMap<Integer, String> labels = new HashMap<>();
        int defI = 0;
        int lblI = 0;
        int cAddr = 0;
        ArrayList<Line> lines = new ArrayList<>();
        while (hasWord()) {
            int v2 = readWord();
            Instruction instr = Instruction.fromBytecode(v1, v2);
            if (instr != null) {
                cAddr += instr.hasSecond() ? 8 : 4;
                switch (instr) {
                    case Load li -> {
                        switch (li.mode) {
                            case LITERAL -> {
                                if (li.data == 0) {
                                    lines.add(Line.format(cAddr, "LOAD %s 0", li.rg));
                                } else {
                                    if (!defines.containsKey(li.data)) {
                                        String defName = "def_" + (defI++);
                                        Define def = new Define(defName, li.data);
                                        defOrder.add(def);
                                        defines.put(li.data, def);
                                    }
                                    lines.add(Line.format(cAddr, "LOAD %s %s", li.rg, defines.get(li.data).name));
                                }
                            }
                            case LITERAL_ADDRESS -> {
                                if (!defines.containsKey(li.data)) {
                                    String defName = "def_" + (defI++);
                                    Define def = new Define(defName, li.data);
                                    defOrder.add(def);
                                    defines.put(li.data, def);
                                }
                                lines.add(Line.format(cAddr, "LOAD MEM %s %s %s", li.size, li.rg, defines.get(li.data).name));
                            }
                            default -> {
                                lines.add(new Line(cAddr, instr.getASM()));
                            }
                        }
                    }
                    case Goto gi -> {
                        String out = "GOTO ";
                        switch(gi.mode) {
                            case PUSH, POP -> {out += gi.mode + " ";}
                            default -> {}
                        }
                        if (gi.condition != ConditionalOperator.UNCONDITIONAL) {
                            out += gi.condition.name().replace("_ZERO", "") + " " + gi.rg + " ";
                        }
                        if (gi.rel) {
                            int tAddr = cAddr + gi.data;
                            if (!labels.containsKey(tAddr)) {
                                String lblName = ":lbl_" + (lblI++);
                                labels.put(tAddr, lblName);
                            }
                            out += labels.get(tAddr) + " // "+gi.data;
                        } else if(gi.mode != Mode.POP) {
                            out += gi.ra;
                        }
                        lines.add(new Line(cAddr, out));
                    }
                    default -> {
                        lines.add(new Line(cAddr, instr.getASM()));
                    }
                }
                if (instr.hasSecond()) {
                    if (!hasWord()) {
                        break;
                    }
                    v1 = readWord();
                } else {
                    v1 = v2;
                }
            } else {
                cAddr += 4;
                lines.add(new Line(cAddr, Instruction.toHexLead(v1)));
                v1 = v2;
            }
        }
        inStream.close();
        
        outWriter = Files.newBufferedWriter(outPath, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);

        for (Define d : defOrder) {
            outWriter.write(String.format("#define %s %s\n", d.name, Instruction.toHexLead(d.value)));
        }

        for (Line l : lines) {
            if(labels.containsKey(l.addr)) {
                outWriter.write(labels.get(l.addr) + "\n");
            }
            outWriter.write(l.line + "\n");
        }

        outWriter.close();
    }
}
