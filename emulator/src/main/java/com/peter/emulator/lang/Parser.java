package com.peter.emulator.lang;

import java.util.ArrayList;
import java.util.HashMap;

import com.peter.emulator.lang.ELSymbol.ELVarSymbol;
import com.peter.emulator.lang.annotations.ELAnnotation;
import com.peter.emulator.lang.annotations.ELEntrypointAnnotation;
import com.peter.emulator.lang.doc.DocComment;
import com.peter.emulator.lang.tokens.OperatorToken.Type;
import com.peter.emulator.lang.tokens.*;

public class Parser {

    protected ArrayList<Token> tokens;
    protected int workingI;

    public HashMap<String, Identifier> identifiers = new HashMap<>();
    public Namespace currentNamespace = null;
    public ArrayList<Namespace> namespaces = new ArrayList<>();

    private final ProgramUnit unit;

    public Parser(ProgramUnit unit) {
        this.unit = unit;
    }

    public Parser(ProgramUnit unit, Namespace namespace) {
        this.unit = unit;
        currentNamespace = namespace;
    }

    /*
    <private|protected|public> <static> <const> [type] [name]< = [value]>;
    <extern> <static> [return] [name]({[type] [name]}...) {}
    
    
    */

    public void parse(ArrayList<Token> tokens, ErrorSet errors) {
        if (tokens == null)
            throw new NullPointerException();
        this.tokens = tokens;
        workingI = 0;
        try {
            while (workingI < tokens.size()) {
                Token t = tokens.get(workingI);
                ArrayList<ELAnnotation> annotations = null;
                DocCommentToken docCommentToken = null;
                // System.out.println("Resetting doc comment");
                if (t instanceof DocCommentToken dcT) {
                    workingI++;
                    docCommentToken = dcT;
                    dcT.addSymbols(unit);
                    if (workingI >= tokens.size()) {
                        errors.error("Found doc comment at end of tokens", dcT.span());
                        break;
                    }
                    // System.out.println("Found doc comment "+dcT.debugString());
                    t = tokens.get(workingI);
                    // System.out.println(t);
                }
                if (t instanceof AnnotationToken) {
                    annotations = new ArrayList<>();
                    while (tokens.get(workingI) instanceof AnnotationToken at2) {
                        workingI++;
                        annotations.add(ELAnnotation.create(at2));
                        if (workingI >= tokens.size()) {
                            errors.error("Found annotation at end of tokens", at2.span());
                            break;
                        }
                    }
                    t = tokens.get(workingI);
                }
                if (t instanceof IdentifierToken idt) {
                    if (idt.value.equals("import")) {
                        workingI++;
                        unit.symbols.add(new ELSymbol(ELSymbol.Type.KEYWORD, idt.span()));
                        String imp;
                        String name;
                        IdentifierToken nameToken = null;
                        if (tokens.get(workingI) instanceof IdentifierToken idt2) {
                            imp = idt2.value;
                            name = imp;
                            nameToken = idt2;
                            unit.symbols.add(new ELSymbol(ELSymbol.Type.NAMESPACE_NAME, idt2.spanFirst()));
                            while (idt2.hasSub()) {
                                idt2 = (IdentifierToken) idt2.subTokens.get(0);
                                imp += "." + idt2.value;
                                name = idt2.value;
                                unit.symbols.add(new ELSymbol(ELSymbol.Type.NAMESPACE_NAME, idt2.spanFirst()));
                            }
                        } else {
                            errors.error("Unexpected token found in import (expected identifier)", tokens.get(workingI).span());
                            continue;
                        }
                        workingI++;
                        if (tokens.get(workingI) instanceof IdentifierToken idt3 && idt3.value.equals("as")) {
                            workingI++;
                            unit.symbols.add(new ELSymbol(ELSymbol.Type.KEYWORD, idt3.span()));
                            if (tokens.get(workingI) instanceof IdentifierToken idt4) {
                                workingI++;
                                name = idt4.value;
                                if (currentNamespace != null) {
                                    errors.error("Import must be outside of namespace", idt);
                                    continue;
                                }
                                unit.symbols.add(new ELSymbol(ELSymbol.Type.NAMESPACE_NAME, idt2.span(), "`%s` as `%s`", name, imp));
                                unit.addImport(name, imp, nameToken);
                            } else {
                                errors.error("Unexpected token found in import (expected alias)", tokens.get(workingI).span());
                                continue;
                            }
                            if (!(tokens.get(workingI) instanceof OperatorToken ot
                                && ot.type == OperatorToken.Type.SEMICOLON)) {
                                errors.error("Unexpected token found in import (expected `;`)", tokens.get(workingI).span());
                                continue;
                            }
                            unit.addSymbol(ELSymbol.Type.SEMICOLON, tokens.get(workingI).span());
                        } else if (tokens.get(workingI) instanceof OperatorToken ot
                                && ot.type == OperatorToken.Type.SEMICOLON) {
                            unit.addSymbol(ELSymbol.Type.SEMICOLON, tokens.get(workingI).span());
                            if (currentNamespace != null) {
                                errors.error("Import must be outside of namespace", idt);
                                continue;
                            }
                            unit.symbols.add(new ELSymbol(ELSymbol.Type.NAMESPACE_NAME, idt2.span(), "`%s`", name));
                            unit.addImport(name, imp, nameToken);
                        } else {
                            errors.error("Unexpected token found in import (expected `as` or `;`)", tokens.get(workingI).span());
                            continue;
                        }
                    } else if (idt.value.equals("static") || idt.value.equals("const") || idt.value.equals("operator") || idt.value.equals("extern") || ELProtectionLevel.valid(idt.value)) {
                        // (<public|protected|private|internal>) (static) (<const|final>) [type] [name] (= [value]);
                        // (<public|protected|private|internal>) (static) (constexp) <[ret]|void> [name](...) {...}
                        // (<public|protected|private|internal>) (static) <extern|abstract> <[ret]|void> [name](...)
                        // (<public|protected|private|internal>) (constexp) (~)[name](...) {...}
                        // (<public|protected|private|internal>) extern (~)[name](...)
                        // operator (constexp) [ret] [name](...)
                        // System.err.println("Func or variable");
                        Location loc = idt.startLocation;
                        workingI++;
                        ELProtectionLevel level = ELProtectionLevel.get(idt.value, ELProtectionLevel.PROTECTED);
                        unit.addSymbol(ELSymbol.Type.KEYWORD, idt.span());
                        boolean stat = idt.value.equals("static");
                        boolean extern = idt.value.equals("extern");
                        Span operator = idt.value.equals("operator") ? idt.span() : null;
                        Span final_ = idt.value.equals("final") ? idt.span() : null;
                        Span const_ = idt.value.equals("const") ? idt.span() : null;
                        Span constexpr = null;
                        if (!stat)
                            if (tokens.get(workingI) instanceof IdentifierToken it && it.value.equals("static")) {
                                stat = true;
                                workingI++;
                                unit.addSymbol(ELSymbol.Type.KEYWORD, it.span());
                            }
                        if (const_ == null)
                            if (tokens.get(workingI) instanceof IdentifierToken it && it.value.equals("const")) {
                                const_ = tokens.get(workingI).span();
                                workingI++;
                                unit.addSymbol(ELSymbol.Type.KEYWORD, it.span());
                            }
                        if (!extern)
                            if (tokens.get(workingI) instanceof IdentifierToken it && it.value.equals("extern")) {
                                extern = true;
                                workingI++;
                                unit.addSymbol(ELSymbol.Type.KEYWORD, it.span());
                            }
                        if (final_ == null)
                            if (tokens.get(workingI) instanceof IdentifierToken it && it.value.equals("final")) {
                                final_ = tokens.get(workingI).span();
                                workingI++;
                                unit.addSymbol(ELSymbol.Type.KEYWORD, it.span());
                            }
                        if (tokens.get(workingI) instanceof IdentifierToken it && it.value.equals("constexpr")) {
                            constexpr = tokens.get(workingI).span();
                            workingI++;
                            unit.addSymbol(ELSymbol.Type.KEYWORD, it.span());
                        }
                        if (operator == null)
                            if (tokens.get(workingI) instanceof IdentifierToken it && it.value.equals("operator")) {
                                operator = tokens.get(workingI).span();
                                workingI++;
                                unit.addSymbol(ELSymbol.Type.KEYWORD, it.span());
                            }
                        Span abs = null;
                        if (tokens.get(workingI) instanceof IdentifierToken it2 && it2.value.equals("abstract")) {
                            abs = tokens.get(workingI).span();
                            workingI++;
                            unit.addSymbol(ELSymbol.Type.KEYWORD, it2.span());
                        }
                        boolean destructor = false;
                        if (tokens.get(workingI) instanceof OperatorToken o2
                                && o2.type == OperatorToken.Type.DESTRUCTOR) {
                            destructor = true;
                            workingI++;
                            unit.addSymbol(ELSymbol.Type.OPERATOR, o2.span());
                        }
                        if (tokens.get(workingI) instanceof IdentifierToken it && it.hasParams()) {
                            // Constructor/destructor
                            if (!(currentNamespace instanceof ELClass)) {
                                throw ELAnalysisError.error((destructor?"Destructor":"Constructor")+" only allowed in class", it);
                            }
                            ELClass currentClass = (ELClass) currentNamespace;
                            if (!currentClass.cName.equals(it.value)) {
                                throw ELAnalysisError.error("Invalid function definition", it);
                            }
                            ELFunction function = new ELFunction(level, extern, currentClass,
                                    currentClass.cName, destructor ? ELFunction.FunctionType.DESTRUCTOR : ELFunction.FunctionType.CONSTRUCTOR, false, unit, loc);
                            unit.addSymbol(new ELSymbol.ELFuncDefSymbol(function, it.spanFirst()));
                            if (docCommentToken != null) {
                                function.doc = new DocComment(docCommentToken);
                            }
                            function.ret = currentClass.getType();
                            function.ingestParams(it.params);
                            if (annotations != null)
                                function.annotations = annotations;
                            if (destructor) {
                                if (currentClass.destructor != null) {
                                    errors.error("Class " + currentClass.getQualifiedName() + " already had a destructor", function.span());
                                    continue;
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
                                unit.addSymbol(ELSymbol.Type.SEMICOLON, ot.span());
                                // no body;
                            } else {
                                errors.error("Unexpected token found, expected function body or `;`", tokens.get(workingI).span());
                                continue;
                            }
                            continue;
                        }
                        ELType.Builder typeBuilder = new ELType.Builder();
                        boolean ingestOne = false;
                        while (typeBuilder.ingest(tokens.get(workingI))) {
                            ingestOne = true;
                            workingI++;
                        }
                        if (!ingestOne) {
                            errors.error("Type building didn't ingest any tokens", tokens.get(workingI));
                            continue;
                        }
                        ELType type = typeBuilder.build();
                        // unit.symbols.add(new ELTypeSymbol(type));
                        String name;
                        IdentifierToken nameToken;

                        if (tokens.get(workingI) instanceof IdentifierToken it4) {
                            nameToken = it4;
                            name = it4.value;
                            workingI++;
                        } else {
                            errors.error("Unexpected token found for name (expected identifier)", tokens.get(workingI));
                            continue;
                        }
                        
                        if (nameToken.hasParams()) { // function
                            if (final_ != null) {
                                errors.error("Functions can not be made final.", final_);
                            }
                            if (const_ != null) {
                                errors.error("Functions can not be made constant.", const_);
                            }
                            // workingI++;
                            if (!(currentNamespace instanceof ELClass) && !stat) {
                                errors.error("Functions outside of a class must be marked static.", nameToken);
                                stat = true;
                            }
                            ELFunction.FunctionType funcType = stat ? ELFunction.FunctionType.STATIC
                                    : ELFunction.FunctionType.INSTANCE;
                            if (operator != null) {
                                if(currentNamespace instanceof ELClass) {
                                    funcType = ELFunction.FunctionType.OPERATOR;
                                } else {
                                    errors.error("Functions outside of a class may not be operator functions.", const_);
                                }
                            }
                            ELFunction function = new ELFunction(level, extern, currentNamespace, name, funcType, constexpr != null, unit, loc);
                            unit.addSymbol(new ELSymbol.ELFuncDefSymbol(function, nameToken.spanFirst()));
                            if (docCommentToken != null) {
                                function.doc = new DocComment(docCommentToken);
                            }
                            if (annotations != null)
                                function.annotations = annotations;
                            if (currentNamespace == null) {
                                errors.error("Can not have a function outside of namespace or class", function.span());
                                continue;
                            }

                            if (!type.isVoid())
                                function.ret = type;
                            else {
                                unit.addSymbol(ELSymbol.Type.KEYWORD, type.span());
                            }
                            function.abstractFunction = abs != null;
                            function.ingestParams(nameToken.params);
                            unit.addSymbol(ELSymbol.Type.FUNCTION_NAME, nameToken.spanFirst());
                            
                            if (stat) {
                                currentNamespace.addStaticFunction(function);
                            } else if (currentNamespace instanceof ELClass currentClass) {
                                currentClass.addFunction(function);
                            } else {
                                errors.error("Can not have non-static function outside of class", function.span());
                                continue;
                            }
                            if (tokens.get(workingI) instanceof BlockToken bt) {
                                function.ingestBody(bt);
                            } else if (tokens.get(workingI) instanceof OperatorToken ot
                                    && ot.type == OperatorToken.Type.SEMICOLON) {
                                // no body;
                                function.bodyLocation = ot.startLocation;
                                unit.addSymbol(ELSymbol.Type.SEMICOLON, ot.span());
                            } else {
                                errors.error("Unexpected token found, expected function body or `;`: "+tokens.get(workingI), tokens.get(workingI));
                                continue;
                            }
                            if (function.hasAnnotation(ELEntrypointAnnotation.class)) {
                                if (unit.module.entrypoint != null) {
                                    errors.error("Module already had a function marked as entrypoint: "+unit.module.entrypoint.debugString(""), tokens.get(workingI));
                                    continue;
                                }
                                unit.module.entrypoint = function;
                            }
                        } else { // variable
                            if (constexpr != null) {
                                errors.error("Variables can not be constant expression.", constexpr);
                            }
                            if (operator != null) {
                                errors.error("Variables can not be operator.", operator);
                            }
                            Location endLocation = tokens.get(workingI-1).endLocation;
                            if(abs != null) {
                                errors.error("Variables can not be marked abstract", abs);
                            }
                            if (!(currentNamespace instanceof ELClass) && !stat) {
                                errors.error("Variables outside of a class must be marked static.", nameToken);
                                stat = true;
                            }
                            ELVariable var = new ELVariable(level, (const_ != null) ? ELVariable.Type.CONST : (stat ? ELVariable.Type.STATIC : ELVariable.Type.MEMBER), type, name, final_ != null, currentNamespace, unit, loc, endLocation);
                            if (annotations != null)
                                var.annotations = annotations;
                            unit.symbols.add(new ELVarSymbol(var, nameToken.spanFirst()));
                            if (docCommentToken != null) {
                                var.doc = new DocComment(docCommentToken);
                            }
                            if (currentNamespace == null) {
                                errors.error("Can not have a variable outside of namespace or class", var.span());
                                continue;
                            }
                            else if (stat) {
                                currentNamespace.addStaticVariable(var);
                            } else if (currentNamespace instanceof ELClass currentClass) {
                                currentClass.addMember(var);
                            } else {
                                errors.error("Can not have non-static variable outside of class", var.span());
                                continue;
                            }
                            if (tokens.get(workingI) instanceof OperatorToken ot) {
                                switch (ot.type) {
                                    case SEMICOLON -> {
                                        unit.addSymbol(ELSymbol.Type.SEMICOLON, ot.span());
                                        workingI++;
                                        continue;
                                    }
                                    case ASSIGN -> {
                                        unit.addSymbol(ELSymbol.Type.OPERATOR, ot.span());
                                        workingI++;
                                    }
                                    default -> {
                                        errors.error("Unexpected token found, expected `;` or `=`", tokens.get(workingI));
                                        continue;
                                    }
                                }
                            } else {
                                errors.error("Unexpected token found, expected `;` or `=`", tokens.get(workingI));
                                continue;
                            }
                            var.valueLocation = tokens.get(workingI).startLocation;
                            while (var.ingestValue(tokens.get(workingI))) {
                                workingI++;
                            }
                            if(!(tokens.get(workingI) instanceof OperatorToken ot2 && ot2.type == Type.SEMICOLON)) {
                                errors.error("Unexpected token found, expected `;`", tokens.get(workingI));
                                continue;
                            }
                            unit.addSymbol(ELSymbol.Type.SEMICOLON, tokens.get(workingI).span());
                        }
                    } else if (idt.value.equals("namespace")) {
                        // namespace [name];
                        // namespace [name] {...}
                        unit.symbols.add(new ELSymbol(ELSymbol.Type.KEYWORD, idt.span()));
                        workingI++;
                        Namespace namespace = null;
                        if (tokens.get(workingI) instanceof IdentifierToken it) {
                            if (!it.simple()) {
                                throw ELAnalysisError.error("Namespace name can not contain index or params", it);
                            }
                            namespace = makeNamespace(it.value, namespace);
                            // System.out.println(namespace.getQualifiedName());
                            boolean err = false;
                            while (it.hasSub()) {
                                Token tkn = it.subTokens.get(0);
                                if (tkn instanceof IdentifierToken it2) {
                                    // System.out.println("- " + it2.value);
                                    namespace = makeNamespace(it2.value, namespace);
                                    // System.out.println(namespace.getQualifiedName());
                                    it = it2;
                                } else {
                                    errors.error("Unexpected token found, expected identifier", tkn);
                                    err = true;
                                    break;
                                }
                            }
                            unit.addSymbol(new ELSymbol.ELNamespaceSymbol(namespace, it.span()));
                            if(err)
                                continue;
                            namespaces.add(namespace);
                        } else {
                            errors.error("Unknown token found (expected identifier)", tokens.get(workingI));
                            continue;
                        }
                        workingI++;
                        if(tokens.size() <= workingI) {
                            errors.error("Unexpected end of tokens, expected block or `;`", tokens.get(tokens.size()-1).endLocation.span());
                            continue;
                        } else if (tokens.get(workingI) instanceof BlockToken bt) {
                            new Parser(unit, namespace).parse(bt.subTokens, errors);
                        } else if (tokens.get(workingI) instanceof OperatorToken ot
                                && ot.type == OperatorToken.Type.SEMICOLON) {
                            unit.addSymbol(ELSymbol.Type.SEMICOLON, ot.span());
                            currentNamespace = namespace;
                        } else {
                            errors.error("Unexpected token found, expected block or `;`", tokens.get(workingI));
                            continue;
                        }
                    } else if (idt.value.equals("abstract") || idt.value.equals("class") || idt.value.equals("struct")) {
                        // <struct|(abstract) class> [name] {...}
                        boolean abs = idt.value.equals("abstract");
                        unit.addSymbol(ELSymbol.Type.KEYWORD, idt.span());
                        if (abs) {
                            workingI++;
                            idt = (IdentifierToken) tokens.get(workingI);
                            if(idt == null)
                                continue;
                        }
                        boolean struct = idt.value.equals("struct");
                        unit.addSymbol(ELSymbol.Type.KEYWORD, idt.span());
                        workingI++;
                        ELClass clazz;
                        if (tokens.get(workingI) instanceof IdentifierToken it) {
                            if (struct)
                                clazz = new ELStruct(it.value, currentNamespace, unit);
                            else
                                clazz = new ELClass(it.value, currentNamespace, unit);
                            namespaces.add(clazz);
                            unit.addSymbol(new ELSymbol.ELNamespaceSymbol(clazz, it.span()));
                        } else {
                            errors.error("Unknown token found (expected identifier)", tokens.get(workingI));
                            continue;
                        }
                        if (annotations != null)
                            clazz.annotations = annotations;
                        clazz.abstractClass = abs;
                        workingI++;
                        if (tokens.get(workingI) instanceof OperatorToken ot
                                && ot.type == OperatorToken.Type.ANGLE_LEFT) {
                            workingI++;
                            unit.addSymbol(ELSymbol.Type.KEYWORD, ot.span());
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
                                        unit.addSymbol(ELSymbol.Type.CLASS_NAME, tit.span());
                                        workingI++;
                                    } else {
                                        errors.error("Unexpected token found in type (expected operator)", tkn);
                                        r = false;
                                    }
                                } else if (builder != null) {
                                    if (!builder.ingest(tkn)) {
                                        if (tkn instanceof OperatorToken ot2 && ot2.type == OperatorToken.Type.COMMA) {
                                            unit.addSymbol(ELSymbol.Type.KEYWORD, ot2.span());
                                            ELType gt = builder.build();
                                            gt.addSymbol(unit);
                                            clazz.generics.put(tName, gt);
                                            builder = null;
                                            tName = null;
                                            workingI++;
                                        } else if (tkn instanceof OperatorToken ot2
                                                && ot2.type == OperatorToken.Type.ANGLE_RIGHT) {
                                            unit.addSymbol(ELSymbol.Type.KEYWORD, ot2.span());
                                            ELType gt = builder.build();
                                            gt.addSymbol(unit);
                                            clazz.generics.put(tName, gt);
                                            r = false;
                                            workingI++;
                                        } else {
                                            errors.error("Unexpected token found in type (expected operator)", tkn);
                                            r = false;
                                        }
                                    } else {
                                        workingI++;
                                    }
                                } else {
                                    if (tkn instanceof OperatorToken ot2 && ot2.type == OperatorToken.Type.COMMA) {
                                        unit.addSymbol(ELSymbol.Type.KEYWORD, ot2.span());
                                        tName = null;
                                        workingI++;
                                    } else if (tkn instanceof OperatorToken ot2
                                            && ot2.type == OperatorToken.Type.ANGLE_RIGHT) {
                                        unit.addSymbol(ELSymbol.Type.KEYWORD, ot2.span());
                                        r = false;
                                        workingI++;
                                    } else {
                                        errors.error("Unexpected token found in type parameter (expected `extends`, `,` or `>`)", tkn);
                                        r = false;
                                    }
                                }
                            }
                        }
                        if (tokens.get(workingI) instanceof IdentifierToken tit) {
                            if (tit.value.equals("extends")) {
                                unit.addSymbol(ELSymbol.Type.KEYWORD, tit.span());
                                ELType.Builder builder = new ELType.Builder();
                                workingI++;
                                
                                while(builder.ingest(tokens.get(workingI)))
                                    workingI++;
                                ELType pt = builder.build();
                                pt.addSymbol(unit);
                                clazz.setParentType(pt);
                            }
                        }
                        if (tokens.get(workingI) instanceof BlockToken bt) {
                            new Parser(unit, clazz).parse(bt.subTokens, errors);
                        } else {
                            errors.error("Unknown token found (expected `extends` or block)", tokens.get(workingI));
                            continue;
                        }
                    }
                }
                workingI++;
            }
        } /* catch (IndexOutOfBoundsException e) {
             return Optional.of("Index out of bounds");
          } */ catch (ELCompileException e) {
            // String last = "";
            // boolean f = true;
            // if (workingI >= tokens.size())
            //     workingI = tokens.size() - 1;
            // for (int i = workingI < 4 ? 0 : workingI - 4; i <= workingI; i++) {
            //     if (!f)
            //         last += ", ";
            //     f = false;
            //     last += tokens.get(i);
            // }
            errors.error(e.getMessage(), tokens.get(workingI));
        } catch (ELAnalysisError e) {
            errors.add(e);
        }
    }
    
    private Namespace makeNamespace(String name, Namespace parent) {
        Namespace namespace;
        if(parent == null)
            namespace = unit.module.getNamespace(name);
        else
            namespace = unit.module.getNamespace(parent.getQualifiedName() + "." + name);
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
