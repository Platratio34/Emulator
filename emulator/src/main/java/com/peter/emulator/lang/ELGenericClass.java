package com.peter.emulator.lang;

import java.util.ArrayList;

import com.peter.emulator.lang.tokens.IdentifierToken;

public class ELGenericClass extends ELClass {

    public final ELClass baseClass;
    public final ELType[] types;

    public ELGenericClass(ELClass clazz, ELType... types) {
        super(clazz.cName, clazz.namespace, clazz.unit);
        baseClass = clazz;
        this.types = types;
    }

    @Override
    public ELVariable getFirstVar(IdentifierToken id, ProgramUnit unit) {
        return baseClass.getFirstVar(id, unit);
    }

    @Override
    protected ELVariable getFirstVar(IdentifierToken id, int index, ProgramUnit unit) {
        return baseClass.getFirstVar(id, index, unit);
    }

    @Override
    protected ELFunction getFunction(String name) {
        return baseClass.getFunction(name);
    }

    @Override
    public int getOffset(String member) {
        return baseClass.getOffset(member);
    }

    @Override
    public String getQualifiedName() {
        return baseClass.getQualifiedName();
    }

    @Override
    public String getClassType() {
        return baseClass.getClassType();
    }

    @Override
    public int getSize() {
        return baseClass.getSize();
    }

    @Override
    public int getSizeWords() {
        return baseClass.getSizeWords();
    }

    @Override
    public ELType getType() {
        ELType ot = baseClass.getType();
        for (int i = 0; i < types.length; i++) {
            ot.genericTypes.set(i, types[i]);
        }
        return ot;
    }
    
    @Override
    public ELClass getType(ELType base, Namespace srcNs, ProgramUnit unit) {
        return baseClass.getType(base, srcNs, unit);
    }

    @Override
    public ELClass getType(ELType base, int lvl, Namespace srcNs, ProgramUnit unit) {
        return baseClass.getType(base, lvl, srcNs, unit);
    }

    @Override
    public ELVariable getVar(Identifier identifier) {
        return baseClass.getVar(identifier);
    }

    @Override
    public ELVariable getVar(Identifier identifier, int i) {
        return baseClass.getVar(identifier, i);
    }

    @Override
    public ArrayList<ELVariable> getVarStack(Identifier identifier, ArrayList<ELVariable> stack) {
        return baseClass.getVarStack(identifier, stack);
    }

    @Override
    protected ELVariable getVariable(String name) {
        return baseClass.getVariable(name);
    }

    @Override
    public boolean hasType(ELType base) {
        return baseClass.hasType(base);
    }

    @Override
    public boolean hasType(ELType base, int lvl) {
        return baseClass.hasType(base, lvl);
    }

    @Override
    protected boolean hasVariable(String name) {
        return baseClass.hasVariable(name);
    }

    @Override
    public boolean canStaticCast(ELType target) {
        return baseClass.canStaticCast(target);
    }

    @Override
    public String debugString() {
        return baseClass.debugString();
    }

    @Override
    public String debugString(String prefix) {
        return baseClass.debugString(prefix);
    }

    @Override
    protected ELFunction findFunction(String name, ArrayList<ELType> params) {
        return baseClass.findFunction(name, params);
    }

    @Override
    public boolean isParentClassOf(Namespace ns) {
        return baseClass.isParentClassOf(ns);
    }

    @Override
    public boolean isParentNSOf(Namespace ns) {
        return baseClass.isParentNSOf(ns);
    }

}
