package edu.binghamton.my.model;

import static edu.binghamton.my.common.Constants.*;

public enum InstructionType {

	BZ("BZ"),
	OR("OR"), 
	BNZ("BNZ"),
	AND("AND"),
	MOV("MOV"),
	ADD("ADD"),
	SUB("SUB"),
	MUL("MUL"),
	BAL("BAL"),
	HALT("HALT"),
	MOVC("MOVC"),
	JUMP("JUMP"),
 	LOAD("LOAD"),
	EX_OR("EX-OR"),
	STORE("STORE"),
	SQUASH("SQUASH");

	private String value;

	InstructionType(String val) {
		value = val;
	}

	public String getValue() {
		return this.value;
	}

	public static InstructionType getInstructionType(String instruction) {
		String type = instruction.split(" ")[0];
		switch (type) {
		case BZ_INSTRUCTION:
			return BZ;

		case OR_INSTRUCTION:
			return OR;

		case BNZ_INSTRUCTION:
			return BNZ;

		case AND_INSTRUCTION:
			return AND;
			
		case MOV_INSTRUCTION:
			return MOV;
			
		case ADD_INSTRUCTION:
			return ADD;
			
		case SUB_INSTRUCTION:
			return SUB;
			
		case MUL_INSTRUCTION:
			return MUL;
			
		case BAL_INSTRUCTION:
			return BAL;
			
		case HALT_INSTRUCTION:
			return HALT;
		
		case MOVC_INSTRUCTION:
			return MOVC;
		
		case JUMP_INSTRUCTION:
			return JUMP;
			
		case LOAD_INSTRUCTION:
			return LOAD;
			
		case STORE_INSTRUCTION:
			return STORE;
			
		case EX_OR_INSTRUCTION:
			return EX_OR;
			
		case SQUASH_INSTRUCTION:
			return SQUASH;
			
		default:
			return null;
		}
	}
}
