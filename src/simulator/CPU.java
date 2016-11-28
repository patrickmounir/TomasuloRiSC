package simulator;

//Comment malosh lazma

import java.util.Arrays;
import java.util.LinkedList;
import java.util.Scanner;

import memory.Cache;
import memory.Memory;
import simulator.Queue.ROBEntry;

public class CPU {
	
	static enum functionalUnit{
		MULT, LOGICAL, ADD, BRANCH, LOAD, STORE
	}
	static enum Instruction{
		LW,SW,JMP,BEQ,JALR,RET,ADD,ADDI,SUB,NAND,MUL
	}

	int missPrediction = 0;
	int branchCount = 0;
	int pipelineWidth;
	int instructionBufferSize;
	java.util.Queue<int[]> instructionBuffer;
	int [] regFile = new int[8];
	int [] regTable = new int[8];
	Queue ROB;
	RSEntry [][] reservationStation = new RSEntry[6][];
	int[] reservationStationLatencies = new int[6];
	Cache instructionCache;
	Cache dataCache;
	int PC;
	int oldPC;
	int newPC;
	
	public CPU(Cache instructionCache, Cache dataCache,int ...unitsNo) {
		this.instructionCache=instructionCache;
		this.dataCache=dataCache;
		Arrays.fill(regTable, -1);
		int i = 0;
		for(int size : unitsNo){
			if(i<6)	reservationStation[i] = new RSEntry [size];
			else {
				reservationStationLatencies[i-6] = size;
				
			}
			i++;
		}
	}
	
	public int[] decode(int instruction){
		
		int opcode = (instruction&(7<<13))>>13;
		int functional= (instruction&(7));
		int [] decoded = null;
		switch(opcode){
		case 7:
			if(functional > 3) return null;
			
			decoded = new int [6];
			decoded[2] = (instruction&(7<<10))>>10;
			decoded[3] =(instruction&(7<<7))>>7;
			decoded[4] = (instruction&(7<<4))>>4;
			
			switch(functional){
			case 0:decoded[0] = functionalUnit.ADD.ordinal();
			       decoded[1] =Instruction.ADD.ordinal() ;
			break;
			case 1:decoded[0] = functionalUnit.ADD.ordinal();
		       decoded[1] =Instruction.SUB.ordinal() ;
		       break;
			case 2:decoded[0] = functionalUnit.LOGICAL.ordinal();
		       decoded[1] =Instruction.NAND.ordinal() ;
		       break;
			case 3:decoded[0] = functionalUnit.MULT.ordinal();
		       decoded[1] =Instruction.MUL.ordinal() ;
		       break;
				
			}break;
			case 0:
				decoded = new int [6];
				decoded[0]=functionalUnit.LOAD.ordinal();
				decoded[1] =Instruction.LW.ordinal() ;
				decoded[2] = (instruction&(7<<10))>>10;
				decoded[3] =(instruction&(7<<7))>>7;
				if(((instruction&(1<<6))>>6)==1){
					
					decoded[4] = (instruction&(127))-128;	
				}else{
					decoded[4] = (instruction&(63));
				}break;
			case 1:
				decoded = new int [6];
				decoded[0]=functionalUnit.STORE.ordinal();
				decoded[1] =Instruction.SW.ordinal() ;
				decoded[2] = (instruction&(7<<10))>>10;
				decoded[3] =(instruction&(7<<7))>>7;
				if(((instruction&(1<<6))>>6)==1){
					
					decoded[4] = (instruction&(127))-128;	
				}else{
					decoded[4] = (instruction&(63));
				}break;
			case 2:
				decoded = new int [6];
				decoded[0]=functionalUnit.BRANCH.ordinal();
				decoded[1] =Instruction.BEQ.ordinal() ;
				decoded[2] = (instruction&(7<<10))>>10;
				decoded[3] =(instruction&(7<<7))>>7;
				if(((instruction&(1<<6))>>6)==1){
					
					decoded[4] = (instruction&(127))-128;	
				}else{
					decoded[4] = (instruction&(63));
				}
				break;
			case 3:
				decoded = new int [6];
				decoded[0]=functionalUnit.ADD.ordinal();
				decoded[1] =Instruction.ADDI.ordinal() ;
				decoded[2] = (instruction&(7<<10))>>10;
				decoded[3] =(instruction&(7<<7))>>7;
				if(((instruction&(1<<6))>>6)==1){
					
					decoded[4] = (instruction&(127))-128;	
				}else{
					decoded[4] = (instruction&(63));
				}break;
			case 4:
				decoded = new int [6];
				decoded[0]=functionalUnit.BRANCH.ordinal();
				decoded[1] =Instruction.JMP.ordinal() ;
				decoded[2] = (instruction&(7<<10))>>10;
				if(((instruction&(1<<9))>>9)==1){
					
					decoded[3] = (instruction&(1023))-1024;	
				}else{
					decoded[3] = (instruction&(1023));
				}
				break;

		case 5:decoded = new int [6];
				decoded[0]=functionalUnit.BRANCH.ordinal();
				decoded[1] =Instruction.JALR.ordinal() ;
				decoded[2] = (instruction&(7<<10))>>10;
				decoded[3] =(instruction&(7<<7))>>7;
				break;
		case 6:decoded = new int [6];
				decoded[0]=functionalUnit.BRANCH.ordinal();
					decoded[1] =Instruction.RET.ordinal() ;
					decoded[2] = (instruction&(7<<10))>>10;
					break;
			
			
		}
		return decoded;
	}
	
