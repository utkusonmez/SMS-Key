package bankdroid.util;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.net.Uri;
import android.preference.PreferenceManager;
import android.util.Log;
import android.widget.Toast;
import bankdroid.smskey.Codes;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Date;

public class ErrorLogger implements Codes {

	public static void logError(final Context context, final Exception e, final String process) {
		Log.e(TAG, "Failed finish proces: " + process, e);

		try {
			String stackTrace = "";

			final StringWriter err = new StringWriter();
			final PrintWriter wr = new PrintWriter(err);
			wr.println(process + " PROBLEM OCCURED!");
			wr.println("Failed at: " + new Date());
			wr.println("OS version: " + android.os.Build.VERSION.SDK_INT + " / " + android.os.Build.VERSION.RELEASE);
			wr.println("Device: " + android.os.Build.MANUFACTURER + " / " + android.os.Build.MODEL);
			wr.println();
			e.printStackTrace(wr);
			stackTrace = err.toString();

			final SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
			final Editor editor = preferences.edit();
			editor.putString(Codes.PREF_INSTALL_LOG, stackTrace);
			editor.commit();
		} catch (final Exception e2) {
			Log.e(TAG, "Failed to log the error in process: " + process, e);
		}
	}

	public static void sendEmail(final Context context, final String[] address, final String subject, final String msg) {
		final Intent view = new Intent(Intent.ACTION_VIEW);
		final StringBuilder uri = new StringBuilder("mailto:");
		uri.append(address[0]);
		uri.append("?subject=").append(Uri.encode(subject));
		uri.append("&body=").append(Uri.encode(msg));
		Log.d(TAG, "URI: " + uri);
		view.setData(Uri.parse(uri.toString()));
		try {
			context.startActivity(view);
		} catch (final Exception e) {
			Log.e(TAG, "Failed to send message as no e-mail client is available.", e);
			Toast.makeText(context, e.getMessage(), Toast.LENGTH_LONG).show();
		}
	}

}
