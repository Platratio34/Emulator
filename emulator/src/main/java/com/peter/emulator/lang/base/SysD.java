package com.peter.emulator.lang.base;

import com.peter.emulator.lang.ELFunction.FunctionType;
import com.peter.emulator.lang.actions.ActionScope;
import com.peter.emulator.lang.actions.Register;
import com.peter.emulator.lang.doc.DocComment;
import com.peter.emulator.lang.*;
import com.peter.emulator.lang.tokens.IdentifierToken;
import com.peter.emulator.machinecode.Reg;

public class SysD extends Namespace {

    protected static final Location SYSD_LOCATION = new Location("<SysD>", 1, 1);

    protected final ProgramUnit unit;

    public SysD(ProgramModule module) {
        super("SysD");
        unit = new ProgramUnit(module, "<SysD>");
        // void memCopy(void* src, int32 start, int32 end, void* dest, int32 destStart);
        ELFunction memCopy = addStaticFunction(new ELFunction(ELProtectionLevel.PUBLIC, false, this, "memCopy", FunctionType.STATIC, InlineType.INLINE_RAW, unit, SYSD_LOCATION));
        memCopy.addParameter(ELPrimitives.VOID_PTR, "src");
        memCopy.addParameter(ELPrimitives.INT32, "start");
        memCopy.addParameter(ELPrimitives.INT32, "end");
        memCopy.addParameter(ELPrimitives.VOID_PTR, "dest");
        memCopy.addParameter(ELPrimitives.INT32, "destStart");
        memCopy.actions.addDirect("ADD src src start");
        memCopy.actions.addDirect("ADD dest dest destStart");
        memCopy.actions.addDirect("SUB end end start");
        memCopy.actions.addDirect(":SysD.memCopy_loop_$i1");
        memCopy.actions.addDirect("COPY MEM src dest INC_RS INC_RD");
        memCopy.actions.addDirect("INC end -1");
        memCopy.actions.addDirect("GOTO GT end :SysD.memCopy_loop_$i1");

        // void <T> memCopy(T* src, int32 start, int32 end, T* dest, int32 start);
        // boolean <T> memEquals(T* a, T* b, int32 length);
        ELFunction memEquals = addStaticFunction(new ELFunction(ELProtectionLevel.PUBLIC, true, this, "memEquals", FunctionType.STATIC, InlineType.INLINE_RAW, unit, SYSD_LOCATION));
        memEquals.addParameter(ELPrimitives.VOID_PTR, "a");
        memEquals.addParameter(ELPrimitives.VOID_PTR, "b");
        memEquals.addParameter(ELPrimitives.INT32, "length");
        memEquals.ret = ELPrimitives.BOOL;
        memEquals.actions.addDirect("LOAD ret 0x1");
        memEquals.actions.addDirect(":SysD.memEquals_loop_$i1");
        memEquals.actions.addDirect("GOTO LEQ length :SysD.memEquals_exit_$i1");
        memEquals.actions.addDirect("INC length -1");
        memEquals.actions.addDirect("LOAD MEM $r1 a INC_RA");
        memEquals.actions.addDirect("LOAD MEM $r2 b INC_RA");
        memEquals.actions.addDirect("SUB $r1 $r1 $r2");
        memEquals.actions.addDirect("GOTO EQ $r1 :SysD.memEquals_loop_$i1");
        memEquals.actions.addDirect("LOAD ret 0x0");
        memEquals.actions.addDirect(":SysD.memEquals_exit_$i1");

        // void* sysCall(int32 call)
        // ELFunction sysCall = addStaticFunction(new ELFunction(ELProtectionLevel.PUBLIC, true, this, "sysCall", FunctionType.STATIC, InlineType.INLINE_RAW, unit, SYSD_LOCATION));
        // sysCall.addParameter(ELPrimitives.INT32, "call");
        // sysCall.ret = ELPrimitives.VOID_PTR;

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
        ELStruct AddressSpace = new ELStruct("AddressSpace", SYSD_LOCATION.span(), this, unit);
        AddressSpace.addMember(new ELVariable(ELProtectionLevel.PUBLIC, ELVariable.Type.MEMBER, ELPrimitives.INT32, "addressOffset", false, this, unit, SYSD_LOCATION));
        AddressSpace.addMember(new ELVariable(ELProtectionLevel.PUBLIC, ELVariable.Type.MEMBER, ELPrimitives.INT32, "pid", false, this, unit, SYSD_LOCATION));
        AddressSpace.addMember(new ELVariable(ELProtectionLevel.PUBLIC, ELVariable.Type.MEMBER, ELPrimitives.UINT8, "type", false, this, unit, SYSD_LOCATION));
        AddressSpace.addMember(
                new ELVariable(ELProtectionLevel.PUBLIC, ELVariable.Type.MEMBER, ELPrimitives.UINT8, "state", false,
                        this, unit, SYSD_LOCATION));
                
        for (Reg reg : Reg.values()) {
            if(reg == Reg.UNKNOWN)
                continue;
            ELVariable var = addStaticVariable(
                    new PseudoVariable(getRegType(reg), reg.string, this, unit, SYSD_LOCATION, new Register(new ActionScope(this,unit, null), reg.code)));
            var.doc = new DocComment(reg.description);
        }
    }

    private static ELType getRegType(Reg reg) {
        return switch (reg) {
            case UNKNOWN -> null;
            
            case PGM, STACK, SYS_TABLE, MEM_TABLE, INTERRUPT_HANDLER, PGM_I, STACK_I, MEM_TABLE_I -> ELPrimitives.VOID_PTR;
            case AF, PID, INTERRUPT_CODE, CPU_ID, AF_I, PID_I -> ELPrimitives.INT32;
            case PRIVILEGE, PRIVILEGE_I -> ELPrimitives.BOOL;

            default -> ELPrimitives.INT32;
        };
    }

    public static ELType getVarType(IdentifierToken it) {
        return getRegType(Reg.from(it.value));
    }
    
    public static ProgramModule newSysD(LanguageServer languageServer) {
        ProgramModule module = new ProgramModule("SysD", languageServer);
        module.addNamespace(new SysD(module));
        module.addNamespace(new Peripheral(module));
        module.addNamespace(new Mutex(module));
        return module;
    }
}
