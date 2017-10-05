package bankdroid.smskey;

import java.util.HashMap;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;
import android.text.TextUtils;
import android.util.Log;
import bankdroid.smskey.bank.Bank;
import bankdroid.smskey.bank.BankDescriptor;
import bankdroid.smskey.bank.Expression;
import bankdroid.util.ErrorLogger;

public class BankProvider extends ContentProvider implements Codes
{

	public static void resetDb( final Context context )
	{
		final DatabaseHelper helper = new DatabaseHelper(context);
		helper.reset();
		helper.close();
	}

	/**
	 * This class helps open, create, and upgrade the database file.
	 */
	private static class DatabaseHelper extends SQLiteOpenHelper
	{

		private static final String DATABASE_NAME = "bank.db";
		private static final int DATABASE_VERSION = 14;//2012-10-23 //2012-06-26

		private final Context context;

		DatabaseHelper( final Context context )
		{
			super(context, DATABASE_NAME, null, DATABASE_VERSION);
			this.context = context;
		}

		@Override
		public void onCreate( final SQLiteDatabase db )
		{
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

		private void insertDefaultBanks( final SQLiteDatabase db )
		{
			//load constants here
			try
			{
				final Bank[] banks = BankDescriptor.getDefaultBanks();
				final ContentValues values = new ContentValues(9);
				for ( int i = 0; i < banks.length; i++ )
				{
					final Bank bank = banks[i];
					values.clear();
					values.put(Bank.F_NAME, bank.getName());
					values.put(Bank.F_VALIDITY, bank.getExpiry());
					values.put(Bank.F_COUNTRY, bank.getCountryCode());
					values.put(Bank.F_PHONENUMBERS, BankManager.escapeStrings(bank.getPhoneNumbers()));
					final Expression[] exps2 = bank.getExtractExpressions();
					final String[] exps = new String[exps2.length];
					for ( int j = 0; j < exps.length; j++ )
					{
						exps[j] = exps2[j].toString(); //toString is necessary to persist the Transaction Sign flag
					}
					values.put(Bank.F_EXPRESSIONS, BankManager.escapeStrings(exps));

					db.insert(T_BANK, null, values);
				}
			}
			catch ( final Exception e )
			{
				ErrorLogger.logError(context, e, "DBINIT");
			}
		}

		public void reset()
		{
			fullCleanUp(getWritableDatabase());
		}

		private void fullCleanUp( final SQLiteDatabase db )
		{
			Log.w(TAG, "Upgrading database that will destroy all old data.");
			db.execSQL("DROP TABLE IF EXISTS " + T_BANK);
			onCreate(db);
		}

		@Override
		public void onUpgrade( final SQLiteDatabase db, final int oldVersion, final int newVersion )
		{
			// if there is not other special reason it means that only the bank list is updated. 
			db.delete(T_BANK, Bank.F_COUNTRY + "<>'" + Bank.CUSTOM_COUNTRY + "'", null);
			insertDefaultBanks(db);
		}
	}

	private DatabaseHelper dbHelper;

	private static HashMap<String, String> projectionMap;

	private static final int BANKS = 1;
	private static final int BANK_ID = 2;

	private static final String T_BANK = "T_BANK";

	private static final UriMatcher uriMatcher;

	static
	{
		uriMatcher = new UriMatcher(UriMatcher.NO_MATCH);
		uriMatcher.addURI(PROVIDER_AUTHORITY, "banks", BANKS);
		uriMatcher.addURI(PROVIDER_AUTHORITY, "banks/#", BANK_ID);

		projectionMap = new HashMap<String, String>();
		projectionMap.put(Bank.F__ID, Bank.F__ID);
		projectionMap.put(Bank.F_NAME, Bank.F_NAME);
		projectionMap.put(Bank.F_VALIDITY, Bank.F_VALIDITY);
		projectionMap.put(Bank.F_COUNTRY, Bank.F_COUNTRY);
		projectionMap.put(Bank.F_EXPRESSIONS, Bank.F_EXPRESSIONS);
		projectionMap.put(Bank.F_PHONENUMBERS, Bank.F_PHONENUMBERS);
		projectionMap.put(Bank.F_LASTMESSAGE, Bank.F_LASTMESSAGE);
		projectionMap.put(Bank.F_LASTADDRESS, Bank.F_LASTADDRESS);
		projectionMap.put(Bank.F_TIMESTAMP, Bank.F_TIMESTAMP);
	}

