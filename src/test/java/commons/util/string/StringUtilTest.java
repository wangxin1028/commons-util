package commons.util.string;

import static org.junit.Assert.*;

import org.junit.AfterClass;
import org.junit.Test;

public class StringUtilTest {

	@AfterClass
	public static void tearDownAfterClass() throws Exception {
	}

	@Test
	public void testParseModelYear() {
		fail("Not yet implemented");
	}

	@Test
	public void testGetModelYear() {
		int year = StringUtil.getModelYear("现代-索纳塔八 61款 2.0L 自动尊贵版");
		System.out.println(year);
	}

}
