package com.peter;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;

import com.peter.emulator.Emulator;
import com.peter.emulator.assembly.Assembler;
import com.peter.emulator.debug.Debugger;
import com.peter.emulator.lang.ELAnalysisError;
import com.peter.emulator.lang.ELAnalysisError.Severity;
import com.peter.emulator.lang.LanguageServer;
import com.peter.emulator.lang.Namespace;
import com.peter.emulator.lang.ProgramModule;

public class Main {

    public static final Path ROOT_PATH = Path.of("run");

    public static void main(String[] args) {
        LanguageServer ls = new LanguageServer();

        // ProgramModule kernal = ls.addModule("Kernal");
        // kernal.addRefModule("SysD");
        // kernal.addFiles(ROOT_PATH.resolve("lang/Kernal"));

        // ProgramModule system = ls.addModule("System");
        // system.addRefModule("SysD");
        // system.addRefModule("Kernal");
        // system.addFiles(ROOT_PATH.resolve("lang/System"));
        
        // ProgramModule testMod = ls.addModule("Test");
        // testMod.addRefModule("SysD");
        // testMod.addRefModule("Kernal");
        // testMod.addRefModule("System");
        // testMod.addFiles(ROOT_PATH.resolve("lang/Test"));
        
        ProgramModule testD = ls.addModule("TestD");
        testD.addRefModule("SysD");
        testD.addFiles(ROOT_PATH.resolve("lang/TestD"));

        System.out.println("\nParsing:");
        for (ELAnalysisError err : ls.parse()) {
            if (!err.severity.atLeast(Severity.WARNING))
                continue;
            System.out.println("- " + err);
        }
        // System.out.println("Kernal:");
        // for(Namespace ns : kernal.getNamespaces())
        //     System.out.println(ns.debugString());
        // System.out.println("System:");
        // for(Namespace ns : system.getNamespaces())
        //     System.out.println(ns.debugString());
        System.out.println("TestD:");
        for(Namespace ns : testD.getNamespaces())
            System.out.println(ns.debugString());

        System.out.println("\nResolution:");
        for (ELAnalysisError err : ls.resolve()) {
            if (!err.severity.atLeast(Severity.WARNING))
                continue;
            System.out.println("- " + err);
        }
        
        System.out.println("\nAnalysis:");
        for (ELAnalysisError err : ls.analyze()) {
            if (!err.severity.atLeast(Severity.WARNING))
                continue;
            System.out.println("- "+err);
        }
        if(ls.clearError())
            return;

        System.out.println("\n");
        String asm = testD.assemble();
        // System.out.println(asm);
        Path p = ROOT_PATH.resolve("testd.asm");
        try {
            new File(p.toUri()).delete();
            Files.writeString(p, asm, StandardOpenOption.WRITE, StandardOpenOption.CREATE);
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
            return;
        }


        // Tokenizer tokenizer = new Tokenizer(readFile("lang/kernal.el"), 1, 1);
        // error = tokenizer.tokenize();
        // if (error.isPresent()) {
        //     Tokenizer.printTokens(tokenizer.tokens);
        //     System.out.println("Error tokenizing: " + error.get());
        //     return;
        // }
        // Tokenizer.printTokens(tokenizer.tokens);
        // Parser parser = new Parser();
        // error = parser.parse(tokenizer.tokens);
        // if (error.isPresent()) {
        //     System.out.println("Error parsing: " + error.get());
        //     return;
        // }
        // System.out.println(parser.currentNamespace.debugString());
        // for (Entry<String, Identifier> entry : parser.currentNamespace.identifiers.entrySet()) {
        //     System.out.println(entry.getValue().debugString(entry.getKey()));
        // }

        
        Emulator emulator = new Emulator();
        
        /*
        KernalBuilder kernalBuilder = new KernalBuilder();
        try {
            int[] kernal = kernalBuilder.build();
            if (kernal.length == 0)
                throw new IOException("Error building kernal");
            emulator.ram.copy(kernal, 0);
            emulator.ram.copy(kernalBuilder.sysCallTable(), 0x1_0000);
            
            try {
                Files.write(ROOT_PATH.resolve("bin/kernal.bin"), kernalBuilder.binary(), StandardOpenOption.CREATE, StandardOpenOption.WRITE );
                Files.writeString(ROOT_PATH.resolve("obj/kernal.obj"), kernalBuilder.getSymbols().toFileKernal(),
                        StandardOpenOption.CREATE, StandardOpenOption.WRITE);
            } catch (IOException e) {
                e.printStackTrace();
            }
            
            Assembler programAssembler = new Assembler();
            programAssembler.setSource(ROOT_PATH.resolve("kernalTest.asm"));
            if(!programAssembler.assemble()) {
                System.err.println(String.format("%d error(s) assembling program:", programAssembler.errors.size()));
                for (AssemblerError err : programAssembler.errors) {
                    System.err.println(err.getPrintString());
                }
                throw new IOException("Error building program");
            }
        
            Debugger programDebugger = new Debugger(kernalBuilder.getSymbols(), programAssembler.symbols);
            emulator.cores[0].debugger = programDebugger;
            emulator.ram.copy(programAssembler.build(), 0x2_0000);
        
        } catch (IOException e) {
            e.printStackTrace();
            return;
        }
        */
        Assembler assembler = new Assembler();
        try {
            assembler.setSource(p);
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
            return;
        }
        if (!assembler.assemble()) {
            System.err.println("Error assembling");
            return;
        }
        try {
            Files.writeString(ROOT_PATH.resolve("obj/testd.obj"), assembler.symbols.toFile(), StandardOpenOption.WRITE, StandardOpenOption.CREATE);
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        emulator.ram.copy(assembler.build());
        emulator.cores[0].debugger = new Debugger(assembler.symbols, assembler.symbols);
        
        // Assembler assembler = new Assembler();
        // try {
        //     assembler.setSource(ROOT_PATH.resolve("test3.asm"));
        // } catch (IOException e) {
        //     e.printStackTrace();
        //     return;
        // }
        // assembler.assemble();
        // int[] pgm = assembler.build();
        // try {
        //     Files.write(ROOT_PATH.resolve("bin/test3.bin"), Assembler.toBytes(pgm), StandardOpenOption.CREATE);
        // } catch (IOException e) {
        //     e.printStackTrace();
        // }
        // emulator.setProgram(pgm);
        emulator.run();
        
        while (emulator.isRunning() && emulator.cores[0].running) {
            try {
                Thread.sleep(10);
            } catch (InterruptedException e) {
                e.printStackTrace();
                break;
            }
        }

        System.err.println("\n");
        System.out.println(emulator.cores[0].dump());
        
        System.out.println("\n\n+----------+\n| MEM DUMP |\n+----------+");
        System.out.println("Kernal");
        System.out.println(emulator.ram.debugPrint(0x0000, 8));
        System.out.println(String.format("Stack (Pointer: 0x%x)", emulator.cores[0].stackPtr));
        System.out.println(emulator.ram.debugPrint(0x1000, 2));
        System.out.println("Syscall table");
        System.out.println(emulator.ram.debugPrint(0x1_0000, 8));
        System.out.println();
        
        System.out.println("Console");
        System.out.println(emulator.ram.debugPrint(0x0800, 3));
        System.out.println("Peripheral Manager");
        System.out.println(emulator.ram.debugPrint(0x8000, 2));
        System.out.println("Heap");
        System.out.println(emulator.ram.debugPrint(0x9000, 4));
        
        emulator.stop();
        
    }
    
    public static String readFile(String path) {
        try {
            return Files.readString(ROOT_PATH.resolve(path));
        } catch (IOException e) {
            return "";
        }
    }
}