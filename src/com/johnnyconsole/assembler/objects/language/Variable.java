package com.johnnyconsole.assembler.objects.language;

import com.johnnyconsole.assembler.enumeration.Type;

public class Variable {

    private final String name;
    private final Type type;
    private int value;

    public Variable(String name, Type type, int value) {
        this.name = name;
        this.type = type;
        this.value = value;
    }

    public Variable(String name, Type type) {
        this(name, type, 0);
    }

    public String name() {
        return name;
    }

    public Type type() {
        return type;
    }

    public int value() {
        return value;
    }

    public void setValue(int value) {
        this.value = value;
    }

    public String dump() {
        return "Variable[" + name + type.name() + value + "]";
    }

    @Override
    public String toString() {
        return value + "";
    }
}
