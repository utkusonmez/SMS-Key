package bankdroid.smskey.bank.test;

import android.content.Context;
import bankdroid.smskey.bank.Bank;
import bankdroid.smskey.bank.BankDescriptor;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.tngtech.java.junit.dataprovider.DataProvider;
import com.tngtech.java.junit.dataprovider.DataProviderRunner;
import com.tngtech.java.junit.dataprovider.UseDataProvider;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Matchers;
import org.mockito.Mockito;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FilenameFilter;
import java.io.IOException;
import java.lang.reflect.Type;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

@RunWith(DataProviderRunner.class)
public class JsonTests {

	private BankDescriptor bankDescriptor;

	@DataProvider
	public static Object[][] testData() throws FileNotFoundException {
		Map<String, List<TestEntry>> load = load();

		Object[][] array = new Object[count(load)][];
		int counter = 0;
		for (Map.Entry<String, List<TestEntry>> country : load.entrySet()) {
			for (TestEntry testEntry : country.getValue()) {
				array[counter++] = new Object[]{country.getKey(), testEntry};
			}
		}
		return array;
	}

	private static int count(Map<String, List<TestEntry>> map) {
		int sum = 0;
		for (List<TestEntry> testEntries : map.values()) {
			sum = sum + testEntries.size();
		}
		return sum;
	}

	private static Map<String, List<TestEntry>> load() throws FileNotFoundException {
		Gson gson = new Gson();
		Type listType = new TypeToken<List<TestEntry>>() {
		}.getType();

		Map<String, List<TestEntry>> results = new HashMap<>();
		URL data = JsonTests.class.getClassLoader().getResource("test_cases");
		if (null != data) {
			File[] files = new File(data.getPath()).listFiles(new FilenameFilter() {
				@Override
				public boolean accept(File dir, String name) {
					return name.endsWith(".json");
				}
			});
			if (null != files) {
				for (File file : files) {
					String name = file.getName();
					List<TestEntry> banks = gson.fromJson(new BufferedReader(new FileReader(file)), listType);
					results.put(name.substring(0, name.indexOf('.')).toUpperCase(), banks);
				}
			}
		}
		return results;
	}

	@Before
	public void init() throws IOException, IllegalAccessException {
		Context context = Mockito.mock(Context.class);
		bankDescriptor = Mockito.mock(BankDescriptor.class);
		Mockito.when(bankDescriptor.loadBanks()).thenReturn(populateBankData());
		Mockito.when(bankDescriptor.getDefaultBanks()).thenCallRealMethod();
		Mockito.when(bankDescriptor.findByPhoneNumber(Matchers.anyString())).thenCallRealMethod();
	}

	@Test
	@UseDataProvider("testData")
	public void test(String country, TestEntry entry) throws IOException, IllegalAccessException {
		Bank[] banks = bankDescriptor.findByPhoneNumber(entry.getNumber());
		assertTrue("Missing banks supporting given number", banks.length > 0);
		boolean extracted = false;
		for (Bank bank : banks) {
			assertEquals("Founded bank is for different country.", country, bank.getCountryCode());
			String code = bank.extractCode(entry.getMessage());
			if (null != code) {
				assertEquals("Decoded code different than expected one.", entry.getExpectedCode(), code);
				extracted = true;
			}
		}
		assertTrue("At least one bank must decode provided code", extracted);
	}

	@Test
	public void eachBankShouldHaveAtLeastOneTestCase() throws IOException, IllegalAccessException {
		Map<String, List<TestEntry>> testCasesByCountry = load();
		List<BankFacade> decoratedBanks = getDecoratedBanks();

		for (Map.Entry<String, List<TestEntry>> byCountry : testCasesByCountry.entrySet()) {
			for (TestEntry entry : byCountry.getValue()) {
				processTestCase(decoratedBanks, entry, byCountry.getKey());
			}
		}

		List<BankFacade> missingTestCasesBanks = new ArrayList<>();
		for (BankFacade decoratedBank : decoratedBanks) {
			if (!decoratedBank.hadSuccessfulExecution()) {
				missingTestCasesBanks.add(decoratedBank);
			}
		}

		Collections.sort(missingTestCasesBanks, new Comparator<BankFacade>() {
			@Override
			public int compare(BankFacade o1, BankFacade o2) {
				int countryCompareResult = o1.getCountryCode().compareTo(o2.getCountryCode());
				if (0 == countryCompareResult) {
					return o1.getBank().getName().compareTo(o2.getBank().getName());
				} else {
					return countryCompareResult;
				}
			}
		});

		String msg = String.format("There are %s of %s banks without test cases. Missing test cases for: %s",
			missingTestCasesBanks.size(), decoratedBanks.size(), missingTestCasesBanks.toString());
		assertTrue(msg, missingTestCasesBanks.isEmpty());
	}

	private void processTestCase(List<BankFacade> decoratedBanks, TestEntry entry, String country) {
		for (BankFacade bank : decoratedBanks) {
			boolean bankSupportsCountry = bank.getCountryCode().equals(country);
			boolean bankSupportsNumber = Arrays.asList(bank.getBank().getPhoneNumbers()).contains(entry.getNumber());
			if (bankSupportsCountry && bankSupportsNumber) {
				String code = bank.extractCode(entry.getMessage());
				if (null != code && !"".equals(code)) {
					assertEquals(entry.getExpectedCode(), code);
					return;
				}
			}
		}
	}

	private List<BankFacade> getDecoratedBanks() throws IOException, IllegalAccessException {
		List<BankFacade> decorators = new ArrayList<>();
		for (Bank bank : bankDescriptor.getDefaultBanks()) {
			decorators.add(new BankFacade(bank));
		}
		return decorators;
	}

	private List<Bank> populateBankData() throws FileNotFoundException {
		Gson gson = new Gson();
		Type listType = new TypeToken<List<Bank>>() {
		}.getType();
		List<Bank> result = new ArrayList<>();

		//FIXME
		File f = new File("smskey").exists() ?
			new File("smskey/src/main/res/raw") : new File("src/main/res/raw");

		File[] files = f.listFiles();
		for (File file : files) {
			List<Bank> banks = gson.fromJson(new BufferedReader(new FileReader(file)), listType);
			result.addAll(banks);
		}
		return result;
	}

}