	@Override
	public int delete( final Uri uri, final String selection, final String[] selectionArgs )
	{
		final SQLiteDatabase db = dbHelper.getWritableDatabase();
		int count;
		switch ( uriMatcher.match(uri) )
		{
		case BANKS:
			count = db.delete(T_BANK, selection, selectionArgs);
			break;

		case BANK_ID:
			final String bankId = uri.getPathSegments().get(1);
			count = db.delete(T_BANK, Bank.F__ID + "=" + bankId
					+ ( !TextUtils.isEmpty(selection) ? " AND (" + selection + ')' : "" ), selectionArgs);
			break;

		default:
			throw new IllegalArgumentException("Unknown URI " + uri);
		}

		getContext().getContentResolver().notifyChange(uri, null);
		return count;
	}

	@Override
	public String getType( final Uri uri )
	{
		switch ( uriMatcher.match(uri) )
		{
		case BANKS:
			return CONTENT_TYPE;

		case BANK_ID:
			return CONTENT_ITEM_TYPE;

		default:
			throw new IllegalArgumentException("Unknown URI " + uri);
		}
	}

	@Override
	public Uri insert( final Uri uri, final ContentValues values )
	{
		// Validate the requested uri
		if ( uriMatcher.match(uri) != BANKS )
		{
			throw new IllegalArgumentException("Unknown URI " + uri);
		}

		//Make sure that the fields are all set
		if ( !values.containsKey(Bank.F_NAME) || !values.containsKey(Bank.F_COUNTRY)
				|| !values.containsKey(Bank.F_EXPRESSIONS) || !values.containsKey(Bank.F_PHONENUMBERS)
				|| !values.containsKey(Bank.F_VALIDITY) )
		{
			throw new IllegalArgumentException("New bank record is not complete. Missing values!");
		}

		final SQLiteDatabase db = dbHelper.getWritableDatabase();
		final long rowId = db.insert(T_BANK, "bank", values);

		if ( rowId > 0 )
		{
			final Uri noteUri = ContentUris.withAppendedId(CONTENT_URI, rowId);
			getContext().getContentResolver().notifyChange(noteUri, null);
			return noteUri;
		}

		throw new SQLException("Failed to insert row into " + uri);
	}

	@Override
	public boolean onCreate()
	{
		dbHelper = new DatabaseHelper(getContext());
		return true;
	}

	@Override
	public Cursor query( final Uri uri, final String[] projection, final String selection,
			final String[] selectionArgs, final String sortOrder )
	{
		final SQLiteQueryBuilder qb = new SQLiteQueryBuilder();

		switch ( uriMatcher.match(uri) )
		{
		case BANKS:
			qb.setTables(T_BANK);
			qb.setProjectionMap(projectionMap);
			break;

		case BANK_ID:
			qb.setTables(T_BANK);
			qb.setProjectionMap(projectionMap);
			qb.appendWhere(Bank.F__ID + "=" + uri.getPathSegments().get(1));
			break;

		default:
			throw new IllegalArgumentException("Unknown URI " + uri);
		}

		// If no sort order is specified use the default
		String orderBy;
		if ( TextUtils.isEmpty(sortOrder) )
		{
			orderBy = Bank.DEFAULT_SORT_ORDER;
		}
		else
		{
			orderBy = sortOrder;
		}

		// Get the database and run the query
		final SQLiteDatabase db = dbHelper.getReadableDatabase();
		final Cursor c = qb.query(db, projection, selection, selectionArgs, null, null, orderBy);

		// Tell the cursor what uri to watch, so it knows when its source data changes
		c.setNotificationUri(getContext().getContentResolver(), uri);
		return c;
	}

	@Override
	public int update( final Uri uri, final ContentValues values, final String selection, final String[] selectionArgs )
	{
		final SQLiteDatabase db = dbHelper.getWritableDatabase();

		int count;
		switch ( uriMatcher.match(uri) )
		{
		case BANKS:
			count = db.update(T_BANK, values, selection, selectionArgs);
			break;

		case BANK_ID:
			final String bankId = uri.getPathSegments().get(1);
			count = db.update(T_BANK, values, Bank.F__ID + "=" + bankId
					+ ( !TextUtils.isEmpty(selection) ? " AND (" + selection + ')' : "" ), selectionArgs);
			break;

		default:
			throw new IllegalArgumentException("Unknown URI " + uri);
		}

		getContext().getContentResolver().notifyChange(uri, null);
		return count;
	}

}
