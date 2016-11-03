package assembler;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;

public class Assembler {
	//{"LW","SW","ADDI","BEQ","JMP"}
	private static ArrayList<String> instructionWithImmediate =new ArrayList<String>();
	
	public Assembler(){
		instructionWithImmediate.add("LW");
		instructionWithImmediate.add("SW");
		instructionWithImmediate.add("ADDI");
		instructionWithImmediate.add("BEQ");
		instructionWithImmediate.add("JMP");	
	}
	public ArrayList<Short> parse(String fileName)throws IOException{
		
			BufferedReader reader = new BufferedReader(new FileReader(fileName));
			ArrayList<Short> parsedInstructions = new ArrayList<Short>();
			String instruction ="";
		
				while((instruction = reader.readLine())!=null){
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
							instructionParsed |= getCode(argument);
						}else{
							if(operation.equals("JMP")){
								instructionParsed |= (Short.parseShort(argument)^0x03ff);//to make all but the least significant 10 bits zeroes
							}else{
								instructionParsed |= (Byte.parseByte(argument)^0x7f);//to make all but the least significant 7 bits zeroes
							}
							
						}
					}
					parsedInstructions.add(instructionParsed);
				}
				reader.close();
				
				return  parsedInstructions;
			
	}
	
	//This method is for getting the code that corresponds to the operation or the registers 
	private short getCode(String nameInText) {
		// TODO Auto-generated method stub
		return 0;
	}
}
