package bankdroid.smskey;

import android.net.Uri;

public interface Codes {
	String SUBMISSION_ADDRESS = "smskey@vmails.pl";

	String BANKDROID_SMSKEY_PLAYSOUND = "bankdroid.smskey.PlaySound";
	String BANKDROID_SMSKEY_MESSAGE = "bankdroid.smskey.SMSMessage";
	String BANKDROID_SMSKEY_BANK = "bankdroid.smskey.Bank";

	String INTENT_GITHUB_SEND_MESSAGE = "bankdroid.smskey.githubsend.message";
	String INTENT_GITHUB_SEND_ADDRESS = "bankdroid.smskey.githubsend.address";

	String PREF = "bankdroid.smskey";
	String PREF_NOTIFICATION = "bankdroid.smskey.Notification";
	String PREF_KEEP_SCREEN_ON = "bankdroid.smskey.KeepScreenOn";
	String PREF_RESET_DB = "bankdroid.smskey.ResetDb";
	String PREF_UNLOCK_SCREEN = "bankdroid.smskey.UnlockScreen";
	String PREF_AUTO_COPY = "bankdroid.smskey.AutoCopy";
	String PREF_SHAKE_TO_COPY = "bankdroid.smskey.ShakeToCopy";
	String PREF_CODE_COUNT = "bankdroid.smskey.CodeCount";
	String PREF_PLAY_SOUND = "bankdroid.smskey.PlaySound";
	String PREF_NOTIFICATION_RINGTONE = "bankdroid.smskey.NotificationRingtone";
	String PREF_INSTALL_LOG = "bankdroid.smskey.InstallLog";
	String PREF_SPLIT_CODE = "bankdroid.smskey.SplitCode";

	boolean DEFAULT_NOTIFICATION = false;
	boolean DEFAULT_KEEP_SCREEN_ON = true;
	boolean DEFAULT_UNLOCK_SCREEN = true;
	boolean DEFAULT_AUTO_COPY = true;
	boolean DEFAULT_PLAY_SOUND = true;
	String DEFAULT_SPLIT_CODE = "0";

	String TAG = "SKNG";

	int NOTIFICATION_ID = 7632;

	String PROVIDER_AUTHORITY = "bankdroid.smskey.Bank";

	String ACTION_DISPLAY = "bankdroid.smskey.action.Display";
	String ACTION_REDISPLAY = "bankdroid.smskey.action.Redisplay";

	//activity results
	int REQUEST_EMAIL_SEND = 1001;
	int REQUEST_SELECT_SMS = 1002;

	/**
	 * The MIME type of {@link #CONTENT_URI} providing a directory of banks.
	 */
	String CONTENT_TYPE = "vnd.android.cursor.dir/bankdroid.smskey.bank";

	/**
	 * The MIME type of a {@link #CONTENT_URI} sub-directory of a single bank.
	 */
	String CONTENT_ITEM_TYPE = "vnd.android.cursor.item/bankdroid.smskey.bank";

	/**
	 * The content:// style URL for this table.
	 */
	Uri CONTENT_URI = Uri.parse("content://bankdroid.smskey.Bank/banks");

}
