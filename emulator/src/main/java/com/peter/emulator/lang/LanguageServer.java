package com.peter.emulator.lang;

import java.util.HashMap;
import java.util.NoSuchElementException;

import com.peter.emulator.lang.base.SysD;

public class LanguageServer {

    public HashMap<String, ProgramModule> modules = new HashMap<>();
    protected boolean err = false;

    public LanguageServer() {
        addModule("SysD").namespaces.put("SysD", SysD.INSTANCE);
    }

    public ProgramModule addModule(String name) {
        ProgramModule module = new ProgramModule(name, this);
        modules.put(name, module);
        return module;
    }

    public ErrorSet parse() {
        ErrorSet errors = new ErrorSet();
        for (ProgramModule module : modules.values()) {
            module.parse(errors);
        }
        if(errors.hadError())
            err = true;
        return errors;
    }
    
    public ErrorSet analyze() {
        ErrorSet errors = new ErrorSet();
        for (ProgramModule module : modules.values()) {
            ErrorSet errs = module.analyze();
            errors.combine(errs);
        }
        if(errors.hadError())
            err = true;
        return errors;
    }
    public ErrorSet analyze(String moduleName) {
        if(!modules.containsKey(moduleName))
            throw new NoSuchElementException("No module with name " + moduleName);
        return modules.get(moduleName).analyze();
    }
    
    public ErrorSet resolve() {
        ErrorSet errors = new ErrorSet();
        for (ProgramModule module : modules.values()) {
            ErrorSet errs = module.resolve();
            errors.combine(errs);
        }
        if(errors.hadError())
            err = true;
        return errors;
    }
    public ErrorSet resolve(String moduleName) {
        if(!modules.containsKey(moduleName))
            throw new NoSuchElementException("No module with name " + moduleName);
        return modules.get(moduleName).resolve();
    }

    public boolean hasError() {
        return err;
    }

    public boolean clearError() {
        boolean e = err;
        err = false;
        return e;
    }

    public ErrorSet recompile() {
        err = false;
        for(ProgramModule pm : modules.values())
            pm.onRecompile();
        ErrorSet errors = parse();
        if(errors.hadError())
            return errors;
        errors.combine(resolve());
        if(errors.hadError())
            return errors;
        errors.combine(analyze());
        return errors;
    }
}
