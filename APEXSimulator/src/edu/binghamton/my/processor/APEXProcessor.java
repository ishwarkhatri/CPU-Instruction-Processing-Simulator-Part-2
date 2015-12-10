package edu.binghamton.my.processor;

import static edu.binghamton.my.common.Utility.*;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;

import static edu.binghamton.my.common.Constants.*;

import edu.binghamton.my.common.CircularQueue;
import edu.binghamton.my.common.FileIO;
import edu.binghamton.my.model.FunctionalUnitType;
import edu.binghamton.my.model.Instruction;
import edu.binghamton.my.model.InstructionType;
import edu.binghamton.my.model.RenameTableEntry;

public class APEXProcessor {
	private static Integer PC = 20000;
	private static Integer UPDATED_PC = 0;
	private static String[] lastTwoInstructions = new String[2];
	private static LinkedList<String> printQueue = new LinkedList<>();
	private static Map<String, Integer> REGISTER_FILE = new HashMap<>();
	private static Queue<Instruction> fetchFetchLatch = new LinkedList<>();
	private static Queue<Instruction> fetchDecodeLatch = new LinkedList<>();
	private static Queue<Instruction> issueQueueIntegerFuLatch = new LinkedList<>();
	private static Queue<Instruction> issueQueueMultiplyFuLatch = new LinkedList<>();
	private static Queue<Instruction> lsqMemory1Latch = new LinkedList<>();
	private static Queue<String> decodeExecuteLatch = new LinkedList<>();
	private static Queue<String> executeMemoryLatch = new LinkedList<>();
	private static Queue<String> memoryWriteBackLatch = new LinkedList<>();
	private static List<String> instructionList = new ArrayList<>(PC);
	private static Integer[] MEMORY_ARRAY = new Integer[10000];
	private static String lastFetchedInstruction;
	private static boolean isFetch1Done, isFetch2Done, isDecodeDone, isExecuteDone, isMemoryDone, isWriteBackDone, isIntegerFuFree, isMultiplyFuFree, isMemoryFuFree;
	private static boolean BRANCH_TAKEN = false;
	private static boolean JUMP_DETECTED = false;
	private static boolean INVALID_PC = false;
	private static boolean HALT_ALERT;
	private static boolean ZERO_FLAG;
	private static List<Instruction> LSQ = new ArrayList<>();
	private static List<Instruction> ISSUE_QUEUE = new ArrayList<>();
	private static List<String> FREE_PHYSICAL_REGISTER_LIST = new ArrayList<>();
	private static Map<String, String> ARCHITECTURAL_TO_PHYSICAL_MAP = new HashMap<>();
	private static Map<String, RenameTableEntry> RENAME_TABLE = new HashMap<>();
	private static CircularQueue ROB = new CircularQueue();

	public static void init(File file) {
		echo("\nSet PC to 20000");
		PC = 20000;

		instructionList = FileIO.loadFile(file, PC);

		lastTwoInstructions[0] = lastTwoInstructions[1] = "";

		echo("Initialize Memory...");
		for(int i = 0; i < MEMORY_ARRAY.length; i++)
			MEMORY_ARRAY[i] = 0;

		echo("Initialize Registers...");
		for(String regName : REGISTER_FILE.keySet())
			REGISTER_FILE.put(regName, 0);

		echo("Reset flags...");
		isFetch1Done = isFetch2Done = isDecodeDone = isExecuteDone = isMemoryDone = isWriteBackDone = false;
		HALT_ALERT = false;
		BRANCH_TAKEN = false;
		JUMP_DETECTED = false;
		ZERO_FLAG = false;
		echo("\nSimulator state intialized successfully");
	}

	public static void displaySimulationResult() {
		display(printQueue, REGISTER_FILE, MEMORY_ARRAY);
	}
	
