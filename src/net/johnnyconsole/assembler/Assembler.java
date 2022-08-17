package net.johnnyconsole.assembler;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.Arrays;
import java.util.Scanner;

@SuppressWarnings({"DuplicatedCode", "unused"})
public class Assembler {
    private static int charClass, lexLen, nextToken, lineNo = -1;
    private static char nextChar;
    private static char[] lexeme = new char[100];
    private static Scanner in_fp, input;
    private static String line;
    private static boolean inComment = false;

    private static final String[] BYTE_REGISTERS = {"RAH", "RAL", "RBH", "RBL", "RCH", "RCL", "RDH", "RDL"};
    private static final String[] WORD_REGISTERS = {"RAW", "RBW", "RCW", "RDW"};
    private static final String[] DWORD_REGISTERS = {"RAD", "RBD", "RCD", "RDD"};

    private static final int EOF = -1, LETTER = 0, DIGIT = 1, UNKNOWN = 99;
    private static final int INT_LIT = 10, IDENT = 11, COMMA = 20, PERIOD = 21, COLON = 22, SEMICOLON = 23;

    private static final int SIGN = 0,
                             ZERO = 1,
                             OVERFLOW = 2,
                             GREATER = 3,
                             LESS = 4;

    private static final byte[] FLAGS = new byte[5],
                                RLP = new byte[32],
                                RAD = new byte[32],
                                RBD = new byte[32],
                                RCD = new byte[32],
                                RDD = new byte[32];

    public static void main(String[] args) {
        input = new Scanner(System.in);
        String filename = "AssemblerInput.asmblr";
        try {
            if (args.length == 2 && (args[0].equals("-input") || args[0].equals("-i") || args[0].equals("-assemble") || args[0].equals("-a"))) {
                filename = args[1];
            }
            if (!filename.endsWith(".asmblr"))
                throw new AssemblerException("Incorrect file type: \"" + filename.substring(filename.indexOf('.')) + "\"");
            File file = new File(filename);
            in_fp = new Scanner(file);
            System.out.println("Executing ASSEMBLER code from input file: " + filename + "...");
            if(parse()) System.out.println("ASSEMBLER file executed successfully.");
        } catch(FileNotFoundException ex) {
            throw new AssemblerException("Could not open input file \"" + filename + "\"");
        }
    }

    private static int lex() {
        lexLen = 0;
        lexeme = new char[lexeme.length];
        getNonBlank();
        switch(charClass) {
            case LETTER:
                addChar();
                getChar();
                while(charClass == LETTER) {
                    addChar();
                    getChar();
                }
                nextToken = IDENT;
                break;
            case DIGIT:
                addChar();
                getChar();
                while(charClass == DIGIT) {
                    addChar();
                    getChar();
                }
                nextToken = INT_LIT;
                break;
            case UNKNOWN:
                lookup(nextChar);
                getChar();
                break;
            case EOF:
                nextToken = EOF;
                lexeme[0] = 'E';
                lexeme[1] = 'O';
                lexeme[2] = 'F';
                lexeme[3] = 0;
                return nextToken;
        }
        if(lexLen == 0) {
            return lex();
        }

        return nextToken;
    }

    private static boolean parse() {
        lex();
        if(trim(lexeme).equals("ASSEMBLER")) {
            lex();
            if(trim(lexeme).equals("BEGIN")) {
                lex();
                if(nextToken == COLON) {
                    lex();
                    statements();
                    if(trim(lexeme).equals("ASSEMBLER")) {
                        lex();
                        if (trim(lexeme).equals("END")) {
                            lex();
                            if (nextToken == SEMICOLON) return true;
                            else throw new AssemblerException("Missing symbol ';' at end of ASSEMBLER END directive");
                        }
                        else throw new AssemblerException("Missing END directive");
                    }
                    else throw new AssemblerException("Missing ASSEMBLER directive");
                }
                else throw new AssemblerException("Missing symbol ':' at end of ASSEMBLER BEGIN directive");
            }
            else throw new AssemblerException("Missing BEGIN directive, or missing symbol ':' at end of ASSEMBLER BEGIN directive");
        }
        else throw new AssemblerException("Missing ASSEMBLER directive");
    }

    private static void statements() {
        while(!trim(lexeme).equals("ASSEMBLER")) {
            statement();
            //CMT directive wll lex the period
            if(nextToken != PERIOD) lex();
            if(nextToken != PERIOD) throw new AssemblerException("Missing symbol '.' at end of statement on line " + lineNo);
            lex();
        }
    }

