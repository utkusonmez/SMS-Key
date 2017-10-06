package bankdroid.smskey.bank;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.lang.reflect.Type;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

public class BankDescriptor {
	private static Bank[] defaultBanks = null;

	public static Bank[] getDefaultBanks() throws IOException {
		if (defaultBanks == null) {

			final List<Bank> result = loadBanks();

			defaultBanks = result.toArray(new Bank[result.size()]);
		}
		return defaultBanks;
	}

	public static Bank[] findByPhoneNumber(String phoneNumber) throws IOException {
		phoneNumber = phoneNumber.trim();
		final Bank[] banks = getDefaultBanks();
		final List<Bank> bankFound = new ArrayList<>();
		for (final Bank bank : banks) {
			if (bank.isBankPhoneNumber(phoneNumber)) {
				bankFound.add(bank);
			}
		}
		return bankFound.toArray(new Bank[bankFound.size()]);
	}

	private static List<Bank> loadBanks() throws FileNotFoundException {
		Gson gson = new Gson();
		Type listType = new TypeToken<List<Bank>>() {}.getType();

		List<Bank> results = new ArrayList<>();

		URL data = Thread.currentThread().getContextClassLoader().getResource("data");
		if (null != data) {
			File[] files = new File(data.getPath()).listFiles();
			if (null != files) {
				for (File file : files) {
					List<Bank> banks = gson.fromJson(new BufferedReader(new FileReader(file)), listType);
					results.addAll(banks);
				}
			}
		}
		return results;
	}
}
