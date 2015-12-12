package edu.binghamton.my.common;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import edu.binghamton.my.model.FunctionalUnitType;
import edu.binghamton.my.model.Instruction;
import edu.binghamton.my.model.InstructionType;
import edu.binghamton.my.model.RenameTableEntry;

public class Utility {

	public static void display(LinkedList<String> printQueue, Map<String, Integer> registerFileMap, Integer[] memoryArray) {
		displayHeader();
		int size = printQueue.size();
		int noOfCycles = size / 9;

		for(int i = 0; i < noOfCycles; i++) {
			System.out.print("|" + justify(printQueue.poll()) + "|");
			System.out.print(justify(printQueue.poll()) + "|");
			System.out.print(justify(printQueue.poll()) + "|");
			System.out.print(justify(printQueue.poll()) + "|");
			System.out.print(justify(printQueue.poll()) + "|");
			System.out.print(justify(printQueue.poll()) + "|");
			System.out.print(justify(printQueue.poll()) + "|");
			System.out.print(justify(printQueue.poll()) + "|");
			System.out.println(justify(printQueue.poll()) + "|");
			System.out.println(repeat("-", 153));
		}

		System.out.println("\nRegister Content");
		List<String> registerNames = new ArrayList<>();
		registerNames.addAll(registerFileMap.keySet());
		Collections.sort(registerNames);

		for(int i = 0; i < registerNames.size(); i++) {
			System.out.print(justify(registerNames.get(i), 10));
		}
		System.out.print("\n");
		for(int i = 0; i < registerNames.size(); i++) {
			System.out.print(justify(String.valueOf(registerFileMap.get(registerNames.get(i))), 10));
		}

		System.out.println("\n\nMemory Content");
		for(int i = 0; i < 10; i++) {
			System.out.print(justify(String.valueOf(i), 5) + ":" + justify(String.valueOf(memoryArray[i]), 5) + "|");
			System.out.print(justify(String.valueOf(i + 10), 5) + ":" + justify(String.valueOf(memoryArray[i + 10]), 5) + "|");
			System.out.print(justify(String.valueOf(i + 20), 5) + ":" + justify(String.valueOf(memoryArray[i + 20]), 5) + "|");
			System.out.print(justify(String.valueOf(i + 30), 5) + ":" + justify(String.valueOf(memoryArray[i + 30]), 5) + "|");
			System.out.print(justify(String.valueOf(i + 40), 5) + ":" + justify(String.valueOf(memoryArray[i + 40]), 5) + "|");
			System.out.print(justify(String.valueOf(i + 50), 5) + ":" + justify(String.valueOf(memoryArray[i + 50]), 5) + "|");
			System.out.print(justify(String.valueOf(i + 60), 5) + ":" + justify(String.valueOf(memoryArray[i + 60]), 5) + "|");
			System.out.print(justify(String.valueOf(i + 70), 5) + ":" + justify(String.valueOf(memoryArray[i + 70]), 5) + "|");
			System.out.print(justify(String.valueOf(i + 80), 5) + ":" + justify(String.valueOf(memoryArray[i + 80]), 5) + "|");
			System.out.println(justify(String.valueOf(i + 90), 5) + ":" + justify(String.valueOf(memoryArray[i + 90]), 5));
		}
	}

	public static void displaySimulatorMenu() {
		System.out.print("\nAPEX Simulator\n1. INITIALIZE\n2. SIMULATE\n3. DISPLAY\n4. EXIT\nEnter your choice: ");
	}

	public static void displayHeader() {
		System.out.println(repeat("-", 153));
		System.out.println("|" + justify("FETCH-1") + "|" + justify("FETCH-2") + "|" + justify("DECODE") + "|" + justify("INTEGER") + "|" + justify("MULTIPLY") + "|" + justify("MEMORY-1") + "|" + justify("MEMORY-2") + "|" + justify("MEMORY-3") + "|" + justify("Commit") + "|");
		System.out.println(repeat("-", 153));
	}

	public static String repeat(String data, int numberOfRepeatation) {
		String temp = data;
		for(int i = 0; i < numberOfRepeatation; i++)
			temp += data;
		return temp;
	}

	public static String justify(String data, int space) {
		int dataLen = data.length();
		int extraSpace = space - dataLen;
		if(extraSpace <= 0 )
			return data;

		int padding = extraSpace / 2;
		String formattedData = "";
		for(int i = 0; i < padding; i++) {
			formattedData += " ";
		}
		formattedData += data;

		int newLength = formattedData.length();
		for(int i = newLength; i < space; i++) {
			formattedData += " ";
		}

		return formattedData;
	}

	public static String justify(String data) {
		int space = 16;
		return justify(data, space);
	}

	public static void echo(String data) {
		System.out.println(data);
	}

	public static Instruction getInstructionObject(String instruction) {
		String[] parts = instruction.split(" ");
		InstructionType type = InstructionType.getInstructionType(instruction);

		Instruction instructObj = new Instruction();
		instructObj.setOpCode(type);
		instructObj.setFuType(FunctionalUnitType.getFunctionalUnitType(type.getValue()));

		if(parts.length > 1) {
			instructObj.setDestRegName(parts[1]);
			instructObj.setNoOfSources(0);
		}

		if(parts.length > 2) {
			instructObj.setSrc1RegName(parts[2]);
			instructObj.setNoOfSources(1);
		}

		if(parts.length > 3) {
			instructObj.setSrc2RegName(parts[3]);
			instructObj.setNoOfSources(2);
		}

		return instructObj;
	}

	public static void dispatchToRob(Instruction instruction, CircularQueue ROB) {
		int robSlotId = ROB.getNextSlotIndex();
		instruction.setRobSlotId(robSlotId);
		ROB.add(instruction);
	}

	public static void dispatchToIQ(Instruction instruction, List<Instruction> ISSUE_QUEUE) {
		ISSUE_QUEUE.add(instruction);
	}

	public static void dispatchToLSQ(Instruction instruction, List<Instruction> LSQ) {
		LSQ.add(instruction);
	}

	public static Instruction getEntryFromROBBySlotId(int slotId, CircularQueue ROB) {
		Iterator<Instruction> iterator = ROB.iterator();

		while(iterator.hasNext()) {
			Instruction instruction = iterator.next();
			if(instruction.getRobSlotId() == slotId) {
				return instruction;
			}
		}
		return null;
	}

	public static void updateRenameTable(Instruction instruction, Map<String, RenameTableEntry> RENAME_TABLE, boolean isCommitted) {
		RenameTableEntry entry = RENAME_TABLE.get(instruction.getDestRegName());
		if(isCommitted) {
			//src_bit = 0
			entry.setSrcBit(0);
			entry.setRegisterSrc(null);
		} else {
			//src_bit = 1
			//src = ROB_Slot_ID
			entry.setSrcBit(1);
			entry.setRegisterSrc(String.valueOf(instruction.getRobSlotId()));
		}
	}
}