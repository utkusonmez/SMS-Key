package bankdroid.smskey.activities;

import android.app.LoaderManager;
import android.content.Context;
import android.content.CursorLoader;
import android.content.Intent;
import android.content.Loader;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView.AdapterContextMenuInfo;
import android.widget.CheckBox;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;
import android.widget.SimpleCursorAdapter.ViewBinder;
import android.widget.TextView;
import android.widget.Toast;
import bankdroid.smskey.Codes;
import bankdroid.smskey.R;
import bankdroid.smskey.bank.Bank;
import org.androidannotations.annotations.AfterInject;
import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.Click;
import org.androidannotations.annotations.EActivity;
import org.androidannotations.annotations.ItemClick;
import org.androidannotations.annotations.OptionsItem;
import org.androidannotations.annotations.OptionsMenu;
import org.androidannotations.annotations.ViewById;

@EActivity(R.layout.banklist)
@OptionsMenu(R.menu.banklistmenu)
public class BankListActivity extends MenuActivity implements Codes, LoaderManager.LoaderCallbacks<Cursor> {

	private static final int LOADER_ID = 2;

	// @formatter:off
	@ViewById(R.id.bankListView) ListView bankListView;
	@ViewById(R.id.showAllCountry) CheckBox showAllCountry;
	// @formatter:on

	private SimpleCursorAdapter adapter;
	private boolean filtered = true;
	private String userCountry;

	@AfterInject
	void updateUserCountry() {
		final TelephonyManager telephony = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
		userCountry = telephony.getSimCountryIso().toUpperCase();
		Log.d(TAG, "User's country: " + userCountry);
	}

	@AfterViews
	void init() {

		final String[] columns = new String[]{Bank.F_NAME, Bank.F_PHONENUMBERS};
		final int[] names = new int[]{R.id.bankName, R.id.phoneNumber};

		adapter = new SimpleCursorAdapter(this, R.layout.banklistitem, null, columns, names, 0);
		bankListView.setAdapter(adapter);
		registerForContextMenu(bankListView);

		getLoaderManager().initLoader(LOADER_ID, null, this);
	}

	@Override
	protected void onResume() {
		super.onResume();
		updateData();
	}

	@ItemClick(R.id.bankListView)
	public void onItemClick(final int position) {
		Log.d(TAG, "Following pos is selected: " + position);
		startEdit(bankListView.getItemIdAtPosition(position));
	}

	private void startEdit(final long id) {
		BankEditActivity_.intent(getBaseContext())
			.action(Intent.ACTION_EDIT).data(Uri.withAppendedPath(CONTENT_URI, String.valueOf(id))).start();
	}

	@OptionsItem(R.id.addBank)
	void addBank() {
		BankEditActivity_.intent(getBaseContext()).action(Intent.ACTION_INSERT).data(CONTENT_URI).start();
	}

	@Override
	public boolean onContextItemSelected(final MenuItem item) {
		if (item.getItemId() == R.id.deleteBank) {
			final long id = ((AdapterContextMenuInfo) item.getMenuInfo()).id;
			getContentResolver().delete(Uri.withAppendedPath(CONTENT_URI, String.valueOf(id)), null, null);
			Toast.makeText(getBaseContext(), R.string.bankDeleted, Toast.LENGTH_SHORT).show();
		} else if (item.getItemId() == R.id.editBank) {
			startEdit(((AdapterContextMenuInfo) item.getMenuInfo()).id);
		}
		return super.onContextItemSelected(item);
	}

	@Override
	public void onCreateContextMenu(final ContextMenu menu, final View v, final ContextMenuInfo menuInfo) {
		if (v.getId() == R.id.bankListView) {
			final MenuInflater inflater = getMenuInflater();
			inflater.inflate(R.menu.banklistcontextmenu, menu);
		}
		super.onCreateContextMenu(menu, v, menuInfo);
	}

	@Click(R.id.showAllCountry)
	void updateData() {
		filtered = !showAllCountry.isChecked();
		getLoaderManager().restartLoader(LOADER_ID, null, this);
	}

	@Override
	public Loader<Cursor> onCreateLoader(int id, Bundle args) {
		String[] projection = {Bank.F__ID, Bank.F_NAME, Bank.F_PHONENUMBERS, Bank.F_COUNTRY};
		String selection = filtered ? Bank.F_COUNTRY + "=?" : null;
		String[] selectionArgs = filtered ? new String[]{userCountry} : null;
		String order = Bank.DEFAULT_SORT_ORDER;
		return new CursorLoader(this, CONTENT_URI, projection, selection, selectionArgs, order);
	}

	@Override
	public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
		switch (loader.getId()) {
			case LOADER_ID:
				final int nameIndex = cursor.getColumnIndex(Bank.F_NAME);
				final int countryIndex = cursor.getColumnIndex(Bank.F_COUNTRY);
				adapter.setViewBinder(new ViewBinder() {

					@Override
					public boolean setViewValue(final View view, final Cursor cursor, final int columnIndex) {
						if (columnIndex == nameIndex) {
							((TextView) view).setText(new StringBuilder(cursor.getString(nameIndex)).append(" [").append(
								cursor.getString(countryIndex)).append(']'));
							return true;
						}
						return false;
					}
				});
				adapter.swapCursor(cursor);
				break;
			default:
				break;
		}
	}

	@Override
	public void onLoaderReset(Loader<Cursor> loader) {
		adapter.setViewBinder(null);
		adapter.swapCursor(null);
	}
}
