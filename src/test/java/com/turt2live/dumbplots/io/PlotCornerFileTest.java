package com.turt2live.dumbplots.io;

import static org.junit.Assert.assertEquals;

import java.io.File;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;
import org.junit.runners.MethodSorters;

import com.turt2live.dumbplots.plot.CornerType;
import com.turt2live.dumbplots.plot.PlotCorner;

@RunWith (JUnit4.class)
@FixMethodOrder (MethodSorters.NAME_ASCENDING)
public class PlotCornerFileTest {

	private static File testFile;
	private static PlotCorner corner1;
	private static PlotCorner corner2;
	private static PlotCorner corner3;

	@BeforeClass
	public static void before() {
		testFile = new File("plotCorner.corner");
		corner1 = new PlotCorner(8, 8, "test");
		corner2 = new PlotCorner(2, 8, "test");
		corner3 = new PlotCorner(8, 10, "test");
	}

	@AfterClass
	public static void after() {
		testFile.delete();
	}

	@Test
	public void AtestWrite() {
		PlotCornerFile file = new PlotCornerFile(testFile);
		file.open(FileMode.WRITE);
		file.writeCorner(corner1);
		file.writeCorner(corner2);
		file.writeCorner(corner3);
		file.close();
	}

	@Test
	public void BtestRead() {
		PlotCornerFile file = new PlotCornerFile(testFile);
		file.open(FileMode.OPEN);
		PlotCorner cornerA = file.getCorner(corner1.getX(), corner1.getZ(), corner1.getWorld());
		PlotCorner cornerB = file.getCorner(corner2.getX(), corner2.getZ(), corner2.getWorld());
		PlotCorner cornerC = file.getCorner(corner3.getX(), corner3.getZ(), corner3.getWorld());
		PlotCorner cornerD = file.getCorner(1000, 5, "test"); // Should be an empty corner

		// Check corners
		for(CornerType corner : CornerType.values()) {
			System.out.println("A vs 1 :: CHECK " + corner.name());
			assertEquals(corner1.getInternalId(corner), cornerA.getInternalId(corner));
			System.out.println("B vs 2 :: CHECK " + corner.name());
			assertEquals(corner2.getInternalId(corner), cornerB.getInternalId(corner));
			System.out.println("C vs 3 :: CHECK " + corner.name());
			assertEquals(corner3.getInternalId(corner), cornerC.getInternalId(corner));

			// Check for null/0
			System.out.println("D vs X :: CHECK " + corner.name() + " (should not exist)");
			assertEquals(0, cornerD.getInternalId(corner));
		}
		file.close();
	}

	@Test
	public void CtestOverwrite() {
		PlotCornerFile file = new PlotCornerFile(testFile);
		file.erase();
		file.open(FileMode.WRITE);
		file.writeCorner(corner1);
		file.close();
	}

	@Test
	public void DtestRead() {
		PlotCornerFile file = new PlotCornerFile(testFile);
		file.open(FileMode.OPEN);
		PlotCorner cornerA = file.getCorner(corner1.getX(), corner1.getZ(), corner1.getWorld());
		PlotCorner cornerB = file.getCorner(corner2.getX(), corner2.getZ(), corner2.getWorld());
		PlotCorner cornerC = file.getCorner(corner3.getX(), corner3.getZ(), corner3.getWorld());
		PlotCorner cornerD = file.getCorner(1000, 5, "test"); // Should be an empty corner

		// Check corners
		for(CornerType corner : CornerType.values()) {
			System.out.println("A vs 1 :: CHECK " + corner.name());
			assertEquals(corner1.getInternalId(corner), cornerA.getInternalId(corner));

			// Check for null/0
			System.out.println("B vs X :: CHECK " + corner.name());
			assertEquals(0, cornerB.getInternalId(corner));
			System.out.println("C vs X :: CHECK " + corner.name());
			assertEquals(0, cornerC.getInternalId(corner));
			System.out.println("D vs X :: CHECK " + corner.name() + " (should not exist)");
			assertEquals(0, cornerD.getInternalId(corner));
		}
		file.close();
	}

	@Test
	public void EtestReadWrite() {
		// WRITE
		PlotCornerFile file = new PlotCornerFile(testFile);
		file.open(FileMode.WRITE);
		file.writeCorner(corner1);
		file.writeCorner(corner2);
		file.writeCorner(corner3);

		// READ
		file.open(FileMode.OPEN);
		PlotCorner cornerA = file.getCorner(corner1.getX(), corner1.getZ(), corner1.getWorld());
		PlotCorner cornerB = file.getCorner(corner2.getX(), corner2.getZ(), corner2.getWorld());
		PlotCorner cornerC = file.getCorner(corner3.getX(), corner3.getZ(), corner3.getWorld());
		PlotCorner cornerD = file.getCorner(1000, 5, "test"); // Should be an empty corner

		// Check corners
		for(CornerType corner : CornerType.values()) {
			System.out.println("A vs 1 :: CHECK " + corner.name());
			assertEquals(corner1.getInternalId(corner), cornerA.getInternalId(corner));

			// Check for null/0
			System.out.println("B vs X :: CHECK " + corner.name());
			assertEquals(0, cornerB.getInternalId(corner));
			System.out.println("C vs X :: CHECK " + corner.name());
			assertEquals(0, cornerC.getInternalId(corner));
			System.out.println("D vs X :: CHECK " + corner.name() + " (should not exist)");
			assertEquals(0, cornerD.getInternalId(corner));
		}
	}

}
