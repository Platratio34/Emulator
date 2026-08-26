package com.peter.emulator.lang.base;

import com.peter.emulator.lang.ELFunction.FunctionType;
import com.peter.emulator.lang.*;
import com.peter.emulator.lang.tokens.IdentifierToken;

public class SysD extends Namespace {

    protected static final Location SYSD_LOCATION = new Location("<SysD>", 1, 1);

    protected final ProgramUnit unit;

    public SysD(ProgramModule module) {
        super("SysD");
        unit = new ProgramUnit(module, "<SysD>");
        // void memSet(int32 address, int32 value)
        ELFunction memSet = addStaticFunction(new ELFunction(ELProtectionLevel.PUBLIC, true, this, "memSet", FunctionType.STATIC, true, unit, SYSD_LOCATION));
        memSet.addParameter(ELPrimitives.INT32, "address");
        memSet.addParameter(ELPrimitives.INT32, "value");
        // void memSet(int32 address, char value)
        ELFunction memSet2 = addStaticFunction(new ELFunction(ELProtectionLevel.PUBLIC, true, this, "memSet", FunctionType.STATIC, true, unit, SYSD_LOCATION));
        memSet2.addParameter(ELPrimitives.INT32, "address");
        memSet2.addParameter(ELPrimitives.CHAR, "value");
        // int32 memGet(int32 address)
        ELFunction memGet = addStaticFunction(new ELFunction(ELProtectionLevel.PUBLIC, true, this, "memGet", FunctionType.STATIC, true, unit, SYSD_LOCATION));
        memGet.addParameter(ELPrimitives.INT32, "address");
        memGet.ret = ELPrimitives.INT32;
        // void memCopy(void* src, int32 start, int32 end, void* dest, int32 destStart);
        ELFunction memCopy = addStaticFunction(new ELFunction(ELProtectionLevel.PUBLIC, true, this, "memCopy", FunctionType.STATIC, true, unit, SYSD_LOCATION));
        memCopy.addParameter(ELPrimitives.VOID_PTR, "src");
        memCopy.addParameter(ELPrimitives.INT32, "start");
        memCopy.addParameter(ELPrimitives.INT32, "end");
        memCopy.addParameter(ELPrimitives.VOID_PTR, "dest");
        memCopy.addParameter(ELPrimitives.INT32, "destStart");
        // void <T> memCopy(T* src, int32 start, int32 end, T* dest, int32 start);
        // boolean <T> memEquals(T* a, T* b, int32 length);
        ELFunction memEquals = addStaticFunction(new ELFunction(ELProtectionLevel.PUBLIC, true, this, "memEquals", FunctionType.STATIC, true, unit, SYSD_LOCATION));
        memEquals.addParameter(ELPrimitives.VOID_PTR, "a");
        memEquals.addParameter(ELPrimitives.VOID_PTR, "b");
        memEquals.addParameter(ELPrimitives.INT32, "length");
        memEquals.ret = ELPrimitives.BOOL;

        // void* sysCall(int32 call)
        ELFunction sysCall = addStaticFunction(new ELFunction(ELProtectionLevel.PUBLIC, true, this, "sysCall", FunctionType.STATIC, true, unit, SYSD_LOCATION));
        sysCall.addParameter(ELPrimitives.INT32, "call");
        sysCall.ret = ELPrimitives.VOID_PTR;

        // int32 getPID()
        ELFunction getPID = addStaticFunction(new ELFunction(ELProtectionLevel.PUBLIC, true, this, "getPID", FunctionType.STATIC, true, unit, SYSD_LOCATION));
        getPID.ret = ELPrimitives.INT32;

        // const int32 MEMORY_DEVICE_START = 0x1_0000;
        addStaticVariable(new ELVariable(ELProtectionLevel.PUBLIC, ELVariable.Type.CONST, ELPrimitives.INT32, "MEMORY_DEVICE_START", true, this, unit, SYSD_LOCATION).setValue(0x1_0000));
        // const int32 MEMORY_PROCESS_START = 0x2_0000;
        addStaticVariable(new ELVariable(ELProtectionLevel.PUBLIC, ELVariable.Type.CONST, ELPrimitives.INT32, "MEMORY_PROCESS_START", true, this, unit, SYSD_LOCATION).setValue(0x2_0000));
        // const int32 MEMORY_BLOCK_SIZE = 0x8000;
        addStaticVariable(new ELVariable(ELProtectionLevel.PUBLIC, ELVariable.Type.CONST, ELPrimitives.INT32, "MEMORY_BLOCK_SIZE", true, this, unit, SYSD_LOCATION).setValue(0x8000));

        /*
        struct AddressSpace {
            public int32 addressOffset;
            public int32 pid;
            public uint8 type;
            public uint8 state;
        }
         */
        ELStruct AddressSpace = new ELStruct("AddressSpace", this, unit);
        AddressSpace.addMember(new ELVariable(ELProtectionLevel.PUBLIC, ELVariable.Type.MEMBER, ELPrimitives.INT32, "addressOffset", false, this, unit, SYSD_LOCATION));
        AddressSpace.addMember(new ELVariable(ELProtectionLevel.PUBLIC, ELVariable.Type.MEMBER, ELPrimitives.INT32, "pid", false, this, unit, SYSD_LOCATION));
        AddressSpace.addMember(new ELVariable(ELProtectionLevel.PUBLIC, ELVariable.Type.MEMBER, ELPrimitives.UINT8, "type", false, this, unit, SYSD_LOCATION));
        AddressSpace.addMember(
                new ELVariable(ELProtectionLevel.PUBLIC, ELVariable.Type.MEMBER, ELPrimitives.UINT8, "state", false, this, unit, SYSD_LOCATION));
    }

    public static ELType getVarType(IdentifierToken it) {
        switch (it.value) {
            case "rPgm", "rStack", "rMTbl", "rIH", "rPgmI", "rStackI", "rMTblI" -> {
                return ELPrimitives.VOID_PTR;
            }
            case "rAF", "rPID", "rIC", "rID", "rAFI", "rPIDI" -> {
                return ELPrimitives.INT32;
            }
            case "rPM", "rPMI" -> {
                return ELPrimitives.BOOL;
            }
            default -> {
                if (it.value.matches("r\\d\\d?I?")) {
                    return ELPrimitives.INT32;
                }
            }
        }
        return null;
    }
    
    public static ProgramModule newSysD(LanguageServer languageServer) {
        ProgramModule module = new ProgramModule("SysD", languageServer);
        module.addNamespace(new SysD(module));
        module.addNamespace(new Peripheral(module));
        return module;
    }
}
