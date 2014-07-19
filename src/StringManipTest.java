import junit.framework.Assert;
import junit.framework.TestCase;

import org.junit.Test;


public class StringManipTest extends TestCase {
	@Test
	public void testReplaceNumbersWithLetters() {
		Assert.assertTrue(StringManip.replaceNumbersWithLetters("abc123ghj").equals("abcBCDghj"));
		Assert.assertTrue(StringManip.replaceNumbersWithLetters("0123456789").equals("ABCDEFGHIJ"));
	}
	
	@Test
	public void testReplaceLettersWithNumbers() {
		Assert.assertTrue(StringManip.replaceLettersWithNumbers("ABCDEFGHIJ").equals("0123456789"));
		Assert.assertTrue(StringManip.replaceLettersWithNumbers("ABC123HIJ").equals("012123789"));
	}
}
