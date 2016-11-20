package simulator;

import java.util.Arrays;

import memory.Cache;

public class CPU {
	static enum functionalUnit{
		LOAD, STROE, LOGICAL, ADD,MUL,BRANCH
	}
	static enum Instruction{
		LW,SW,JMP,BEQ,JALR,RET,ADD,ADDI,SUB,NAND,MUL
	}
	int [] regFile = new int[8];
	int [] regTable = new int[8];
	RSEntry [][] reservationStation = new RSEntry[6][];
	Cache instructionCache;
	Cache dataCache;
	int PC;
	
	public CPU(Cache instructionCache, Cache dataCache,int ...unitsNo) {
		this.instructionCache=instructionCache;
		this.dataCache=dataCache;
		Arrays.fill(regTable, -1);
		int i = 0;
		for(int size : unitsNo){
			reservationStation[i++] = new RSEntry [size];
		}
	}
	public int[] decode(int instruction){
		
		int opcode = (instruction&(7<<13))>>13;
		int functional= (instruction&(3));
		int [] decoded = null;
		switch(opcode){
		case 7:
			decoded = new int [5];
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
			case 3:decoded[0] = functionalUnit.MUL.ordinal();
		       decoded[1] =Instruction.MUL.ordinal() ;
		       break;
				
			}
			case 0:
				decoded = new int [5];
				decoded[0]=functionalUnit.LOAD.ordinal();
				decoded[1] =Instruction.LW.ordinal() ;
				decoded[2] = (instruction&(7<<10))>>10;
				decoded[3] =(instruction&(7<<7))>>7;
				decoded[4] = (instruction&(127));break;
			case 1:
				decoded = new int [5];
				decoded[0]=functionalUnit.STROE.ordinal();
				decoded[1] =Instruction.SW.ordinal() ;
				decoded[2] = (instruction&(7<<10))>>10;
				decoded[3] =(instruction&(7<<7))>>7;
				decoded[4] = (instruction&(127));break;
			case 2:
				decoded = new int [5];
				decoded[0]=functionalUnit.BRANCH.ordinal();
				decoded[1] =Instruction.BEQ.ordinal() ;
				decoded[2] = (instruction&(7<<10))>>10;
				decoded[3] =(instruction&(7<<7))>>7;
				decoded[4] = (instruction&(127));break;
			case 3:
				decoded = new int [5];
				decoded[0]=functionalUnit.ADD.ordinal();
				decoded[1] =Instruction.ADDI.ordinal() ;
				decoded[2] = (instruction&(7<<10))>>10;
				decoded[3] =(instruction&(7<<7))>>7;
				decoded[4] = (instruction&(127));break;
			case 4:
				decoded = new int [4];
				decoded[0]=functionalUnit.BRANCH.ordinal();
				decoded[1] =Instruction.JMP.ordinal() ;
				decoded[2] = (instruction&(7<<10))>>10;
				decoded[3] =(instruction&(1023));
				break;

		case 5:decoded = new int [4];
				decoded[0]=functionalUnit.BRANCH.ordinal();
				decoded[1] =Instruction.JALR.ordinal() ;
				decoded[2] = (instruction&(7<<10))>>10;
				decoded[3] =(instruction&(7<<7))>>7;
				break;
		case 6:decoded = new int [3];
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

		public RSEntry(String name, String op, int vj, int vk, int qj, int qk, int dest, int a,int cyclesRemToWrite,int instructionIndex) {
			this.name = name;
			this.op = op;
			Vj = vj;
			Vk = vk;
			Qj = qj;
			Qk = qk;
			Dest = dest;
			A = a;
			this.cyclesRemToWrite = cyclesRemToWrite;
			this.instrcutionIndex=instructionIndex;
			busy=true;
		}
		public String toString(){
			return "<" + name + ", " + busy + ", " + op + ", " + Vj + ", " + Vk + ", " + Qj + ", " + Qk + ", " + Dest + ", " + A + ", " + cyclesRemToWrite + ">";
		} 
		
	}
	public static void main(String[] args) {
		System.out.println(Arrays.toString(Instruction.values()));
	}
}
