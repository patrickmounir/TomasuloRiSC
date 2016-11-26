package assembler;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;

public class Assembler {
	//{"LW","SW","ADDI","BEQ","JMP"}
	private static ArrayList<String> instructionWithImmediate =new ArrayList<String>();
	private int lineNumber;
	public Assembler(){
		lineNumber =0;
		instructionWithImmediate.add("LW");
		instructionWithImmediate.add("SW");
		instructionWithImmediate.add("ADDI");
		instructionWithImmediate.add("BEQ");
		instructionWithImmediate.add("JMP");	
	}
	public ArrayList<Short> parse(String fileName)throws IOException, AssemblyException{
		
			BufferedReader reader = new BufferedReader(new FileReader(fileName));
			ArrayList<Short> parsedInstructions = new ArrayList<Short>();
			String instruction ="";
		
				while((instruction = reader.readLine())!=null){
					lineNumber++;
					int j;
					for(j =0;j<instruction.length();j++){
						//getting to the first space to see where to split from 
						if(instruction.charAt(j)==' '){
							break;
						}
					}
					short instructionParsed = 0;
					String operation = instruction.substring(0, j).toUpperCase().trim();//splitting the operation from the arguments
					
					instructionParsed |= getCode(operation);
					
					String arguments = instruction.substring(j);//extracting the arguments 
					
					String [] argumentsSplited = arguments.split(",");//splitting the arguments 
					
					for(int i =0; i<argumentsSplited.length;i++){
						
						String argument = argumentsSplited[i].toUpperCase().trim();
						
						if(!(instructionWithImmediate.contains(operation)&& i==argumentsSplited.length-1)){
							instructionParsed <<=3;
							instructionParsed |= getCode(argument);
						}else{
							if(operation.equals("JMP")){
								if(Integer.parseInt(argument)<512 && Integer.parseInt(argument)>-513){
									instructionParsed <<=10;
									instructionParsed |= (Short.parseShort(argument)&0x03ff);//to make all but the least significant 10 bits zeroes	
								}else{
									reader.close();
									throw new AssemblyException("The Immediate of the JMP instruction must be between 511 and -512 at line number :"+lineNumber);
								}
								
							}else{
								if(Integer.parseInt(argument)<63 && Integer.parseInt(argument)>-64){
									instructionParsed <<=7;
									instructionParsed |= (Byte.parseByte(argument)&0x7f);//to make all but the least significant 7 bits zeroes
		
									
								}else{
									reader.close();
									throw new AssemblyException("The Immediate of the BEQ,LW,SW,and ADDI instruction must be between 63 and -64 at line number :"+lineNumber);

								}
														}
							
						}
					}
					switch(operation){
					case "JALR":instructionParsed<<=7;break;
					case "RET":instructionParsed<<=10;break;
					case "NAND":
					case "SUB":
					case "ADD":
					case "MUL":instructionParsed<<=4;
								instructionParsed |= getFunctionCode(operation);
								break;
					
					}
					parsedInstructions.add(instructionParsed);
				}
			reader.close();
			// Instructionat end to stop program
			parsedInstructions.add((short)0b1110000000000111);
			//TODO: write the assembly in a seperate file
			return  parsedInstructions;
			
	}
	
	private short getFunctionCode(String operation) {
		switch(operation){
		
		case "NAND":return 0b0010;
		case "SUB":return 0b0001;
		case "ADD":return 0b0000;
		case "MUL":return 0b0011;
	
		}
		return -1;
		
	}
	//This method is for getting the code that corresponds to the operation or the registers 
	private short getCode(String nameInText) throws AssemblyException {
		switch(nameInText){
		
		case "R0":
		case "LW":return 0b000;
		case "R1":
		case "SW":return 0b001;
		case "R2":
		case "BEQ" :return 0b010;
		case "R3":
		case "ADDI": return 0b011;
		case "R4":
		case "JMP":return 0b100;
		case "R5":
		case "JALR":return 0b101;
		case "R6":
		case "RET":return 0b110;
		case "R7":
		case "NAND":
		case "SUB":
		case "ADD":
		case "MUL":return 0b111;
		default: throw new AssemblyException("There is no such instruction or register at line number : "+lineNumber);
		}
		
	}
	public static void main(String[] args) {
		Assembler assembler = new Assembler();
		try {
			ArrayList<Short> result = assembler.parse("assembly.asm");
			for(int i = 0;i<result.size();i++){
				Short line = result.get(i);
				System.out.println(line);
				String binary =Integer.toBinaryString(line);
				if(binary.length()>16){
					binary =binary.substring(binary.length()-16);
				}else{
					while(binary.length()<16){
						binary="0"+binary;
					}
				}
				System.out.println(binary);
			}
		} catch (IOException | AssemblyException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}
}
