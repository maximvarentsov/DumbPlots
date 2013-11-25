package com.turt2live.dumbplots.plot.corner;

import static org.junit.Assert.assertEquals;

import java.io.File;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;
import org.junit.runners.MethodSorters;

import com.turt2live.dumbplots.plot.corner.CornerType;
import com.turt2live.dumbplots.plot.corner.FileMode;
import com.turt2live.dumbplots.plot.corner.PlotCorner;
import com.turt2live.dumbplots.plot.corner.PlotCornerFile;

@RunWith (JUnit4.class)
@FixMethodOrder (MethodSorters.NAME_ASCENDING)
public class PlotCornerFileTest {

	private static File testFilePath;
	private static PlotCorner corner1;
	private static PlotCorner corner2;
	private static PlotCorner corner3;
	private static PlotCorner largeCorner;

	private static int rx = 9;
	private static int ry = 5;
	private static String world = "testWorld";
	private static int maxWidth = 64;

	@BeforeClass
	public static void before() {
		testFilePath = new File("target", "testCorners");
		corner1 = new PlotCorner(8, 8, world);
		corner2 = new PlotCorner(2, 8, world);
		corner3 = new PlotCorner(8, 10, world);
		largeCorner = new PlotCorner(0, 0, world);
		largeCorner.setId(Long.MAX_VALUE, CornerType.A);
		largeCorner.setId(Long.MAX_VALUE - 1, CornerType.B);
		largeCorner.setId(Long.MAX_VALUE / 2, CornerType.C);
		largeCorner.setId(Long.MAX_VALUE - (Long.MAX_VALUE / 5), CornerType.D);
		largeCorner.setId(Long.MIN_VALUE, CornerType.UNKNOWN);

		// Wipe the file
		PlotCornerFile file = new PlotCornerFile(rx, ry, world, testFilePath, maxWidth);
		file.erase();
		file.open(FileMode.WRITE);
		file.close();
	}

	@AfterClass
	public static void after() {}

	@Test
	public void AtestWrite() {
		PlotCornerFile file = new PlotCornerFile(rx, ry, world, testFilePath, maxWidth);
		file.open(FileMode.WRITE);
		file.writeCorner(corner1);
		file.writeCorner(corner2);
		file.writeCorner(corner3);
		file.close();
	}

	@Test
	public void BtestRead() {
		PlotCornerFile file = new PlotCornerFile(rx, ry, world, testFilePath, maxWidth);
		file.open(FileMode.OPEN);
		PlotCorner cornerA = file.getCorner(corner1.getX(), corner1.getZ(), corner1.getWorld());
		PlotCorner cornerB = file.getCorner(corner2.getX(), corner2.getZ(), corner2.getWorld());
		PlotCorner cornerC = file.getCorner(corner3.getX(), corner3.getZ(), corner3.getWorld());
		PlotCorner cornerD = file.getCorner(1000, 5, world); // Should be an empty corner

		// Check corners
		for(CornerType corner : CornerType.values()) {
			assertEquals(corner1.getId(corner), cornerA.getId(corner));
			assertEquals(corner2.getId(corner), cornerB.getId(corner));
			assertEquals(corner3.getId(corner), cornerC.getId(corner));

			// Check for null/0
			assertEquals(0, cornerD.getId(corner));
		}
		file.close();
	}

	@Test
	public void CtestOverwrite() {
		PlotCornerFile file = new PlotCornerFile(rx, ry, world, testFilePath, maxWidth);
		file.erase();
		file.open(FileMode.WRITE);
		file.writeCorner(corner1);
		file.close();
	}

	@Test
	public void DtestRead() {
		PlotCornerFile file = new PlotCornerFile(rx, ry, world, testFilePath, maxWidth);
		file.open(FileMode.OPEN);
		PlotCorner cornerA = file.getCorner(corner1.getX(), corner1.getZ(), corner1.getWorld());
		PlotCorner cornerB = file.getCorner(corner2.getX(), corner2.getZ(), corner2.getWorld());
		PlotCorner cornerC = file.getCorner(corner3.getX(), corner3.getZ(), corner3.getWorld());
		PlotCorner cornerD = file.getCorner(1000, 5, world); // Should be an empty corner

		// Check corners
		for(CornerType corner : CornerType.values()) {
			assertEquals(corner1.getId(corner), cornerA.getId(corner));

			// Check for null/0
			assertEquals(0, cornerB.getId(corner));
			assertEquals(0, cornerC.getId(corner));
			assertEquals(0, cornerD.getId(corner));
		}
		file.close();
	}