	public static void simulate(int cycleCount) {
		int cycle = 0;
		LinkedList<String> tempList = new LinkedList<>();
		while(cycle != cycleCount) {
			if(INVALID_PC || (HALT_ALERT && isFetch1Done && isFetch2Done && isDecodeDone && isExecuteDone  && isWriteBackDone)) {
				break;
			}

			doWriteBack();
			doExecution();
			doSelection();
			doDecode();
			doFetch2();
			doFetch1();
			while(!printQueue.isEmpty())
				tempList.add(printQueue.removeLast());

			cycle++;

		}
		printQueue.addAll(tempList);

		if(cycle != cycleCount && (INVALID_PC ||HALT_ALERT)) {
			displaySimulationResult();
			if(HALT_ALERT)
				echo("\nSimulation ended due to HALT instruction...");
			if(INVALID_PC)
				echo("\nSimulation ended due to bad PC value..." + PC);
			System.exit(0);
		}
	}

	private static void doFetch1() {
		if(!fetchFetchLatch.isEmpty()) {
			printQueue.add("Stall");
		} else if(PC == instructionList.size()) {
			isFetch1Done = true;
			printQueue.add("Done");
		} else {
			if(PC > instructionList.size()) {
				echo("Invalid PC value detected: " + PC);
				INVALID_PC = true;
			} else {
				String temp = instructionList.get(PC++);
				Instruction instruction = getInstructionObject(temp);
				fetchFetchLatch.add(instruction);

				/*if(HALT_ALERT) {
					isFetchDone = true;
					String temp = fetchDecodeLatch.poll();
					instruction = SQUASH_INSTRUCTION + SPACE + temp;
					fetchDecodeLatch.add(instruction);
				}

				if(BRANCH_TAKEN || JUMP_DETECTED) {
					String temp = fetchDecodeLatch.poll();
					instruction = SQUASH_INSTRUCTION + SPACE + temp;
					fetchDecodeLatch.add(instruction);
					PC = UPDATED_PC;
					if(BRANCH_TAKEN)
						BRANCH_TAKEN = false;
					if(JUMP_DETECTED)
						JUMP_DETECTED = false;
				}*/

				printQueue.add(instruction.toString());
			}
		}		
	}

	private static Instruction getInstructionObject(String instruction) {
		String[] parts = instruction.split(" ");
		InstructionType type = InstructionType.getInstructionType(instruction);
		FunctionalUnitType fuType = FunctionalUnitType.getFunctionalUnitType(type.getValue());

		Instruction instructObj = new Instruction();
		instructObj.setOpCode(type);
		if(parts.length > 1)
			instructObj.setDestination(parts[1]);

		if(parts.length > 2)
			instructObj.setSrc1(parts[2]);

		if(parts.length > 3)
			instructObj.setSrc2(parts[3]);

		return instructObj;
	}

	private static void doFetch2() {
		if(!fetchDecodeLatch.isEmpty()) {
			printQueue.add("Stall");
		} else {
			Instruction instruction = fetchFetchLatch.poll();
			if(instruction == null) {
				if(isFetch1Done) {
					isFetch2Done = true;
				} else {
					printQueue.add("Stall");
				}
			} else {
				fetchDecodeLatch.add(instruction);
				printQueue.add(instruction.toString());
			}
		}
	}

