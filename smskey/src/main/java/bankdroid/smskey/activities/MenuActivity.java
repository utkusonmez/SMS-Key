package bankdroid.smskey.activities;

import android.app.Activity;
import android.content.Intent;
import android.view.View;
import bankdroid.smskey.Codes;
import bankdroid.smskey.R;
import org.androidannotations.annotations.EActivity;
import org.androidannotations.annotations.OptionsItem;
import org.androidannotations.annotations.OptionsMenu;

@EActivity
@OptionsMenu(R.menu.normalmenu)
public abstract class MenuActivity extends Activity implements Codes {

	@OptionsItem(R.id.menuPreferences)
	void onPreferences() {
		final Intent settingsActivity = new Intent(getBaseContext(), Preferences.class);
		startActivity(settingsActivity);
	}

	@OptionsItem(R.id.menuAbout)
	void onAbout() {
		AboutActivity_.intent(getBaseContext()).start();
	}

	public void onMenu(final View v) {
		openOptionsMenu();
	}

}
