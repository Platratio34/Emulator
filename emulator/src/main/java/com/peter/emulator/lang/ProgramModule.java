package com.peter.emulator.lang;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Optional;

public class ProgramModule {

    protected ArrayList<Path> files = new ArrayList<>();
    protected ArrayList<String> refModules = new ArrayList<>();

    protected HashMap<String, Namespace> namespaces = new HashMap<>();
    protected ELFunction entrypoint;

    public final String name;
    private final LanguageServer languageServer;

    public ProgramModule(String name, LanguageServer languageServer) {
        this.name = name;
        this.languageServer = languageServer;
    }

    public void addFile(Path f) {
        if (files.contains(f))
            return;
        if (!new File(f.toUri()).isFile()) {
            throw new RuntimeException("Path " + f.toString() + " does not exist");
        }
        files.add(f);
    }

    public void addFiles(Path root) {
        addFiles(new File(root.toUri()));
    }
    public void addFiles(File root) {
        if(!root.isDirectory()) {
            throw new RuntimeException("Path "+root.getAbsolutePath()+" does not exist");
        }
        for (File f : root.listFiles()) {
            if (f.isFile() && f.getName().endsWith(".el")) {
                addFile(f.toPath());
            } else if (f.isDirectory()) {
                addFiles(f);
            }
        }
    }
    
    public void addRefModule(String module) {
        if (refModules.contains(module))
            return;
        refModules.add(module);
    }

    private void addNamespace(Namespace namespace) {
        String name = namespace.getQualifiedName();
        if (namespaces.containsKey(name)) {
            if (namespaces.get(name) != namespace) {
                // System.out.println("Appending to namespace " + name + " (" + namespace.cName + ")");
                namespaces.get(name).append(namespace);
            }
        } else if (namespace.namespace != null) {
            // System.out.println("Adding parent namespace for "+namespace.cName+": "+namespace.namespace.cName+" ("+name+")");
            addNamespace(namespace.namespace);
        } else {
            // System.out.println("Adding namespace "+name+" ("+namespace.cName+")");
            namespaces.put(name, namespace);
        }
    }

    public void parse(ErrorSet errors) {
        for (Path path : files) {
            try {
                String str = Files.readString(path);
                Tokenizer tk = new Tokenizer(str, new Location(path.toString(), 1, 1), false);
                try {
                    Optional<String> err = tk.tokenize();
                    if (err.isPresent()) {
                        errors.error("Tokenizer error: "+err.get());
                        return;
                    }
                    Parser parser = new Parser(this);
                    parser.parse(tk.tokens, errors);
                    for (Namespace ns : parser.namespaces) {
                        addNamespace(ns);
                    }
                } catch (ELCompileException e) {
                    errors.error("Exception: "+e.getMessage());
                }
            } catch (IOException e) {
                errors.error("IO Exception: "+e);
                return ;
            }
        }
    }
    
    public ErrorSet resolve() {
        ErrorSet errors = new ErrorSet();
        for (String m : refModules) {
            if (!languageServer.modules.containsKey(m))
                errors.error("Could not resolve referenced module " + m);
        }
        for (Namespace ns : namespaces.values()) {
            ns.resolve(errors, this);
        }
        return errors;
    }
    
    public ErrorSet analyze() {
        ErrorSet errors = new ErrorSet();
        for (Namespace ns : namespaces.values()) {
            ns.analyze(errors, this);
        }
        return errors;
    }

    public Namespace getNamespace(String fullName) {
        Namespace ns = namespaces.get(fullName);
        if (ns != null)
            return ns;
        if (fullName.contains(".")) {
            // System.out.println("* checking name parts of " + fullName);
            String[] p = fullName.split("\\.");
            if (namespaces.containsKey(p[0])) {
                // System.out.println("* * Had first level NS: " + p[0]);
                ns = namespaces.get(p[0]);
                for (int i = 1; i < p.length; i++) {
                    if (ns.namespaces.containsKey(p[i])) {
                        // System.out.println("* * Had next level NS: " + p[i]);
                        ns = ns.namespaces.get(p[i]);
                    } else {
                        ns = null;
                        break;
                    }
                }
            }
        }
        return ns;
    }

