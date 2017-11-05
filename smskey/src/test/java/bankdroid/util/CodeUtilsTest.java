package bankdroid.util;

import com.tngtech.java.junit.dataprovider.DataProvider;
import com.tngtech.java.junit.dataprovider.DataProviderRunner;
import com.tngtech.java.junit.dataprovider.UseDataProvider;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.io.FileNotFoundException;

@RunWith(DataProviderRunner.class)
public class CodeUtilsTest {

	private CodeUtils codeUtils = new CodeUtils();

	@DataProvider
	public static Object[][] testData() throws FileNotFoundException {
		return new Object[][]{
			{"1234", 0, "1234"},
			{"1234", 1, "1 2 3 4"},
			{"1234", 2, "12 34"},
			{"1234", 3, "123 4"},
			{"1234", 4, "1234"}
		};
	}

	@Test
	@UseDataProvider("testData")
	public void testSplitCode(String code, int splitSize, String expectedResult) throws Exception {
		Assert.assertEquals(expectedResult, codeUtils.splitCode(code, splitSize));
	}

}
