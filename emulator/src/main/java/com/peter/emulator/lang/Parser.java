package com.peter.emulator.lang;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Optional;

import com.peter.emulator.lang.Token.AnnotationToken;
import com.peter.emulator.lang.Token.BlockToken;
import com.peter.emulator.lang.Token.IdentifierToken;
import com.peter.emulator.lang.Token.OperatorToken;
import com.peter.emulator.lang.Token.SetToken;
import com.peter.emulator.lang.annotations.ELAnnotation;
import com.peter.emulator.lang.annotations.ELEntrypointAnnotation;

public class Parser {

    protected ArrayList<Token> tokens;
    protected int workingI;

    public HashMap<String, Identifier> identifiers = new HashMap<>();
    public Namespace currentNamespace = null;
    public ArrayList<Namespace> namespaces = new ArrayList<>();
    public HashMap<String, String> imports = new HashMap<>();

    private final ProgramModule module;

    public Parser(ProgramModule module) {
        this.module = module;
    }

    public Parser(Namespace namespace, ProgramModule module) {
        currentNamespace = namespace;
        this.module = module;
    }

    /*
    <private|protected|public> <static> <const> [type] [name]< = [value]>;
    <extern> <static> [return] [name]({[type] [name]}...) {}
    
    
    */

    public Optional<String> parse(ArrayList<Token> tokens) {
        if (tokens == null)
            throw new NullPointerException();
        this.tokens = tokens;
        workingI = 0;
        try {
            while (workingI < tokens.size()) {
                Token t = tokens.get(workingI);
                ArrayList<ELAnnotation> annotations = null;
                if (t instanceof AnnotationToken) {
                    annotations = new ArrayList<>();
                    while (tokens.get(workingI) instanceof AnnotationToken at2) {
                        workingI++;
                        annotations.add(ELAnnotation.create(at2));
                        if (workingI >= tokens.size())
                            throw new ELCompileException("Found annotation at end of tokens");
                    }
                    t = tokens.get(workingI);
                }
                if (t instanceof IdentifierToken idt) {
                    if (idt.value.equals("import")) {
                        workingI++;
                        String imp;
                        String name;
                        if (tokens.get(workingI) instanceof IdentifierToken idt2) {
                            imp = idt2.value;
                            name = imp;
                            while (idt2.hasSub()) {
                                idt2 = (IdentifierToken) idt2.subTokens.get(0);
                                imp += "." + idt2.value;
                                name = idt2.value;
                            }
                        } else {
                            throw new ELCompileException(
                                    "Unexpected token found in import (expected identifier): " + tokens.get(workingI));
                        }
                        workingI++;
                        if (tokens.get(workingI) instanceof IdentifierToken idt3 && idt3.value.equals("as")) {
                            workingI++;
                            if (tokens.get(workingI) instanceof IdentifierToken idt4) {
                                workingI++;
                                name = idt4.value;
                                imports.put(name, imp);
                            } else {
                                throw new ELCompileException(
                                        "Unexpected token found in import (expected alias): " + tokens.get(workingI));
                            }
                            if (!(tokens.get(workingI) instanceof OperatorToken ot
                                && ot.type == OperatorToken.Type.SEMICOLON)) {
                                throw new ELCompileException(
                                        "Unexpected token found in import (expected `;`): " + tokens.get(workingI));
                            }
                        } else if (tokens.get(workingI) instanceof OperatorToken ot
                                && ot.type == OperatorToken.Type.SEMICOLON) {
                            imports.put(name, imp);
                        } else {
                            throw new ELCompileException(
                                    "Unexpected token found in import (expected `as` or `;`): " + tokens.get(workingI));
                        }
                    } else if (idt.value.equals("static") || idt.value.equals("operator") || idt.value.equals("extern")
                            || ELProtectionLevel.valid(idt.value)) {
                        Location loc = idt.startLocation;
                        workingI++;
                        ELProtectionLevel level = ELProtectionLevel.get(idt.value, ELProtectionLevel.PROTECTED);
                        boolean stat = idt.value.equals("static");
                        boolean extern = idt.value.equals("extern");
                        boolean operator = idt.value.equals("operator");
                        boolean final_ = idt.value.equals("final");
                        boolean constexpr = false;
                        if (!stat)
                            if (tokens.get(workingI) instanceof IdentifierToken it && it.value.equals("static")) {
                                stat = true;
                                workingI++;
                            }
                        if (!extern)
                            if (tokens.get(workingI) instanceof IdentifierToken it && it.value.equals("extern")) {
                                extern = true;
                                workingI++;
                            }
                        if (!final_)
                            if (tokens.get(workingI) instanceof IdentifierToken it && it.value.equals("final")) {
                                final_ = true;
                                workingI++;
                            }
                        if (tokens.get(workingI) instanceof IdentifierToken it && it.value.equals("constexpr")) {
                            constexpr = true;
                            workingI++;
                        }
                        if (!operator)
                            if (tokens.get(workingI) instanceof IdentifierToken it && it.value.equals("operator")) {
                                operator = true;
                                workingI++;
                            }
                        boolean abs = false;
                        if (tokens.get(workingI) instanceof IdentifierToken it2 && it2.value.equals("abstract")) {
                            abs = true;
                            workingI++;
                        }
                        boolean destructor = false;
                        if (tokens.get(workingI) instanceof OperatorToken o2
                                && o2.type == OperatorToken.Type.DESTRUCTOR) {
                            destructor = true;
                            workingI++;
                        }
                        ELType.Builder typeBuilder = new ELType.Builder();
                        while (typeBuilder.ingest(tokens.get(workingI))) {
                            workingI++;
                        }
                        ELType type = typeBuilder.build();
                        String name;
                        if (currentNamespace instanceof ELClass currentClass && type.equals(currentClass.getType())
                                && tokens.get(workingI) instanceof SetToken set) {
                            // this is a constructor;
                            ELFunction function = new ELFunction(level, extern, currentClass,
                                    currentClass.cName, destructor ? ELFunction.FunctionType.DESTRUCTOR : ELFunction.FunctionType.CONSTRUCTOR, false, loc);
                            function.ret = type;
                            function.ingestParams(set);
                            if (annotations != null)
                                function.annotations = annotations;
                            if (destructor) {
                                if (currentClass.destructor != null) {
                                    throw new ELCompileException("Class " + currentClass.getQualifiedName() + " already had a destructor");
                                }
                                currentClass.destructor = function;
                            } else {
                                if (currentClass.constructor == null) {
                                    currentClass.constructor = function;
                                } else {
                                    currentClass.constructor.addOverload(function);
                                }
                            }
                            workingI++;
                            if (tokens.get(workingI) instanceof BlockToken bt) {
                                function.ingestBody(bt);
                            } else if (tokens.get(workingI) instanceof OperatorToken ot
                                    && ot.type == OperatorToken.Type.SEMICOLON) {
                                // no body;
                            } else {
                                throw new ELCompileException("Unexpected token found, expected function body or `;`: "
                                        + tokens.get(workingI));
                            }
                            continue;
                        } else {
                            if (tokens.get(workingI) instanceof IdentifierToken it4) {
                                name = it4.value;
                                workingI++;
                            } else {
                                return Optional.of("Unexpected token found for name (expected identifier): "
                                        + tokens.get(workingI));
                            }
                        }
                        if (tokens.get(workingI) instanceof SetToken set) { // function
                            workingI++;
                            ELFunction.FunctionType funcType = stat ? ELFunction.FunctionType.STATIC
                                    : ELFunction.FunctionType.INSTANCE;
                            if(operator)
                                funcType = ELFunction.FunctionType.OPERATOR;
                            ELFunction function = new ELFunction(level, extern, currentNamespace, name, funcType, constexpr, loc);
                            if (annotations != null)
                                function.annotations = annotations;
                            if (currentNamespace == null)
                                throw new ELCompileException("Can not have a function outside of namespace or class");

                            if (!type.isVoid())
                                function.ret = type;
                            function.abstractFunction = abs;
                            function.ingestParams(set);
                            
                            if (stat) {
                                currentNamespace.addStaticFunction(function);
                            } else if (currentNamespace instanceof ELClass currentClass) {
                                currentClass.addFunction(function);
                            } else {
                                throw new ELCompileException("Can not have non-static function outside of class");
                            }
                            if (tokens.get(workingI) instanceof BlockToken bt) {
                                function.ingestBody(bt);
                            } else if (tokens.get(workingI) instanceof OperatorToken ot
                                    && ot.type == OperatorToken.Type.SEMICOLON) {
                                // no body;
                                function.bodyLocation = ot.startLocation;
                            } else {
                                throw new ELCompileException("Unexpected token found, expected function body or `;`: "
                                        + tokens.get(workingI));
                            }
                            if(function.hasAnnotation(ELEntrypointAnnotation.class)) {
                                module.entrypoint = function;
                            }
                        } else { // variable
                            if(abs)
                                throw new ELCompileException("Variable can not be marked abstract");
                            ELVariable var = new ELVariable(level, stat, type, name, final_, loc);
                            if (annotations != null)
                                var.annotations = annotations;
                            if (currentNamespace == null)
                                throw new ELCompileException("Can not have a variable outside of namespace or class");
                            else if (stat) {
                                currentNamespace.addStaticVariable(var);
                            } else if (currentNamespace instanceof ELClass currentClass) {
                                currentClass.addMember(var);
                            } else {
                                throw new ELCompileException("Can not have non-static variable outside of class");
                            }
                            if (tokens.get(workingI) instanceof OperatorToken ot) {
                                switch (ot.type) {
                                    case SEMICOLON -> {
                                        workingI++;
                                        continue;
                                    }
                                    case ASSIGN -> workingI++;
                                    default -> throw new ELCompileException(
                                                "Unexpected token found, expected `;` or `=`: " + tokens.get(workingI));
                                }
                            } else {
                                throw new ELCompileException(
                                        "Unexpected token found, expected `;` or `=`: " + tokens.get(workingI));
                            }
                            var.valueLocation = tokens.get(workingI).startLocation;
                            while (var.ingestValue(tokens.get(workingI))) {
                                workingI++;
                            }
                        }
                    } else if (idt.value.equals("namespace")) {
                        workingI++;
                        Namespace namespace = null;
                        if (tokens.get(workingI) instanceof IdentifierToken it) {
                            namespace = makeNamespace(it.value, namespace);
                            // System.out.println(namespace.getQualifiedName());
                            while (it.hasSub()) {
                                Token tkn = it.subTokens.get(0);
                                if (tkn instanceof IdentifierToken it2) {
                                    // System.out.println("- " + it2.value);
                                    namespace = makeNamespace(it2.value, namespace);
                                    // System.out.println(namespace.getQualifiedName());
                                    it = it2;
                                } else {
                                    throw new ELCompileException("Unexpected token found, expected identifier: " + tkn);
                                }
                            }
                            namespaces.add(namespace);
                            namespace.addImports(imports);
                        } else {
                            return Optional.of("Unknown token found (expected identifier): " + tokens.get(workingI));
                        }
                        workingI++;
                        if (tokens.get(workingI) instanceof BlockToken bt) {
                            Optional<String> opt = new Parser(namespace, module).parse(bt.subTokens);
                            if (opt.isPresent()) {
                                return opt;
                            }
                        } else if (tokens.get(workingI) instanceof OperatorToken ot
                                && ot.type == OperatorToken.Type.SEMICOLON) {
                            currentNamespace = namespace;
                        } else {
                            throw new ELCompileException(
                                    "Unexpected token found, expected block or `;`: " + tokens.get(workingI));
                        }
                    } else if (idt.value.equals("abstract") || idt.value.equals("class")
                            || idt.value.equals("struct")) {
                        boolean abs = idt.value.equals("abstract");
                        if (abs) {
                            workingI++;
                            idt = (IdentifierToken) tokens.get(workingI);
                            if(idt == null)
                                continue;
                        }
                        boolean struct = idt.value.equals("struct");
                        workingI++;
                        ELClass clazz;
                        if (tokens.get(workingI) instanceof IdentifierToken it) {
                            if (struct)
                                clazz = new ELStruct(it.value, currentNamespace);
                            else
                                clazz = new ELClass(it.value, currentNamespace);
                            namespaces.add(clazz);
                        } else {
                            return Optional.of("Unknown token found (expected identifier): " + tokens.get(workingI));
                        }
                        if (annotations != null)
                            clazz.annotations = annotations;
                        clazz.abstractClass = abs;
                        workingI++;
                        if (tokens.get(workingI) instanceof OperatorToken ot
                                && ot.type == OperatorToken.Type.ANGLE_LEFT) {
                            workingI++;
                            ELType.Builder builder = null;
                            boolean r = true;
                            String tName = null;
                            while (r) {
                                Token tkn = tokens.get(workingI);
                                if (tName == null) {
                                    if (tkn instanceof IdentifierToken tit) {
                                        tName = tit.value;
                                        clazz.genericsOrder.add(tName);
                                        clazz.generics.put(tName, null);
                                        workingI++;
                                    } else {
                                        throw new ELCompileException(
                                                "Unexpected token found in type (expected operator): " + tkn);
                                    }
                                } else if (builder != null) {
                                    if (!builder.ingest(tkn)) {
                                        if (tkn instanceof OperatorToken ot2 && ot2.type == OperatorToken.Type.COMMA) {
                                            clazz.generics.put(tName, builder.build());
                                            builder = null;
                                            tName = null;
                                            workingI++;
                                        } else if (tkn instanceof OperatorToken ot2
                                                && ot2.type == OperatorToken.Type.ANGLE_RIGHT) {
                                            clazz.generics.put(tName, builder.build());
                                            r = false;
                                            workingI++;
                                        } else {
                                            throw new ELCompileException(
                                                    "Unexpected token found in type (expected operator): " + tkn);
                                        }
                                    } else {
                                        workingI++;
                                    }
                                } else {
                                    if (tkn instanceof OperatorToken ot2 && ot2.type == OperatorToken.Type.COMMA) {
                                        tName = null;
                                        workingI++;
                                    } else if (tkn instanceof OperatorToken ot2
                                            && ot2.type == OperatorToken.Type.ANGLE_RIGHT) {
                                        r = false;
                                        workingI++;
                                    } else {
                                        throw new ELCompileException(
                                                "Unexpected token found in type parameter (expected `extends`, `,` or `>`): "
                                                        + tkn);
                                    }
                                }
                            }
                        }
                        if (tokens.get(workingI) instanceof IdentifierToken tit) {
                            if(tit.value.equals("extends")) {
                                ELType.Builder builder = new ELType.Builder();
                                workingI++;
                                
                                while(builder.ingest(tokens.get(workingI)))
                                    workingI++;
                                clazz.setParentType(builder.build());
                            }
                        }
                        if (tokens.get(workingI) instanceof BlockToken bt) {
                            Optional<String> opt = new Parser(clazz, module).parse(bt.subTokens);
                            if (opt.isPresent()) {
                                return opt;
                            }
                        } else {
                            return Optional.of("Unknown token found (expected `extends` or block): " + tokens.get(workingI));
                        }
                    }
                }
                workingI++;
            }
        } /* catch (IndexOutOfBoundsException e) {
             return Optional.of("Index out of bounds");
          } */ catch (ELCompileException e) {
            String last = "";
            boolean f = true;
            if (workingI >= tokens.size())
                workingI = tokens.size() - 1;
            for (int i = workingI < 4 ? 0 : workingI - 4; i <= workingI; i++) {
                if (!f)
                    last += ", ";
                f = false;
                last += tokens.get(i);
            }
            return Optional.of(e.getMessage() + "; " + last);
        }
        return Optional.empty();
    }
    
    private Namespace makeNamespace(String name, Namespace parent) {
        if (module == null) {
            return new Namespace(name, parent);
        }
        Namespace namespace;
        if(parent == null)
            namespace = module.getNamespace(name);
        else
            namespace = module.getNamespace(parent.getQualifiedName() + "." + name);
        if(namespace == null)
            namespace = new Namespace(name, parent);
        return namespace;
    }

    public static class Identifier {
        public String type;
        public Token value;
        public boolean constVal = false;
        public boolean pointer = false;
        public boolean array = false;

        public Identifier(String type) {
            this.type = type;
        }

        public String debugString(String name) {
            String out = "";
            if (constVal)
                out = "const ";
            out += type;
            if (pointer) {
                out += "*";
            }
            if (array) {
                out += "[]";
            }
            out += " " + name;
            if (value != null) {
                out += " = " + value;
            }
            return out;
        }
    }
}
