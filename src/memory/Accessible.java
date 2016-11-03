package memory;

public interface Accessible {

	Object read(short address, boolean firstLevel);
	void write(short address, Object data, boolean firstLevel);
}
