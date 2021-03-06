package org.gottschd;

public class AlchemicalReductionVersion001 implements AlchemicalReductionable {

	@Override
	public String reduce(String pInput) {
		char[] in = pInput.toCharArray();
		for (int i = 0; i < in.length - 1; i++) {
			char curChar = in[i];
			char nextChar = in[i + 1];

			if (curChar != nextChar && (Character.toLowerCase(curChar) == Character.toLowerCase(nextChar))) {
				String firstPart = new String(in, 0, i);
				String lastPart = new String(in, i + 2, in.length - (i + 2));
				return reduce(firstPart + lastPart);
			}

		}
		return pInput;
	}
}
