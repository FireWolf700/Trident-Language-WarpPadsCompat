package com.energyxxer.trident.compiler.semantics;

public class Symbol {

    public enum SymbolAccess {
        GLOBAL, LOCAL, PROTECTED
    }

    private final String name;
    private final SymbolAccess access;
    private Object value;

    public Symbol(String name) {
        this(name, SymbolAccess.PROTECTED);
    }

    public Symbol(String name, SymbolAccess access) {
        this(name, access, null);
    }

    public Symbol(String name, SymbolAccess access, Object value) {
        this.name = name;
        this.access = access;
        this.value = value;
    }

    public String getName() {
        return name;
    }

    public SymbolAccess getAccess() {
        return access;
    }

    public Object getValue() {
        return value;
    }

    public void setValue(Object value) {
        this.value = value;
    }
}
