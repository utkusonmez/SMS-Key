package bankdroid.smskey.activities;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.RelativeLayout;
import android.widget.RelativeLayout.LayoutParams;
import android.widget.Toast;
import bankdroid.smskey.BankManager;
import bankdroid.smskey.Codes;
import bankdroid.smskey.R;
import bankdroid.smskey.bank.Bank;
import bankdroid.smskey.bank.Expression;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author gyenes
 */
public class BankEditActivity extends MenuActivity implements OnClickListener, Codes {

	private final static int[][] PATTERN_FIELDS = new int[][]{//
		{R.id.removePattern1, R.id.pattern1},//
		{R.id.removePattern2, R.id.pattern2},//
		{R.id.removePattern3, R.id.pattern3}};

	private final static int[][] PHONE_FIELDS = new int[][]{//
		{R.id.removePhoneNumber1, R.id.phoneNumber1},//
		{R.id.removePhoneNumber2, R.id.phoneNumber2},//
		{R.id.removePhoneNumber3, R.id.phoneNumber3}};

	private Bank bank;

	@Override
	protected void onCreate(final Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.bankedit);

		((Button) findViewById(R.id.done)).setOnClickListener(this);
		((Button) findViewById(R.id.cancel)).setOnClickListener(this);
		((ImageButton) findViewById(R.id.addPhoneNumber)).setOnClickListener(this);
		((ImageButton) findViewById(R.id.addPattern)).setOnClickListener(this);
		((ImageButton) findViewById(R.id.removePattern3)).setOnClickListener(this);
		((ImageButton) findViewById(R.id.removePattern2)).setOnClickListener(this);
		((ImageButton) findViewById(R.id.removePattern1)).setOnClickListener(this);
		((ImageButton) findViewById(R.id.removePhoneNumber3)).setOnClickListener(this);
		((ImageButton) findViewById(R.id.removePhoneNumber2)).setOnClickListener(this);
		((ImageButton) findViewById(R.id.removePhoneNumber1)).setOnClickListener(this);
	}

	@Override
	protected void onResume() {
		super.onResume();

		final Intent intent = getIntent();
		if (bank == null && intent != null) {
			if (Intent.ACTION_EDIT.equals(intent.getAction())) {
				final Uri uri = intent.getData();
				bank = BankManager.findByUri(getApplicationContext(), uri);
			} else if (Intent.ACTION_INSERT.equals(intent.getAction())) {
				Log.d(TAG, "Bank to be created.");
				bank = new Bank();
				bank.addPhoneNumber("");
				bank.addExtractExpression(new Expression(false, ""));
			} else {
				Log.w(TAG, "Invalid Intent Action: " + intent.getAction());
			}
		}

		if (bank != null) {
			Log.d(TAG, "Initializing the layout for bank: " + bank);
			((EditText) findViewById(R.id.bankName)).setText(bank.getName());
			((EditText) findViewById(R.id.expiry)).setText(String.valueOf(bank.getExpiry()));

			showLines(R.id.addPattern, PATTERN_FIELDS, bank.getExtractExpressions());

			showLines(R.id.addPhoneNumber, PHONE_FIELDS, bank.getPhoneNumbers());
		}
	}

	private void storeValues() {
		bank.setName(((EditText) findViewById(R.id.bankName)).getText().toString());

		try {
			bank.setExpiry(Integer.parseInt(((EditText) findViewById(R.id.expiry)).getText().toString()));
		} catch (final NumberFormatException e) {
			bank.setExpiry(-1);
		}
		saveExpressions(PATTERN_FIELDS, bank.getExtractExpressions());
		saveFields(PHONE_FIELDS, bank.getPhoneNumbers());
	}

	private void saveFields(final int[][] fields, final String[] store) {
		final int count = Math.min(fields.length, store.length);
		for (int i = 0; i < count; i++) {
			store[i] = ((EditText) findViewById(fields[i][1])).getText().toString().trim();
		}
	}

	private void saveExpressions(final int[][] fields, final Expression[] store) {
		final int count = Math.min(fields.length, store.length);
		for (int i = 0; i < count; i++) {
			store[i].setExpression(((EditText) findViewById(fields[i][1])).getText().toString().trim());
		}
	}

	private void showLines(final int topId, final int[][] fields, final Object[] values) {
		int row = Math.min(fields.length, values.length);
		//set values
		for (int i = 0; i < row; i++) {
			((EditText) findViewById(fields[i][1])).setText(values[i].toString());
		}

		if (row == 0) {
			row = 1;
			((EditText) findViewById(fields[row][1])).setText("");
		}
		//set visibility
		for (int i = 0; i < row; i++) {
			final int[] viewIds = fields[i];
			for (int j = 0; j < viewIds.length; j++) {
				if (!(i == 0 && j == 0)) // the very first element should remain invisible
					findViewById(viewIds[j]).setVisibility(View.VISIBLE);
			}
		}

		for (int i = row; i < fields.length; i++) {
			final int[] viewIds = fields[i];
			for (int j = 0; j < viewIds.length; j++) {
				findViewById(viewIds[j]).setVisibility(View.INVISIBLE);
			}
		}

		//restore layout params
		for (int i = 0; i < row - 1; i++) {
			final View below = findViewById(fields[i][0]);

			final RelativeLayout.LayoutParams layoutParams = (LayoutParams) below.getLayoutParams();
			layoutParams.addRule(RelativeLayout.BELOW, fields[i + 1][0]);
		}
		//set high
		final View high = findViewById(fields[row - 1][0]);

		final RelativeLayout.LayoutParams layoutParams = (LayoutParams) high.getLayoutParams();
		layoutParams.addRule(RelativeLayout.BELOW, topId);

		high.getParent().requestLayout();

	}

	@Override
	protected void onSaveInstanceState(final Bundle outState) {
		super.onSaveInstanceState(outState);
		storeValues();
		outState.putSerializable(BANKDROID_SMSKEY_BANK, bank);
	}

	@Override
	protected void onRestoreInstanceState(final Bundle savedInstanceState) {
		super.onRestoreInstanceState(savedInstanceState);
		if (savedInstanceState.containsKey(BANKDROID_SMSKEY_BANK)) {
			this.bank = (Bank) savedInstanceState.getSerializable(BANKDROID_SMSKEY_BANK);
		}
	}

	@Override
	public void onClick(final View button) {
		switch (button.getId()) {
			case R.id.done:
				storeValues();
				if (!isValid())
					return;
				BankManager.storeBank(getBaseContext(), bank);
				finish();
				break;

			case R.id.cancel:
				finish();
				break;

			case R.id.removePattern1:
				removePattern(1);
				break;

			case R.id.removePattern2:
				removePattern(2);
				break;

			case R.id.removePattern3:
				removePattern(3);
				break;

			case R.id.removePhoneNumber1:
				removePhoneNumber(1);
				break;

			case R.id.removePhoneNumber2:
				removePhoneNumber(2);
				break;

			case R.id.removePhoneNumber3:
				removePhoneNumber(3);
				break;

			case R.id.addPhoneNumber:
				addPhoneNumber();
				break;

			case R.id.addPattern:
				addPattern();
				break;
		}

	}

	private boolean isValid() {
		final String name = bank.getName();
		if (name == null || name.trim().length() < 1)
			return showError(R.string.specifyBankName);

		final String[] pn = bank.getPhoneNumbers();
		if (pn == null || pn.length < 1)
			return showError(R.string.minPhoneNumber);

		for (final String phoneNumber : pn) {
			if (phoneNumber == null || phoneNumber.length() < 1)
				return showError(R.string.noEmptyPhoneNumber);
		}

		final Expression[] ee = bank.getExtractExpressions();
		if (ee == null || ee.length < 1)
			return showError(R.string.minPattern);

		for (final Expression expression : ee) {
			if (expression == null || expression.getExpression().length() < 1)
				return showError(R.string.noEmptyExpression);

			//check expression
			try {
				final Pattern pattern = Pattern.compile(expression.getExpression());
				final Matcher matcher = pattern.matcher("test");
				if (matcher.groupCount() != 1) {
					throw new IllegalArgumentException("Invalid number of groups in pattern.");
				}
			} catch (final Exception e) {
				Log.d(TAG, "Failed to compile pattern: " + expression.getExpression(), e);
				return showError(R.string.invalidExpression);
			}
		}

		return true;
	}

	private boolean showError(final int messageId) {
		final Toast toast = Toast.makeText(getBaseContext(), messageId, Toast.LENGTH_SHORT);
		toast.show();
		return false;
	}

	private void addPattern() {
		final int numberOfPatterns = bank.getExtractExpressions().length;
		if (numberOfPatterns > 2) {
			showError(R.string.tooMuchPattern);
		} else {
			bank.addExtractExpression(new Expression(false, ""));
			showLines(R.id.addPattern, PATTERN_FIELDS, bank.getExtractExpressions());
		}
	}

	private void addPhoneNumber() {
		final int numberOfPhones = bank.getPhoneNumbers().length;
		if (numberOfPhones > 2) {
			showError(R.string.tooMuchPhoneNumber);
		} else {
			bank.addPhoneNumber("");
			showLines(R.id.addPhoneNumber, PHONE_FIELDS, bank.getPhoneNumbers());
		}
	}

	private void removePhoneNumber(final int i) {
		if (bank.getPhoneNumbers().length == 1) {
			showError(R.string.minPhoneNumber);
		} else {
			storeValues();
			bank.removePhoneNumber(i - 1);
			showLines(R.id.addPhoneNumber, PHONE_FIELDS, bank.getPhoneNumbers());
		}
	}

	private void removePattern(final int i) {
		if (bank.getExtractExpressions().length == 1) {
			showError(R.string.minPattern);
		} else {
			storeValues();
			bank.removeExtractExpression(i - 1);
			showLines(R.id.addPattern, PATTERN_FIELDS, bank.getExtractExpressions());
		}
	}

}
