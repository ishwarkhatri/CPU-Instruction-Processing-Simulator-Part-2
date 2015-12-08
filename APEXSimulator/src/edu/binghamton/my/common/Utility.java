package edu.binghamton.my.common;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class Utility {

	public static void display(LinkedList<String> printQueue, Map<String, Integer> registerFileMap, Integer[] memoryArray) {
		displayHeader();
		int size = printQueue.size();
		int noOfCycles = size / 5;

		for(int i = 0; i < noOfCycles; i++) {
			System.out.print("|" + justify(String.valueOf((i + 1))) + "|");
			System.out.print(justify(printQueue.poll()) + "|");
			System.out.print(justify(printQueue.poll()) + "|");
			System.out.print(justify(printQueue.poll()) + "|");
			System.out.print(justify(printQueue.poll()) + "|");
			System.out.println(justify(printQueue.poll()) + "|");
			System.out.println(repeat("-", 156));
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
		System.out.println(repeat("-", 156));
		System.out.println("|" + justify("Cycle") + "|" + justify("FETCH") + "|" + justify("DECODE") + "|" + justify("EXECUTE") + "|" + justify("MEMORY") + "|" + justify("WRITE-BACK") + "|");
		System.out.println(repeat("-", 156));
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
		int space = 25;
		return justify(data, space);
	}

	public static void echo(String data) {
		System.out.println(data);
	}
}
