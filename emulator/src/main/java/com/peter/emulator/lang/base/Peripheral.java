package com.peter.emulator.lang.base;

import com.peter.emulator.lang.ELFunction;
import com.peter.emulator.lang.ELFunction.FunctionType;
import com.peter.emulator.lang.ELProtectionLevel;
import com.peter.emulator.lang.ELStruct;
import com.peter.emulator.lang.ELVariable;
import com.peter.emulator.lang.ELVariable.Type;
import com.peter.emulator.lang.Location;
import com.peter.emulator.lang.Namespace;
import com.peter.emulator.lang.ProgramModule;
import com.peter.emulator.lang.ProgramUnit;
import com.peter.emulator.lang.actions.DirectAction;
import com.peter.emulator.peripherals.PeripheralManager;

public class Peripheral extends Namespace {

    
    protected static final Location PERIPHERAL_LOCATION = new Location("<Peripheral>", 1, 1);

    protected final ProgramUnit unit;

    public static final int TYPE_DISPLAY_CHARACTER = 0x0100_0011;
    public static final int TYPE_STORAGE_VIRTUAL = 0x0100_0001;
    public static final int TYPE_STORAGE_BLOCK = 0x0100_0002;

    public Peripheral(ProgramModule module) {
        super("Peripheral");
        unit = new ProgramUnit(module, "<Peripheral>");
        
        addStaticVariable(new ELVariable(ELProtectionLevel.PUBLIC, Type.CONST, ELPrimitives.UINT32.pointerTo(), "CMD_ADDR", false, this, unit, PERIPHERAL_LOCATION).setValue(PeripheralManager.PERIPHERAL_START));
        addStaticVariable(new ELVariable(ELProtectionLevel.PUBLIC, Type.CONST, ELPrimitives.UINT32.pointerTo(), "CMD_SIZE", false, this, unit, PERIPHERAL_LOCATION).setValue(PeripheralManager.PERIPHERAL_CMD_SIZE));
        addStaticVariable(new ELVariable(ELProtectionLevel.PUBLIC, Type.CONST, ELPrimitives.UINT32.pointerTo(), "CMD_DATA", false, this, unit, PERIPHERAL_LOCATION).setValue(PeripheralManager.PERIPHERAL_CMD_MSG));
        
        addStaticVariable(new ELVariable(ELProtectionLevel.PUBLIC, Type.CONST, ELPrimitives.UINT8.pointerTo(), "RSP_STATUS", false, this, unit, PERIPHERAL_LOCATION).setValue(PeripheralManager.PERIPHERAL_RSP_STATUS));
        addStaticVariable(new ELVariable(ELProtectionLevel.PUBLIC, Type.CONST, ELPrimitives.UINT8.pointerTo(), "RSP_DEVICE", false, this, unit, PERIPHERAL_LOCATION).setValue(PeripheralManager.PERIPHERAL_RSP_DEVICE));
        addStaticVariable(new ELVariable(ELProtectionLevel.PUBLIC, Type.CONST, ELPrimitives.UINT32.pointerTo(), "RSP_DATA", false, this, unit, PERIPHERAL_LOCATION).setValue(PeripheralManager.PERIPHERAL_RSP_DATA));
        
        addStaticVariable(new ELVariable(ELProtectionLevel.PUBLIC, Type.CONST, ELPrimitives.UINT32.pointerTo(), "TABLE",
                false, this, unit, PERIPHERAL_LOCATION).setValue(PeripheralManager.PERIPHERAL_TABLE));
                
        addStaticVariable(new ELVariable(ELProtectionLevel.PUBLIC, Type.CONST, ELPrimitives.UINT32.pointerTo(), "TIMERS",
                false, this, unit, PERIPHERAL_LOCATION).setValue(PeripheralManager.PERIPHERAL_START + 0x200));
        
        addStaticVariable(new ELVariable(ELProtectionLevel.PUBLIC, Type.CONST, ELPrimitives.UINT32.pointerTo(), "TYPE_DISPLAY_CHARACTER", true, this, unit, PERIPHERAL_LOCATION).setValue(TYPE_DISPLAY_CHARACTER));
        addStaticVariable(new ELVariable(ELProtectionLevel.PUBLIC, Type.CONST, ELPrimitives.UINT32.pointerTo(), "TYPE_STORAGE_VIRTUAL", true, this, unit, PERIPHERAL_LOCATION).setValue(TYPE_STORAGE_VIRTUAL));
        addStaticVariable(new ELVariable(ELProtectionLevel.PUBLIC, Type.CONST, ELPrimitives.UINT32.pointerTo(), "TYPE_STORAGE_BLOCK", true, this, unit, PERIPHERAL_LOCATION).setValue(TYPE_STORAGE_BLOCK));

        ELFunction command = new ELFunction(ELProtectionLevel.PUBLIC, false, this, "command", FunctionType.STATIC,
                false, unit, PERIPHERAL_LOCATION);
        command.addParameter(ELPrimitives.UINT32, "deviceId");
        command.addParameter(ELPrimitives.UINT32, "cmdSize");
        command.addParameter(ELPrimitives.UINT32.pointerTo(), "cmd");
        // ComplexAction commandBody = new ComplexAction(new ActionScope(this, unit, command));
        // command.setBody(commandBody);
        command.actions.add(new DirectAction("#line <Peripheral> 1:1"));
        command.actions.add(new DirectAction("STACK PUSH r15"));
        command.actions.add(new DirectAction("COPY rStack r15"));

        command.actions.add(new DirectAction("#stackVar uint32 deviceId -20"));
        command.actions.add(new DirectAction("#stackVar uint32 cmdSize -16"));
        command.actions.add(new DirectAction("#stackVar uint32* cmd -12"));
        
        command.actions.add(new DirectAction("LOAD r1 Peripheral.CMD_SIZE"));
        command.actions.add(new DirectAction("COPY r15 r2"));
        command.actions.add(new DirectAction("INC r2 -16"));
        command.actions.add(new DirectAction("LOAD MEM r2 r2")); // cmdSize

        command.actions.add(new DirectAction("STORE r2 r1"));

        command.actions.add(new DirectAction("LOAD r1 Peripheral.CMD_DATA"));

        command.actions.add(new DirectAction("COPY r15 r3"));
        command.actions.add(new DirectAction("INC r3 -12"));
        command.actions.add(new DirectAction("LOAD MEM r3 r3")); // cmd*

        command.actions.add(new DirectAction(":Peripheral.command_loop"));
        command.actions.add(new DirectAction("COPY MEM r3 r1 INC_RS INC_RD"));
        command.actions.add(new DirectAction("INC r2 -1"));
        command.actions.add(new DirectAction("GOTO GT r2 :Peripheral.command_loop"));

        command.actions.add(new DirectAction("COPY r15 r1"));
        command.actions.add(new DirectAction("INC r1 -20"));
        command.actions.add(new DirectAction("LOAD MEM r1 r1")); // deviceId
        
        command.actions.add(new DirectAction("LOAD r2 0x0101_0000"));
        command.actions.add(new DirectAction("OR r1 r1 r2"));
        
        command.actions.add(new DirectAction("LOAD r2 Peripheral.CMD_ADDR"));
        command.actions.add(new DirectAction("STORE r1 r2"));

        command.actions.add(new DirectAction("#stackVarClear deviceId"));
        command.actions.add(new DirectAction("#stackVarClear cmdSize"));
        command.actions.add(new DirectAction("#stackVarClear cmd"));

        command.actions.add(new DirectAction("STACK POP r15"));

        command.actions.add(new DirectAction("#lineend"));
        addStaticFunction(command);

        ELStruct PeripheralDescriptor = new ELStruct("PeripheralDescriptor", this, unit);
        PeripheralDescriptor.addMember(new ELVariable(ELProtectionLevel.PUBLIC, ELVariable.Type.MEMBER, ELPrimitives.UINT32, "id", true, this, unit, PERIPHERAL_LOCATION));
        PeripheralDescriptor.addMember(new ELVariable(ELProtectionLevel.PUBLIC, ELVariable.Type.MEMBER, ELPrimitives.UINT32, "type", true, this, unit, PERIPHERAL_LOCATION));
        PeripheralDescriptor.addMember(new ELVariable(ELProtectionLevel.PUBLIC, ELVariable.Type.MEMBER, ELPrimitives.CHAR.builder().array(16).location(PERIPHERAL_LOCATION).build(), "manufacturer", true, this, unit, PERIPHERAL_LOCATION));
        PeripheralDescriptor.addMember(new ELVariable(ELProtectionLevel.PUBLIC, ELVariable.Type.MEMBER, ELPrimitives.CHAR.builder().array(16).location(PERIPHERAL_LOCATION).build(), "serial", true, this, unit, PERIPHERAL_LOCATION));
        PeripheralDescriptor.addMember(new ELVariable(ELProtectionLevel.PUBLIC, ELVariable.Type.MEMBER,
                ELPrimitives.UINT32.builder().array(6).location(PERIPHERAL_LOCATION).build(), "data", true, this, unit,
                PERIPHERAL_LOCATION));
    }

}
