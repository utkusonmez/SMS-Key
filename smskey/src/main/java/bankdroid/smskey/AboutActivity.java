package bankdroid.smskey;

import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.net.Uri;
import android.os.Bundle;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.method.LinkMovementMethod;
import android.text.method.MovementMethod;
import android.text.style.ClickableSpan;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

/**
 * TODO new preference: use only local banks
 * TODO video
 * <p>
 * XXX forward password to e-mail server, when code received - security risk
 *
 * @author Gabe
 */
public class AboutActivity extends MenuActivity {

	private static final String BANKDROID_TEXT = "BankDroid";

	@Override
	protected void onCreate(final Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.about);

		//Set links in description0
		final TextView desc0 = (TextView) findViewById(R.id.description);
		final String text0 = desc0.getText().toString();
		final SpannableString f0 = new SpannableString(text0);

		final int x0 = text0.indexOf(BANKDROID_TEXT);
		final int y0 = x0 + BANKDROID_TEXT.length();

		f0.setSpan(new ClickSpan(new SpanClickListener() {
			@Override
			public void onSpanClicked(final View source, final int spanId) {
				startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(URL_PROJECT_HOME)));
			}
		}, 1), x0, y0, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);

		desc0.setText(f0);

		final MovementMethod m0 = desc0.getMovementMethod();
		if ((m0 == null) || !(m0 instanceof LinkMovementMethod)) {
			if (desc0.getLinksClickable()) {
				desc0.setMovementMethod(LinkMovementMethod.getInstance());
			}
		}
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

	public void onMail(final View v) {
		final Intent viewIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(GMAIL_URL));
		startActivity(viewIntent);
	}

	public void onTwitter(final View v) {
		final Intent viewIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(TWITTER_URL));
		startActivity(viewIntent);
	}

	public void onFacebook(final View v) {
		final Intent viewIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(FACEBOOK_URL));
		startActivity(viewIntent);
	}

	public void onDonate(final View v) {
		final Intent viewIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(Codes.URL_PAYPAL));
		startActivity(viewIntent);
	}

	static interface SpanClickListener {
		void onSpanClicked(View source, int spanId);
	}

	static class ClickSpan extends ClickableSpan {
		SpanClickListener listener;
		int spanId;

		public ClickSpan(final SpanClickListener listener, final int spanId) {
			this.listener = listener;
			this.spanId = spanId;
		}

		@Override
		public void onClick(final View widget) {
			listener.onSpanClicked(widget, spanId);
		}
	}

}
