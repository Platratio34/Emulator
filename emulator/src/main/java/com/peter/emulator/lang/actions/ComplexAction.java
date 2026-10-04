package com.peter.emulator.lang.actions;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.function.Function;

import com.peter.emulator.machinecode.Reg;

public class ComplexAction extends Action {

    public final ArrayList<Action> actions = new ArrayList<>();

    public ComplexAction(ActionScope scope) {
        super(scope);
    }

    protected static int globalIndex = 0;

    @Override
    public String toAssembly() {
        String out = "";
        boolean f = true;
        HashMap<Integer, Reg> tempRegMap = new HashMap<>();
        for (Action action : actions) {
            String asm = action.toAssembly();
            if (asm == null || asm.length() == 0)
                continue;
            
            if (asm.contains("$r")) {
                for (int i = 0; i < 16; i++) {
                    String id = "$r" + i;
                    if (asm.contains(id)) {
                        Reg reg;
                        if (tempRegMap.containsKey(i)) {
                            reg = tempRegMap.get(i);
                        } else {
                            int r = scope.firstFreeR();
                            scope.reserve(r);
                            reg = Reg.from(r);
                            tempRegMap.put(i, reg);
                            asm = String.format("// Resolving placeholder %d to %s\n%s", i, reg, asm);
                        }
                        asm = asm.replace(id, reg.toString());
                    }
                }
            }
            out += (f ? "" : "\n") + asm;
            f = false;
        }
        if (out.contains("$i")) {
            for (int i = 0; i < 256; i++) {
                String id = "$i" + i;
                if (out.contains(id)) {
                    out = out.replace(id, "" + (globalIndex++));
                }
            }
        }
        for (Reg r : tempRegMap.values()) {
            scope.release(r.code);
        }
        return out;
    }

    public void add(Action action) {
        actions.add(action);
    }

    public void addDirect(String asm, Object... args) {
        actions.add(new DirectAction(asm, args));
    }
    public void addDirect(String asm) {
        actions.add(new DirectAction(asm));
    }

    public void addReserve(Register register) {
        actions.add(new RegisterAction(scope, register, false));
    }
    public void addFind(Register register) {
        actions.add(new RegisterAction(scope, register, false, true));
    }
    public void addRelease(Register register) {
        actions.add(new RegisterAction(scope, register, true));
    }

    public void add(Function<ActionScope, String> onCompile) {
        actions.add(new CompilerAction(scope, onCompile));
    }

    public boolean isEmpty() {
        return actions.isEmpty();
    }
}
