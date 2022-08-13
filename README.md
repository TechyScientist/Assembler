# Assembler
Java-based, interpreted assembly language using a top-down recursive descent parsing algorithm

### Instruction Set
The current instruction set for ASSEMBLER includes 7 instructions and 6 directives.

#### Directives
| **Directive** | **Description**                                                               | **Example**                                      |
|---------------|-------------------------------------------------------------------------------|--------------------------------------------------|
| ASSEMBLER     | Used at the beginning and end of an ASSEMBLER file                            | *__ASSEMBLER__ BEGIN:* or *__ASSEMBLER__ END;*   |
| BEGIN         | Used at the beginning of an ASSEMBLER file                                    | *ASSEMBLER __BEGIN__:*                           |
| END           | Used at the end of an ASSEMBLER file                                          | *ASSEMBLER __END__;*                             |
| CMT           | Used like an instruction to denote a single line comment                      | *__CMT__* This is a comment.                     |
| BCM           | Used to begin a multiline block comment. Comment must begin on the same line. | *__BCM__* This is <br/>Multi-line comment *ECM*. |
| ECM           | Used to end a multiline block comment. Must be placed on the last line.       | *BCM* This is <br/>Multi-line comment *__ECM__*. |

#### Instructions
The instructions are basic, and they are based on the Intel x86 instruction set.

Instruction mnemonics are three letters in length and take a comma-seperated list of operands.

| **Instruction Mnemonic** | **Instruction Description** | **Operands**                                                             | **Order**    |
|--------------------------|-----------------------------|--------------------------------------------------------------------------|--------------|
| MOV                      | Move                        | Two registers of the same size                                           | Source-First |
| MIM                      | Move Immediate              | One immediate value and one register                                     | Source-First |
| MEX                      | Move and Extend             | Two registers of different sizes: destination must be larger than source | Source-First |
| PBR                      | Print Byte Register         | One Byte Register                                                        | ---          |
| PWR                      | Print Word Register         | One Word Register                                                        | ---          |
| PDR                      | Print Doubleword Register   | One Doubleword Register                                                  | ---          |
| RIN                      | Input to Register           | One Register                                                             | ---          |

### [Language Specification](Language Specification.txt)
### [Future Instructions](Future Instructions.txt)
