package org.gottschd;

public class AlchemicalReductionVersion002 implements AlchemicalReductionable {

	@Override
	public String reduce(String pInput) {
		char[] in = pInput.toCharArray();
		for (int i = 0; i < in.length - 1; i++) {

			char curChar = in[i];
			char nextChar = in[i + 1];

			if (curChar != nextChar && (Character.toLowerCase(curChar) == Character.toLowerCase(nextChar))) {
				char[] reducedChars = new char[in.length - 2];

				// first part
				System.arraycopy(in, 0, reducedChars, 0, i);

				// last Part
				System.arraycopy(in, i + 2, reducedChars, i, in.length - (i + 2));

				return reduce(new String(reducedChars));
			}

		}
		return pInput;
	}
}
