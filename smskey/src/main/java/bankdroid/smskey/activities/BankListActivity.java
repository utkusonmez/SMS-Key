package bankdroid.smskey.activities;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.AdapterContextMenuInfo;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.CheckBox;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;
import android.widget.SimpleCursorAdapter.ViewBinder;
import android.widget.TextView;
import android.widget.Toast;
import bankdroid.smskey.Codes;
import bankdroid.smskey.R;
import bankdroid.smskey.bank.Bank;
import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.EActivity;
import org.androidannotations.annotations.OptionsItem;
import org.androidannotations.annotations.OptionsMenu;
import org.androidannotations.annotations.ViewById;

@EActivity(R.layout.banklist)
@OptionsMenu(R.menu.banklistmenu)
public class BankListActivity extends MenuActivity implements Codes, OnItemClickListener, OnClickListener {
	SimpleCursorAdapter adapter;
	// @formatter:off
	@ViewById(R.id.bankListView) ListView bankListView;
	@ViewById(R.id.showAllCountry) CheckBox showAllCountry;
	// @formatter:on

	private boolean filtered = true;
	private String userCountry;

	@AfterViews
	void init() {
		final TelephonyManager telephony = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
		userCountry = telephony.getSimCountryIso().toUpperCase();
		Log.d(TAG, "User's country: " + userCountry);

		final Cursor cursor = getContentResolver().query(CONTENT_URI,
			new String[]{Bank.F__ID, Bank.F_NAME, Bank.F_PHONENUMBERS, Bank.F_COUNTRY}, Bank.F_COUNTRY + "=?",
			new String[]{userCountry}, Bank.DEFAULT_SORT_ORDER);

		startManagingCursor(cursor);

		final String[] columns = new String[]{Bank.F_NAME, Bank.F_PHONENUMBERS};
		final int[] names = new int[]{R.id.bankName, R.id.phoneNumber};

		adapter = new SimpleCursorAdapter(this, R.layout.banklistitem, cursor, columns, names);
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

		bankListView.setAdapter(adapter);
		bankListView.setOnItemClickListener(this);
		registerForContextMenu(bankListView);

		showAllCountry.setOnClickListener(this);
	}

	@Override
	protected void onResume() {
		super.onResume();

		setCursor();
	}

	@Override
	public void onItemClick(final AdapterView<?> parent, final View view, final int position, final long id) {
		if (parent.getId() == R.id.bankListView) {
			Log.d(TAG, "Following pos is selected: " + position);
			startEdit(id);
		}
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
			final Toast succes = Toast.makeText(getBaseContext(), R.string.bankDeleted, Toast.LENGTH_SHORT);
			succes.show();
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

	@Override
	public void onClick(final View view) {
		if (view.getId() == R.id.showAllCountry) {
			setCursor();
		}
	}

	private void setCursor() {
		filtered = !showAllCountry.isChecked();

		final Cursor old = adapter.getCursor();
		if (old != null) {
			stopManagingCursor(adapter.getCursor());
			old.close();
		}

		final Cursor cursor = getContentResolver().query(CONTENT_URI,
			new String[]{Bank.F__ID, Bank.F_NAME, Bank.F_PHONENUMBERS, Bank.F_COUNTRY},
			filtered ? Bank.F_COUNTRY + "=?" : null, filtered ? new String[]{userCountry} : null,
			Bank.DEFAULT_SORT_ORDER);

		startManagingCursor(cursor);

		adapter.changeCursor(cursor);
	}
}
