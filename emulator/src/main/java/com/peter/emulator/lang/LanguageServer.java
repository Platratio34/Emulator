package com.peter.emulator.lang;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.NoSuchElementException;
import java.util.Optional;

import com.peter.emulator.lang.base.SysD;

public class LanguageServer {

    public HashMap<String, ProgramModule> modules = new HashMap<>();

    public LanguageServer() {
        addModule("SysD").namespaces.put("SysD", SysD.INSTANCE);
    }

    public ProgramModule addModule(String name) {
        ProgramModule module = new ProgramModule(name, this);
        modules.put(name, module);
        return module;
    }

    public Optional<String> parse() {
        for (ProgramModule module : modules.values()) {
            Optional<String> err = module.parse();
            if (err.isPresent())
                return err;
        }
        return Optional.empty();
    }
    
    public ArrayList<ELAnalysisError> analyze() {
        ArrayList<ELAnalysisError> errors = new ArrayList<>();
        for (ProgramModule module : modules.values()) {
            ArrayList<ELAnalysisError> errs = module.analyze();
            errors.addAll(errs);
        }
        return errors;
    }
    public ArrayList<ELAnalysisError> analyze(String moduleName) {
        if(!modules.containsKey(moduleName))
            throw new NoSuchElementException("No module with name " + moduleName);
        return modules.get(moduleName).analyze();
    }
    
    public ArrayList<ELAnalysisError> resolve() {
        ArrayList<ELAnalysisError> errors = new ArrayList<>();
        for (ProgramModule module : modules.values()) {
            ArrayList<ELAnalysisError> errs = module.resolve();
            errors.addAll(errs);
        }
        return errors;
    }
    public ArrayList<ELAnalysisError> resolve(String moduleName) {
        if(!modules.containsKey(moduleName))
            throw new NoSuchElementException("No module with name " + moduleName);
        return modules.get(moduleName).resolve();
    }
}
