package com.johnnyconsole.assembler.objects.language;

import java.util.ArrayList;

public class Procedure {

    private final String name;
    private final ArrayList<String> instructions = new ArrayList<>();

    public Procedure(String name) {
        this.name = name;
    }

    public String name() {
        return name;
    }

    public String[] instructions() {
        return instructions.toArray(new String[0]);
    }

    public void addInstruction(String instruction) {
        instructions.add(instruction);
    }
}
