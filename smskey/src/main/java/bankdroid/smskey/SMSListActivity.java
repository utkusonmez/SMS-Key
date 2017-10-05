package bankdroid.smskey;

import java.util.Date;
import java.util.HashSet;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;
import android.widget.Toast;
import bankdroid.smskey.bank.Bank;
import bankdroid.util.ErrorLogger;

/**
 * @author gyenes
 */
public class SMSListActivity extends MenuActivity implements OnItemClickListener
{
	private final static int DIALOG_KNOWN_SMS = 678;

	private SimpleCursorAdapter adapter;

	private String[] addresses;
	private String[] bodies;
	private long[] timestamps;

	private int addressIndex;
	private int bodyIndex;
	private int timestampIndex;

	@Override
	protected void onCreate( final Bundle savedInstanceState )
	{
		super.onCreate(savedInstanceState);

		setContentView(R.layout.smslist);

		final Cursor cursor = getContentResolver().query(Uri.parse("content://sms/inbox"),
				new String[] { "_id", "address", "person", "body", "date" }, null, null, "date DESC");

		addressIndex = cursor.getColumnIndexOrThrow("address");
		bodyIndex = cursor.getColumnIndexOrThrow("body");
		timestampIndex = cursor.getColumnIndexOrThrow("date");

		startManagingCursor(cursor); //display person if known

		final String[] columns = new String[] { "address", "body" };
		final int[] names = new int[] { R.id.smsSender, R.id.smstext };

		adapter = new SimpleCursorAdapter(this, R.layout.smslistitem, cursor, columns, names);

		final ListView list = (ListView) findViewById(R.id.smsListView);
		list.setAdapter(adapter);
		list.setOnItemClickListener(this);
	}

	@Override
	protected void onResume()
	{
		super.onResume();

		final Cursor cursor = adapter.getCursor();

		//collect data from cursor
		cursor.moveToFirst();

		final int count = cursor.getCount();
		if ( count == 0 )
		{
			final Toast toast = Toast.makeText(getApplicationContext(), R.string.noSMSInInbox, Toast.LENGTH_SHORT);
			toast.show();

			Log.d(TAG, "There is no SMS in the inbox. Existing from SMS selection activity.");
			finish();
		}
		else
		{
			addresses = new String[count];
			bodies = new String[count];
			timestamps = new long[count];
			for ( int i = 0; i < count; i++ )
			{
				addresses[i] = cursor.getString(addressIndex);
				bodies[i] = cursor.getString(bodyIndex);
				timestamps[i] = cursor.getLong(timestampIndex);

				cursor.moveToNext();
			}

			cursor.moveToFirst();
		}
	}

	@Override
	public void onItemClick( final AdapterView<?> adapter, final View view, final int position, final long id )
	{
		final String address = addresses[position];
		final String body = bodies[position];
		final Date timestamp = new Date(timestamps[position]);

		Log.d(TAG, "SMS was selected: " + address + " :: " + body);

		//verify whether the SMS is already known or not
		final Message code = BankManager.getCode(this, address, body, timestamp, false);
		if ( code != null )
		{
			Log.w(TAG, "User selected known SMS as sample. Identified bank: " + code.getBank().getName());
			showDialog(DIALOG_KNOWN_SMS);
			return;
		}

		//construct e-mail body
		final StringBuilder builder = new StringBuilder();

		builder.append(getString(R.string.emailPrefix)).append(" ");
		builder.append(getString(R.string.emailOriginator)).append(" <<").append(address).append(">>. ");
		builder.append(getString(R.string.emailSMSText)).append(" {{").append(body).append("}} \n\n");
		builder.append(getString(R.string.app_name)).append(" ");
		//set version number
		try
		{
			final PackageManager manager = getPackageManager();
			final PackageInfo info = manager.getPackageInfo(getPackageName(), 0);
			final String versionName = info.versionName;
			builder.append("v").append(versionName);
		}
		catch ( final NameNotFoundException e )
		{
			Log.e(TAG, "Error getting package name.", e);
		}

		//generate DB stats for debugging purposes
		final Bank[] banks = BankManager.getAllBanks(this);
		final HashSet<String> countries = new HashSet<String>();
		for ( final Bank bank : banks )
		{
			countries.add(bank.getCountryCode());
		}
		final int countryCount = countries.size();
		final int bankCount = banks.length;

		builder.append("\n").append(String.format(getString(R.string.statText), bankCount, countryCount));

		final SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
		final String installLog = preferences.getString(Codes.PREF_INSTALL_LOG, "");
		if ( installLog.length() > 1 )
			builder.append("\n").append(installLog);

		ErrorLogger.sendEmail(this, new String[] { SUBMISSION_ADDRESS }, getString(R.string.emailSubject),
				builder.toString());//no I18N
	}

	@Override
	protected Dialog onCreateDialog( final int id )
	{
		final Dialog dialog;
		switch ( id )
		{
		case DIALOG_KNOWN_SMS:
			// do the work to define the pause Dialog
			final AlertDialog.Builder builder = new AlertDialog.Builder(this);
			builder.setMessage(R.string.msgKnownSMS).setCancelable(false)
					.setNeutralButton(R.string.ok, new DialogInterface.OnClickListener()
					{
						@Override
						public void onClick( final DialogInterface dialog, final int id )
						{
							dialog.cancel();
						}
					});
			dialog = builder.create();
			break;
		default:
			dialog = null;
		}
		return dialog;
	}

}