    private static void statement() {
        String statement = trim(lexeme);
        switch (statement) {
            case "MOV":
                lex();
                String reg1 = trim(lexeme);
                lex();
                if(nextToken != COMMA) throw new AssemblerException("Missing Symbol ',' for MOV on line " + lineNo);
                lex();
                String reg2 = trim(lexeme);
                mov(reg1, reg2);
                break;
            case "MIM":
                try {
                    lex();
                    int immediate = Integer.parseInt(trim(lexeme));
                    lex();
                    if (nextToken != COMMA)
                        throw new AssemblerException("Missing symbol ',' for MIM on line " + lineNo);
                    lex();
                    String register = trim(lexeme);
                    mim(immediate, register);
                } catch (NumberFormatException ex) {
                    throw new AssemblerException("Invalid Immediate for MIM on line " + lineNo);
                }
                break;
            case "MEX":
                lex();
                String src = trim(lexeme);
                lex();
                if(nextToken != COMMA) throw new AssemblerException("Missing symbol ',' for MEX on line " + lineNo);
                lex();
                String dest = trim(lexeme);
                mex(src, dest);
                break;
            case "PBR": {
                lex();
                String register = trim(lexeme);
                pbr(register);
                break;
            }
            case "PWR": {
                lex();
                String register = trim(lexeme);
                pwr(register);
                break;
            }
            case "PDR": {
                lex();
                String register = trim(lexeme);
                pdr(register);
                break;
            }
            case "CMT":
                inComment = true;
                while (nextToken != PERIOD) {
                    lex();
                }
                inComment = false;
                break;
            case "BCM":
                inComment = true;
                while (!trim(lexeme).equals("ECM")) {
                    lex();
                }
                inComment = false;
                break;
            case "RIN": {
                lex();
                String register = trim(lexeme);
                rin(register);
                break;
            }
            case "NEG": {
                lex();
                String register = trim(lexeme);
                neg(register);
                break;
            }

            default:
                throw new AssemblerException("Invalid Instruction: " + statement + " on line " + lineNo);
        }
    }

    private static byte[] toBinaryArray(int number, int bits) {
        byte[] binary = new byte[bits];
        if(number < 0 || number >= Math.pow(2, bits)) throw new AssemblerException("Immediate " + number + " is either negative or too large");
        int i = bits - 1;
        while(number != 0) {
            binary[i--] = (byte)(number % 2);
            number /= 2;
        }
        return binary;
    }

    private static boolean arrayContains(String[] array, String value) {
        for (String s : array) {
            if (s.equals(value)) return true;
        }
        return false;
    }

