import junit.framework.Assert;
import junit.framework.TestCase;

import org.junit.Test;


public class InfDefTest extends TestCase {
	@Test
	public void testEncodeDecode() {
		String a = "ajdoisoifwur3r939y7380g7g80r4n ty 80y 78t47t4t40\n\n\n\new9093y3280r323r2yrh";
		String b = "huisdh37y2y98hiof;sfdjHUIhIPUiuip Ef wepf7fy43 3fs8 wfe8fd sd[fds\0";
		String c = "ufiosiosfds9fe9urjokmsdn;vxc ef8e8f32fh233iof32[uoe[qug]p";
		String d = "x";
		Assert.assertTrue(InfDef.decode(InfDef.encode(a)).equals(a));
		Assert.assertTrue(InfDef.decode(InfDef.encode(b)).equals(b));
		Assert.assertTrue(InfDef.decode(InfDef.encode(c)).equals(c));
		Assert.assertTrue(InfDef.decode(InfDef.encode(d)).equals(d));
	}
}
