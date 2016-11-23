package simulator;

import java.util.Arrays;

public class Queue{
	private ROBEntry [] data;
	
	public ROBEntry[] getData() {
		return data;
	}

	public int getTail() {
		return tail;
	}

	private int size = 0, head = 0, tail = 0;
	
	public Queue(int robSize) {
		data = new ROBEntry [robSize];
	}
	
	public boolean isEmpty(){
		return size == 0;
	}
	
	public boolean isFull(){
		return size == data.length;
	}
	
	public int insert(ROBEntry entry){
		data[tail] = entry;
		int index = tail;
		tail = (tail + 1) % data.length;
		size++;
		return index;
	}
	
	public ROBEntry remove(){
		ROBEntry result = data[head];
		if(!result.ready){
			return null;
		}
		head = (head + 1) % data.length;
		size--;
		return result;
	}
	
	
	static class ROBEntry{
		int index;
		String instructionType;
		int value;
		boolean ready;
		public ROBEntry(int index, String instructionType) {
			this.instructionType =instructionType;
			this.index =index;
			ready=false;
		}
		public String toString(){
			return "<"+ index+", "+instructionType+", "+value+", "+ready+">";
		}
		
	}
	
	public static void main(String[] args) {
		Queue q = new Queue(3);
		q.insert(new ROBEntry(q.tail,"BEQ"));
		q.insert(new ROBEntry(q.tail,"INT"));
		System.out.println(Arrays.toString(q.data)+" size:"+q.size+" head:"+q.head+" tail:"+q.tail);
		q.remove();
		System.out.println(Arrays.toString(q.data)+" size:"+q.size+" head:"+q.head+" tail:"+q.tail);
		q.data[0].value=10;
		q.data[0].ready=true;
		q.remove();
		System.out.println(Arrays.toString(q.data)+" size:"+q.size+" head:"+q.head+" tail:"+q.tail);
		q.insert(new ROBEntry(q.tail,"DDA"));
		q.insert(new ROBEntry(q.tail,"TDP"));
		System.out.println(Arrays.toString(q.data)+" size:"+q.size+" head:"+q.head+" tail:"+q.tail);
		
	}
}
