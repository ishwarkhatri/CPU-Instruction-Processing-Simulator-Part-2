package edu.binghamton.my.main;
import static edu.binghamton.my.common.Constants.DISPLAY;
import static edu.binghamton.my.common.Constants.EXIT;
import static edu.binghamton.my.common.Constants.INITIALIZE;
import static edu.binghamton.my.common.Constants.SIMULATE;
import static edu.binghamton.my.common.Utility.displaySimulatorMenu;

import java.io.File;
import java.util.Scanner;

import edu.binghamton.my.processor.APEXProcessor;

public class APEXSimulatorRunner {

	public static void main(String[] args) {
		//Instruction file name ("instructions.txt") has to be given as cmdline args
		if(args.length == 0) {
			System.out.println("Instruction file name absent!!!");
			System.exit(1);
		}

		Scanner scan = new Scanner(System.in);
		File file = new File(args[0]);

		while(true) {
			displaySimulatorMenu();
			int option = scan.nextInt();

			switch (option) {
			case INITIALIZE:
				APEXProcessor.init(file);
				break;

			case SIMULATE:
				System.out.print("Enter number of cycles : ");
				int cycleCount = scan.nextInt();
				APEXProcessor.simulate(cycleCount);
				break;

			case DISPLAY:
				APEXProcessor.displaySimulationResult();
				break;

			case EXIT:
				scan.close();
				System.exit(0);
				break;

			default:
				break;
			}
		}
	}

}
