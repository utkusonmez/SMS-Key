package bankdroid.smskey.bank.test;

import bankdroid.smskey.bank.Bank;
import bankdroid.smskey.bank.BankDescriptor;

import java.util.ArrayList;
import java.util.List;

public class GenerateTest {
	public static void main(final String[] args) throws Exception {
		final Bank[] defaultBanks = BankDescriptor.getDefaultBanks();

		final List<String> countries = new ArrayList<String>();
		for (int i = 0; i < defaultBanks.length; i++) {
			final String country = defaultBanks[i].getCountryCode();
			if (!countries.contains(country))
				countries.add(country);
		}

		for (final String country : countries) {
			System.out.println("\n //country " + country);
			for (final Bank bank : defaultBanks) {
				if (bank.getCountryCode().equals(country)) {
					final String name = bank.getName();
					for (final String phoneNumber : bank.getPhoneNumbers()) {
						System.out.printf("testBank(\"%s\", \"%s\");\n", name, phoneNumber);
					}
				}
			}
		}
	}
}
