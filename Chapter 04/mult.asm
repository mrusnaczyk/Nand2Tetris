// This file is part of www.nand2tetris.org
// and the book "The Elements of Computing Systems"
// by Nisan and Schocken, MIT Press.
// File name: projects/04/Mult.asm

// Multiplies R0 and R1 and stores the result in R2.
// (R0, R1, R2 refer to RAM[0], RAM[1], and RAM[2], respectively.)

// Put your code here.

@R2
M=0	//Reset Mem[2]

(LOOP)
	@R1
	D=M	//Load Mem[1] into the D register 

	@END
	D;JEQ	//Jump to END if D (Mem[1]) is zero

	@R1	
	M=D-1	//Decrement Mem[1]

	@R0
	D=M	//Load Mem[0] into D

	@R2
	M=M+D	//Increment Mem[2] by D

	@LOOP
	0;JMP

(END)
	@END	
	0;JMP
