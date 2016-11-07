package memory;

import java.util.Arrays;
import java.util.Random;

public class Cache extends Memory{

	private int associativity;
	private int hits, misses;
	protected Memory lowerLevel;
	private CacheEntry [] lines;
	
	public Cache(int size, short blockSize, int associativity, boolean writePolicy, int cycleAccessTime, Memory lowerLevel) {
		super(size, cycleAccessTime, writePolicy, blockSize);
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

	public Object read(short address, boolean firstLevel) {
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

	private short[] setAssociativeRead(short address) {
		short addressCopy = address; //make a copy of address , because the original one will be manipulated
		address=(short) (address/blockSize); //getting rid of offset
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
				hits++;
				return lines[i].data;
			}
		}
			
				
		misses++;
		
		if(emptySpaceIndex == -1){
			Random rand = new Random(); //if we found no empty space , pick a random entry to remove within the set
			int replacedIndex = start + rand.nextInt(associativity); // we add {start} to ensure its within our set
			CacheEntry entry = lines[replacedIndex];
			
			if(!writePolicy && entry.valid && entry.dirty){
				short constuctedAddress = (short) ((entry.tag * noSets + index) * blockSize); //in case we use the write policy and the entry to be replaced is valid and dirty , this entry must be written into lowerLevel in its specified address, which we re-construct using its tag
				this.lowerLevel.write(constuctedAddress, entry.data, false); // we send false to ensure that the replaced entry will be written as entire block (surely not firstLevel)
			}
			index = replacedIndex; //we update our index now with the replaced index . In case we didn't enter this if statement , the index would have remained the empty entry we found upwards (before the if statement)
		}
			
		lines[index] = new CacheEntry((short)tag, (short[]) this.lowerLevel.read(addressCopy, false)); // we search in lowerLevel , and initialize our data to whatever resulting from the read in that lowerLevel 
		lines[index].valid = true;
		return lines[index].data;
	}

	private short[] fullyAssociativeRead(short address) {
		short tag = (short) (address/blockSize);
		int emptySpaceIndex = -1;
		boolean found = false;
		for(int i=0;i<lines.length;i++){   // search the entire cache instead of just the set
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
			index = rand.nextInt(size/blockSize); // the random will be any number of the whole cache 
			
			CacheEntry entry = lines[index];
			
			if(!writePolicy && entry.valid && entry.dirty){
				short constuctedAddress = (short) (entry.tag * blockSize); //the only difference is the way of constructing the address
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
			short constuctedAddress = (short) ((entry.tag * noLines +index ) * blockSize); //different way of constructing the address
			this.lowerLevel.write(constuctedAddress, entry.data, false);
		}
			
		//no replacement policy --> no randomness --> always we know what to replace because it's a single option
		lines[index] = new CacheEntry((short)tag, (short[]) this.lowerLevel.read(addressCopy, false));
		lines[index].valid = true;
		return lines[index].data;
	}

	public void write(short address, Object data, boolean firstLevel) {
		workCycles += cycleAccessTime; //since every Time we write (either miss or hit) , we increase the workCycles
		if(!writePolicy)	//allocate
			read(address, false);
		
		if(associativity == 1)
			directMappedWrite(address, data);  // associativity = 1  --> Direct Mapped
		else if(associativity == (size / blockSize))
			fullyAssociativeWrite(address, data); // associativity = C --> Fully Associative
		else setAssociativeWrite(address, data); // Otherwise --> Set Associative (We assume CPU handles errors .. e.g :- associativity > C)
			
	}
	
	private void setAssociativeWrite(short address, Object data) {
		// TODO Auto-generated method stub
		
	}

	private void fullyAssociativeWrite(short address, Object data) {
		// TODO Auto-generated method stub
		
	}

	private void directMappedWrite(short address, Object data) {
		short addressCopy = address;
		address=(short) (address/blockSize);
		short noLines = (short) (size / blockSize);
		int index = address % noLines;
		int tag = address / noLines;
		
		CacheEntry entry = lines[index];
		if(entry.valid && entry.tag == tag){
			hits++;
			this.lowerLevel.write(addressCopy, data, false);
		}
	}

	public static void main(String[] args) {
		Cache l1 = new Cache((short)1024, (short)256, 1, true, (short)2, null);
		CacheEntry entry1 = new CacheEntry((short) 5, new short[]{1, 2, 3});
		l1.lines[0] = entry1;
		System.out.println(Arrays.toString(l1.lines));

	}
	
	
}
