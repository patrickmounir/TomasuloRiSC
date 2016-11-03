package memory;

import java.util.Arrays;
import java.util.Random;

public class Cache implements Accessible{

	private int size, workCycles, associativity;
	private boolean writePolicy; // true : through, false : back
	private int hits, misses, cycleAccessTime;
	protected Accessible lowerLevel;
	private CacheEntry [] lines;
	private short blockSize;
	
	public Cache(int size, short blockSize, int associativity, boolean writePolicy, int cycleAccesTime, Accessible lowerLevel) {
		this.size = size;
		this.blockSize = blockSize;
		this.associativity = associativity;
		this.writePolicy = writePolicy;
		this.cycleAccessTime = cycleAccessTime;
		this.lowerLevel = lowerLevel;
		this.lines = new CacheEntry[size/blockSize];
		for(int i=0;i<this.lines.length;i++)
			lines[i] = new CacheEntry((short) 0, null);
	}
	
	private static class CacheEntry{
		private boolean valid = false;
		private boolean dirty = false;
		private short tag;
		private short [] data;
		
		@SuppressWarnings("unused")
		public CacheEntry(short tag, short [] data) {
			this.tag = tag;
			this.data = data;
		}
		
		public String toString(){
			return "<" + valid + ", " + dirty + ", " + tag + ", " + Arrays.toString(data) + ">\n";
		}
	}

	@Override
	public Object read(short address, boolean firstLevel) {
		workCycles += cycleAccessTime;
		short[] result;
		if(associativity == 1)
			result = directMappedRead(address);
		else if(associativity == (size / blockSize))
			result = fullyAssociativeRead(address);
		else result = setAssociativeRead(address);
		
		int offset = address % blockSize;
		if(firstLevel)
			return result[offset];
		return result;
	}

	private short[] setAssociativeRead(short address) {
		short addressCopy = address;
		address=(short) (address/blockSize);
		short noLines = (short) (size / blockSize);
		short noSets = (short)(noLines / associativity);
		int index = address % noSets;
		int tag = address / noSets;
		int emptySpaceIndex = -1;
		boolean found = false;
		int start = index * associativity;
		
		for(int i=start;i<start + associativity;i++){
			if(!lines[i].valid && !found){
				emptySpaceIndex = i;
				found =true;
			}
				
			if(lines[i].valid && lines[i].tag == tag){
				hits++;
				return lines[i].data;
			}
		}
			
				
		misses++;
		
		if(emptySpaceIndex == -1){
			Random rand = new Random();
			int replacedIndex = start + rand.nextInt(associativity);
			CacheEntry entry = lines[replacedIndex];
			
			if(!writePolicy && entry.valid && entry.dirty){
				short constuctedAddress = (short) ((entry.tag * noSets + index) * blockSize);
				this.lowerLevel.write(constuctedAddress, entry.data, false);
			}
			index = replacedIndex;
		}
			
		lines[index] = new CacheEntry((short)tag, (short[]) this.lowerLevel.read(addressCopy, false));
		lines[index].valid = true;
		return lines[index].data;
	}

	private short[] fullyAssociativeRead(short address) {
		short tag = (short) (address/blockSize);
		int emptySpaceIndex = -1;
		boolean found = false;
		for(int i=0;i<lines.length;i++){
			if(!lines[i].valid && !found){
				emptySpaceIndex = i;
				found =true;
			}
			if(lines[i].valid && lines[i].tag == tag){
				hits++;
				return lines[i].data;
			}
		}
		misses++;
		
		int index = emptySpaceIndex;
		
		if(emptySpaceIndex == -1){
			Random rand = new Random();
			index = rand.nextInt(size/blockSize);
			
			CacheEntry entry = lines[index];
			
			if(!writePolicy && entry.valid && entry.dirty){
				short constuctedAddress = (short) (entry.tag * blockSize);
				this.lowerLevel.write(constuctedAddress, entry.data, false);
			}
		}
			
		lines[index] = new CacheEntry((short)tag, (short[]) this.lowerLevel.read(address, false));
		lines[index].valid = true;
		return lines[index].data;
	}

	private short[] directMappedRead(short address) {
		short addressCopy = address;
		address=(short) (address/blockSize);
		short noLines = (short) (size / blockSize);
		int index = address % noLines;
		int tag = address / noLines;
		
		CacheEntry entry = lines[index];
		if(entry.valid && entry.tag == tag){
			hits++;
			return entry.data;
		}
			
		misses++;
		
		if(!writePolicy && entry.valid && entry.dirty){
			short constuctedAddress = (short) ((entry.tag * noLines +index ) * blockSize);
			this.lowerLevel.write(constuctedAddress, entry.data, false);
		}
			
			
		lines[index] = new CacheEntry((short)tag, (short[]) this.lowerLevel.read(addressCopy, false));
		lines[index].valid = true;
		return lines[index].data;
	}

	@Override
	public void write(short address, Object data, boolean firstLevel) {
		// TODO Auto-generated method stub
		
	}
	
	public static void main(String[] args) {
		Cache l1 = new Cache((short)1024, (short)256, 1, true, (short)2, null);
		CacheEntry entry1 = new CacheEntry((short) 5, new short[]{1, 2, 3});
		l1.lines[0] = entry1;
		System.out.println(Arrays.toString(l1.lines));

	}
	
	
}
