package simulator;


import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Scanner;

import assembler.Assembler;
import assembler.AssemblyException;
import memory.Cache;
import memory.MainMemory;
import memory.Memory;


public class Simulator {
	
	
	
	
	class cacheDetails {
		int size;						// Cache Size in Words.
		short blockSize;					// Size for one Block in Words.
		int associativityLevel;			// Level of Associativity Sets i.e: if =size/blockSize then Full Associative,
										// else if =1 then Direct Mapped else Set Associative.
		int accessCycles;				// No. of cycles needed for accessing cache
		boolean writePolicy;			// { true: "Write Through", false: "Write Back" } 
		
		public cacheDetails(int size, short blockSize, int associativityLevel, int accessCycles, boolean writePolicy) {
			this.size = size;
			this.blockSize = blockSize;
			this.associativityLevel = associativityLevel;
			this.accessCycles = accessCycles;
			this.writePolicy = writePolicy;
		}
	}
	ArrayList<Integer> indices = new ArrayList<Integer>(), memData = new ArrayList<Integer>();
	int loadStoreLantencies;
	int cacheLevels; 
	Scanner sc;
	cacheDetails[] cacheDetails;
	String assemblyFileName;
	int pipelineWidth; /* The number of instructions that can be issued to the reservation stations
	simultaneously under ideal circumstances */
	int instructionBufferSize;
	int ROBSize;
	int mainMemoryAccessTime;
	short startAddress;
	String[] reservationStationTypes = { "Mult", "Logical", "Add", "Branch", "Load", "Store" };
	int[] reservationStationCount = new int[6];
	int[] reservationStationCycles = new int[6];
	File assembly;
	String fileName;
	Assembler assembler;
	ArrayList<Short> programBinary;
	
	public void inputCacheDetails() {

		 sc = new Scanner(System.in);
		
		System.out.println("Welcome to Tomasulo's RiSC Simoulator, Please follow the "
				+ "instructions to specify simulation parameters. \n");
		
		System.out.println("\nHow many cache levels You'd like the processor to have (Minimum=1) ? : ");
		cacheLevels = sc.nextInt();
		cacheDetails = new cacheDetails[cacheLevels+1];	
		
		System.out.println("Keep in mind Cache L1 will be the nearest to Processor ! \n\n");
		
		// Getting Inputs for every cache Level
		// NOTE: Cache Li will have its details at cacheDetails[i] // 0 is NOT used.
		loadStoreLantencies=0;
		for(int i=1; i<=cacheLevels; i++){
			System.out.printf("For cache L%d please provide these details: \n", i);
			System.out.println("\tCache Size in words: ");
			int size = sc.nextInt();
			System.out.println("\tSize for one Block in Words: ");
			short blockSize = sc.nextShort();
			
			if(i>1) while(blockSize != cacheDetails[i-1].blockSize) {
				System.out.println("\t\tCaches cannot have different block sizes, please enter " + cacheDetails[i-1].blockSize + ": ");
				blockSize = sc.nextShort();
			}
			
			System.out.println("\tLevel of Associativity Sets; ie: number of elements in one set of cache: ");
			int associativityLevel = sc.nextInt();
			System.out.println("\tNo. of cycles needed for accessing cache: ");
			int accessCycles = sc.nextInt();
			System.out.println("\tWrite Policy { 0: \"Write Through\", 1: \"Write Back\" }: ");
			boolean writePolicy = (sc.nextInt()==0);
			
			cacheDetails[i] = new cacheDetails(size, blockSize, associativityLevel, accessCycles, writePolicy);
			loadStoreLantencies+=accessCycles;
		}
		
		System.out.println("Specify main memory access time (In Cycles): ");
		mainMemoryAccessTime = sc.nextInt();
		loadStoreLantencies+=mainMemoryAccessTime;
		cacheDetails[0] = new cacheDetails(cacheDetails[1].size, cacheDetails[1].blockSize, 
				cacheDetails[1].associativityLevel, cacheDetails[1].accessCycles, cacheDetails[1].writePolicy);
		
		
	}
	
