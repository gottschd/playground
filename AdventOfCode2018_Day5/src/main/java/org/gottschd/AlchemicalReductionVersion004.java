package org.gottschd;

public class AlchemicalReductionVersion004 implements AlchemicalReductionable {

	@Override
	public String reduce(String pInput) {	
		char[] in = pInput.toCharArray();
				
		int curPos = 0;
		int reductionCount = 0;
		while( curPos < (in.length - 1 - reductionCount) ) {
			char curChar = in[curPos];
			char nextChar = in[curPos + 1];
			
			if (curChar != nextChar && (Character.toLowerCase(curChar) == Character.toLowerCase(nextChar))) {
				// reduce the chars by copy-over
				System.arraycopy(in, curPos+2, in, curPos, in.length - (curPos+2));
				
				// count that we reduced 2 chars
				reductionCount = reductionCount + 2;

				// and reset to curPos - 1, because it might happen that a new reduce is possible
				curPos--;
				
				continue;
			}
			
			curPos++;
		}
		
		return new String(in, 0, in.length - reductionCount);
	}
}
