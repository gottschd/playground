package org.gottschd;

import static org.junit.Assert.assertEquals;

import org.gottschd.AlchemicalReduction;
import org.junit.Test;

public class AlchemicalReduction001Test {
	@Test
	public void testReduction() {
		AlchemicalReduction reduction = new AlchemicalReduction();
		assertEquals("dabCBAcaDA", reduction.reduce001("dabAcCaCBAcCcaDA"));
	}

	@Test
	public void testReductionEmpty() {
		AlchemicalReduction reduction = new AlchemicalReduction();
		assertEquals("", reduction.reduce001(""));
	}

	@Test
	public void testReductionSingleChar() {
		AlchemicalReduction reduction = new AlchemicalReduction();
		assertEquals("A", reduction.reduce001("A"));
	}

	@Test
	public void testReductiontTwoCharsReduction() {
		AlchemicalReduction reduction = new AlchemicalReduction();
		assertEquals("", reduction.reduce001("Aa"));
	}

	@Test
	public void testReductiontTwoChars() {
		AlchemicalReduction reduction = new AlchemicalReduction();
		assertEquals("AB", reduction.reduce001("AB"));
	}
}
