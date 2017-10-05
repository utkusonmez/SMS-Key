package bankdroid.smskey;

import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.util.Log;

import java.util.Date;

import static bankdroid.smskey.Codes.TAG;

public class SMSCheckerTask extends AsyncTask<Void, Integer, Message> {
	public static final Uri SMS_CONTENT_URI = Uri.parse("content://sms");
	public static final Uri SMS_INBOX_CONTENT_URI = Uri.withAppendedPath(SMS_CONTENT_URI, "inbox");

	private final Context context;
	private OnFinishListener listener;

	public SMSCheckerTask(final Context context) {
		this.context = context;
	}

	@Override
	protected Message doInBackground(final Void... params) {
		Message last = null;
		final String[] projection = new String[]{"_id", "thread_id", "address", "date", "body"};
		int count = 0;

		final Cursor cursor = context.getContentResolver().query(SMS_INBOX_CONTENT_URI, projection, null, null,
			"date ASC");

		if (cursor != null) {
			try {
				count = cursor.getCount();
				if (count > 0) {
					cursor.moveToFirst();

					do {
						final String address = cursor.getString(2);
						final Date timestamp = new Date(cursor.getLong(3));
						final String body = cursor.getString(4);

						final Message code = BankManager.getCode(context, address, body, timestamp, false);

						if (code != null) {
							//save code to inbox
							BankManager.updateLastMessage(context, code);

							last = code;
						}
					}
					while (cursor.moveToNext());
				}
			} catch (final Exception e) {
				Log.e(TAG, "Failed to process inbox content.", e);
			} finally {
				cursor.close();
			}
		}
		return last;
	}

	@Override
	protected void onPostExecute(final Message result) {
		super.onPostExecute(result);
		if (listener != null)
			listener.onFinished(result);
	}

	public void setOnFinishListener(final OnFinishListener listener) {
		this.listener = listener;
	}

	public interface OnFinishListener {
		void onFinished(Message last);
	}
}
