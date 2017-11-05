package bankdroid.smskey;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.media.AudioManager;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.provider.Settings;
import android.support.v4.app.NotificationCompat;
import android.telephony.SmsMessage;
import android.text.ClipboardManager;
import bankdroid.smskey.activities.SMSOTPDisplay_;
import org.androidannotations.annotations.EReceiver;
import org.androidannotations.annotations.ReceiverAction;

import java.text.MessageFormat;
import java.util.Calendar;

@EReceiver
public class SMSReceiver extends BroadcastReceiver implements Codes {
	private static final String ACTION = "android.provider.Telephony.SMS_RECEIVED";

	@ReceiverAction(actions = ACTION)
	void smsReceived(final Context context, final Intent intent) {
		final Bundle bundle = intent.getExtras();
		if (bundle != null) {
			//retrieve the SMS message received
			final Object[] pdus = (Object[]) bundle.get("pdus");

			final SmsMessage sms = SmsMessage.createFromPdu((byte[]) pdus[0]);

			final String originatingAddress = sms.getOriginatingAddress();
			final String message = sms.getMessageBody();

			final Message code = BankManager.getCode(context, originatingAddress, message, Calendar.getInstance()
				.getTime(), true);

			if (code != null) {
				processCode(context, code);
			}
		}
	}

	@Override
	public void onReceive(final Context context, final Intent intent) {
		// empty, will be overridden in generated subclass
	}

	private void processCode(final Context context, final Message message) {
		// Restore preferences
		final SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(context);
		final boolean notificationOnly = settings.getBoolean(PREF_NOTIFICATION, DEFAULT_NOTIFICATION);
		final boolean autoCopy = settings.getBoolean(PREF_AUTO_COPY, DEFAULT_AUTO_COPY);
		final boolean playSound = settings.getBoolean(PREF_PLAY_SOUND, DEFAULT_PLAY_SOUND);
		final int codeCount = settings.getInt(PREF_CODE_COUNT, 0) + 1;
		final Editor edit = settings.edit();
		edit.putInt(PREF_CODE_COUNT, codeCount);
		edit.commit();

		if (autoCopy && message.getCode() != null) {
			((ClipboardManager) context.getSystemService(Context.CLIPBOARD_SERVICE)).setText(message.getCode());
		}

		if (notificationOnly) {
			//display notification
			final NotificationManager nm = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

			//create notification
			final int icon = android.R.drawable.stat_sys_warning;
			final CharSequence tickerText = MessageFormat.format(context.getText(R.string.notificationTicker)
				.toString(), message.getBank().getName());
			final long when = System.currentTimeMillis();

			//set extended message
			final CharSequence contentTitle = context.getText(R.string.notificationTitle);
			final CharSequence contentText = MessageFormat.format(
				context.getText(R.string.notificationText).toString(), message.getBank().getName());


			final Intent notificationIntent = SMSOTPDisplay_.intent(context)
				.action(ACTION_DISPLAY)
				.flags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK)
				.extra(BANKDROID_SMSKEY_MESSAGE, message).get();

			final PendingIntent contentIntent = PendingIntent.getActivity(context, 0, notificationIntent,
				PendingIntent.FLAG_UPDATE_CURRENT);

			NotificationCompat.Builder notificationBuilder =
				new NotificationCompat.Builder(context, "SMS-Key")
				.setSmallIcon(icon)
				.setBadgeIconType(icon)
				.setTicker(tickerText)
				.setWhen(when)
				.setContentTitle(contentTitle)
				.setContentText(contentText)
				.setContentIntent(contentIntent);

			if (playSound) {
				final String ringtoneURI = settings.getString(PREF_NOTIFICATION_RINGTONE,
					Settings.System.DEFAULT_NOTIFICATION_URI.toString());

				if (ringtoneURI != null && !"".equals(ringtoneURI)) {
					notificationBuilder.setSound(Uri.parse(ringtoneURI), AudioManager.STREAM_NOTIFICATION);
				}
			}

			//display notification
			nm.notify(NOTIFICATION_ID, notificationBuilder.build());
		} else {
			//start display activity directly.
			SMSOTPDisplay_.intent(context)
				.action(ACTION_DISPLAY)
				.flags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK)
				.extra(BANKDROID_SMSKEY_MESSAGE, message)
				.extra(BANKDROID_SMSKEY_PLAYSOUND, playSound).start();
		}
	}
}