    private static void mov(String src, String dest) {
        if(arrayContains(BYTE_REGISTERS, src) && arrayContains(BYTE_REGISTERS, dest)) {
            if(src.equals(dest)) return;
            switch(src) {
                case "RAL":
                    switch(dest) {
                        case "RAH":
                            System.arraycopy(RAD, 24, RAD, 16, 8);
                            break;
                        case "RBL":
                            System.arraycopy(RAD, 24, RBD, 24,8);
                            break;
                        case "RBH":
                            System.arraycopy(RAD, 24, RBD, 16, 8);
                            break;
                        case "RCL":
                            System.arraycopy(RAD, 24, RCD, 24,8);
                            break;
                        case "RCH":
                            System.arraycopy(RAD, 24, RCD, 16, 8);
                            break;
                        case "RDL":
                            System.arraycopy(RAD, 24, RDD, 24,8);
                            break;
                        case "RDH":
                            System.arraycopy(RAD, 24, RDD, 16, 8);
                            break;
                    }
                    break;
                case "RAH":
                    switch(dest) {
                        case "RAL":
                            System.arraycopy(RAD, 16, RAD, 24, 8);
                            break;
                        case "RBL":
                            System.arraycopy(RAD, 16, RBD, 24,8);
                            break;
                        case "RBH":
                            System.arraycopy(RAD, 16, RBD, 16, 8);
                            break;
                        case "RCL":
                            System.arraycopy(RAD, 16, RCD, 24,8);
                            break;
                        case "RCH":
                            System.arraycopy(RAD, 16, RCD, 16, 8);
                            break;
                        case "RDL":
                            System.arraycopy(RAD, 16, RDD, 24,8);
                            break;
                        case "RDH":
                            System.arraycopy(RAD, 16, RDD, 16, 8);
                            break;
                    }
                    break;
                case "RBL":
                    switch(dest) {
                        case "RAH":
                            System.arraycopy(RBD, 24, RAD, 16, 8);
                            break;
                        case "RAL":
                            System.arraycopy(RBD, 24, RAD, 24,8);
                            break;
                        case "RBH":
                            System.arraycopy(RBD, 24, RBD, 16, 8);
                            break;
                        case "RCL":
                            System.arraycopy(RBD, 24, RCD, 24,8);
                            break;
                        case "RCH":
                            System.arraycopy(RBD, 24, RCD, 16, 8);
                            break;
                        case "RDL":
                            System.arraycopy(RBD, 24, RDD, 24,8);
                            break;
                        case "RDH":
                            System.arraycopy(RBD, 24, RDD, 16, 8);
                            break;
                    }
                    break;
                case "RBH":
                    switch(dest) {
                        case "RAL":
                            System.arraycopy(RBD, 16, RAD, 24, 8);
                            break;
                        case "RBL":
                            System.arraycopy(RBD, 16, RBD, 24,8);
                            break;
                        case "RAH":
                            System.arraycopy(RBD, 16, RAD, 16, 8);
                            break;
                        case "RCL":
                            System.arraycopy(RBD, 16, RCD, 24,8);
                            break;
                        case "RCH":
                            System.arraycopy(RBD, 16, RCD, 16, 8);
                            break;
                        case "RDL":
                            System.arraycopy(RBD, 16, RDD, 24,8);
                            break;
                        case "RDH":
                            System.arraycopy(RBD, 16, RDD, 16, 8);
                            break;
                    }
                    break;
                case "RCL":
                    switch(dest) {
                        case "RAH":
                            System.arraycopy(RCD, 24, RAD, 16, 8);
                            break;
                        case "RAL":
                            System.arraycopy(RCD, 24, RAD, 24,8);
                            break;
                        case "RBH":
                            System.arraycopy(RCD, 24, RBD, 16, 8);
                            break;
                        case "RBL":
                            System.arraycopy(RCD, 24, RBD, 24,8);
                            break;
                        case "RCH":
                            System.arraycopy(RCD, 24, RCD, 16, 8);
                            break;
                        case "RDL":
                            System.arraycopy(RCD, 24, RDD, 24,8);
                            break;
                        case "RDH":
                            System.arraycopy(RCD, 24, RDD, 16, 8);
                            break;
                    }
                    break;
                case "RCH":
                    switch(dest) {
                        case "RAL":
                            System.arraycopy(RCD, 16, RAD, 24, 8);
                            break;
                        case "RBL":
                            System.arraycopy(RCD, 16, RBD, 24,8);
                            break;
                        case "RAH":
                            System.arraycopy(RCD, 16, RAD, 16, 8);
                            break;
                        case "RCL":
                            System.arraycopy(RCD, 16, RCD, 24,8);
                            break;
                        case "RBH":
                            System.arraycopy(RCD, 16, RBD, 16, 8);
                            break;
                        case "RDL":
                            System.arraycopy(RCD, 16, RDD, 24,8);
                            break;
                        case "RDH":
                            System.arraycopy(RCD, 16, RDD, 16, 8);
                            break;
                    }
                    break;
                case "RDL":
                    switch(dest) {
                        case "RAH":
                            System.arraycopy(RDD, 24, RAD, 16, 8);
                            break;
                        case "RAL":
                            System.arraycopy(RDD, 24, RAD, 24,8);
                            break;
                        case "RBH":
                            System.arraycopy(RDD, 24, RBD, 16, 8);
                            break;
                        case "RCL":
                            System.arraycopy(RDD, 24, RCD, 24,8);
                            break;
                        case "RCH":
                            System.arraycopy(RDD, 24, RCD, 16, 8);
                            break;
                        case "RBL":
                            System.arraycopy(RDD, 24, RBD, 24,8);
                            break;
                        case "RDH":
                            System.arraycopy(RDD, 24, RDD, 16, 8);
                            break;
                    }
                    break;
                case "RDH":
                    switch(dest) {
                        case "RAL":
                            System.arraycopy(RDD, 16, RAD, 24, 8);
                            break;
                        case "RBL":
                            System.arraycopy(RDD, 16, RBD, 24,8);
                            break;
                        case "RAH":
                            System.arraycopy(RDD, 16, RAD, 16, 8);
                            break;
                        case "RCL":
                            System.arraycopy(RDD, 16, RCD, 24,8);
                            break;
                        case "RCH":
                            System.arraycopy(RDD, 16, RCD, 16, 8);
                            break;
                        case "RDL":
                            System.arraycopy(RDD, 16, RDD, 24,8);
                            break;
                        case "RBH":
                            System.arraycopy(RDD, 16, RBD, 16, 8);
                            break;
                    }
                    break;
            }
        } else if(arrayContains(WORD_REGISTERS, src) && arrayContains(WORD_REGISTERS, dest)) {
            switch (src) {
                case "RAW":
                    switch (dest) {
                        case "RBW":
                            System.arraycopy(RAD, 16, RBD, 16, 16);
                            break;
                        case "RCW":
                            System.arraycopy(RAD, 16, RCD, 16, 16);
                            break;
                        case "RDW":
                            System.arraycopy(RAD, 16, RDD, 16, 16);
                            break;
                    }
                    break;
                case "RBW":
                    switch (dest) {
                        case "RAW":
                            System.arraycopy(RBD, 16, RAD, 16, 16);
                            break;
                        case "RCW":
                            System.arraycopy(RBD, 16, RCD, 16, 16);
                            break;
                        case "RDW":
                            System.arraycopy(RBD, 16, RDD, 16, 16);
                            break;
                    }
                    break;
                case "RCW":
                    switch (dest) {
                        case "RAW":
                            System.arraycopy(RCD, 16, RAD, 16, 16);
                            break;
                        case "RBW":
                            System.arraycopy(RCD, 16, RBD, 16, 16);
                            break;
                        case "RDW":
                            System.arraycopy(RCD, 16, RDD, 16, 16);
                            break;
                    }
                    break;
                case "RDW":
                    switch (dest) {
                        case "RAW":
                            System.arraycopy(RDD, 16, RAD, 16, 16);
                            break;
                        case "RCW":
                            System.arraycopy(RDD, 16, RCD, 16, 16);
                            break;
                        case "RBW":
                            System.arraycopy(RDD, 16, RBD, 16, 16);
                            break;
                    }
                    break;
            }
        }
        else if(arrayContains(DWORD_REGISTERS, src) && arrayContains(DWORD_REGISTERS, dest)) {
            switch (src) {
                case "RAD":
                    switch (dest) {
                        case "RBD":
                            System.arraycopy(RAD, 0, RBD, 0, 32);
                            break;
                        case "RCD":
                            System.arraycopy(RAD, 0, RCD, 0, 32);
                            break;
                        case "RDD":
                            System.arraycopy(RAD, 0, RDD, 0, 32);
                            break;
                    }
                    break;
                case "RBD":
                    switch (dest) {
                        case "RAD":
                            System.arraycopy(RBD, 0, RAD, 0, 32);
                            break;
                        case "RCD":
                            System.arraycopy(RBD, 0, RCD, 0, 32);
                            break;
                        case "RDD":
                            System.arraycopy(RBD, 0, RDD, 0, 32);
                            break;
                    }
                    break;
                case "RCD":
                    switch (dest) {
                        case "RBD":
                            System.arraycopy(RCD, 0, RBD, 0, 32);
                            break;
                        case "RAD":
                            System.arraycopy(RCD, 0, RAD, 0, 32);
                            break;
                        case "RDD":
                            System.arraycopy(RCD, 0, RDD, 0, 32);
                            break;
                    }
                    break;
                case "RDD":
                    switch (dest) {
                        case "RBD":
                            System.arraycopy(RDD, 0, RBD, 0, 32);
                            break;
                        case "RCD":
                            System.arraycopy(RDD, 0, RCD, 0, 32);
                            break;
                        case "RAD":
                            System.arraycopy(RDD, 0, RAD, 0, 32);
                            break;
                    }
                    break;
            }

        } else throw new AssemblerException("Instruction operands (" + src + ", " + dest + ") must be the same size for MOV instruction");
    }