	@Test
	public void EtestReadWrite() {
		// WRITE
		PlotCornerFile file = new PlotCornerFile(rx, ry, world, testFilePath, maxWidth);
		file.open(FileMode.WRITE);
		file.writeCorner(corner1);
		file.writeCorner(corner2);
		file.writeCorner(corner3);

		// READ
		file.open(FileMode.OPEN);
		PlotCorner cornerA = file.getCorner(corner1.getX(), corner1.getZ(), corner1.getWorld());
		PlotCorner cornerB = file.getCorner(corner2.getX(), corner2.getZ(), corner2.getWorld());
		PlotCorner cornerC = file.getCorner(corner3.getX(), corner3.getZ(), corner3.getWorld());
		PlotCorner cornerD = file.getCorner(1000, 5, world); // Should be an empty corner

		// Check corners
		for(CornerType corner : CornerType.values()) {
			assertEquals(corner1.getId(corner), cornerA.getId(corner));

			// Check for null/0
			assertEquals(0, cornerB.getId(corner));
			assertEquals(0, cornerC.getId(corner));
			assertEquals(0, cornerD.getId(corner));
		}
	}

	@Test
	public void FtestSize() {
		long minWrite = 0;
		long maxWrite = 0;
		long totalWrite = 0;
		long minRead = 0;
		long maxRead = 0;
		long totalRead = 0;
		int iterations = 0;

		PlotCornerFile file = new PlotCornerFile(rx, ry, world, testFilePath, maxWidth);
		file.erase();
		file.open(FileMode.WRITE);
		for(int x = 0; x < maxWidth; x++) {
			for(int z = 0; z < maxWidth; z++) {
				PlotCorner corner = new PlotCorner(x, z, world);
				for(CornerType c : CornerType.values()) {
					corner.setId(largeCorner.getId(c), c);
				}
				long startTime = System.nanoTime();
				file.writeCorner(corner);
				long writeTime = System.nanoTime() - startTime;
				if (writeTime < minWrite) {
					minWrite = writeTime;
				}
				if (writeTime > maxWrite) {
					maxWrite = writeTime;
				}
				totalWrite += writeTime;
				iterations++;
			}
		}
		file.open(FileMode.OPEN);
		for(int x = 0; x < maxWidth; x++) {
			for(int z = 0; z < maxWidth; z++) {
				long startTime = System.nanoTime();
				PlotCorner corner = file.getCorner(x, z, world);
				long readTime = System.nanoTime() - startTime;
				if (readTime < minRead) {
					minRead = readTime;
				}
				if (readTime > maxRead) {
					maxRead = readTime;
				}
				totalRead += readTime;

				for(CornerType c : CornerType.values()) {
					assertEquals(largeCorner.getId(c), corner.getId(c));
				}
			}
		}

		System.out.println("MIN WRITE TIME: " + minWrite + " ns \t(" + (minWrite / 1000000) + " ms)");
		System.out.println("MAX WRITE TIME: " + maxWrite + " ns \t(" + (maxWrite / 1000000) + " ms)");
		System.out.println("AVG WRITE TIME: " + (totalWrite / iterations) + " ns \t(" + ((totalWrite / iterations) / 1000000) + " ms)");
		System.out.println();

		System.out.println("MIN READ TIME: " + minRead + " ns \t(" + (minRead / 1000000) + " ms)");
		System.out.println("MAX READ TIME: " + maxRead + " ns \t(" + (maxRead / 1000000) + " ms)");
		System.out.println("AVG READ TIME: " + (totalRead / iterations) + " ns \t(" + ((totalRead / iterations) / 1000000) + " ms)");
		System.out.println();

		long b = file.size();
		long kb = b / 1024;
		long mb = kb / 1024;
		long gb = mb / 1024;
		System.out.println("FILE SIZE: " + b + " b | " + kb + " kb | " + mb + " mb | " + gb + " gb");

	}

}
