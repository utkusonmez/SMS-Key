package bankdroid.smskey;

import android.content.ContentValues;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;
import bankdroid.smskey.bank.Bank;
import bankdroid.smskey.bank.BankDescriptor;
import bankdroid.smskey.bank.Expression;
import bankdroid.util.ErrorLogger;

/**
 * This class helps open, create, and upgrade the database file.
 */
class DatabaseHelper extends SQLiteOpenHelper {

	private static final String T_BANK = "T_BANK";
	private static final String DATABASE_NAME = "bank.db";
	private static final int DATABASE_VERSION = 14;//2012-10-23 //2012-06-26

	private final Context context;

	DatabaseHelper(final Context context) {
		super(context, DATABASE_NAME, null, DATABASE_VERSION);
		this.context = context;
	}

	@Override
	public void onCreate(final SQLiteDatabase db) {
		db.execSQL("CREATE TABLE " + T_BANK + " (" + //
			Bank.F__ID + " INTEGER PRIMARY KEY," + //
			Bank.F_NAME + " TEXT," + //
			Bank.F_VALIDITY + " INTEGER," + //
			Bank.F_COUNTRY + " TEXT," + //
			Bank.F_PHONENUMBERS + " TEXT," + //
			Bank.F_EXPRESSIONS + " TEXT," + //
			Bank.F_LASTMESSAGE + " TEXT," + //
			Bank.F_LASTADDRESS + " TEXT," + //
			Bank.F_TIMESTAMP + " TEXT" + //
			");");

		insertDefaultBanks(db);
	}

	private void insertDefaultBanks(final SQLiteDatabase db) {
		//load constants here
		try {
			final Bank[] banks = new BankDescriptor(context).getDefaultBanks();
			final ContentValues values = new ContentValues(9);
			for (int i = 0; i < banks.length; i++) {
				final Bank bank = banks[i];
				values.clear();
				values.put(Bank.F_NAME, bank.getName());
				values.put(Bank.F_VALIDITY, bank.getExpiry());
				values.put(Bank.F_COUNTRY, bank.getCountryCode());
				values.put(Bank.F_PHONENUMBERS, BankManager.escapeStrings(bank.getPhoneNumbers()));
				final Expression[] exps2 = bank.getExtractExpressions();
				final String[] exps = new String[exps2.length];
				for (int j = 0; j < exps.length; j++) {
					exps[j] = exps2[j].toString(); //toString is necessary to persist the Transaction Sign flag
				}
				values.put(Bank.F_EXPRESSIONS, BankManager.escapeStrings(exps));

				db.insert(T_BANK, null, values);
			}
		} catch (final Exception e) {
			ErrorLogger.logError(context, e, "DBINIT");
		}
	}

	public void reset() {
		fullCleanUp(getWritableDatabase());
	}

	private void fullCleanUp(final SQLiteDatabase db) {
		Log.w(Codes.TAG, "Upgrading database that will destroy all old data.");
		db.execSQL("DROP TABLE IF EXISTS " + T_BANK);
		onCreate(db);
	}

	@Override
	public void onUpgrade(final SQLiteDatabase db, final int oldVersion, final int newVersion) {
		// if there is not other special reason it means that only the bank list is updated.
		db.delete(T_BANK, Bank.F_COUNTRY + "<>'" + Bank.CUSTOM_COUNTRY + "'", null);
		insertDefaultBanks(db);
	}
}
