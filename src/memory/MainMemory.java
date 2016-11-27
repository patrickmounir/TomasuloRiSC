package memory;

public final class MainMemory extends Memory {
	
	public short[] memoryData;

	public MainMemory(int size, int cycleAccessTime, boolean writePolicy, short blockSize) {
		super(size, cycleAccessTime, false, blockSize);
		
		memoryData = new short[size];
	}

	@Override
	short[] read(int address, boolean firstLevel) {
		short[] blockData = new short[blockSize];
		int startAddress = (address/blockSize) * blockSize;
		
		for(int i=0; i<blockSize; i++)
			blockData[i] = memoryData[i+startAddress];
		
		workCycles += cycleAccessTime;
		
		return blockData;
		
		
	}

	@Override
	public void write(int address, Object data, boolean firstLevel) {
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
	

}
