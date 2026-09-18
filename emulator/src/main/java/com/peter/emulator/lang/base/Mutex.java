package com.peter.emulator.lang.base;

import com.peter.emulator.lang.ELClass;
import com.peter.emulator.lang.ELFunction;
import com.peter.emulator.lang.ELProtectionLevel;
import com.peter.emulator.lang.Location;
import com.peter.emulator.lang.ProgramModule;
import com.peter.emulator.lang.ProgramUnit;
import com.peter.emulator.lang.actions.DirectAction;
import com.peter.emulator.lang.doc.DocComment;
import com.peter.emulator.lang.ELFunction.FunctionType;

public class Mutex extends ELClass {

    public Mutex(ProgramModule module) {
        super("Mutex", null, new ProgramUnit(module, "<Mutex>"));

        ELFunction acquireFunction = new ELFunction(ELProtectionLevel.PUBLIC, false, this, "acquire",
                FunctionType.INSTANCE,
                false, unit, new Location("<Mutex>:acquire", 1, 1));
        addFunction(acquireFunction);
        acquireFunction.actions.add(new DirectAction("#line <Mutex>:acquire 1:1"));
        acquireFunction.actions.add(new DirectAction(":Mutex.acquire_loop"));
        acquireFunction.actions.add(new DirectAction("TEST AND SET r1 r0"));
        acquireFunction.actions.add(new DirectAction("GOTO NEQ r1 :Mutex.acquire_loop"));
        acquireFunction.actions.add(new DirectAction("#lineend"));
        acquireFunction.doc = new DocComment("Attempts to acquire the mutex and will live wait until possible.");

        ELFunction tryFunction = new ELFunction(ELProtectionLevel.PUBLIC, false, this, "try", FunctionType.INSTANCE,
                false, unit, new Location("<Mutex>:try", 1, 1));
        tryFunction.ret = ELPrimitives.BOOL;
        addFunction(tryFunction);
        tryFunction.actions.add(new DirectAction("#line <Mutex>:try 1:1"));
        tryFunction.actions.add(new DirectAction("TEST AND SET r1 r0"));
        tryFunction.actions.add(new DirectAction("SUB r2 rStack 8"));
        tryFunction.actions.add(new DirectAction("SET FORCE EQ r1 r1"));
        tryFunction.actions.add(new DirectAction("STORE r1 r2"));
        tryFunction.actions.add(new DirectAction("#lineend"));
        tryFunction.doc = new DocComment("Attempts to acquire the mutex. Returns `true` if acquisition was successful otherwise returns `false`");

        ELFunction releaseFunction = new ELFunction(ELProtectionLevel.PUBLIC, false, this, "release",
                FunctionType.INSTANCE,
                false, unit, new Location("<Mutex>:release", 1, 1));
        addFunction(releaseFunction);
        releaseFunction.actions.add(new DirectAction("#line <Mutex>:release 1:1"));
        releaseFunction.actions.add(new DirectAction("STORE BYTE 0x0 r0"));
        releaseFunction.actions.add(new DirectAction("#lineend"));
        releaseFunction.doc = new DocComment("Releases the mutex. **Only call if you know you have the mutex right now**");
    }

    @Override
    public int getSize() {
        return 1;
    }

}
