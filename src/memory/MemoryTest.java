package memory;

import static org.junit.Assert.*;

import org.junit.Test;

public class MemoryTest {
	MainMemory main = new MainMemory(4 * 1024, 100, true, (short) 512); // through-around

	Cache l1 = new Cache(1024, (short) 512, 1, true, 20, main); // through-around
	Cache l2 = new Cache(256, (short) 512, 1, true, 10, l1);
	Cache l3 = new Cache(128, (short) 512, 1, true, 1, l2);

	@Test
	public void testReadOnMiss() {
		main.memoryFill();
		System.out.println("---------------On Miss-----------------");

		for (short i = 0; i < main.size; i++) {

			short address = 4;
			short block1 = (short) l1.read(i, true);
			short[] blockMain = main.read(i, true);
			int j;
			for (j = 0; j < blockMain.length; j++) {
				if (blockMain[j] == block1)
					break;
			}
			assertEquals(blockMain[j], block1);
			System.out.println("l1 Work cycle: " + (l1.workCycles) + "\nMemory work cycle: " + (main.workCycles));
			assertTrue("Error in miss cache L1", l1.workCycles + main.workCycles > l1.cycleAccessTime);

		}
	}
@Test
	public void testReadOnHit() {
		main.memoryFill();
		testReadOnMiss();
		System.out.println("---------------On Hit-----------------");
		for (short i = 0; i < main.size; i++) {
			
			int l1worktime = l1.workCycles;
			int mainworktime = main.workCycles;
			short address = 4;
			short block1 = (short) l1.read(i, true);
			short[] blockMain = main.read(i, true);
			int j;

			for (j = 0; j < blockMain.length; j++) {
				if (blockMain[j] == block1)
					break;
			}
			assertEquals(blockMain[j], block1);
			System.out.println("l1 Work cycle: " + (l1.workCycles) + "\nMemory work cycle: " + (main.workCycles));
			assertTrue("Error in hit cache L1", (l1.workCycles + main.workCycles) < 2 * (l1worktime + mainworktime));

		}
	}

}
