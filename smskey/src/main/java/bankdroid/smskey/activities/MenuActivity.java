package bankdroid.smskey.activities;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import bankdroid.smskey.Codes;
import bankdroid.smskey.Preferences;
import bankdroid.smskey.R;
import bankdroid.smskey.activities.AboutActivity;

public class MenuActivity extends Activity implements Codes {

	@Override
	public boolean onOptionsItemSelected(final MenuItem item) {
		if (item.getItemId() == R.id.menuAbout) {
			onAbout(null);
			return true;
		}

		if (item.getItemId() == R.id.menuPreferences) {
			onPreferences(null);
			return true;
		}

		if (item.getItemId() == R.id.menuUserGuide) {
			onOnlineHelp(null);
			return true;
		}
		return super.onOptionsItemSelected(item);
	}

	@Override
	public boolean onCreateOptionsMenu(final Menu menu) {
		final MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.normalmenu, menu);
		return true;
	}

	public void onProductInfo(final View v) {
		final Intent viewIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(URL_HOME_PAGE));
		startActivity(viewIntent);
	}

	public void onOnlineHelp(final View v) {
		final Intent viewIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(URL_HELP));
		startActivity(viewIntent);
	}

	public void onPreferences(final View v) {
		final Intent settingsActivity = new Intent(getBaseContext(), Preferences.class);
		startActivity(settingsActivity);
	}

	public void onAbout(final View v) {
		final Intent aboutIntent = new Intent(getBaseContext(), AboutActivity.class);
		startActivity(aboutIntent);
	}

	public void onMenu(final View v) {
		openOptionsMenu();
	}

}
