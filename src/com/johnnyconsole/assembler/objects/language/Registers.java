package com.johnnyconsole.assembler.objects.language;

import com.johnnyconsole.assembler.objects.language.error.IllegalOperationError;
import com.johnnyconsole.assembler.objects.language.error.InvalidRegisterError;

import java.util.Locale;

public final class Registers {

    public static Register EAX = new Register("EAX");
    public static Register EBX = new Register("EBX");
    public static Register ECX = new Register("ECX");
    public static Register EDX = new Register("EDX");
    public static Register ESI = new Register("ESI");
    public static Register EDI = new Register("EDI");
    public static Register EBP = new Register("EBP");
    public static Register ESP = new Register("ESP");
    public static Register EIP = new Register("EIP");
    public static Register EFLAGS = new Register("EFLAGS");

    public static Register get(String registerName) throws IllegalOperationError, InvalidRegisterError {
        if(registerName.equalsIgnoreCase("EIP") ||
                registerName.equalsIgnoreCase("IP") ||
                registerName.equalsIgnoreCase("EFLAGS") ||
                registerName.equalsIgnoreCase("FLAGS")) {
            throw new IllegalOperationError("Illegal Register Access: " + registerName);
        }
        switch (registerName.toUpperCase(Locale.ROOT)) {
            case "EAX":
            case "AX":
            case "AL":
            case "AH":
                return EAX;
            case "EBX":
            case "BX":
            case "BL":
            case "BH":
                return EBX;
            case "ECX":
            case "CX":
            case "CL":
            case "CH":
                return ECX;
            case "EDX":
            case "DX":
            case "DL":
            case "DH":
                return EDX;
            case "ESI":
            case "SI":
                return ESI;
            case "EDI":
            case "DI":
                return EDI;
            case "EBP":
            case "BP":
                return EBP;
            case "ESP":
            case "SP":
                return ESP;
            default:
                throw new InvalidRegisterError("Invalid Register: " + registerName);
        }
    }


}
