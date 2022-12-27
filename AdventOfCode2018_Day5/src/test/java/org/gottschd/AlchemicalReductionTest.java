package org.gottschd;


import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.stream.Stream;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;


class AlchemicalReductionTest {

	static Stream<AlchemicalReductionable> reducerImplStream() {
		return Stream.of( 
			new AlchemicalReductionVersion001() ,
			new AlchemicalReductionVersion002(), 
			new AlchemicalReductionVersion003() , 
			new AlchemicalReductionVersion004());
	}

	@ParameterizedTest
	@MethodSource("reducerImplStream")
	void testReduction(AlchemicalReductionable reducer) {
		assertEquals("dabCBAcaDA", reducer.reduce("dabAcCaCBAcCcaDA"));
	}

	@ParameterizedTest
	@MethodSource("reducerImplStream")
	void testReductionEmpty(AlchemicalReductionable reducer) {
		assertEquals("", reducer.reduce(""));
	}

	@ParameterizedTest
	@MethodSource("reducerImplStream")
	void testReductionSingleChar(AlchemicalReductionable reducer) {
		assertEquals("A", reducer.reduce("A"));
	}

	@ParameterizedTest
	@MethodSource("reducerImplStream")
	void testReductiontTwoCharsReduction(AlchemicalReductionable reducer) {
		assertEquals("", reducer.reduce("Aa"));
	}

	@ParameterizedTest
	@MethodSource("reducerImplStream")
	void testReductiontTwoChars(AlchemicalReductionable reducer) {
		assertEquals("AB", reducer.reduce("AB"));
	}
}
