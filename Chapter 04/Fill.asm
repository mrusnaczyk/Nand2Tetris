// This file is part of www.nand2tetris.org
// and the book "The Elements of Computing Systems"
// by Nisan and Schocken, MIT Press.
// File name: projects/04/Fill.asm

// Runs an infinite loop that listens to the keyboard input.
// When a key is pressed (any key), the program blackens the screen,
// i.e. writes "black" in every pixel;
// the screen should remain fully black as long as the key is pressed. 
// When no key is pressed, the program clears the screen, i.e. writes
// "white" in every pixel;
// the screen should remain fully clear as long as no key is pressed.

// Put your code here.

(RST)
	@SCREEN
	D=A
	@R2
	M=D

(LOOP)
	@KBD
	D=M	//Load keyboard input
	
	@DARK
	D;JGT	//Jump to DARK if key is pressed

(LIGHT)
	@R2	
	A=M
	M=0	//Set the color to white

	@R2
	M=M+1	//Increment the address of the current section of the screen

	@KBD
	D=M	//Load keyboard input

	@RST
	D;JGT	//If input has changed, start over

	@LIGHT
	0;JMP	

(DARK)
	@R2
	A=M
	M=-1	//Set the color to black

	@R2
	M=M+1	//Increment the address of the current section of the screen
	
	@KBD
	D=M

	@RST
	D;JEQ

	@DARK
	0;JMP