    private static void mim(int immediate, String register) {
        if(arrayContains(BYTE_REGISTERS, register)) {
            byte[] bits = toBinaryArray(immediate, 8);
            switch(register) {
                case "RAL":
                    System.arraycopy(bits, 0, RAD, 24,8);
                    break;
                case "RAH":
                    System.arraycopy(bits, 0, RAD, 16, 8);
                    break;
                case "RBL":
                    System.arraycopy(bits, 0, RBD, 24,8);
                    break;
                case "RBH":
                    System.arraycopy(bits, 0, RBD, 16, 8);
                    break;
                case "RCL":
                    System.arraycopy(bits, 0, RCD, 24,8);
                    break;
                case "RCH":
                    System.arraycopy(bits, 0, RCD, 16, 8);
                    break;
                case "RDL":
                    System.arraycopy(bits, 0, RDD, 24,8);
                    break;
                case "RDH":
                    System.arraycopy(bits, 0, RDD, 16, 8);
                    break;
            }
        }
        else if(arrayContains(WORD_REGISTERS, register)) {
            byte[] bits = toBinaryArray(immediate, 16);
            switch (register) {
                case "RAW":
                    System.arraycopy(bits, 0, RAD, 16, 16);
                    break;
                case "RBW":
                    System.arraycopy(bits, 0, RBD, 16, 16);
                    break;
                case "RCW":
                    System.arraycopy(bits, 0, RCD, 16, 16);
                    break;
                case "RDW":
                    System.arraycopy(bits, 0, RDD, 16, 16);
                    break;
            }
        } else if(arrayContains(DWORD_REGISTERS, register)) {
            byte[] bits = toBinaryArray(immediate, 32);
            switch (register) {
                case "RAD":
                    System.arraycopy(bits, 0, RAD, 0, 32);
                    break;
                case "RBD":
                    System.arraycopy(bits, 0, RBD, 0, 32);
                    break;
                case "RCD":
                    System.arraycopy(bits, 0, RCD, 0, 32);
                    break;
                case "RDD":
                    System.arraycopy(bits, 0, RDD, 0, 32);
                    break;
            }
        }
        else throw new AssemblerException("Incorrect Register for MIM: \"" + register + "\"");
    }

