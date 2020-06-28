package org.gottschd;

import static org.junit.Assert.assertEquals;

import java.util.ArrayList;
import java.util.List;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

@RunWith(Parameterized.class)
public class AlchemicalReductionTest {
	private final AlchemicalReductionable reducer;

	public AlchemicalReductionTest(AlchemicalReductionable pReducer) {
		reducer = pReducer;
	}

	@Parameters
	public static List<Object[]> getParameters() {
		List<Object[]> params = new ArrayList<>();
		params.add(new Object[] { new AlchemicalReductionVersion001() });
		params.add(new Object[] { new AlchemicalReductionVersion002() });
		params.add(new Object[] { new AlchemicalReductionVersion003() });
		return params;
	}

	@Test
	public void testReduction() {
		assertEquals("dabCBAcaDA", reducer.reduce("dabAcCaCBAcCcaDA"));
	}

	@Test
	public void testReductionEmpty() {
		assertEquals("", reducer.reduce(""));
	}

	@Test
	public void testReductionSingleChar() {
		assertEquals("A", reducer.reduce("A"));
	}

	@Test
	public void testReductiontTwoCharsReduction() {
		assertEquals("", reducer.reduce("Aa"));
	}

	@Test
	public void testReductiontTwoChars() {
		assertEquals("AB", reducer.reduce("AB"));
	}
}
