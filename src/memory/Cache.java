package memory;

import java.util.Arrays;
import java.util.Random;

public final class Cache extends Memory{

	private int associativity;
	private int hits, misses;
	protected Memory lowerLevel;
	public Memory getLowerLevel() {
		return lowerLevel;
	}

	private CacheEntry [] lines;
	
	public Cache(int size, short blockSize, int associativity, boolean writePolicy, int cycleAccessTime, Memory lowerLevel) {
		super(size, cycleAccessTime, writePolicy, blockSize);
		this.lowerLevel = lowerLevel;
		this.lines = new CacheEntry[size/blockSize];
		for(int i=0;i<this.lines.length;i++)
			lines[i] = new CacheEntry(0, null);
		this.associativity = associativity;
	}
	
	private static class CacheEntry{
		private boolean valid = false;
		private boolean dirty = false;
		private int tag;
		private short [] data;
		
		public CacheEntry(int tag, short [] data) {
			this.tag = tag;
			this.data = data;
		}
		
		public String toString(){
			return "<" + valid + ", " + dirty + ", " + tag + ", " + Arrays.toString(data) + ">\n";
		}
	}

	public Object read(int address, boolean firstLevel) {
		workCycles += cycleAccessTime; //since every Time we read (either miss or hit) , we increase the workCycles
		short[] result;
		if(associativity == 1)
			result = directMappedRead(address);  // associativity = 1  --> Direct Mapped
		else if(associativity == (size / blockSize))
			result = fullyAssociativeRead(address); // associativity = C --> Fully Associative
		else result = setAssociativeRead(address); // Otherwise --> Set Associative (We assume CPU handles errors .. e.g :- associativity > C)
		
		int offset = address % blockSize;  
		if(firstLevel)
			return result[offset]; // Note that :- first level of Cache , always return a certain word , while all other levels (including memory) returns the full block
		return result;
	}

	private short[] setAssociativeRead(int address) {
		int addressCopy = address; //make a copy of address , because the original one will be manipulated
		address = (address/blockSize); //getting rid of offset
		short noLines = (short) (size / blockSize);  //calculating number of lines
		short noSets = (short)(noLines / associativity); // calculating number of sets
		int index = address % noSets;
		int tag = address / noSets;
		int emptySpaceIndex = -1; //searching for empty space , initially -1 telling we didn't find one yet
		boolean found = false;    // This boolean is used to return the first empty space , in order to place content into it (if needed) .. we wont need it for sure if we found our entry later
		int start = index * associativity; //in order to reach the first entry in our desired set , we should skip sets from 0 to index-1  , those have a count of {index} , each with {associativity} entries , so we skip {index*associativity} entries
		
		for(int i=start;i<start + associativity;i++){
			if(!lines[i].valid && !found){
				emptySpaceIndex = i; //whenever finding empty space , record it
				found =true;         // set boolean to true to prevent future overrides of the emptySpaceIndex variable
			}
				
			if(lines[i].valid && lines[i].tag == tag){
				setHits(getHits() + 1);
				return lines[i].data;
			}
		}
			
				
		setMisses(getMisses() + 1);
		index = emptySpaceIndex;
		if(emptySpaceIndex == -1){
			Random rand = new Random(); //if we found no empty space , pick a random entry to remove within the set
			int replacedIndex = start + rand.nextInt(associativity); // we add {start} to ensure its within our set
			CacheEntry entry = lines[replacedIndex];
			
			if(!writePolicy && entry.valid && entry.dirty){
				int constuctedAddress = ((entry.tag * noSets + index) * blockSize); //in case we use the write policy and the entry to be replaced is valid and dirty , this entry must be written into lowerLevel in its specified address, which we re-construct using its tag
				this.lowerLevel.write(constuctedAddress, entry.data, false); // we send false to ensure that the replaced entry will be written as entire block (surely not firstLevel)
			}
			index = replacedIndex; //we update our index now with the replaced index . In case we didn't enter this if statement , the index would have remained the empty entry we found upwards (before the if statement)
		}
		
		short [] tmp = (short[]) this.lowerLevel.read(addressCopy, false);
		short [] newer = new short[tmp.length];
		System.arraycopy(tmp, 0, newer, 0, tmp.length);
		lines[index] = new CacheEntry(tag, newer); // we search in lowerLevel , and initialize our data to whatever resulting from the read in that lowerLevel 
		lines[index].valid = true;
		return lines[index].data;
	}

