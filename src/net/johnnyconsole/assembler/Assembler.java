package net.johnnyconsole.assembler;

import java.util.Scanner;

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

    //Flags: S Z O D P A
    private static final byte[] FLAGS = new byte[5],
                                RLP = new byte[32],
                                RDA = new byte[32],
                                RDB = new byte[32],
                                RDC = new byte[32],
                                RDD = new byte[32];

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
