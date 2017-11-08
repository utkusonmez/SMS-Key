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
import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.Click;
import org.androidannotations.annotations.EActivity;
import org.androidannotations.annotations.ViewById;

import java.util.List;

@EActivity(R.layout.main)
public class Main extends MenuActivity implements Codes {
	private final static int DIALOG_NOCODE = 567;
	private static final int PERMISSION_REQUEST_CODE = 123;
	private static final int IGNORED_REQUEST_CODE = 0;

	@ViewById(R.id.bankWarning)
	View bankWarning;

	private ProgressDialog progressDialog;

	@Override
	protected void onCreate(final Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		Eula.show(this);

		if (ContextCompat.checkSelfPermission(getBaseContext(), Manifest.permission.READ_SMS) == PackageManager.PERMISSION_DENIED) {
			ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_SMS, Manifest.permission.RECEIVE_SMS}, IGNORED_REQUEST_CODE);
		}

		printSmsReceiversInfoIfDebug();
	}

	private void printSmsReceiversInfoIfDebug() {
		if (BuildConfig.DEBUG) {
			Intent smsRecvIntent = new Intent("android.provider.Telephony.SMS_RECEIVED");
			List<ResolveInfo> infos = getPackageManager().queryBroadcastReceivers(smsRecvIntent, 0);
			for (ResolveInfo info : infos) {
				Log.d(TAG, "Receiver: " + info.activityInfo.name + ", priority=" + info.priority);
			}
		}
	}

	@AfterViews
	void showWarningIfNeeded() {
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
		bankWarning.setVisibility(numberOfBanks > 0 ? View.GONE : View.VISIBLE);
	}

	@Click(R.id.onSubmitSampleRow)
	void onSubmitSample() {
		SMSListActivity_.intent(getBaseContext()).start();
	}

	@Click(R.id.viewLastCodeRow)
	void onViewLastCode() {
		if (ContextCompat.checkSelfPermission(getBaseContext(), Manifest.permission.READ_SMS) == PackageManager.PERMISSION_DENIED) {
			ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_SMS, Manifest.permission.RECEIVE_SMS}, PERMISSION_REQUEST_CODE);
		} else {
			showSmsOTP();
		}
	}

	@Click(R.id.onManageBankRow)
	void onManageBank() {
		BankListActivity_.intent(getBaseContext()).start();
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
		SMSOTPDisplay_.intent(getBaseContext()).action(ACTION_REDISPLAY).extra(BANKDROID_SMSKEY_MESSAGE, message).start();
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
