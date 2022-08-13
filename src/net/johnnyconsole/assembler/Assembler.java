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
}