	private static void doDecode() {
		//Validations
		//Renaming
		//Dependency
		//Read values from ARF/PRF
		//Dispatch to IQ & LSQ
		Instruction instruction = fetchDecodeLatch.poll();
		if(instruction == null) {
			if(isFetch2Done) {
				isDecodeDone = true;
				printQueue.add("Done");
			} else {
				printQueue.add("Stall");
			}

			return;
		}
		
		//At this point instruction will not be null
		//Do we need this condition of free phy. reg. for bz/bnz/jump/bal???
		//
		/**/

		if(HALT_INSTRUCTION.equalsIgnoreCase(instruction.getOpCode().getValue())) {
			HALT_ALERT = true;
			printQueue.add("HALT");
			return;
		}

		InstructionType instructionType = instruction.getOpCode();
		//If instruction type is LOAD/STORE && LSQ is full then stall
		//else if instruction type is other than LOAD/STORE && IQ is full then stall
		if((isLoadStoreInstruction(instructionType) && isLSQFull())) {
			printQueue.add("LSQ Full");
			fetchDecodeLatch.add(instruction);
			return;
		}

		if(!isLoadStoreInstruction(instructionType) && isIssueQueueFull()) {
			printQueue.add("IQ Full");
			fetchDecodeLatch.add(instruction);
			return;
		}

		switch (instruction.getOpCode()) {
		case MOV:
			//TODO: Fill entries in rename table
			RenameTableEntry rmt = RENAME_TABLE.get(instruction.getSrc1());
			Integer srcValue;
			if(rmt.getSrcBit() == 0) { //Read values from register file
				srcValue = REGISTER_FILE.get(instruction.getSrc1());
				instruction.setSrc1Value(srcValue);
				instruction.setSrc1Ready(true);
				instruction.setValid(true);
			} else {
				Instruction robInstruction = getEntryFromROBBySlotId(Integer.parseInt(rmt.getRegisterSrc()));
				if(robInstruction.isValid()) {
					srcValue = robInstruction.getDestinationValue();
					instruction.setSrc1Value(srcValue);
					instruction.setSrc1Ready(true);
					instruction.setValid(true);
				} else {
					instruction.setSrc1Value(robInstruction.getRobSlotId());
					instruction.setSrc1Ready(false);
					instruction.setValid(false);
				}
			}
			dispatchToIQ(instruction);
			dispatchToRob(instruction);
			break;

		default:
			break;
		}
		
		/*if(!STORE_INSTRUCTION.equalsIgnoreCase(instructionType.getValue()) && !isFreePhysicalRegisterAvailable()) {
			printQueue.add("No Free Phys. Reg.");
			return;
		}*/

		//instruction = getRenamedInstruction(instruction);
		/*if(isInstructionDependent()) {
			//Dispatch without reading register values form PRF
		} else {
			//Read register values from PRF
		}*/
	}

	private static void dispatchToRob(Instruction instruction) {
		int robSlotId = ROB.getNextSlotIndex();
		instruction.setRobSlotId(robSlotId);
		ROB.add(instruction);
	}

	private static void dispatchToIQ(Instruction instruction) {
		ISSUE_QUEUE.add(instruction);
	}

	private static Instruction getEntryFromROBBySlotId(int slotId) {
		Iterator<Instruction> iterator = ROB.iterator();

		while(iterator.hasNext()) {
			Instruction instruction = iterator.next();
			if(instruction.getRobSlotId() == slotId) {
				return instruction;
			}
		}
		return null;
	}

	/*private static String getRenamedInstruction(String instruction) {
		InstructionType instructionType = InstructionType.getInstructionType(instruction);
		String[] parts = instruction.split(" ");
		if(isLoadStoreInstruction(instructionType)) {
			//Handle renaming for load store separately
			String dest = parts[1];
			String op1 = parts[2];
			String op2 = parts[3];
			String renamedSrc1 = getMappedPhysicalRegister(op1);
			String renamedSrc2 = op2;
			String renamedDest;

			if(!isLiteral(op2)) {
				renamedSrc2 = getMappedPhysicalRegister(op2);
			}

			if(LOAD_INSTRUCTION.equalsIgnoreCase(instructionType.getValue())) {
				renamedDest = getFreePhysicalRegister();
				ARCHITECTURAL_TO_PHYSICAL_MAP.put(dest, renamedDest);
			} else {
				renamedDest = getMappedPhysicalRegister(dest);
			}

			instruction = renamedDest + SPACE + renamedSrc1 + SPACE + renamedSrc2;
		} else if(isBranchInstruction(instructionType)) {
			//Handle branch instruction renaming here
		} else {
			String dest = parts[1];
			String src1 = parts[2];
			String src2 = parts[3];
			String renamedSrc1 = getMappedPhysicalRegister(src1);
			String renamedSrc2 = getMappedPhysicalRegister(src2);
			String renamedDest = getFreePhysicalRegister();
			instruction = renamedDest + SPACE + renamedSrc1 + SPACE + renamedSrc2;

			//Add mapping for dest & renamedDest in map
			ARCHITECTURAL_TO_PHYSICAL_MAP.put(dest, renamedDest);
		}
		return instruction;
	}

	private static boolean isLiteral(String op2) {
		return 'R' != (op2.charAt(0));
	}

	private static String getFreePhysicalRegister() {
		return	FREE_PHYSICAL_REGISTER_LIST.remove(0);
	}

	private static String getMappedPhysicalRegister(String srcArchitecturalRegister) {
		return ARCHITECTURAL_TO_PHYSICAL_MAP.get(srcArchitecturalRegister);
	}

	private static boolean isBranchInstruction(InstructionType instructionType) {
		return (BZ_INSTRUCTION.equalsIgnoreCase(instructionType.getValue())
				|| BNZ_INSTRUCTION.equalsIgnoreCase(instructionType.getValue())
				|| BAL_INSTRUCTION.equalsIgnoreCase(instructionType.getValue())
				|| JUMP_INSTRUCTION.equalsIgnoreCase(instructionType.getValue()));
	}*/

