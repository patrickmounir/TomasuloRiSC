package memory;

public abstract class Memory {

	protected int size, workCycles, cycleAccessTime;
	protected boolean writePolicy; // true : through, false : back
	protected short blockSize;
	
	public Memory(int size, int cycleAccessTime, boolean writePolicy, short blockSize) {
		this.size = size;
		this.cycleAccessTime = cycleAccessTime;
		this.writePolicy = writePolicy;
		this.blockSize = blockSize;
	}
	
	abstract Object read(short address, boolean firstLevel);
	abstract void write(short address, Object data, boolean firstLevel);
}