    private static void mex(String src, String dest) {
        if(arrayContains(BYTE_REGISTERS, src) && arrayContains(WORD_REGISTERS, dest)) {
            byte[] newBits = new byte[16];
            switch(src) {
                case "RAL":
                    System.arraycopy(RAD, 24, newBits, 8, 8);
                    switch(dest) {
                        case "RAW":
                            System.arraycopy(newBits, 0, RAD, 16,16);
                            break;
                        case "RBW":
                            System.arraycopy(newBits, 0, RBD, 16,16);
                            break;
                        case "RCW":
                            System.arraycopy(newBits, 0, RCD, 16,16);
                            break;
                        case "RDW":
                            System.arraycopy(newBits, 0, RDD, 16,16);
                            break;
                    }
                    break;
                case "RAH":
                    System.arraycopy(RAD, 16, newBits, 8, 8);
                    switch(dest) {
                        case "RAW":
                            System.arraycopy(newBits, 0, RAD, 16,16);
                            break;
                        case "RBW":
                            System.arraycopy(newBits, 0, RBD, 16,16);
                            break;
                        case "RCW":
                            System.arraycopy(newBits, 0, RCD, 16,16);
                            break;
                        case "RDW":
                            System.arraycopy(newBits, 0, RDD, 16,16);
                            break;
                    }
                    break;
                case "RBL":
                    System.arraycopy(RBD, 24, newBits, 8, 8);
                    switch(dest) {
                        case "RAW":
                            System.arraycopy(newBits, 0, RAD, 16,16);
                            break;
                        case "RBW":
                            System.arraycopy(newBits, 0, RBD, 16,16);
                            break;
                        case "RCW":
                            System.arraycopy(newBits, 0, RCD, 16,16);
                            break;
                        case "RDW":
                            System.arraycopy(newBits, 0, RDD, 16,16);
                            break;
                    }
                    break;
                case "RBH":
                    System.arraycopy(RBD, 16, newBits, 8, 8);
                    switch(dest) {
                        case "RAW":
                            System.arraycopy(newBits, 0, RAD, 16,16);
                            break;
                        case "RBW":
                            System.arraycopy(newBits, 0, RBD, 16,16);
                            break;
                        case "RCW":
                            System.arraycopy(newBits, 0, RCD, 16,16);
                            break;
                        case "RDW":
                            System.arraycopy(newBits, 0, RDD, 16,16);
                            break;
                    }
                    break;
                case "RCL":
                    System.arraycopy(RCD, 24, newBits, 8, 8);
                    switch(dest) {
                        case "RAW":
                            System.arraycopy(newBits, 0, RAD, 16,16);
                            break;
                        case "RBW":
                            System.arraycopy(newBits, 0, RBD, 16,16);
                            break;
                        case "RCW":
                            System.arraycopy(newBits, 0, RCD, 16,16);
                            break;
                        case "RDW":
                            System.arraycopy(newBits, 0, RDD, 16,16);
                            break;
                    }
                    break;
                case "RCH":
                    System.arraycopy(RCD, 16, newBits, 8, 8);
                    switch(dest) {
                        case "RAW":
                            System.arraycopy(newBits, 0, RAD, 16,16);
                            break;
                        case "RBW":
                            System.arraycopy(newBits, 0, RBD, 16,16);
                            break;
                        case "RCW":
                            System.arraycopy(newBits, 0, RCD, 16,16);
                            break;
                        case "RDW":
                            System.arraycopy(newBits, 0, RDD, 16,16);
                            break;
                    }
                    break;
                case "RDL":
                    System.arraycopy(RDD, 24, newBits, 8, 8);
                    switch(dest) {
                        case "RAW":
                            System.arraycopy(newBits, 0, RAD, 16,16);
                            break;
                        case "RBW":
                            System.arraycopy(newBits, 0, RBD, 16,16);
                            break;
                        case "RCW":
                            System.arraycopy(newBits, 0, RCD, 16,16);
                            break;
                        case "RDW":
                            System.arraycopy(newBits, 0, RDD, 16,16);
                            break;
                    }
                    break;
                case "RDH":
                    System.arraycopy(RDD, 16, newBits, 8, 8);
                    switch(dest) {
                        case "RAW":
                            System.arraycopy(newBits, 0, RAD, 16,16);
                            break;
                        case "RBW":
                            System.arraycopy(newBits, 0, RBD, 16,16);
                            break;
                        case "RCW":
                            System.arraycopy(newBits, 0, RCD, 16,16);
                            break;
                        case "RDW":
                            System.arraycopy(newBits, 0, RDD, 16,16);
                            break;
                    }
                    break;
            }

        } else if(arrayContains(BYTE_REGISTERS, src) && arrayContains(DWORD_REGISTERS, dest)) {
            byte[] newBits = new byte[32];
            switch(src) {
                case "RAL":
                    System.arraycopy(RAD, 24, newBits, 24, 8);
                    switch(dest) {
                        case "RAD":
                            System.arraycopy(newBits, 0, RAD, 0,32);
                            break;
                        case "RBD":
                            System.arraycopy(newBits, 0, RBD, 0,32);
                            break;
                        case "RCD":
                            System.arraycopy(newBits, 0, RCD, 0,32);
                            break;
                        case "RDD":
                            System.arraycopy(newBits, 0, RDD, 0,32);
                            break;
                    }
                    break;
                case "RAH":
                    System.arraycopy(RAD, 16, newBits, 24, 8);
                    switch(dest) {
                        case "RAD":
                            System.arraycopy(newBits, 0, RAD, 0,32);
                            break;
                        case "RBD":
                            System.arraycopy(newBits, 0, RBD, 0,32);
                            break;
                        case "RCD":
                            System.arraycopy(newBits, 0, RCD, 0,32);
                            break;
                        case "RDD":
                            System.arraycopy(newBits, 0, RDD, 0,32);
                            break;
                    }
                    break;
                case "RBL":
                    System.arraycopy(RBD, 24, newBits, 24, 8);
                    switch(dest) {
                        case "RAD":
                            System.arraycopy(newBits, 0, RAD, 0,32);
                            break;
                        case "RBD":
                            System.arraycopy(newBits, 0, RBD, 0,32);
                            break;
                        case "RCD":
                            System.arraycopy(newBits, 0, RCD, 0,32);
                            break;
                        case "RDD":
                            System.arraycopy(newBits, 0, RDD, 0,32);
                            break;
                    }
                    break;
                case "RBH":
                    System.arraycopy(RBD, 16, newBits, 24, 8);
                    switch(dest) {
                        case "RAD":
                            System.arraycopy(newBits, 0, RAD, 0,32);
                            break;
                        case "RBD":
                            System.arraycopy(newBits, 0, RBD, 0,32);
                            break;
                        case "RCD":
                            System.arraycopy(newBits, 0, RCD, 0,32);
                            break;
                        case "RDD":
                            System.arraycopy(newBits, 0, RDD, 0,32);
                            break;
                    }
                    break;
                case "RCL":
                    System.arraycopy(RCD, 24, newBits, 24, 8);
                    switch(dest) {
                        case "RAD":
                            System.arraycopy(newBits, 0, RAD, 0,32);
                            break;
                        case "RBD":
                            System.arraycopy(newBits, 0, RBD, 0,32);
                            break;
                        case "RCD":
                            System.arraycopy(newBits, 0, RCD, 0,32);
                            break;
                        case "RDD":
                            System.arraycopy(newBits, 0, RDD, 0,32);
                            break;
                    }
                    break;
                case "RCH":
                    System.arraycopy(RCD, 16, newBits, 24, 8);
                    switch(dest) {
                        case "RAD":
                            System.arraycopy(newBits, 0, RAD, 0,32);
                            break;
                        case "RBD":
                            System.arraycopy(newBits, 0, RBD, 0,32);
                            break;
                        case "RCD":
                            System.arraycopy(newBits, 0, RCD, 0,32);
                            break;
                        case "RDD":
                            System.arraycopy(newBits, 0, RDD, 0,32);
                            break;
                    }
                    break;
                case "RDL":
                    System.arraycopy(RDD, 24, newBits, 24, 8);
                    switch(dest) {
                        case "RAD":
                            System.arraycopy(newBits, 0, RAD, 0,32);
                            break;
                        case "RBD":
                            System.arraycopy(newBits, 0, RBD, 0,32);
                            break;
                        case "RCD":
                            System.arraycopy(newBits, 0, RCD, 0,32);
                            break;
                        case "RDD":
                            System.arraycopy(newBits, 0, RDD, 0,32);
                            break;
                    }
                    break;
                case "RDH":
                    System.arraycopy(RDD, 16, newBits, 24, 8);
                    switch(dest) {
                        case "RAD":
                            System.arraycopy(newBits, 0, RAD, 0,32);
                            break;
                        case "RBD":
                            System.arraycopy(newBits, 0, RBD, 0,32);
                            break;
                        case "RCD":
                            System.arraycopy(newBits, 0, RCD, 0,32);
                            break;
                        case "RDD":
                            System.arraycopy(newBits, 0, RDD, 0,32);
                            break;
                    }
                    break;
            }
        }
        else if(arrayContains(WORD_REGISTERS, src) && arrayContains(DWORD_REGISTERS, dest)) {
            byte[] newBits = new byte[32];
            switch(src) {
                case "RAW":
                    System.arraycopy(RAD, 16, newBits,  16, 16);
                    switch(dest) {
                        case "RAD":
                            System.arraycopy(newBits, 0, RAD, 0, 32);
                            break;
                        case "RBD":
                            System.arraycopy(newBits, 0, RBD, 0, 32);
                            break;
                        case "RCD":
                            System.arraycopy(newBits, 0, RCD, 0, 32);
                            break;
                        case "RDD":
                            System.arraycopy(newBits, 0, RDD, 0, 32);
                            break;
                    }
                    break;
                case "RBW":
                    System.arraycopy(RBD, 16, newBits, 16, 16);
                    switch(dest) {
                        case "RAD":
                            System.arraycopy(newBits, 0, RAD, 0, 32);
                            break;
                        case "RBD":
                            System.arraycopy(newBits, 0, RBD, 0, 32);
                            break;
                        case "RCD":
                            System.arraycopy(newBits, 0, RCD, 0, 32);
                            break;
                        case "RDD":
                            System.arraycopy(newBits, 0, RDD, 0, 32);
                            break;
                    }
                    break;
                case "RCW":
                    System.arraycopy(RCD, 16, newBits, 16, 16);
                    switch(dest) {
                        case "RAD":
                            System.arraycopy(newBits, 0, RAD, 0, 32);
                            break;
                        case "RBD":
                            System.arraycopy(newBits, 0, RBD, 0, 32);
                            break;
                        case "RCD":
                            System.arraycopy(newBits, 0, RCD, 0, 32);
                            break;
                        case "RDD":
                            System.arraycopy(newBits, 0, RDD, 0, 32);
                            break;
                    }
                    break;
                case "RDW":
                    System.arraycopy(RDD, 16, newBits, 16, 16);
                    switch(dest) {
                        case "RAD":
                            System.arraycopy(newBits, 0, RAD, 0, 32);
                            break;
                        case "RBD":
                            System.arraycopy(newBits, 0, RBD, 0, 32);
                            break;
                        case "RCD":
                            System.arraycopy(newBits, 0, RCD, 0, 32);
                            break;
                        case "RDD":
                            System.arraycopy(newBits, 0, RDD, 0, 32);
                            break;
                    }
                    break;
            }
        } else throw new AssemblerException("Invalid Instruction format for MEX");
    }

