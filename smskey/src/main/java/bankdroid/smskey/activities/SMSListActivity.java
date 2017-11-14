package bankdroid.smskey.activities;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.LoaderManager;
import android.content.CursorLoader;
import android.content.DialogInterface;
import android.content.Loader;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;
import android.widget.Toast;
import bankdroid.smskey.BankManager;
import bankdroid.smskey.Message;
import bankdroid.smskey.R;
import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.EActivity;
import org.androidannotations.annotations.ViewById;

import java.util.Date;

@EActivity(R.layout.smslist)
public class SMSListActivity extends MenuActivity implements OnItemClickListener, LoaderManager.LoaderCallbacks<Cursor> {
	private static final int LOADER_ID = 1;
	private final static int DIALOG_KNOWN_SMS = 678;
	// @formatter:off
	@ViewById(R.id.smsListView) ListView smsListView;
	// @formatter:on

	private SimpleCursorAdapter adapter;
	private String[] addresses;
	private String[] bodies;
	private long[] timestamps;
	private int addressIndex;
	private int bodyIndex;
	private int timestampIndex;

	@AfterViews
	void init() {
		final String[] columns = new String[]{"address", "body"};
		final int[] names = new int[]{R.id.smsSender, R.id.smstext};

		adapter = new SimpleCursorAdapter(this, R.layout.smslistitem, null, columns, names, 0);
		smsListView.setAdapter(adapter);
		smsListView.setOnItemClickListener(this);

		getLoaderManager().initLoader(LOADER_ID, null, this);
	}

	@Override
	protected void onResume() {
		super.onResume();
		final Cursor cursor = adapter.getCursor();
		if (null != cursor) {
			updateData(cursor);
		}
	}

	private void updateData(Cursor cursor) {
		cursor.moveToFirst();
		final int count = cursor.getCount();
		if (count == 0) {
			Toast.makeText(getApplicationContext(), R.string.noSMSInInbox, Toast.LENGTH_SHORT).show();
			Log.d(TAG, "There is no SMS in the inbox. Existing from SMS selection activity.");
			finish();
		} else {
			addresses = new String[count];
			bodies = new String[count];
			timestamps = new long[count];
			for (int i = 0; i < count; i++) {
				addresses[i] = cursor.getString(addressIndex);
				bodies[i] = cursor.getString(bodyIndex);
				timestamps[i] = cursor.getLong(timestampIndex);
				cursor.moveToNext();
			}
			cursor.moveToFirst();
		}
	}

	@Override
	public void onItemClick(final AdapterView<?> adapter, final View view, final int position, final long id) {
		final String address = addresses[position];
		final String body = bodies[position];
		final Date timestamp = new Date(timestamps[position]);

		Log.d(TAG, "SMS was selected: " + address + " :: " + body);

		//verify whether the SMS is already known or not
		final Message code = BankManager.getCode(this, address, body, timestamp, false);
		if (code != null) {
			Log.w(TAG, "User selected known SMS as sample. Identified bank: " + code.getBank().getName());
			showDialog(DIALOG_KNOWN_SMS);
			return;
		}

		GitHubSendActivity_.intent(getBaseContext()).action(ACTION_REDISPLAY)
			.extra(INTENT_GITHUB_SEND_MESSAGE, body).extra(INTENT_GITHUB_SEND_ADDRESS, address).start();
	}

	@Override
	protected Dialog onCreateDialog(final int id) {
		final Dialog dialog;
		switch (id) {
			case DIALOG_KNOWN_SMS:
				// do the work to define the pause Dialog
				final AlertDialog.Builder builder = new AlertDialog.Builder(this);
				builder.setMessage(R.string.msgKnownSMS).setCancelable(false)
					.setNeutralButton(R.string.ok, new DialogInterface.OnClickListener() {
						@Override
						public void onClick(final DialogInterface dialog, final int id) {
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

	@Override
	public Loader<Cursor> onCreateLoader(int id, Bundle args) {
		Uri uri = Uri.parse("content://sms/inbox");
		String[] projection = {"_id", "address", "person", "body", "date"};
		String selection = null;
		String[] selectionArgs = null;
		String order = "date DESC";
		return new CursorLoader(this, uri, projection, selection, selectionArgs, order);
	}

	@Override
	public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
		switch (loader.getId()) {
			case LOADER_ID:
				addressIndex = cursor.getColumnIndexOrThrow("address");
				bodyIndex = cursor.getColumnIndexOrThrow("body");
				timestampIndex = cursor.getColumnIndexOrThrow("date");
				adapter.swapCursor(cursor);
				updateData(cursor);
				break;
			default:
				break;
		}
	}

	@Override
	public void onLoaderReset(Loader<Cursor> loader) {
		adapter.swapCursor(null);
	}
}
