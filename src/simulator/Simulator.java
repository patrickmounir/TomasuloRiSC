package simulator;

import java.io.File;
import java.util.Scanner;

public class Simulator {
	
	@SuppressWarnings("unused")
	public static void main(String[] args) {
		
		class cacheDetails {
			int size;						// Cache Size in Words.
			int blockSize;					// Size for one Block in Words.
			int associativityLevel;			// Level of Associativity Sets i.e: if =size/blockSize then Full Associative,
											// else if =1 then Direct Mapped else Set Associative.
			int accessCycles;				// No. of cycles needed for accessing cache
			boolean writePolicy;			// { true: "Write Through", false: "Write Back" } 
			
			public cacheDetails(int size, int blockSize, int associativityLevel, int accessCycles, boolean writePolicy) {
				this.size = size;
				this.blockSize = blockSize;
				this.associativityLevel = associativityLevel;
				this.accessCycles = accessCycles;
				this.writePolicy = writePolicy;
			}
		}
		
		int cacheLevels; 
		cacheDetails[] cacheDetails;
		String assemblyFileName;
		Scanner sc = new Scanner(System.in);
		
		System.out.println("Welcome to Tomasulo's RiSC Simoulator, Please follow the "
				+ "instructions to specify simulation parameters. \n");
		
		System.out.println("\nHow many cache levels You'd like the processor to have (Minimum=1) ? : ");
		cacheLevels = sc.nextInt();
		cacheDetails = new cacheDetails[cacheLevels+1];	
		
		System.out.println("Keep in mind Cache L1 will be the nearest to Processor ! \n\n");
		
		// Getting Inputs for every cache Level
		// NOTE: Cache Li will have its details at cacheDetails[i] // 0 is NOT used.
		
		for(int i=1; i<=cacheLevels; i++){
			System.out.printf("For cache L%i please provide these details: \n", i);
			System.out.println("\tCache Size in words: ");
			int size = sc.nextInt();
			System.out.println("\tSize for one Block in Words: ");
			int blockSize = sc.nextInt();
			
			if(i>1) while(blockSize != cacheDetails[i-1].blockSize) {
				System.out.println("\t\tCaches cannot have different block sizes, please enter " + cacheDetails[i-1].blockSize + ": ");
				blockSize = sc.nextInt();
			}
			
			System.out.println("\tLevel of Associativity Sets; ie: number of elements in one set of cache: ");
			int associativityLevel = sc.nextInt();
			System.out.println("\tNo. of cycles needed for accessing cache: ");
			int accessCycles = sc.nextInt();
			System.out.println("\tWrite Policy { 0: \"Write Through\", 1: \"Write Back\" }: ");
			boolean writePolicy = (sc.nextInt()==0);
			
			cacheDetails[i] = new cacheDetails(size, blockSize, associativityLevel, accessCycles, writePolicy);
		}
		
		int pipelineWidth; /* The number of instructions that can be issued to the reservation stations
							simultaneously under ideal circumstances */
		int instructionBufferSize;
		int ROBSize;
		String[] reservationStationTypes = { "Mult", "Logical", "Add", "Branch", "Load", "Store" };
		int[] reservationStationCount = new int[6];
		int[] reservationStationCycles = new int[6];
		
		System.out.println("Nice work for now; We need also to know your desired pipeline Width: ");
		pipelineWidth = sc.nextInt();
		System.out.println("In addition; instruction Buffer Size: ");
		instructionBufferSize = sc.nextInt();
		System.out.println("Also; Maximum number of entries in Reorder Buffer: ");
		ROBSize = sc.nextInt();
		
		System.out.println("Next Stage: We need to know Specifications (Count, cycle latency) "
				+ "for each Reservation station type ! :) \n");
		for(int i=0; i<6; i++) {
			System.out.printf("\tFor \"%s\" Reservation Station Type\n", reservationStationTypes[i]);
			System.out.println("\t\tHow many ? : ");
			reservationStationCount[i] = sc.nextInt();
			System.out.println("\t\tTheir Latency (in Cycles) ? : ");
			reservationStationCycles[i] = sc.nextInt();
		}
		
		System.out.println("\n\nNice Work, Now we need to know your assembly program filename : ");
		assemblyFileName = sc.nextLine();
		
		File assembly = new File(sc.nextLine());
		while(!(assembly.exists() && !assembly.isDirectory())) {
			System.out.println("Sorry ! The file you specified cannot be read or does not exist .. ");
			System.out.println("Please specify a valid file name: ");
			assembly = new File(sc.nextLine());
		}
		
		
		// TODO: Initialing Main Memory
		// TODO: Initializing Assembler to assemble file and get binaries
		// TODO: If AssemblerException thrown print to the user asking to edit file and loop to try again.
		
		// TODO: put program into memory starting at address given by user
		
		// TODO: Initialize CPU and start simulation
		
		
		
		
		sc.close();
		
	}

}
