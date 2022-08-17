# Assembler
Java-based, interpreted assembly language using a top-down recursive descent parsing algorithm

### Register Layout
ASSEMBLER uses data types similar to the Intel x86 assembler. Registers are either byte (8 bits/1 byte), word (16 bits/2 bytes) or doubleword (aka dword, 32 bits/4 bytes).

##### Doubleword registers
RAD, RBD, RCD, RDD

##### Word Registers
*Word registers are the __lower__ 16 bits of the doubleword register of the same letter.*

RAW, RBW, RCW, RDW

##### Byte Registers
*Each word register can be represented as __two__ byte registers, represented with the same letter, and __H__ for the __upper__ 8 bits and __L__ for the __lower__ 8 bits.*

RAH, RAL, RBH, RBL, RCH, RCL, RDH, RDL

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

| **Mnemonic** | **Description**             | **Operands**                                                             | **Order**    |
|--------------|-----------------------------|--------------------------------------------------------------------------|--------------|
| MOV          | Move                        | Two registers of the same size                                           | Source-First |
| MIM          | Move Immediate              | One immediate value and one register                                     | Source-First |
| MEX          | Move and Extend             | Two registers of different sizes: destination must be larger than source | Source-First |
| PBR          | Print Byte Register         | One Byte Register                                                        | ---          |
| PWR          | Print Word Register         | One Word Register                                                        | ---          |
| PDR          | Print Doubleword Register   | One Doubleword Register                                                  | ---          |
| RIN          | Input to Register           | One Register                                                             | ---          |
| NEG          | Negate Register (flip bits) | One Register                                                             | ---          |

### Language Specification
A complete EBNF spcecification can be found in the included Language Specification.txt file.

### Future Instructions
Currently planned future instructions, including their EBNF definitions, can be found in the included Future Instructions.txt file.