    private static void pdr(String register) {
        if(arrayContains(DWORD_REGISTERS, register)) {
            System.out.print(register + " = ");
            switch (register) {
                case "RAD":
                    System.out.println(Arrays.toString(RAD));
                    break;
                case "RBD":
                    System.out.println(Arrays.toString(RBD));
                    break;
                case "RCD":
                    System.out.println(Arrays.toString(RCD));
                    break;
                case "RDD":
                    System.out.println(Arrays.toString(RDD));
                    break;
            }
        }
        else throw new AssemblerException("Incorrect Register for PDR: \"" + register + "\"");
    }

    private static void pwr(String register) {
        if(arrayContains(WORD_REGISTERS, register)) {
            System.out.print(register + " = ");
            byte[] wreg = new byte[16];
            switch (register) {
                case "RAW":
                    System.arraycopy(RAD, 16, wreg, 0, 16);
                    break;
                case "RBW":
                    System.arraycopy(RBD, 16, wreg, 0, 16);
                    break;
                case "RCW":
                    System.arraycopy(RCD, 16, wreg, 0, 16);
                    break;
                case "RDW":
                    System.arraycopy(RDD, 16, wreg, 0, 16);
                    break;
            }
            System.out.println(Arrays.toString(wreg));
        }
        else throw new AssemblerException("Incorrect Register for PWR: \"" + register + "\"");
    }