	public void inputCPUDetails() throws IOException {
		
		
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
			if(i<4){
				System.out.println("\t\tTheir Latency (in Cycles) ? : ");
				reservationStationCycles[i] = sc.nextInt();
			}
			
		}
		reservationStationCycles[4]=loadStoreLantencies;
		reservationStationCycles[5]=loadStoreLantencies;
		
		System.out.println("\n\nNice Work, Now we need to know your assembly program filename : ");
		assemblyFileName = sc.nextLine();
		
		assembly = new File(assemblyFileName);
		while(!(assembly.exists() && !assembly.isDirectory())) {
			//System.out.println("Sorry ! The file you specified cannot be read or does not exist .. ");
			System.out.println("Please specify a valid file name: ");
			assemblyFileName = sc.nextLine();
			assembly = new File(assemblyFileName);
		}
		
		System.out.println("Please Specify The start address of your program: ");
		startAddress = sc.nextShort();
		System.out.println("\n\nNice Work, Now we need to know your filename where the program data is stored : ");
		String programDataFileName = sc.nextLine();
		BufferedReader reader = new BufferedReader(new FileReader(sc.nextLine()));
		String s = "";
		while((s = reader.readLine()) != null){
			String [] tmp = s.split(",");
			indices.add(Integer.parseInt(tmp[0]));
			memData.add(Integer.parseInt(tmp[1]));
		}
		reader.close();
		System.out.println();
		
		
	}
	
	public void Assemble() throws IOException, AssemblyException {
		assembler = new Assembler();
		programBinary = assembler.parse(assemblyFileName);
	}
	
	public static void main(String[] args) throws IOException {
		
		
		Simulator sim = new Simulator();
		
		sim.inputCacheDetails();
		sim.inputCPUDetails();
		
		while(true) {
			try {
				
				sim.Assemble();
				break;
			} catch (Exception e) {
				e.printStackTrace();
				System.err.println("COMPILATION ERROR");
			}
		}
		
		MainMemory ram = new MainMemory((64/2)*1024, sim.mainMemoryAccessTime, false, sim.cacheDetails[0].blockSize);
		for(int i =0;i<sim.indices.size();i++){
			ram.memoryData[sim.indices.get(i)]=((short)(sim.memData.get(i).intValue()));
		}
		Memory current = ram;
		for(int i=sim.cacheDetails.length-1; i>0; i--) {
			Cache next = new Cache(sim.cacheDetails[i].size,
									sim.cacheDetails[i].blockSize, 
									sim.cacheDetails[i].associativityLevel, 
									sim.cacheDetails[i].writePolicy, 
									sim.cacheDetails[i].accessCycles, 
									current);
			current = next;
		}
		
		Cache L1Data = (Cache) current;
		Cache L1Instruction = new Cache(sim.cacheDetails[0].size,
										sim.cacheDetails[0].blockSize, 
										sim.cacheDetails[0].associativityLevel, 
										sim.cacheDetails[0].writePolicy, 
										sim.cacheDetails[0].accessCycles, 
										L1Data.getLowerLevel());
		
		// Write program in memory at startAddress
		for(short i=0; i<sim.programBinary.size(); i++) 
			ram.write((short)(sim.startAddress+i), sim.programBinary.get(i), true);
		
		
		int [] cpuVarArgs = new int[12];
		System.arraycopy(sim.reservationStationCount, 0, cpuVarArgs, 0, 6);
		System.arraycopy(sim.reservationStationCycles, 0, cpuVarArgs, 6, 6);
		
		CPU cpu = new CPU(L1Instruction, L1Data, cpuVarArgs);
		cpu.init(sim.pipelineWidth, sim.instructionBufferSize, sim.startAddress, sim.ROBSize);
		cpu.simulate();
				
		// TODO: start simulation
		
		
		
	}

	
}
