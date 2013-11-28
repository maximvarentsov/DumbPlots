package com.turt2live.dumbplots.plot;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Random;

import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;
import org.junit.runners.MethodSorters;

@RunWith (JUnit4.class)
@FixMethodOrder (MethodSorters.NAME_ASCENDING)
public class ChunkLocTest {

	@Test
	public void AtestHashcode() {
		ChunkLoc cl1 = new ChunkLoc(8, 9);
		ChunkLoc cl2 = new ChunkLoc(8, 9);
		ChunkLoc cl3 = new ChunkLoc(9, 9);

		assertEquals(cl1.hashCode(), cl2.hashCode());
		assertNotEquals(cl1.hashCode(), cl3.hashCode());
	}

	@Test
	public void BtestHashcode2() {
		Map<ChunkLoc, String> map = new LinkedHashMap<ChunkLoc, String>();
		Random rand = new Random();
		int m = 10000;
		for(int i = 0; i < m; i++) {
			map.put(new ChunkLoc(rand.nextInt(m * m), rand.nextInt(m * m)), rand.nextInt(m / 2) + "_" + rand.nextDouble());
		}
		for(ChunkLoc location : map.keySet()) {
			ChunkLoc cl2 = new ChunkLoc(location.getX(), location.getZ());
			assertEquals(cl2.hashCode(), location.hashCode());
			assertEquals(map.get(location), map.get(cl2));
		}
	}

}