    private static void pbr(String register) {
        if (arrayContains(BYTE_REGISTERS, register)) {
            System.out.print(register + " = ");
            byte[] breg = new byte[8];
            switch (register) {
                case "RAL":
                    System.arraycopy(RAD, 24, breg,0, 8);
                    break;
                case "RAH":
                    System.arraycopy(RAD, 16, breg,0, 8);
                    break;
                case "RBL":
                    System.arraycopy(RBD, 24, breg,0, 8);
                    break;
                case "RBH":
                    System.arraycopy(RBD, 16, breg,0, 8);
                    break;
                case "RCL":
                    System.arraycopy(RCD, 24, breg,0, 8);
                    break;
                case "RCH":
                    System.arraycopy(RCD, 16, breg,0, 8);
                    break;
                case "RDL":
                    System.arraycopy(RDD, 24, breg,0, 8);
                    break;
                case "RDH":
                    System.arraycopy(RDD, 16, breg,0, 8);
                    break;
            }
            System.out.println(Arrays.toString(breg));
        }
        else throw new AssemblerException("Incorrect Register for PBR: \"" + register + "\"");
    }

    private static void rin(String register) {
        int in = 0;
        try {
            System.out.print("RIN > ");
            in = input.nextInt();
            mim(in, register);
        } catch (AssemblerException ex) {
            if(ex.getMessage().contains("MIM")) {
                throw new AssemblerException("Incorrect Register or Value for RIN: register=\"" + register + "\" value=\"" + in +"\"");
            }
            else {
                throw ex;
            }
        }
    }

