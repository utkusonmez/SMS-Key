package bankdroid.util;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.preference.PreferenceManager;

public class AppId {
	public static final String PREF_APPID = "bankdroid.pref.AppId";

	private static String cachedId = null;

	public static String getAppId(final Context context) {
		if (cachedId == null) {
			final SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
			if (!preferences.contains(PREF_APPID)) {
				final Editor editor = preferences.edit();
				editor.putString(PREF_APPID, generateId(context));
				editor.commit();
			}
			cachedId = preferences.getString(PREF_APPID, null);
		}
		return cachedId;
	}

	private static String generateId(final Context context) {
		final long millis = System.currentTimeMillis();
		final String name = context.getPackageName();
		return name + "_" + millis;
	}
}
