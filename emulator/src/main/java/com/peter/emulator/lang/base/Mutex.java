package com.peter.emulator.lang.base;

import com.peter.emulator.lang.ELClass;
import com.peter.emulator.lang.ELFunction;
import com.peter.emulator.lang.ELProtectionLevel;
import com.peter.emulator.lang.InlineType;
import com.peter.emulator.lang.Location;
import com.peter.emulator.lang.ProgramModule;
import com.peter.emulator.lang.ProgramUnit;
import com.peter.emulator.lang.doc.DocComment;
import com.peter.emulator.lang.ELFunction.FunctionType;

public class Mutex extends ELClass {

    public Mutex(ProgramModule module) {
        super("Mutex", new Location("<Mutex>",1,1).span(), null, new ProgramUnit(module, "<Mutex>"));

        ELFunction acquireFunction = new ELFunction(ELProtectionLevel.PUBLIC, false, this, "acquire",
                FunctionType.INSTANCE,
                InlineType.INLINE_RAW, unit, new Location("<Mutex>:acquire", 1, 1));
        addFunction(acquireFunction);
        // acquireFunction.actions.addDirect("#line <Mutex>:acquire 1:1");
        acquireFunction.actions.addDirect(":Mutex.acquire_loop_$i1");
        acquireFunction.actions.addDirect("TEST AND SET $r1 this");
        acquireFunction.actions.addDirect("GOTO NEQ $r1 :Mutex.acquire_loop_$i1");
        // acquireFunction.actions.addDirect("#lineend");
        acquireFunction.doc = new DocComment("Attempts to acquire the mutex and will live wait until possible.");

        ELFunction tryFunction = new ELFunction(ELProtectionLevel.PUBLIC, false, this, "try", FunctionType.INSTANCE,
                InlineType.INLINE_RAW, unit, new Location("<Mutex>:try", 1, 1));
        tryFunction.ret = ELPrimitives.BOOL;
        addFunction(tryFunction);
        // tryFunction.actions.addDirect("#line <Mutex>:try 1:1");
        tryFunction.actions.addDirect("TEST AND SET ret this");
        // tryFunction.actions.addDirect("SUB r2 rStack 8");
        tryFunction.actions.addDirect("SET FORCE EQ ret ret");
        // tryFunction.actions.addDirect("STORE r1 r2");
        // tryFunction.actions.addDirect("#lineend");
        tryFunction.doc = new DocComment("Attempts to acquire the mutex. Returns `true` if acquisition was successful otherwise returns `false`");

        ELFunction releaseFunction = new ELFunction(ELProtectionLevel.PUBLIC, false, this, "release",
                FunctionType.INSTANCE,
                InlineType.INLINE_RAW, unit, new Location("<Mutex>:release", 1, 1));
        addFunction(releaseFunction);
        // releaseFunction.actions.addDirect("#line <Mutex>:release 1:1");
        releaseFunction.actions.addDirect("STORE BYTE 0x0 this");
        // releaseFunction.actions.addDirect("#lineend");
        releaseFunction.doc = new DocComment("Releases the mutex. **Only call if you know you have the mutex right now**");
    }

    @Override
    public int getSize() {
        return 1;
    }

}