    private static void neg(String register) {
        if(arrayContains(DWORD_REGISTERS, register)) {
            switch(register) {
                case "RAD":
                    negate(RAD, 0, 32);
                    break;
                case "RBD":
                    negate(RBD, 0, 32);
                    break;
                case "RCD":
                    negate(RCD, 0, 32);
                    break;
                case "RDD":
                    negate(RDD, 0, 32);
                    break;
            }
        } else if(arrayContains(WORD_REGISTERS, register)) {
            switch(register) {
                case "RAW":
                    negate(RAD, 16, 32);
                    break;
                case "RBW":
                    negate(RBD, 16, 32);
                    break;
                case "RCW":
                    negate(RCD, 16, 32);
                    break;
                case "RDW":
                    negate(RDD, 16, 32);
                    break;
            }
        }
        else if(arrayContains(BYTE_REGISTERS, register)) {
            switch(register) {
                case "RAH":
                    negate(RAD, 16, 24);
                    break;
                case "RAL":
                    negate(RAD, 24, 32);
                    break;
                case "RBH":
                    negate(RBD, 16, 24);
                    break;
                case "RBL":
                    negate(RBD, 24, 32);
                    break;
                case "RCH":
                    negate(RCD, 16, 24);
                    break;
                case "RCL":
                    negate(RCD, 24, 32);
                    break;
                case "RDH":
                    negate(RDD, 16, 24);
                    break;
                case "RDL":
                    negate(RDD, 24, 32);
                    break;
            }
        }
        else {
            throw new AssemblerException("Invalid Register for NEG: \"" + register + "\"");

        }
    }

    private static void negate(byte[] register, int start, int end) {
        for(int i = start; i < end; i++) {
            if(register[i] == 0) register[i] = 1;
            else register[i] = 0;
        }
    }

    private static String trim(char[] array) {
        StringBuilder s = new StringBuilder();
        for (char c : array) {
            if (c == 0 || c == '\n') break;
            s.append(c);
        }
        return s.toString();
    }

    private static void addChar() {
        if(lexLen <= 98) {
            lexeme[lexLen++] = nextChar;
            lexeme[lexLen] = '\0';
        }
        else throw new AssemblerException("Lexeme too long");
    }

    private static void getChar() {
        while(line == null || line.length() == 0) {
            try {
                line = in_fp.nextLine();
                lineNo++;
            } catch(Exception ex) {
                charClass = EOF;
                return;
            }
        }
        nextChar = line.charAt(0);
        line = line.substring(1);
        if(Character.isAlphabetic(nextChar)) {
            charClass = LETTER;
        }
        else if(Character.isDigit(nextChar)) charClass = DIGIT;
        else charClass = UNKNOWN;
    }


    private static void getNonBlank() {
        while(Character.isWhitespace(nextChar) || nextChar == 0)
            getChar();
    }

    private static void lookup(char ch) {
        switch(ch) {
            case ',':
                addChar();
                nextToken = COMMA;
                break;
            case '.':
                addChar();
                nextToken = PERIOD;
                break;
            case ':':
                addChar();
                nextToken = COLON;
                break;
            case ';':
                addChar();
                nextToken = SEMICOLON;
                break;
            default:
                if(!inComment) throw new AssemblerException("Invalid Symbol: " + ch);
        }
    }
}
