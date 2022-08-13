# Assembler
Java-based, interpreted assembly language using a top-down recursive descent parsing algorithm

### Instruction Set
The current instruction set for ASSEMBLER includes 7 instructions and 6 directives.

#### Directives
| **Directive** | **Description**                                                               |
|---------------|-------------------------------------------------------------------------------|
| ASSEMBLER     | Used at the beginning and end of an ASSEMBLER file                            |
| BEGIN         | Used at the beginning of an ASSEMBLER file                                    |                           |
| END           | Used at the end of an ASSEMBLER file                                          |                            |
| CMT           | Used like an instruction to denote a single line comment                      |                     |
| BCM           | Used to begin a multiline block comment. Comment must begin on the same line. |
| ECM           | Used to end a multiline block comment. Must be placed on the last line.       |

#### Instructions
The instructions are basic, and they are based on the Intel x86 instruction set.

Instruction mnemonics are three letters in length and take a comma-seperated list of operands.

| **Mnemonic** | **Description**           | **Operands**                                                             | **Order**    |
|--------------|---------------------------|--------------------------------------------------------------------------|--------------|
| MOV          | Move                      | Two registers of the same size                                           | Source-First |
| MIM          | Move Immediate            | One immediate value and one register                                     | Source-First |
| MEX          | Move and Extend           | Two registers of different sizes: destination must be larger than source | Source-First |
| PBR          | Print Byte Register       | One Byte Register                                                        | ---          |
| PWR          | Print Word Register       | One Word Register                                                        | ---          |
| PDR          | Print Doubleword Register | One Doubleword Register                                                  | ---          |
| RIN          | Input to Register         | One Register                                                             | ---          |

### [Language Specification](Language Specification.txt)
### [Future Instructions](Future Instructions.txt)
