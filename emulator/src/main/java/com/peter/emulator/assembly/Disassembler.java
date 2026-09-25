package com.peter.emulator.assembly;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;

import com.peter.emulator.machinecode.Instruction;

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

    public Disassembler(Path inPath, Path outPath) throws IOException {
        inStream = Files.newInputStream(inPath);
        if (!hasWord()) {
            inStream.close();
            return;
        }
        int v1 = readWord();
        outWriter = Files.newBufferedWriter(outPath, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
        while (hasWord()) {
            int v2 = readWord();
            Instruction instr = Instruction.fromBytecode(v1, v2);
            if (instr != null) {
                outWriter.write(instr.getASM() + "\n");
                if (instr.hasSecond()) {
                    if (!hasWord()) {
                        break;
                    }
                    v1 = readWord();
                } else {
                    v1 = v2;
                }
            } else {
                outWriter.write(Instruction.toHexLead(v1) + "\n");
                v1 = v2;
            }
        }
        inStream.close();
        outWriter.close();
    }
}