	static class RSEntry{
		String name;
		boolean busy;
		String op;
		int Vj, Vk, Qj, Qk, Dest, A, cyclesRemToWrite;
		int instrcutionIndex;
		int latency;
		int PC;

		public RSEntry(String name, String op, int vj, int vk, int qj, int qk, int dest, int a,
						int cyclesRemToWrite, int latency, int instructionIndex) {
			this.name = name;
			this.op = op;
			Vj = vj;
			Vk = vk;
			Qj = qj;
			Qk = qk;
			Dest = dest;
			A = a;
			this.latency = latency;
			this.cyclesRemToWrite = cyclesRemToWrite;
			this.instrcutionIndex=instructionIndex;
			busy = false;
			
		}
		public String toString(){
			return "<" + name + ", " + busy + ", " + op + ", " + Vj + ", " + Vk + ", " + Qj + ", " + Qk + ", " + Dest + ", " + A + ", " + cyclesRemToWrite + ">";
		} 
		
	}
	
	public void init(int pipelineWidth, int instructionBufferSize, int startAddress, int robSize) {
		this.pipelineWidth = pipelineWidth;
		this.instructionBufferSize = instructionBufferSize;
		this.PC = startAddress;
		instructionBuffer = new LinkedList<int[]>();
		
		functionalUnit[] functionalUnitNames = functionalUnit.values();
		for(int i=0; i<6; i++) {
			for(int j=0; j<reservationStation[i].length; j++) {
				reservationStation[i][j] = new RSEntry(functionalUnitNames[i]+""+(j+1), 
						"", -1, -1, -1, -1, -1, -1, -1, reservationStationLatencies[i], -1);
			}
		}
		
		ROB = new Queue(robSize);
	}
	
