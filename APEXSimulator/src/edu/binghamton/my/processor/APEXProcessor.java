package edu.binghamton.my.processor;

import static edu.binghamton.my.common.Utility.*;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Queue;

import static edu.binghamton.my.common.Constants.*;

import edu.binghamton.my.common.CircularQueue;
import edu.binghamton.my.common.FileIO;
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
	private static Queue<Instruction> memory1memory2Latch = new LinkedList<>();
	private static Queue<Instruction> memory2memory3Latch = new LinkedList<>();
	private static Queue<Instruction> memoryForwardingLatch = new LinkedList<>();
	private static Queue<Instruction> integerForwardingLatch = new LinkedList<>();
	private static Queue<Instruction> multiplyForwardingLatch = new LinkedList<>();
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
		//R0 to R7
		for(int i=0; i<8; i++) {
			String regName = "R" + i;
			REGISTER_FILE.put(regName, 0);
			RenameTableEntry rte = new RenameTableEntry();
			rte.setSrcBit(0);
			rte.setRegisterSrc(null);
			RENAME_TABLE.put(regName, rte);
		}

		echo("Reset flags...");
		isMultiplyFuFree = true;
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

			doCommit();
			doForwarding();
			doExecution();
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

	private static void doCommit() {
		//Check if instruction at ROB head has completed execution
		//If yes then update the destination register value in PRF
		//If not then do nothing
		int headInstructionId = ROB.getHeadIndex();
		boolean isReadyToCommit = false;
		for(Instruction inst : ROB) {
			if(inst.getRobSlotId() == headInstructionId) {
				//Cannot remove entry from ROB while iterating
				//So set a flag and remove entry at head out of iteration
				isReadyToCommit = inst.isDestReady();
				break;
			}
		}

		if(isReadyToCommit) {
			Instruction instruction = ROB.remove();
			int destValue = instruction.getDestinationValue();
			String destRegName = instruction.getDestRegName();
			REGISTER_FILE.put(destRegName, destValue);
			updateRenameTable(instruction, RENAME_TABLE, true);
			if(STORE_INSTRUCTION.equalsIgnoreCase(instruction.getOpCode().getValue())) {
				printQueue.add("--");
			} else {
				printQueue.add(destRegName + " = " + destValue);
			}
		} else {
			printQueue.add("--");
		}
	}

	private static void doFetch1() {
		if(!fetchFetchLatch.isEmpty()) {
			printQueue.add("--");
		} else if(PC == instructionList.size()) {
			isFetch1Done = true;
			printQueue.add("--");
		} else {
			if(PC > instructionList.size()) {//PC is updated by BZ/BNZ/JUMP instructions
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
				instruction.setStringRepresentation(temp);
				printQueue.add(temp);
			}
		}		
	}

	private static void doFetch2() {
		if(!fetchDecodeLatch.isEmpty()) {
			printQueue.add("--");
			return;
		}

		Instruction instruction = fetchFetchLatch.poll();
		if(instruction == null) {
			if(isFetch1Done) {
				isFetch2Done = true;
				printQueue.add("--");
			} else {
				printQueue.add("--");
			}
		} else {
			fetchDecodeLatch.add(instruction);
			printQueue.add(instruction.getStringRepresentation());
		}
	}

	private static void doDecode() {
		//Validations
		//Renaming
		//Dependency
		//Read values from ARF/PRF
		//Dispatch to IQ & LSQ
		if(fetchDecodeLatch.isEmpty()) {
			if(isFetch2Done) {
				isDecodeDone = true;
				printQueue.add("--");
			} else {
				printQueue.add("--");
			}
			return;
		}
		
		Instruction instruction = fetchDecodeLatch.poll();
		InstructionType instructionType = instruction.getOpCode();

		if(HALT_INSTRUCTION.equalsIgnoreCase(instructionType.getValue())) {
			HALT_ALERT = true;
			printQueue.add("HALT");
			return;
		}

		//If instruction type is LOAD/STORE && LSQ is full then stall
		if((isLoadStoreInstruction(instructionType) && isLSQFull())) {
			printQueue.add("LSQ Full");
			fetchDecodeLatch.add(instruction);
			return;
		}

		//If instruction type is other than LOAD/STORE && IQ is full then stall
		if(!isLoadStoreInstruction(instructionType) && isIssueQueueFull()) {
			printQueue.add("IQ Full");
			fetchDecodeLatch.add(instruction);
			return;
		}

		boolean issueToIQ = isInstructionForIssueIQ(instructionType);

		switch (instruction.getOpCode()) {
		case MOVC:
			instruction.setSrc1Value(Integer.parseInt(instruction.getSrc1RegName()));
			instruction.setSrc1Ready(true);
			instruction.setValid(true);
			dispatch(instruction, issueToIQ);
			break;

		case MOV:
			decode(instruction, true, false, false);
			if(instruction.isSrc1Ready()) {
				instruction.setValid(true);
			}
			dispatch(instruction, issueToIQ);
			break;

		case ADD:
		case SUB:
		case MUL:
		case AND:
		case OR:
		case EX_OR:
			decode(instruction, true, true, false);
			if(instruction.isSrc1Ready() && instruction.isSrc2Ready()) {
				instruction.setValid(true);
			}
			dispatch(instruction, issueToIQ);
			break;

		case LOAD:
		case STORE:
			decode(instruction, true, true, true);
			if(instruction.isSrc1Ready() && instruction.isSrc2Ready() && instruction.isDestReady()) {
				instruction.setValid(true);
			}
			dispatch(instruction, issueToIQ);
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
		printQueue.add(instruction.getStringRepresentation());
	}

	private static boolean isLiteral(String value) {
		return value.charAt(0) != 'R';
	}

	private static void decode(Instruction instruction, boolean decodeSrc1, boolean decodeSrc2, boolean decodeDest) {
		if(decodeSrc1) {
			String src1 = instruction.getSrc1RegName();
			if(isLiteral(src1)) {
				instruction.setSrc1Value(Integer.parseInt(src1));
				instruction.setSrc1Ready(true);
			} else {
				RenameTableEntry regSrc1Rte = RENAME_TABLE.get(instruction.getSrc1RegName());
				int srcValue = 0;
				if(regSrc1Rte.getSrcBit() == 0) { //Read values from register file
					srcValue = REGISTER_FILE.get(instruction.getSrc1RegName());
					instruction.setSrc1Value(srcValue);
					instruction.setSrc1Ready(true);
				} else {
					Instruction robInstruction = getEntryFromROBBySlotId(Integer.parseInt(regSrc1Rte.getRegisterSrc()), ROB);
					if(robInstruction.isDestReady()) {
						srcValue = robInstruction.getDestinationValue();
						instruction.setSrc1Value(srcValue);
						instruction.setSrc1Ready(true);
					} else {
						instruction.setSrc1Value(robInstruction.getRobSlotId());
						instruction.setSrc1Ready(false);
					}
				}
			}
			
		}

		if(decodeSrc2) {
			String src2 = instruction.getSrc2RegName();
			if(isLiteral(src2)) {
				instruction.setSrc2Value(Integer.parseInt(src2));
				instruction.setSrc2Ready(true);
			} else {
				RenameTableEntry regSrc2Rte = RENAME_TABLE.get(instruction.getSrc2RegName());
				
				int srcValue;
				if(regSrc2Rte.getSrcBit() == 0) { //Read values from register file
					srcValue = REGISTER_FILE.get(instruction.getSrc2RegName());
					instruction.setSrc2Value(srcValue);
					instruction.setSrc2Ready(true);
				} else {
					Instruction robInstruction = getEntryFromROBBySlotId(Integer.parseInt(regSrc2Rte.getRegisterSrc()), ROB);
					if(robInstruction.isDestReady()) {
						srcValue = robInstruction.getDestinationValue();
						instruction.setSrc2Value(srcValue);
						instruction.setSrc2Ready(true);
					} else {
						instruction.setSrc2Value(robInstruction.getRobSlotId());
						instruction.setSrc2Ready(false);
					}
				}
			}
		}

		if(decodeDest) {
			RenameTableEntry regDestRte = RENAME_TABLE.get(instruction.getDestRegName());
			
			int srcValue;
			if(regDestRte.getSrcBit() == 0) { //Read values from register file
				srcValue = REGISTER_FILE.get(instruction.getDestRegName());
				instruction.setDestinationValue(srcValue);
				//instruction.setDestReady(true);
			} else {
				Instruction robInstruction = getEntryFromROBBySlotId(Integer.parseInt(regDestRte.getRegisterSrc()), ROB);
				if(robInstruction.isDestReady()) {
					srcValue = robInstruction.getDestinationValue();
					instruction.setDestinationValue(srcValue);
					//instruction.setDestReady(true);
				} else {
					instruction.setDestinationValue(robInstruction.getRobSlotId());
					//instruction.setDestReady(false);
				}
			}
		}
	}

	private static boolean isInstructionForIssueIQ(InstructionType instructionType) {
		return !isLoadStoreInstruction(instructionType);
	}

	private static void dispatch(Instruction instruction, boolean issueToIQ) {
		if(issueToIQ) {
			dispatchToIQ(instruction, ISSUE_QUEUE);
		} else {
			dispatchToLSQ(instruction, LSQ);
		}

		dispatchToRob(instruction, ROB);

		if(STORE_INSTRUCTION.equalsIgnoreCase(instruction.getOpCode().getValue())) {
			//In case of store all are register are read. So no need to update Rename Table
		} else {
			updateRenameTable(instruction, RENAME_TABLE, false);
		}
	}

	private static void doForwarding() {
		if(!memoryForwardingLatch.isEmpty()) {
			forwardExecutionResults(memoryForwardingLatch.poll());
		} else if(!integerForwardingLatch.isEmpty()) {
			forwardExecutionResults(integerForwardingLatch.poll());
		} else if(!multiplyForwardingLatch.isEmpty()) {
			forwardExecutionResults(multiplyForwardingLatch.poll());
		}
	}

	private static void forwardExecutionResults(Instruction instruction) {
		for(Instruction robInst : ROB) {
			if(!robInst.isValid()) {
				if(!robInst.isSrc1Ready()) {
					if(robInst.getSrc1Value() == instruction.getRobSlotId()) {
						robInst.setSrc1Value(instruction.getDestinationValue());
						robInst.setSrc1Ready(true);
					}
				}

				if(!robInst.isSrc2Ready()) {
					if(robInst.getSrc2Value() == instruction.getRobSlotId()) {
						robInst.setSrc2Value(instruction.getDestinationValue());
						robInst.setSrc2Ready(true);
					}
				}

				if(robInst.getNoOfSources() == 1 && robInst.isSrc1Ready()) {
					robInst.setValid(true);
				} else if(robInst.getNoOfSources() == 2 && robInst.isSrc1Ready() && robInst.isSrc2Ready()) {
					robInst.setValid(true);
				}
			}
		}
	}

	private static void doExecution() {
		doSelection();
		executeMemory3();
		executeMemory2();
		executeMemory1();
		executeMultiply();
		executeInteger();
	}

	private static void executeInteger() {
		if(issueQueueIntegerFuLatch.isEmpty()) {
			if(ISSUE_QUEUE.isEmpty()) {
				printQueue.add("--");
			} else {
				printQueue.add("--");
			}
			return;
		}


		Instruction intInst = issueQueueIntegerFuLatch.poll();
		String opCode = intInst.getOpCode().getValue();
		int result = 0;
		switch (opCode) {
		case MOV_INSTRUCTION:
		case MOVC_INSTRUCTION:
			result = intInst.getSrc1Value();
			break;
		case ADD_INSTRUCTION:
			result = intInst.getSrc1Value() + intInst.getSrc2Value();
			break;
		case SUB_INSTRUCTION:
			result = intInst.getSrc1Value() - intInst.getSrc2Value();
			break;
		case AND_INSTRUCTION:
			result = intInst.getSrc1Value() & intInst.getSrc2Value();
			break;
		case OR_INSTRUCTION:
			result = intInst.getSrc1Value() | intInst.getSrc2Value();
			break;
		case EX_OR_INSTRUCTION:
			result = intInst.getSrc1Value() ^ intInst.getSrc2Value();
			break;
		default:
			break;
		}
		intInst.setDestinationValue(result);
		intInst.setDestReady(true);
		intInst.setValid(true);

		integerForwardingLatch.add(intInst);
		printQueue.add(intInst.getStringRepresentation());
	}

	private static void executeMultiply() {
		if(issueQueueMultiplyFuLatch.isEmpty()) {
			if(ISSUE_QUEUE.isEmpty()) {
				printQueue.add("--");
			} else {
				printQueue.add("--");
			}
			return;
		}

		Instruction mulInst = issueQueueMultiplyFuLatch.poll();
		int latencyCount = mulInst.getMultiplyLatencyCount();
		
		if(latencyCount == 0) {
			isMultiplyFuFree = false;
			int result = mulInst.getSrc1Value() * mulInst.getSrc2Value();
			mulInst.setDestinationValue(result);
		}

		latencyCount++;
		mulInst.setMultiplyLatencyCount(latencyCount);
		if(latencyCount == 4) {
			isMultiplyFuFree = true;
			mulInst.setDestReady(true);
			mulInst.setValid(true);
			multiplyForwardingLatch.add(mulInst);
		} else {
			issueQueueMultiplyFuLatch.add(mulInst);
		}

		printQueue.add(mulInst.getStringRepresentation());
	}

	private static void executeMemory3() {
		if(memory2memory3Latch.isEmpty()) {
			printQueue.add("--");
			return;
		}

		Instruction loadStoreInst = memory2memory3Latch.poll();
		loadStoreInst.setDestReady(true);
		loadStoreInst.setValid(true);

		if(LOAD_INSTRUCTION.equalsIgnoreCase(loadStoreInst.getOpCode().getValue())) {
			int src1 = loadStoreInst.getSrc1Value();
			int src2 = loadStoreInst.getSrc2Value();
			int addr = src1 + src2;
			int value = MEMORY_ARRAY[addr];
			loadStoreInst.setDestinationValue(value);
			REGISTER_FILE.put(loadStoreInst.getDestRegName(), value);//Update/Insert to Register file
			memoryForwardingLatch.add(loadStoreInst);
		} else {//Store instruction
			int src1 = loadStoreInst.getSrc1Value();
			int src2 = loadStoreInst.getSrc2Value();
			int addr = src1 + src2;
			MEMORY_ARRAY[addr] = loadStoreInst.getDestinationValue();//Update/Insert memory location
			//No forwarding required in case of Store
		}

		printQueue.add(loadStoreInst.getStringRepresentation());
	}
	
	private static void executeMemory2() {
		if(memory1memory2Latch.isEmpty()) {
			printQueue.add("--");
			return;
		}

		Instruction inst = memory1memory2Latch.poll();
		memory2memory3Latch.add(inst);
		printQueue.add(inst.getStringRepresentation());
	}

	private static void executeMemory1() {
		if(lsqMemory1Latch.isEmpty()) {
			printQueue.add("--");
			return;
		}

		Instruction loadStoreInst = lsqMemory1Latch.poll();
		if(LOAD_INSTRUCTION.equalsIgnoreCase(loadStoreInst.getOpCode().getValue())) {
			int src1 = loadStoreInst.getSrc1Value();
			int src2 = loadStoreInst.getSrc2Value();
			int addr = src1 + src2;
			int value = MEMORY_ARRAY[addr];
			REGISTER_FILE.put(loadStoreInst.getDestRegName(), value);//Update/Insert to Register file
		} else {//Store instruction
			int src1 = loadStoreInst.getSrc1Value();
			int src2 = loadStoreInst.getSrc2Value();
			int addr = src1 + src2;
			MEMORY_ARRAY[addr] = loadStoreInst.getDestinationValue();//Update/Insert memory location
		}

		memory1memory2Latch.add(loadStoreInst);
		printQueue.add(loadStoreInst.getStringRepresentation());
	}

	private static void doSelection() {
		Instruction integerInstruction = selectIntegerInstructionFromIQ();
		Instruction multiplyInstruction = selectMultiplyInstructionFromIQ();
		Instruction memoryInstruction = selectInstructionForExecutionFromLSQ();

		if(integerInstruction != null) {
			//Go for execution
			issueQueueIntegerFuLatch.add(integerInstruction);
		}

		if(multiplyInstruction != null) {
			//Go for execution
			issueQueueMultiplyFuLatch.add(multiplyInstruction);
		}

		if(memoryInstruction != null) {
			//Go for execution
			lsqMemory1Latch.add(memoryInstruction);
		}

	}

	private static Instruction selectIntegerInstructionFromIQ() {
		int instIndex = -1;
		Instruction selectedInstruction = null;

		for(int i = 0; i < ISSUE_QUEUE.size(); i++) {
			Instruction inst = ISSUE_QUEUE.get(i);
			String fuType = inst.getFuType().getValue();
			if(inst.isValid() && INTEGER_FU.equalsIgnoreCase(fuType)) {
				instIndex = i;
				break;
			}
		}

		if(instIndex != -1) {
			 selectedInstruction = ISSUE_QUEUE.remove(instIndex);
		}

		return selectedInstruction;
	}

	private static Instruction selectMultiplyInstructionFromIQ() {
		int instIndex = -1;
		Instruction selectedInstruction = null;
		for(int i = 0; i < ISSUE_QUEUE.size(); i++) {
			Instruction inst = ISSUE_QUEUE.get(i);
			String fuType = inst.getFuType().getValue();
			if(inst.isValid() && MULTIPLY_FU.equalsIgnoreCase(fuType) && isMultiplyFuFree) {
				instIndex = i;
				break;
			}
		}

		if(instIndex != -1) {
			 selectedInstruction = ISSUE_QUEUE.remove(instIndex);
		}

		return selectedInstruction;
	}

	private static Instruction selectInstructionForExecutionFromLSQ() {
		if(!LSQ.isEmpty()) {
			Instruction inst = LSQ.get(0);
			if(inst.isValid()) {
				if(STORE_INSTRUCTION.equalsIgnoreCase(inst.getOpCode().getValue())) {
					if(inst.getRobSlotId() == ROB.getHeadIndex()) {
						return LSQ.remove(0);
					}
				} else {
					return LSQ.remove(0);
				}
			}
		}
		return null;
	}

	private static boolean isLSQFull() {
		return LSQ.size() == MAX_LSQ_SIZE;
	}

	private static boolean isLoadStoreInstruction(InstructionType instructionType) {
		return (LOAD_INSTRUCTION.equalsIgnoreCase(instructionType.getValue())
				|| STORE_INSTRUCTION.equalsIgnoreCase(instructionType.getValue()));
	}

	private static boolean isIssueQueueFull() {
		return ISSUE_QUEUE.size() == MAX_ISSUE_QUEUE_SIZE;
	}

}
