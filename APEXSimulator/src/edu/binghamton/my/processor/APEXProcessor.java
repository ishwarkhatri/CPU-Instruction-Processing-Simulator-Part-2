package edu.binghamton.my.processor;

import static edu.binghamton.my.common.Utility.*;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;

import static edu.binghamton.my.common.Constants.*;
import edu.binghamton.my.common.FileIO;
import edu.binghamton.my.model.InstructionType;
import edu.binghamton.my.model.IssueQueueEntry;
import edu.binghamton.my.model.LSQEntry;

public class APEXProcessor {
	private static Integer PC = 20000;
	private static Integer UPDATED_PC = 0;
	private static String[] lastTwoInstructions = new String[2];
	private static LinkedList<String> printQueue = new LinkedList<>();
	private static Queue<String> fetchFetchLatch = new LinkedList<>();
	private static Queue<String> fetchDecodeLatch = new LinkedList<>();
	private static Map<String, Integer> REGISTER_FILE = new HashMap<>();
	private static Queue<String> decodeExecuteLatch = new LinkedList<>();
	private static Queue<String> executeMemoryLatch = new LinkedList<>();
	private static Queue<String> memoryWriteBackLatch = new LinkedList<>();
	private static List<String> instructionList = new ArrayList<String>(PC);
	private static Integer[] MEMORY_ARRAY = new Integer[10000];
	private static String lastFetchedInstruction;
	private static boolean isFetch1Done, isFetch2Done, isDecodeDone, isExecuteDone, isMemoryDone, isWriteBackDone;
	private static boolean BRANCH_TAKEN = false;
	private static boolean JUMP_DETECTED = false;
	private static boolean INVALID_PC = false;
	private static boolean HALT_ALERT;
	private static boolean ZERO_FLAG;
	private static List<LSQEntry> LSQ = new ArrayList<>();
	private static List<IssueQueueEntry> ISSUE_QUEUE = new ArrayList<>();
	private static List<String> FREE_PHYSICAL_REGISTER_LIST = new ArrayList<>();

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
			doDecode();
			doFetch();

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

	private static void doFetch() {
		if(isFetch1Done && isFetch2Done) {
			printQueue.add("Done");
		}

		if(!isFetch1Done) {
			doFetch1();
		}

		if(!isFetch2Done) {
			doFetch2();
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
				String instruction = instructionList.get(PC++);
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

				lastFetchedInstruction = instruction;
				printQueue.add(instruction);
			}
		}		
	}

	private static void doFetch2() {
		if(!fetchDecodeLatch.isEmpty()) {
			printQueue.add("Stall");
		} else {
			String instruction = fetchFetchLatch.poll();
			if(instruction == null) {
				if(isFetch1Done) {
					isFetch2Done = true;
				} else {
					printQueue.add("Stall");
				}
			} else {
				fetchDecodeLatch.add(instruction);
				printQueue.add(instruction);
			}
		}
	}

	private static void doDecode() {
		//Validations
		//Renaming
		//Dependency
		//Read values from ARF/PRF
		//Dispatch to IQ & LSQ
		String instruction = fetchDecodeLatch.poll();
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
		if(!isFreePhysicalRegisterAvailable()) {
			printQueue.add("Stall");
			return;
		}

		InstructionType instructionType = InstructionType.getInstructionType(instruction);
		//If instruction type is LOAD/STORE && LSQ is full then stall
		//else if instruction type is other than LOAD/STORE && IQ is full then stall
		if((isLoadStoreInstruction(instructionType) && isLSQFull())
				|| (!isLoadStoreInstruction(instructionType) && isIssueQueueFull())) {
			printQueue.add("Stall");
			fetchDecodeLatch.add(instruction);
			return;
		}

	}

	private static boolean isLSQFull() {
		return LSQ.size() == MAX_LSQ_SIZE;
	}

	private static boolean isLoadStoreInstruction(InstructionType instructionType) {
		return (LOAD_INSTRUCTION.equalsIgnoreCase(instructionType.getValue())
				|| STORE_INSTRUCTION.equalsIgnoreCase(instructionType.getValue()));
	}

	private static boolean isFreePhysicalRegisterAvailable() {
		return !FREE_PHYSICAL_REGISTER_LIST.isEmpty();
	}

	private static boolean isIssueQueueFull() {
		return ISSUE_QUEUE.size() == MAX_ISSUE_QUEUE_SIZE;
	}

	private static void doExecution() {
		// TODO Auto-generated method stub
		
	}

	private static void doWriteBack() {
		// TODO Auto-generated method stub
		
	}
}