	private static boolean isLSQFull() {
		return LSQ.size() == MAX_LSQ_SIZE;
	}

	private static boolean isLoadStoreInstruction(InstructionType instructionType) {
		return (LOAD_INSTRUCTION.equalsIgnoreCase(instructionType.getValue())
				|| STORE_INSTRUCTION.equalsIgnoreCase(instructionType.getValue()));
	}

	/*private static boolean isFreePhysicalRegisterAvailable() {
		return !FREE_PHYSICAL_REGISTER_LIST.isEmpty();
	}*/

	private static boolean isIssueQueueFull() {
		return ISSUE_QUEUE.size() == MAX_ISSUE_QUEUE_SIZE;
	}


	private static void doWriteBack() {
		// TODO Auto-generated method stub
		
	}

	private static void doExecution() {
		if(isDecodeDone) {
			isExecuteDone = true;
			printQueue.add("Done");
			return;
		}

		String displayData = "";
		Instruction loadStoreInst = lsqMemory1Latch.poll();
		if(loadStoreInst != null) {
			//processLoadStore(load
		}
	}

	private static void doSelection() {
		if(isDecodeDone) {
			printQueue.add("Done");
			return;
		}

		Instruction iqInstruction = selectInstructionForExecutionFromIQ();
		Instruction lsqInstruction = selectInstructionForExecutionFromLSQ();

		if(iqInstruction != null) {
			//Go for execution
			FunctionalUnitType fuType = iqInstruction.getFuType();
			if(INTEGER_FU.equalsIgnoreCase(fuType.getValue())) {
				issueQueueIntegerFuLatch.add(iqInstruction);
			} else {
				issueQueueMultiplyFuLatch.add(iqInstruction);
			}
		}

		if(lsqInstruction != null) {
			//Go for execution
			lsqMemory1Latch.add(lsqInstruction);
		}

	}

	private static Instruction selectInstructionForExecutionFromIQ() {
		int instIndex = -1;
		for(int i = 0; i < ISSUE_QUEUE.size(); i++) {
			Instruction inst = ISSUE_QUEUE.get(i);
			if(inst.isValid() && isFUAvalaible(inst)) {
				instIndex = i;
				break;
			}
		}

		if(instIndex != -1) {
			return ISSUE_QUEUE.remove(instIndex);
		}

		return null;
	}

	private static Instruction selectInstructionForExecutionFromLSQ() {
		if(!LSQ.isEmpty()) {
			Instruction inst = LSQ.get(0);
			if(inst.isValid() && isFUAvalaible(inst)) {
				return LSQ.remove(0);
			}
		}
		return null;
	}

	private static boolean isFUAvalaible(Instruction inst) {
		FunctionalUnitType fuType = inst.getFuType();
		
		return ((INTEGER_FU.equalsIgnoreCase(fuType.getValue()) && isIntegerFuFree)
				|| (MULTIPLY_FU.equalsIgnoreCase(fuType.getValue()) && isMultiplyFuFree)
				|| (MEMORY_FU.equalsIgnoreCase(fuType.getValue()) && isMemoryFuFree));
	}
}
