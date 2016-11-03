package simulator;

import java.util.Scanner;

public class Simulator {
	
	@SuppressWarnings("unused")
	public static void main(String[] args) {
		
		class cacheDetails {
			int size;						// Cache Size in Words.
			int blockSize;					// Size for one Block in Words.
			int associativityLevel;			// Level of Associativity Sets i.e: if =size/blockSize then Direct Mapping,
											// else if =1 then Full Associative else Set Associative.
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
			System.out.println("\tLevel of Associativity Sets; ie: number of Sets the cache is divided to: ");
			int associativityLevel = sc.nextInt();
			System.out.println("\tNo. of cycles needed for accessing cache: ");
			int accessCycles = sc.nextInt();
			System.out.println("\tWrite Policy { 0: \"Write Through\", 1: \"Write Back\" }: ");
			boolean writePolicy = (sc.nextInt()==0);
			
			cacheDetails[i] = new cacheDetails(size, blockSize, associativityLevel, accessCycles, writePolicy);
		}
		
		System.out.println("\n\nNice Work, Now we need to know your assembly program filename : ");
		assemblyFileName = sc.nextLine();
		
		// TODO: Getting file and ensuring No exceptions of IO of FileNotFound
		// TODO: Initialing Main Memory
		// TODO: Initializing Assembler to assemble file and get binaries
		// TODO: If AssemblerException thrown print to the user asking to edit file and loop to try again.
		
		// TODO: put program into memory starting at address 0x0000
		
		// TODO: Initialize CPU and start simulation
		
		
		
		
		sc.close();
		
	}

}
