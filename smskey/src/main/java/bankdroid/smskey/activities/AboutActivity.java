package bankdroid.smskey.activities;

import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;
import bankdroid.smskey.R;

public class AboutActivity extends MenuActivity {

	@Override
	protected void onCreate(final Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.about);
		//set version number
		try {
			final PackageManager manager = getPackageManager();
			final PackageInfo info = manager.getPackageInfo(getPackageName(), 0);
			final String versionName = info.versionName;
			((TextView) findViewById(R.id.versionId)).setText(versionName);
		} catch (final NameNotFoundException e) {
			Log.e(TAG, "Error getting package name.", e);
		}
	}
}