	public void simulate() {
		
		int cycles = 0;
		boolean fetch = true;
		int issueCounter = 0;
		double misprediction =0;
		double predictions = 0;
		Scanner stall = new Scanner(System.in);
		while(true) {
			System.out.println("Cycle t"+cycles);
			
			// FRONT END
			if((instructionBufferSize-instructionBuffer.size()) >= pipelineWidth && fetch) {
				for(int i=0; i<pipelineWidth; i++) {
					short fetchedInstruction = (short) instructionCache.read(PC, true);
					int[] decodedInstruction = this.decode(fetchedInstruction);
					
					instructionBuffer.offer(decodedInstruction);
					if(decodedInstruction == null) { fetch = false; break; }
					
					PC++;
					
					if(decodedInstruction[1]==Instruction.JMP.ordinal()) {
						predictions++;
						decodedInstruction[5]=PC;
						fetch =false;
						break;
					} else if(decodedInstruction[1]==Instruction.JALR.ordinal()) {
						oldPC = PC;
						predictions++;
						decodedInstruction[5]=PC;
						fetch =false;
						break;
					} else if(decodedInstruction[1]==Instruction.RET.ordinal()) {
						predictions++;
						decodedInstruction[5]=PC;
						fetch =false;
						break;
					} else if(decodedInstruction[1]==Instruction.BEQ.ordinal()) {
						predictions++;
						decodedInstruction[5]=PC;
						
						PC += (decodedInstruction[4]<0)?((short) decodedInstruction[4]):0;
					}
					
					
				}
			}
			
			// BACK END
			
			//Will we issue in this cycle ?
			boolean issue = false;
			int issuePos=0;
			if(instructionBuffer.peek()!=null){
				for(issuePos = 0; issuePos<reservationStation[instructionBuffer.peek()[0]].length; issuePos++) {
					issue |= !(reservationStation[instructionBuffer.peek()[0]][issuePos].busy);
					if(issue) break;
				}
					
				issue &= !ROB.isFull();
				issue &= (instructionBuffer.peek()!=null);	
			}
			
			
			
			boolean [][] execute = new boolean [6][];
			for(int i = 0;i<6;i++) {
				for(int j = 0;j<reservationStation[i].length;j++) {
					if(j ==0){
						execute[i]= new boolean[reservationStation[i].length];
					}
					execute[i][j]=(reservationStation[i][j].busy&&
							(reservationStation[i][j].Qj==-1)&&
							(reservationStation[i][j].Qk==-1)&&
							(reservationStation[i][j].cyclesRemToWrite!=0));
				
				}
			}
			//write check
			int minSoFarStation=0,minSoFarPosition=0;
			for(int i = 0;i<6;i++) {
				for(int j = 0;j<reservationStation[i].length;j++) {
					if(reservationStation[i][j].busy && reservationStation[i][j].cyclesRemToWrite==0) {
						if(!reservationStation[minSoFarStation][minSoFarPosition].busy||reservationStation[minSoFarStation][minSoFarPosition].cyclesRemToWrite!=0){
							minSoFarStation=i;
							minSoFarPosition=j;
						}else{
							if((reservationStation[i][j].instrcutionIndex<reservationStation[minSoFarStation][minSoFarPosition].instrcutionIndex)){
								minSoFarStation=i;
								minSoFarPosition=j;
							}
						}
//						if(!reservationStation[minSoFarStation][minSoFarPosition].busy||
//								((reservationStation[i][j].cyclesRemToWrite==0)&&
//								(reservationStation[minSoFarStation][minSoFarPosition].cyclesRemToWrite!=0)&&
//								(reservationStation[i][j].instrcutionIndex<reservationStation[minSoFarStation][minSoFarPosition].instrcutionIndex))) {
//							minSoFarStation=i;
//							minSoFarPosition=j;
//						}	
						
					}		
				}
			}
			//commit stage
			if(!ROB.isEmpty()) {
				ROBEntry entry = ROB.remove();

				if(entry!=null) {
					System.out.println("Rob #"+entry.index+" ("+entry.instructionType+") is committing ");
					if(!(entry.instructionType.equals("SW")||
							entry.instructionType.equals("BEQ")||
							entry.instructionType.equals("JMP")||
							entry.instructionType.equals("JALR")||
							entry.instructionType.equals("RET"))){
						for(int i=0;i<regTable.length;i++){
							if(regTable[i]==entry.index&&i!=0){
								regFile[i]=entry.value;
								regTable[i]=-1;
								break;
							}
								
						}
					}else {
						if(entry.instructionType.equals("BEQ")) {
							if(entry.value!=0) {
								PC=(entry.value & 0x0000ffff);
								flush();
								fetch =true;
								stall.nextLine();
								cycles++;
								continue;
							}
						}else{
							if(entry.instructionType.equals("JMP")||entry.instructionType.equals("RET")){
								PC =entry.value;
								fetch =true;
								stall.nextLine();
								cycles++;
								continue;
							}else{
								if(entry.instructionType.equals("JALR")){
									for(int i=0;i<regTable.length;i++){
										if(regTable[i]==entry.index&&i!=0){
											regFile[i]=entry.value;
											regTable[i]=-1;
											break;
										}
											
									}
									stall.nextLine();
									cycles++;
									fetch =true;
									continue;
								}
							}
						}
					}
				}
				
				
			}
			//Write stage
			if(reservationStation[minSoFarStation][minSoFarPosition].busy&&reservationStation[minSoFarStation][minSoFarPosition].cyclesRemToWrite==0) {
				int value=0;
				RSEntry toBeWritten = reservationStation[minSoFarStation][minSoFarPosition];
				toBeWritten.busy=false;
				System.out.println("I"+toBeWritten.instrcutionIndex+"("+toBeWritten.op+") is writing ");
				switch(toBeWritten.op){
				case "SW":
					dataCache.write(toBeWritten.A, (short)toBeWritten.Vk, true);
					break;
				case "LW":
					value = (int) ((Short)dataCache.read(toBeWritten.A, true));
					break;
				case "ADD": 
				case "ADDI":
					value = toBeWritten.Vj + toBeWritten.Vk;
					break;
				case "SUB" : 
					value = toBeWritten.Vj - toBeWritten.Vk;
					break;
				case "MUL":
					value = toBeWritten.Vj * toBeWritten.Vk;
					break;
				case "NAND":
					value = ~(toBeWritten.Vj & toBeWritten.Vk);
					break;
				case "JMP": 
					
					value=PC+toBeWritten.Vk+toBeWritten.Vj;
					break;
				case "JALR":
					value = toBeWritten.PC;
					PC = toBeWritten.Vj;
					break;
				case "RET":
					value = toBeWritten.Vj;
					break;
				case "BEQ":
					if(toBeWritten.Vj==toBeWritten.Vk&&toBeWritten.A>0) {
						value = (toBeWritten.PC+toBeWritten.A)| (1<<16);
						misprediction++;
					}else {
						if(toBeWritten.Vj!=toBeWritten.Vk&&toBeWritten.A<0) {
							value = (toBeWritten.PC)| (1<<16);
							misprediction++;
						}
						
					}
					
					break;
				
				}
				for(int i = 0;i<6;i++) {
					for(int j = 0;j<reservationStation[i].length;j++) {
						if(reservationStation[i][j].Qj==toBeWritten.Dest){
							reservationStation[i][j].Vj=value;
							reservationStation[i][j].Qj=-1;
						}
						
						if(reservationStation[i][j].Qk==toBeWritten.Dest){
							reservationStation[i][j].Vk=value;
							reservationStation[i][j].Qk=-1;
						}
					}
				}
				ROB.getData()[toBeWritten.Dest].value=value;
				ROB.getData()[toBeWritten.Dest].ready=true;
				
				 
			}
			//execution stage
			for(int i = 0;i<6;i++) {
				for(int j = 0;j<reservationStation[i].length;j++) {
					if(execute[i][j]) {
						reservationStation[i][j].cyclesRemToWrite--;
						System.out.println("I"+reservationStation[i][j].instrcutionIndex+"("+reservationStation[i][j].op+") executing and remaining "+reservationStation[i][j].cyclesRemToWrite+" cycles");
					}
				}
			}
			//issue stage
			if(issue) {
				int[] instructionIssued = instructionBuffer.poll();
				reservationStation[instructionIssued[0]][issuePos].busy=true;
				reservationStation[instructionIssued[0]][issuePos].op=Instruction.values()[instructionIssued[1]].toString();
				reservationStation[instructionIssued[0]][issuePos].cyclesRemToWrite = reservationStationLatencies[instructionIssued[0]];
				reservationStation[instructionIssued[0]][issuePos].instrcutionIndex= issueCounter++;
				reservationStation[instructionIssued[0]][issuePos].Dest=ROB.getTail();
				switch(functionalUnit.values()[instructionIssued[0]].toString()){
				case "MULT": 
				case "LOGICAL":
				case "ADD" :
					if(Instruction.values()[instructionIssued[1]].toString().equals("ADDI")){
						reservationStation[instructionIssued[0]][issuePos].Vk=instructionIssued[4];
					}
					else{
						reservationStation[instructionIssued[0]][issuePos].Qk=regTable[instructionIssued[4]];
						reservationStation[instructionIssued[0]][issuePos].Vk=regFile[instructionIssued[4]];
					}
					
					reservationStation[instructionIssued[0]][issuePos].Qj=regTable[instructionIssued[3]];
					reservationStation[instructionIssued[0]][issuePos].Vj=regFile[instructionIssued[3]];
					regTable[instructionIssued[2]]= reservationStation[instructionIssued[0]][issuePos].Dest;
					break;
				case "LOAD" :
					
					reservationStation[instructionIssued[0]][issuePos].Qj=regTable[instructionIssued[3]];
					reservationStation[instructionIssued[0]][issuePos].Vj=regFile[instructionIssued[3]];
					reservationStation[instructionIssued[0]][issuePos].A=instructionIssued[4];
					regTable[instructionIssued[2]]= reservationStation[instructionIssued[0]][issuePos].Dest;
					break;
				case "STORE" : 
					reservationStation[instructionIssued[0]][issuePos].Qj=regTable[instructionIssued[3]];
					reservationStation[instructionIssued[0]][issuePos].Vj=regFile[instructionIssued[3]];
					reservationStation[instructionIssued[0]][issuePos].Qk=regTable[instructionIssued[2]];
					reservationStation[instructionIssued[0]][issuePos].Vk=regFile[instructionIssued[2]];
					reservationStation[instructionIssued[0]][issuePos].A=instructionIssued[4];
					break;
				case "BRANCH" : 
					switch(Instruction.values()[instructionIssued[1]].toString()){
					case "RET": 
						reservationStation[instructionIssued[0]][issuePos].Qj=regTable[instructionIssued[2]];
						reservationStation[instructionIssued[0]][issuePos].Vj=regFile[instructionIssued[2]];
						reservationStation[instructionIssued[0]][issuePos].PC=instructionIssued[5];
						break;
					case "JMP":
						reservationStation[instructionIssued[0]][issuePos].Qj=regTable[instructionIssued[2]];
						reservationStation[instructionIssued[0]][issuePos].Vj=regFile[instructionIssued[2]];
						reservationStation[instructionIssued[0]][issuePos].Vk=instructionIssued[3];
						reservationStation[instructionIssued[0]][issuePos].Qk=-1;
						reservationStation[instructionIssued[0]][issuePos].PC=instructionIssued[5];
						break;
					case "JALR" :
						reservationStation[instructionIssued[0]][issuePos].Qj=regTable[instructionIssued[3]];
						reservationStation[instructionIssued[0]][issuePos].Vj=regFile[instructionIssued[3]];
						regTable[instructionIssued[2]]= reservationStation[instructionIssued[0]][issuePos].Dest;
						reservationStation[instructionIssued[0]][issuePos].PC=instructionIssued[5];
						break;
					case "BEQ":
						reservationStation[instructionIssued[0]][issuePos].Qj=regTable[instructionIssued[2]];
						reservationStation[instructionIssued[0]][issuePos].Vj=regFile[instructionIssued[2]];
						reservationStation[instructionIssued[0]][issuePos].Qk=regTable[instructionIssued[3]];
						reservationStation[instructionIssued[0]][issuePos].Vk=regFile[instructionIssued[3]];
						reservationStation[instructionIssued[0]][issuePos].A=instructionIssued[4];
						reservationStation[instructionIssued[0]][issuePos].PC=instructionIssued[5];
						break;
					}
					break;
				}
				if(reservationStation[instructionIssued[0]][issuePos].Qj!=-1&&ROB.getData()[reservationStation[instructionIssued[0]][issuePos].Qj].ready){
					
					reservationStation[instructionIssued[0]][issuePos].Vj=ROB.getData()[reservationStation[instructionIssued[0]][issuePos].Qj].value;
					reservationStation[instructionIssued[0]][issuePos].Qj=-1;
				}
				if(reservationStation[instructionIssued[0]][issuePos].Qk!=-1&&ROB.getData()[reservationStation[instructionIssued[0]][issuePos].Qk].ready){
					reservationStation[instructionIssued[0]][issuePos].Vk=ROB.getData()[reservationStation[instructionIssued[0]][issuePos].Qk].value;
					reservationStation[instructionIssued[0]][issuePos].Qk=-1;
				}
				if(instructionIssued[2]==0){
					regTable[instructionIssued[2]]=-1;
				}
				ROB.insert(new ROBEntry(ROB.getTail(),Instruction.values()[instructionIssued[1]].toString()));
				System.out.println("I"+reservationStation[instructionIssued[0]][issuePos].instrcutionIndex+" issuing in "+	reservationStation[instructionIssued[0]][issuePos].op);
				
			}
			
			stall.nextLine();
			cycles++;
			
			if(instructionBuffer.peek() == null && ROB.isEmpty()) break;
		}
		stall.close();
		System.out.println("________________Report_____________________");
		// TODO: Analyse report & statistics.
		System.out.println("Total execution Time: "+cycles);
		
		System.out.println("IPC: "+issueCounter*1.0/cycles);
		double hitRatio = ((Cache)instructionCache).getHits()*1.0/(((Cache)instructionCache).getHits()+((Cache)instructionCache).getMisses());
		System.out.println("Instruction Cache has hit ratio: "+hitRatio);
		Memory current= dataCache;
		int level =1;
		//TODO: Calculate AMAT
		double globalMissRatio=1;
		double AMAT =current.getAccessTime();
		while(current instanceof Cache){
			if(((Cache)current).getHits()+((Cache)current).getMisses()>0){
				hitRatio = ((Cache)current).getHits()*1.0/(((Cache)current).getHits()+((Cache)current).getMisses());
			}else{
				hitRatio=1;
			}
			globalMissRatio *=(1-hitRatio);
			AMAT += globalMissRatio*(((Cache) current).getLowerLevel()).getAccessTime();
			 
			System.out.println("Cache level "+level+++" has hit ratio: "+hitRatio);
			current = ((Cache)current).getLowerLevel();
		}
		System.out.println("The AMAT is equal to : "+AMAT+" cycles");
		if(predictions!=0){
			System.out.println("Misprediction Precentage: "+100*(misprediction/predictions)+"%");
		}
		else{
			System.out.println("Misprediction Precentage: 0%");
		}
		System.out.println(Arrays.toString(regFile));
			
	}
	public void flush() {
		ROB.flush();
		for(int i = 0;i<regTable.length;i++) {
			regTable[i]=-1;
		}
		functionalUnit[] functionalUnitNames = functionalUnit.values();
		for(int i=0; i<6; i++) {
			for(int j=0; j<reservationStation[i].length; j++) {
				reservationStation[i][j] = new RSEntry(functionalUnitNames[i]+""+(j+1), 
						"", -1, -1, -1, -1, -1, -1, -1, reservationStationLatencies[i], -1);
			}
		}
		while(!instructionBuffer.isEmpty()) {
			instructionBuffer.poll();
		}
		
	}
	
	public static void main(String[] args) {
		System.out.println(Instruction.values()[1].toString());
		
	}
}
