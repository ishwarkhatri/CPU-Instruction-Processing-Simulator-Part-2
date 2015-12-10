package edu.binghamton.my.common;

public class Constants {

	public static final String[] ONE_OPERAND_INSTRUCTIONS_ARRAY = {"BZ", "BNZ"};
	public static final String[] TWO_OPERAND_INSTRUCTIONS_ARRAY = {"MOV", "MOVC", "JUMP", "BAL"};
	public static final String[] THREE_OPERAND_INSTRUCTIONS_ARRAY = {"ADD", "SUB", "MUL", "OR", "AND", "EX-OR", "LOAD", "STORE"};
	public static final String X_REGISTER = "X";
	public static final String WRITE_BACK = "WB";
	public static final String BZ_INSTRUCTION = "BZ";
	public static final String OR_INSTRUCTION = "OR"; 
	public static final String BNZ_INSTRUCTION = "BNZ";
	public static final String AND_INSTRUCTION = "AND";
	public static final String MOV_INSTRUCTION = "MOV";
	public static final String ADD_INSTRUCTION = "ADD";
	public static final String SUB_INSTRUCTION = "SUB";
	public static final String MUL_INSTRUCTION = "MUL";
	public static final String BAL_INSTRUCTION = "BAL";
	public static final String HALT_INSTRUCTION = "HALT";
	public static final String MOVC_INSTRUCTION = "MOVC";
	public static final String JUMP_INSTRUCTION = "JUMP";
 	public static final String LOAD_INSTRUCTION = "LOAD";
	public static final String EX_OR_INSTRUCTION = "EX-OR";
	public static final String STORE_INSTRUCTION = "STORE";
	public static final String INTEGER_FU = "INTEGER";
	public static final String MULTIPLY_FU = "MULTIPLY";
	public static final String MEMORY_FU = "MEMORY";
	public static final String SQUASH_INSTRUCTION = "SQUASH";
	public static final int ONE_OPERAND_INSTRUCTION = 0;
	public static final int TWO_OPERAND_INSTRUCTION = 1;
	public static final int THREE_OPERAND_INSTRUCTION = 2;
	public static final int INITIALIZE = 1;
	public static final int SIMULATE = 2;
	public static final int DISPLAY = 3;
	public static final int EXIT = 4;
	public static final String SPACE = " ";
	public static final int MAX_ISSUE_QUEUE_SIZE = 8;
	public static final int MAX_LSQ_SIZE = 4;
}