	private short[] fullyAssociativeRead(int address) {
		int tag = (address/blockSize);
		int emptySpaceIndex = -1;
		boolean found = false;
		for(int i=0;i<lines.length;i++){   // search the entire cache instead of just the set
			if(!lines[i].valid && !found){
				emptySpaceIndex = i;
				found =true;
			}
			if(lines[i].valid && lines[i].tag == tag){
				setHits(getHits() + 1);
				return lines[i].data;
			}
		}
		setMisses(getMisses() + 1);
		
		int index = emptySpaceIndex;
		
		if(emptySpaceIndex == -1){
			Random rand = new Random();  
			index = rand.nextInt(size/blockSize); // the random will be any number of the whole cache 
			
			CacheEntry entry = lines[index];
			
			if(!writePolicy && entry.valid && entry.dirty){
				int constuctedAddress = (entry.tag * blockSize); //the only difference is the way of constructing the address
				this.lowerLevel.write(constuctedAddress, entry.data, false);
			}
		}
			
		short [] tmp = (short[]) this.lowerLevel.read(address, false);
		short [] newer = new short[tmp.length];
		System.arraycopy(tmp, 0, newer, 0, tmp.length);
		lines[index] = new CacheEntry(tag, newer);
		lines[index].valid = true;
		return lines[index].data;
	}

	private short[] directMappedRead(int address) {
		int addressCopy = address;
		address=(address/blockSize);
		short noLines = (short) (size / blockSize);
		int index = address % noLines;
		int tag = address / noLines;
		
		CacheEntry entry = lines[index];
		if(entry.valid && entry.tag == tag){
			setHits(getHits() + 1);
			return entry.data;
		}
			
		setMisses(getMisses() + 1);
		
		if(!writePolicy && entry.valid && entry.dirty){
			int constuctedAddress = ((entry.tag * noLines +index ) * blockSize); //different way of constructing the address
			this.lowerLevel.write(constuctedAddress, entry.data, false);
		}
			
		//no replacement policy --> no randomness --> always we know what to replace because it's a single option
		short [] tmp = (short[]) this.lowerLevel.read(addressCopy, false);
		short [] newer = new short[tmp.length];
		System.arraycopy(tmp, 0, newer, 0, tmp.length);
		lines[index] = new CacheEntry(tag, newer);
		lines[index].valid = true;
		return lines[index].data;
	}

	public void write(int address, Object data, boolean firstLevel) {
		workCycles += cycleAccessTime; //since every Time we write (either miss or hit) , we increase the workCycles
		if(!writePolicy)	//allocate--back
			read(address, false);
		
		if(associativity == 1)
			directMappedWrite(address, data, firstLevel);  // associativity = 1  --> Direct Mapped
		else if(associativity == (size / blockSize))
			fullyAssociativeWrite(address, data, firstLevel); // associativity = C --> Fully Associative
		else setAssociativeWrite(address, data, firstLevel); // Otherwise --> Set Associative (We assume CPU handles errors .. e.g :- associativity > C)
			
	}
	
	private void setAssociativeWrite(int address, Object data, boolean firstLevel) {
		int addressCopy = address; //make a copy of address , because the original one will be manipulated
		int offset = (address%blockSize);
		address=(address/blockSize); //getting rid of offset
		short noLines = (short) (size / blockSize);  //calculating number of lines
		short noSets = (short)(noLines / associativity); // calculating number of sets
		int index = address % noSets;
		int tag = address / noSets;
		int start = index * associativity; //in order to reach the first entry in our desired set , we should skip sets from 0 to index-1  , those have a count of {index} , each with {associativity} entries , so we skip {index*associativity} entries
		int i;
		for(i=start;i<start + associativity;i++){
			
			if(lines[i].valid && lines[i].tag == tag){
				if(!firstLevel){
					lines[i].data = (short []) data;
					if(!writePolicy) {	// back
						lines[i].dirty = true;
						lines[i].valid = true;
						return;
					}
					else break;
				}
				
				if(!writePolicy){ // allocate - back
					
					lines[i].data[offset] = (Short) data;
					lines[i].dirty = true;
					lines[i].valid = true;
					return;
				}
				else {
					lines[i].data[offset] = (Short) data;
					lines[i].valid = true;
					setHits(getHits() + 1);
					break;
				}
			}
		}
		if(i == (start + associativity))
			setMisses(getMisses() + 1);
		this.lowerLevel.write(addressCopy, data, firstLevel);
	}