    public Collection<Namespace> getNamespaces() {
        return namespaces.values();
    }

    public Namespace getNamespaceIncluded(String fullName) {
        Namespace namespace = getNamespace(fullName);
        if (namespace != null)
            return namespaces.get(fullName);
        // System.out.println("= Looking for included namespace "+fullName);
        for (String ref : refModules) {
            // System.out.println("= = checking referenced module "+ref);
            if (!languageServer.modules.containsKey(ref))
                continue;
            // System.out.println("= =+ Module was present");
            ProgramModule m = languageServer.modules.get(ref);
            namespace = m.getNamespace(fullName);
            if (namespace != null)
                return namespace;
            // System.out.println("= =+ Module did not have namespace");
        }
        return null;
    }

    public boolean hasType(ELType base) {
        if (!hasType(base, 0)) {
            for (String r : refModules) {
                if (!languageServer.modules.containsKey(r))
                    continue;
                if(languageServer.modules.get(r).hasTypeIntrinsic(base))
                    return true;
            }
            return false;
        }
        return true;
    }
    public boolean hasTypeIntrinsic(ELType base) {
        return hasType(base, 0);
    }

    public boolean hasType(ELType base, int lvl) {
        // System.out.println("Looking for type "+base.typeString() + " (in "+name+")");
        String n = base.baseClass.last();
        boolean f = base.baseClass.numParts() == lvl+1;
        if (base.baseClass.numParts() > 1 && lvl != 0) {
            // System.err.println("- - had no parents, and was searching for lvl "+lvl);
            return false;
        }
        if (f) {
            // the last step on the chain
        } else if(base.baseClass.numParts() >= lvl) {
            n = base.baseClass.get(lvl);
        } else {
            // System.out.println("- - Ran out of parents");
            return false;
        }
        // System.out.println("- looking in NS "+n);
        if (namespaces.containsKey(n)) {
            Namespace ns = namespaces.get(n);
            if (!f) {
                // System.out.println("- Checking ns");
                return ns.hasType(base, lvl + 1);
            }
            if (!(ns instanceof ELClass)) {
                // System.out.println("- Found ns, but wasn't class");
                return false;
            }
            return true;
        }
        // System.out.println("- Couldn't find NS "+n);
        return false;
    }

    public ELClass getType(ELType base, Namespace srcNs) {
        ELClass clazz = getType(base, 0, srcNs);
        if (clazz == null) {
            for (String r : refModules) {
                if (!languageServer.modules.containsKey(r))
                    continue;
                return languageServer.modules.get(r).getTypeIntrinsic(base, srcNs);
            }
            return null;
        }
        return clazz;
    }
    public ELClass getTypeIntrinsic(ELType base, Namespace srcNs) {
        return getType(base, 0, srcNs);
    }

    public ELClass getType(ELType base, int lvl, Namespace srcNs) {
        // System.out.println("Looking for type "+base.typeString() + " (in "+name+")");
        String n = base.baseClass.last();
        boolean f = base.baseClass.numParts() == lvl+1;
        if (base.baseClass.numParts() > 1 && lvl != 0) {
            // System.err.println("- - had no parents, and was searching for lvl "+lvl);
            return null;
        }
        if (f) {
            // the last step on the chain
        } else if(base.baseClass.numParts() >= lvl) {
            n = base.baseClass.get(lvl);
        } else {
            // System.out.println("- - Ran out of parents");
            return null;
        }
        // System.out.println("- looking in NS "+n);
        if (namespaces.containsKey(n)) {
            Namespace ns = namespaces.get(n);
            if (!f) {
                // System.out.println("- Checking ns");
                return ns.getType(base, lvl + 1, srcNs, this);
            }
            if (ns instanceof ELClass clazz) {
                return clazz;
            }
                // System.out.println("- Found ns, but wasn't class");
            return null;
        }
        // System.out.println("- Couldn't find NS "+n);
        return null;
    }

    public String assemble() {
        return new ELAssembler(this).assemble();
    }

    public void onRecompile() {
        namespaces.clear();
    }
}
