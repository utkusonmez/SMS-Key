package bankdroid.smskey;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.preference.CheckBoxPreference;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceChangeListener;
import android.preference.Preference.OnPreferenceClickListener;
import android.preference.PreferenceActivity;
import android.preference.PreferenceManager;
import android.util.Log;
import android.widget.Toast;
import bankdroid.campaign.CampaignManager;
import bankdroid.smskey.bank.Bank;
import bankdroid.util.ErrorLogger;

import java.util.HashSet;

/**
 * @author Gabe
 */
public class Preferences extends PreferenceActivity implements Codes, OnPreferenceChangeListener,
		OnPreferenceClickListener
{
	private final static int DISPLAY_TOAST = 354;
	private final static int DIALOG_RESETDB = 355;
	private final static int DIALOG_RESETCAMPAIGN = 356;

	@Override
	protected void onCreate( final Bundle savedInstanceState )
	{
		super.onCreate(savedInstanceState);
		addPreferencesFromResource(R.layout.preferences);

		findPreference(PREF_RESET_DB).setOnPreferenceChangeListener(this);
		findPreference(PREF_RESET_CAMPAIGN).setOnPreferenceChangeListener(this);

		final SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
		if ( Build.VERSION.SDK_INT < 5 )
		{
			final Preference unlockScreen = findPreference(PREF_UNLOCK_SCREEN);
			unlockScreen.setEnabled(false);
			unlockScreen.setSelectable(false);
			unlockScreen.setDefaultValue(Boolean.FALSE);
			final Editor editor = preferences.edit();
			editor.putBoolean(PREF_UNLOCK_SCREEN, false);
			editor.commit();
		}

		final int count = preferences.getInt(PREF_CODE_COUNT, 0);
		final Preference codeCount = findPreference(PREF_CODE_COUNT);
		codeCount.setSummary(String.valueOf(count));

		final String errorLog = preferences.getString(PREF_INSTALL_LOG, "");
		if ( errorLog.length() > 0 )
		{
			final Preference prefErrorLog = findPreference(PREF_INSTALL_LOG);
			prefErrorLog.setSummary(errorLog.substring(0, Math.min(30, errorLog.length())));
			prefErrorLog.setEnabled(true);
			prefErrorLog.setOnPreferenceClickListener(this);
		}
	}

	@Override
	public boolean onPreferenceChange( final Preference pref, final Object newValue )
	{
		if ( pref.getKey().equals(PREF_RESET_DB) && ( (Boolean) newValue ) )
		{
			showDialog(DIALOG_RESETDB);
		}
		else if ( pref.getKey().equals(PREF_RESET_CAMPAIGN) && ( (Boolean) newValue ) )
		{
			showDialog(DIALOG_RESETCAMPAIGN);
		}
		return true;
	}

	private void resetDb( final Preference pref )
	{
		final Handler handler = new Handler()
		{
			@Override
			public void handleMessage( final Message msg )
			{
				super.handleMessage(msg);
				if ( msg.what == DISPLAY_TOAST )
				{
					final Toast toast = Toast.makeText(getBaseContext(), R.string.msgDbReset, Toast.LENGTH_SHORT);
					toast.show();
					( (CheckBoxPreference) pref ).setChecked(false);
				}
			}
		};

		( new Thread(new Runnable()
		{

			@Override
			public void run()
			{
				BankProvider.resetDb(getBaseContext());
				handler.sendMessage(handler.obtainMessage(DISPLAY_TOAST));
			}
		}) ).start();
	}

	private void resetCampaign( final Preference pref )
	{
		CampaignManager.resetCampaign(this);

		Toast.makeText(this, R.string.msgCampaignReset, Toast.LENGTH_SHORT).show();
		( (CheckBoxPreference) pref ).setChecked(false);
	}

	@Override
	protected Dialog onCreateDialog( final int id )
	{
		final AlertDialog.Builder builder = new AlertDialog.Builder(this);
		if ( id == DIALOG_RESETDB )
		{
			builder.setMessage(getString(R.string.msgAreYouSure)).setCancelable(false)
					.setPositiveButton(getString(R.string.yes), new DialogInterface.OnClickListener()
					{
						@Override
						public void onClick( final DialogInterface dialog, final int id )
						{
							final Preference pref = findPreference(PREF_RESET_DB);
							resetDb(pref);
							dialog.dismiss();
						}
					}).setNegativeButton(getString(R.string.no), new DialogInterface.OnClickListener()
					{
						@Override
						public void onClick( final DialogInterface dialog, final int id )
						{
							final CheckBoxPreference pref = (CheckBoxPreference) findPreference(PREF_RESET_DB);
							pref.setChecked(false);
							dialog.cancel();
						}
					});
		}
		if ( id == DIALOG_RESETCAMPAIGN )
		{
			builder.setMessage(getString(R.string.msgAreYouSure)).setCancelable(false)
					.setPositiveButton(getString(R.string.yes), new DialogInterface.OnClickListener()
					{
						@Override
						public void onClick( final DialogInterface dialog, final int id )
						{
							final Preference pref = findPreference(PREF_RESET_CAMPAIGN);
							resetCampaign(pref);
							dialog.dismiss();
						}
					}).setNegativeButton(getString(R.string.no), new DialogInterface.OnClickListener()
					{
						@Override
						public void onClick( final DialogInterface dialog, final int id )
						{
							final CheckBoxPreference pref = (CheckBoxPreference) findPreference(PREF_RESET_CAMPAIGN);
							pref.setChecked(false);
							dialog.cancel();
						}
					});
		}
		final AlertDialog alert = builder.create();
		return alert;
	}

	@Override
	public boolean onPreferenceClick( final Preference pref )
	{
		if ( pref.getKey().equals(Codes.PREF_INSTALL_LOG) )
		{
			//construct e-mail body
			final StringBuilder builder = new StringBuilder();

			builder.append("Maintenance e-mail: ");
			builder.append(getString(R.string.app_name)).append(' ');
			//set version number
			try
			{
				final PackageManager manager = getPackageManager();
				final PackageInfo info = manager.getPackageInfo(getPackageName(), 0);
				final String versionName = info.versionName;
				builder.append('v').append(versionName);
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

			builder.append('\n').append(String.format(getString(R.string.statText), bankCount, countryCount));

			final SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
			final String installLog = preferences.getString(Codes.PREF_INSTALL_LOG, "");
			if ( installLog.length() > 1 )
				builder.append('\n').append(installLog);

			ErrorLogger.sendEmail(this, new String[] { SUBMISSION_ADDRESS }, "MAINTENANCE", builder.toString());//no I18N

			pref.setEnabled(false);
			pref.setSummary("-");
			final Editor edit = preferences.edit();
			edit.putString(Codes.PREF_INSTALL_LOG, "");
			edit.commit();
		}
		return false;
	}

}