	private void fullyAssociativeWrite(int address, Object data, boolean firstLevel) {
		int tag = (address/blockSize);
		int offset = (address % blockSize);
		int i;
		for(i=0;i<lines.length;i++){   // search the entire cache instead of just the set
			if(lines[i].valid && lines[i].tag == tag){
				
				if(!firstLevel){
					lines[i].data = (short []) data;
					if(!writePolicy) {	// back
						lines[i].dirty = true;
						lines[i].valid = true;
						return;
					}
					else break;
				}
				
				if(!writePolicy){ // allocate - back
					lines[i].data[offset] = (Short) data;
					lines[i].dirty = true;
					lines[i].valid = true;
					return;
				}
				else {
					lines[i].data[offset] = (Short) data;
					lines[i].valid = true;
					setHits(getHits() + 1);
					break;
				}
				
			}
		}
		if(i == lines.length)
			setMisses(getMisses() + 1);
		this.lowerLevel.write(address, data, firstLevel);
	}

	private void directMappedWrite(int address, Object data, boolean firstLevel) {
		
		int addressCopy = address;
		int offset = (address % blockSize);
		address= (address/blockSize);
		short noLines = (short) (size / blockSize);
		int index = address % noLines;
		int tag = address / noLines;
		
		CacheEntry entry = lines[index];
		
		if(!firstLevel){
			entry.data = (short []) data;
			if(!writePolicy) {	// back
				entry.dirty = true;
				entry.valid = true;
				return;
			}
			this.lowerLevel.write(addressCopy, (short [])data, false);
			return;
		}
		
		if(!writePolicy){ // allocate - back
			if(entry.valid && entry.tag == tag){
				entry.data[offset] = (Short) data;
				entry.dirty = true;
				entry.valid = true;
			}
		}
		else { //through - around
			if(entry.valid && entry.tag == tag){
				entry.data[offset] = (Short) data;
				entry.valid = true;
				setHits(getHits() + 1);
			}
			else setMisses(getMisses() + 1);
			
			this.lowerLevel.write(addressCopy, (Short)data, true);
		}
	}

	public static void main(String[] args) {
		
		
		/*
		 * Just Cache Testing
		 */
		MainMemory mainMemory = new MainMemory(32, 20, false, (short) 2);
		Cache cache2 = new Cache(16, (short)2, 8, true, 1, mainMemory);
		Cache cacheI = new Cache(8, (short)2, 4, true, 1, cache2);
		//Cache cacheD = new Cache(8, (short)2, 2, true, 1, cache2);
		
		cacheI.write(8, (short)22, true);
		System.out.println("Hits "+cacheI.hits+" Misses "+cacheI.misses);
		cacheI.write(9, (short)23, true);
		System.out.println("Hits "+cacheI.hits+" Misses "+cacheI.misses);
		//cacheD.write(2, (short)65, true);
		//System.out.println("Hits "+cacheD.hits+" Misses "+cacheD.misses);
		System.out.println(cacheI.read(8, true));
		System.out.println("Hits "+cacheI.hits+" Misses "+cacheI.misses);
		System.out.println(Arrays.toString(mainMemory.memoryData));
		System.out.println(Arrays.toString(cache2.lines));
		System.out.println(Arrays.toString(cacheI.lines));
		//System.out.println(Arrays.toString(cacheD.lines));
		
	}

	public int getHits() {
		return hits;
	}

	public void setHits(int hits) {
		this.hits = hits;
	}

	public int getMisses() {
		return misses;
	}

	public void setMisses(int misses) {
		this.misses = misses;
	}
	
	
}
