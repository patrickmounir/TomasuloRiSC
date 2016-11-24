package memory;

import java.util.Random;

public class MainMemory extends Memory {
	
	short[] memoryData;

	public MainMemory(int size, int cycleAccessTime, boolean writePolicy, short blockSize) {
		super(size, cycleAccessTime, false, blockSize);
		
		memoryData = new short[size];
	}

	@Override
	short[] read(short address, boolean firstLevel) {
		short[] blockData = new short[blockSize];
		int startAddress = (address/blockSize) * blockSize;
		
		for(int i=0; i<blockSize; i++)
			blockData[i] = memoryData[i+startAddress];
		
		workCycles += cycleAccessTime;
		
		return blockData;
		
		
	}

	@Override
	void write(short address, Object data, boolean firstLevel) {
		if(firstLevel){
			memoryData[address] = (short)data;
		}else 
		{
			short[] blockData = (short[]) data;
			int startAddress = (address/blockSize) * blockSize;
			
			for(int i=0; i<blockSize; i++)
				memoryData[i+startAddress] = blockData[i];
			
		}
		
		workCycles += cycleAccessTime;
		
	}
	
	 void memoryFill (){
			Random rand =new Random();

			//filling memory with blocks of data 
			for(int i=0;i<this.size;i++){
				memoryData[0] = (short)rand.nextInt();
			}
			
		}
	

}
