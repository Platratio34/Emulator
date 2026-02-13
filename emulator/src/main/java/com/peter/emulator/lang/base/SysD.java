package com.peter.emulator.lang.base;

import com.peter.emulator.lang.ELFunction.FunctionType;
import com.peter.emulator.lang.*;

public class SysD extends Namespace {

    private static final Location SYSD_LOCATION = new Location("<SysD>", 1, 1);

    private final ProgramUnit unit;

    public SysD(ProgramModule module) {
        super("SysD");
        unit = new ProgramUnit(module, "<SysD>");
        // void memSet(uint32 address, uint32 value)
        ELFunction memSet = addStaticFunction(new ELFunction(ELProtectionLevel.PUBLIC, true, this, "memSet", FunctionType.STATIC, true, unit, SYSD_LOCATION));
        memSet.addParameter(ELPrimitives.UINT32, "address");
        memSet.addParameter(ELPrimitives.UINT32, "value");
        // void memSet(uint32 address, char value)
        ELFunction memSet2 = addStaticFunction(new ELFunction(ELProtectionLevel.PUBLIC, true, this, "memSet", FunctionType.STATIC, true, unit, SYSD_LOCATION));
        memSet2.addParameter(ELPrimitives.UINT32, "address");
        memSet2.addParameter(ELPrimitives.CHAR, "value");
        // uint32 memGet(uint32 address)
        ELFunction memGet = addStaticFunction(new ELFunction(ELProtectionLevel.PUBLIC, true, this, "memGet", FunctionType.STATIC, true, unit, SYSD_LOCATION));
        memGet.addParameter(ELPrimitives.UINT32, "address");
        memGet.ret = ELPrimitives.UINT32;
        // void memCopy(void* src, uint32 start, uint32 end, void* dest, uint32 destStart);
        ELFunction memCopy = addStaticFunction(new ELFunction(ELProtectionLevel.PUBLIC, true, this, "memCopy", FunctionType.STATIC, true, unit, SYSD_LOCATION));
        memCopy.addParameter(ELPrimitives.VOID_PTR, "src");
        memCopy.addParameter(ELPrimitives.UINT32, "start");
        memCopy.addParameter(ELPrimitives.UINT32, "end");
        memCopy.addParameter(ELPrimitives.VOID_PTR, "dest");
        memCopy.addParameter(ELPrimitives.UINT32, "destStart");
        // void <T> memCopy(T* src, uint32 start, uint32 end, T* dest, uint32 start);
        // boolean <T> memEquals(T* a, T* b, uint32 length);
        ELFunction memEquals = addStaticFunction(new ELFunction(ELProtectionLevel.PUBLIC, true, this, "memEquals", FunctionType.STATIC, true, unit, SYSD_LOCATION));
        memEquals.addParameter(ELPrimitives.VOID_PTR, "a");
        memEquals.addParameter(ELPrimitives.VOID_PTR, "b");
        memEquals.addParameter(ELPrimitives.UINT32, "length");
        memEquals.ret = ELPrimitives.BOOL;

        // void* sysCall(uint32 call)
        ELFunction sysCall = addStaticFunction(new ELFunction(ELProtectionLevel.PUBLIC, true, this, "sysCall", FunctionType.STATIC, true, unit, SYSD_LOCATION));
        sysCall.addParameter(ELPrimitives.UINT32, "call");
        sysCall.ret = ELPrimitives.VOID_PTR;

        // uint32 getPID()
        ELFunction getPID = addStaticFunction(new ELFunction(ELProtectionLevel.PUBLIC, true, this, "getPID", FunctionType.STATIC, true, unit, SYSD_LOCATION));
        getPID.ret = ELPrimitives.UINT32;

        // const uint32 MEMORY_DEVICE_START = 0x1_0000;
        addStaticVariable(new ELVariable(ELProtectionLevel.PUBLIC, ELVariable.Type.STATIC, ELPrimitives.UINT32, "MEMORY_DEVICE_START", true, this, unit, SYSD_LOCATION).setValue(0x1_0000));
        // const uint32 MEMORY_PROCESS_START = 0x2_0000;
        addStaticVariable(new ELVariable(ELProtectionLevel.PUBLIC, ELVariable.Type.STATIC, ELPrimitives.UINT32, "MEMORY_PROCESS_START", true, this, unit, SYSD_LOCATION).setValue(0x2_0000));
        // const uint32 MEMORY_BLOCK_SIZE = 0x8000;
        addStaticVariable(new ELVariable(ELProtectionLevel.PUBLIC, ELVariable.Type.STATIC, ELPrimitives.UINT32, "MEMORY_BLOCK_SIZE", true, this, unit, SYSD_LOCATION).setValue(0x8000));

        // const uint32 REG_PGM_PNTR = 0xf0;
        addStaticVariable(new ELVariable(ELProtectionLevel.PUBLIC, ELVariable.Type.STATIC, ELPrimitives.UINT32, "REG_PGM_PNTR", true, this, unit, SYSD_LOCATION).setValue(0xf0));
        // const uint32 REG_STACK_PNTR = 0xf1;
        addStaticVariable(new ELVariable(ELProtectionLevel.PUBLIC, ELVariable.Type.STATIC, ELPrimitives.UINT32, "REG_STACK_PNTR", true, this, unit, SYSD_LOCATION).setValue(0xf1));
        // const uint32 REG_PID = 0xf8;
        addStaticVariable(new ELVariable(ELProtectionLevel.PUBLIC, ELVariable.Type.STATIC, ELPrimitives.UINT32, "REG_PID", true, this, unit, SYSD_LOCATION).setValue(0xf8));
        // const uint32 REG_MEM_TABLE = 0xf9;
        addStaticVariable(new ELVariable(ELProtectionLevel.PUBLIC, ELVariable.Type.STATIC, ELPrimitives.UINT32, "REG_MEM_TABLE", true, this, unit, SYSD_LOCATION).setValue(0xf9));
        // const uint32 REG_PRIVILEGED_MODE = 0xff;
        addStaticVariable(new ELVariable(ELProtectionLevel.PUBLIC, ELVariable.Type.STATIC, ELPrimitives.UINT32, "REG_PRIVILEGED_MODE", true, this, unit, SYSD_LOCATION).setValue(0xff));

        /*
        struct AddressSpace {
            public uint32 addressOffset;
            public uint32 pid;
            public uint8 type;
            public uint8 state;
        }
         */
        ELStruct AddressSpace = new ELStruct("AddressSpace", this, unit);
        AddressSpace.addMember(new ELVariable(ELProtectionLevel.PUBLIC, ELVariable.Type.MEMBER, ELPrimitives.UINT32, "addressOffset", false, this, unit, SYSD_LOCATION));
        AddressSpace.addMember(new ELVariable(ELProtectionLevel.PUBLIC, ELVariable.Type.MEMBER, ELPrimitives.UINT32, "pid", false, this, unit, SYSD_LOCATION));
        AddressSpace.addMember(new ELVariable(ELProtectionLevel.PUBLIC, ELVariable.Type.MEMBER, ELPrimitives.UINT8, "type", false, this, unit, SYSD_LOCATION));
        AddressSpace.addMember(
                new ELVariable(ELProtectionLevel.PUBLIC, ELVariable.Type.MEMBER, ELPrimitives.UINT8, "state", false, this, unit, SYSD_LOCATION));
        
        /*
        struct PeripheralDescriptorShort {
            const uint32 deviceID;
            const uint32 deviceType;
        }
         */
        ELStruct PeripheralDescriptorShort = new ELStruct("PeripheralDescriptorShort", this, unit);
        PeripheralDescriptorShort.addMember(new ELVariable(ELProtectionLevel.PUBLIC, ELVariable.Type.MEMBER, ELPrimitives.UINT32, "id", true, this, unit, SYSD_LOCATION));
        PeripheralDescriptorShort.addMember(
                new ELVariable(ELProtectionLevel.PUBLIC, ELVariable.Type.MEMBER, ELPrimitives.UINT32, "type", true, this, unit, SYSD_LOCATION));
        
        /*
        struct PeripheralDescriptor {
            const uint32 id;
            const uint32 type;
            const uint32[4] manufacturer;
            const uint32[4] serial;
            const uint32[6] data;
        }
         */
        ELStruct PeripheralDescriptor = new ELStruct("PeripheralDescriptor", this, unit);
        PeripheralDescriptor.addMember(new ELVariable(ELProtectionLevel.PUBLIC, ELVariable.Type.MEMBER, ELPrimitives.UINT32, "id", true, this, unit, SYSD_LOCATION));
        PeripheralDescriptor.addMember(new ELVariable(ELProtectionLevel.PUBLIC, ELVariable.Type.MEMBER, ELPrimitives.UINT32, "type", true, this, unit, SYSD_LOCATION));
        PeripheralDescriptor.addMember(new ELVariable(ELProtectionLevel.PUBLIC, ELVariable.Type.MEMBER, ELPrimitives.UINT32.builder().array(4).build(), "manufacturer", true, this, unit, SYSD_LOCATION));
        PeripheralDescriptor.addMember(new ELVariable(ELProtectionLevel.PUBLIC, ELVariable.Type.MEMBER, ELPrimitives.UINT32.builder().array(4).build(), "serial", true, this, unit, SYSD_LOCATION));
        PeripheralDescriptor.addMember(new ELVariable(ELProtectionLevel.PUBLIC, ELVariable.Type.MEMBER, ELPrimitives.UINT32.builder().array(6).build(), "data", true, this, unit, SYSD_LOCATION));
    }

}
