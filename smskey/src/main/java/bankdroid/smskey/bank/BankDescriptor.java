package bankdroid.smskey.bank;

import android.content.Context;
import android.support.annotation.VisibleForTesting;
import android.util.Log;
import bankdroid.smskey.R;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Field;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

public class BankDescriptor {
	private static Bank[] defaultBanks = null;

	private Gson gson = new Gson();
	private Context ctx;

	public BankDescriptor(Context ctx) {
		this.ctx = ctx;
	}

	public Bank[] getDefaultBanks() throws IOException, IllegalAccessException {
		if (defaultBanks == null) {

			final List<Bank> result = loadBanks();

			defaultBanks = result.toArray(new Bank[result.size()]);
		}
		return defaultBanks;
	}

	public Bank[] findByPhoneNumber(String phoneNumber) throws IOException, IllegalAccessException {
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

	@VisibleForTesting
	public List<Bank> loadBanks() throws FileNotFoundException, IllegalAccessException {
		List<Bank> results = new ArrayList<>();

		Field[] fields = R.raw.class.getFields();
		for (int count = 0; count < fields.length; count++) {
			int resourceID = fields[count].getInt(fields[count]);
			List<Bank> banks = loadRawAsset(resourceID);
			results.addAll(banks);
			Log.i("Raw Asset: ", fields[count].getName());
		}

		return results;
	}

	private List<Bank> loadRawAsset(int resourceId) {
		Type listType = new TypeToken<List<Bank>>() {
		}.getType();
		InputStream inputStream = ctx.getResources().openRawResource(resourceId);
		return gson.fromJson(new InputStreamReader(inputStream), listType);
	}

}
