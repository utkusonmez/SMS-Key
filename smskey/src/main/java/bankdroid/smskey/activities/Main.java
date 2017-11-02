package bankdroid.smskey.activities;

import android.Manifest;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.View;
import bankdroid.smskey.BankManager;
import bankdroid.smskey.BuildConfig;
import bankdroid.smskey.Codes;
import bankdroid.smskey.Eula;
import bankdroid.smskey.Message;
import bankdroid.smskey.R;
import bankdroid.smskey.SMSCheckerTask;
import bankdroid.smskey.SMSCheckerTask.OnFinishListener;
import bankdroid.smskey.bank.Bank;

import java.util.List;

public class Main extends MenuActivity implements Codes {
	private final static int DIALOG_NOCODE = 567;
	private static final int PERMISSION_REQUEST_CODE = 123;
	private static final int IGNORED_REQUEST_CODE = 0;
	private ProgressDialog progressDialog;

	@Override
	protected void onCreate(final Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		Eula.show(this);

		setContentView(R.layout.main);

		if(ContextCompat.checkSelfPermission(getBaseContext(), Manifest.permission.READ_SMS) == PackageManager.PERMISSION_DENIED) {
			ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_SMS}, IGNORED_REQUEST_CODE);
		}

		if (BuildConfig.DEBUG) {
			Intent smsRecvIntent = new Intent("android.provider.Telephony.SMS_RECEIVED");
			List<ResolveInfo> infos = getPackageManager().queryBroadcastReceivers(smsRecvIntent, 0);
			for (ResolveInfo info : infos) {
				Log.d(TAG, "Receiver: " + info.activityInfo.name + ", priority=" + info.priority);
			}
		}

		//find out whether there is any supported bank in the country
		final TelephonyManager telephonyManager = (TelephonyManager) getSystemService(TELEPHONY_SERVICE);
		final String userCountry = telephonyManager.getSimCountryIso().toUpperCase();

		final Cursor cursor = getContentResolver().query(CONTENT_URI, new String[]{Bank.F__ID,},
			Bank.F_COUNTRY + "=?", new String[]{userCountry}, Bank.DEFAULT_SORT_ORDER);
		int numberOfBanks = 0;
		if (cursor != null) {
			numberOfBanks = cursor.getCount();
			cursor.close();
		}

		Log.d(TAG, "Number of banks in " + userCountry + " is " + numberOfBanks + ".");
		findViewById(R.id.bankWarning).setVisibility(numberOfBanks > 0 ? View.GONE : View.VISIBLE);
	}

	public void onSubmitSample(final View v) {
		final Intent submitIntent = new Intent(getBaseContext(), SMSListActivity.class);
		startActivity(submitIntent);
	}

	public void onViewLastCode(final View v) {
		if(ContextCompat.checkSelfPermission(getBaseContext(), Manifest.permission.READ_SMS) == PackageManager.PERMISSION_DENIED) {
			ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_SMS}, PERMISSION_REQUEST_CODE);
		} else {
			showSmsOTP();
		}
	}

	@Override
	public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
		if (PERMISSION_REQUEST_CODE == requestCode &&
			grantResults.length == 1 &&
			grantResults[0] == PackageManager.PERMISSION_GRANTED &&
			Manifest.permission.READ_SMS.equals(permissions[0])) {
			showSmsOTP();
		}
	}

	private void showSmsOTP() {
		final Message message = BankManager.getLastMessage(getApplicationContext());
		if (message == null) {
			//if there is no processed code yet, check the SMS inbox for archive.
			progressDialog = ProgressDialog.show(this, getString(R.string.loading),
				getString(R.string.msgCheckForMessages));

			final SMSCheckerTask task = new SMSCheckerTask(this);
			task.setOnFinishListener(new OnFinishListener() {

				@Override
				public void onFinished(final Message last) {
					try {
						progressDialog.dismiss();
					} catch (final Exception e) {
						Log.e(TAG, "Failed to close dialog.", e);
					}
					if (last != null) {
						openSMSOTPDisplay(last);
					} else {
						showDialog(DIALOG_NOCODE);
					}
				}
			});
			task.execute((Void) null);
		} else {
			openSMSOTPDisplay(message);
		}
	}

	private void openSMSOTPDisplay(final Message message) {
		final Intent intent = new Intent(getBaseContext(), SMSOTPDisplay.class);
		intent.setAction(ACTION_REDISPLAY);
		intent.putExtra(BANKDROID_SMSKEY_MESSAGE, message);
		startActivity(intent);
	}

	public void onManageBank(final View v) {
		final Intent bankListIntent = new Intent(getBaseContext(), BankListActivity.class);
		startActivity(bankListIntent);
	}

	@Override
	protected Dialog onCreateDialog(final int id) {
		final Dialog dialog;
		switch (id) {
			case DIALOG_NOCODE:
				// do the work to define the pause Dialog
				final AlertDialog.Builder builder = new AlertDialog.Builder(this);
				builder.setMessage(R.string.noMessageYet).setCancelable(false)
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
}
