package bankdroid.smskey;

import android.net.Uri;

public interface Codes
{
	public static final String DEFAULT_COUNTRY = "HU";

	public static final String FACEBOOK_URL = "http://goo.gl/1TZ9x";
	public static final String TWITTER_URL = "http://twitter.com/bankdroid";

	public static final String GMAIL_URL = "mailto:info@bankdroid.info";
	public static final String SUBMISSION_ADDRESS = "sample@bankdroid.info";

	public static final String URL_PROJECT_HOME = "http://goo.gl/9oiKb";
	public static final String URL_HELP = "http://goo.gl/Kpxbk";
	public static final String URL_HOME_PAGE = "http://goo.gl/gIVFJ";
	public static final String URL_BANK_LIST = "http://goo.gl/ukYYk";
	public static final String URL_MARKET_DETAILS = "http://goo.gl/3skg0";
	public static final String URL_PAYPAL = "https://www.paypal.com/cgi-bin/webscr?cmd=_s-xclick&hosted_button_id=6LYSJ5ATUZRFJ";

	public static final String BANKDROID_SMSKEY_PLAYSOUND = "bankdroid.smskey.PlaySound";
	public static final String BANKDROID_SMSKEY_MESSAGE = "bankdroid.smskey.SMSMessage";
	public static final String BANKDROID_SMSKEY_BANK = "bankdroid.smskey.Bank";

	public static final String PREF = "bankdroid.smskey";
	public static final String PREF_NOTIFICATION = "bankdroid.smskey.Notification";
	public static final String PREF_KEEP_SMS = "bankdroid.smskey.KeepSMS";
	public static final String PREF_KEEP_SCREEN_ON = "bankdroid.smskey.KeepScreenOn";
	public static final String PREF_RESET_DB = "bankdroid.smskey.ResetDb";
	public static final String PREF_RESET_CAMPAIGN = "bankdroid.smskey.ResetCampaign";
	public static final String PREF_UNLOCK_SCREEN = "bankdroid.smskey.UnlockScreen";
	public static final String PREF_AUTO_COPY = "bankdroid.smskey.AutoCopy";
	public static final String PREF_SHAKE_TO_COPY = "bankdroid.smskey.ShakeToCopy";
	public static final String PREF_CODE_COUNT = "bankdroid.smskey.CodeCount";
	public static final String PREF_PLAY_SOUND = "bankdroid.smskey.PlaySound";
	public static final String PREF_NOTIFICATION_RINGTONE = "bankdroid.smskey.NotificationRingtone";
	public static final String PREF_INSTALL_LOG = "bankdroid.smskey.InstallLog";
	public static final String PREF_SPLIT_CODE = "bankdroid.smskey.SplitCode";

	public static final boolean DEFAULT_NOTIFICATION = false;
	public static final boolean DEFAULT_KEEP_SMS = false;
	public static final boolean DEFAULT_KEEP_SCREEN_ON = true;
	public static final boolean DEFAULT_UNLOCK_SCREEN = true;
	public static final boolean DEFAULT_AUTO_COPY = true;
	public static final boolean DEFAULT_PLAY_SOUND = true;
	public static final String DEFAULT_SPLIT_CODE = "0";

	public static final String TAG = "SKNG";

	public static final int NOTIFICATION_ID = 7632;

	public static final String PROVIDER_AUTHORITY = "bankdroid.smskey.Bank";

	public static final String ACTION_DISPLAY = "bankdroid.smskey.action.Display";
	public static final String ACTION_REDISPLAY = "bankdroid.smskey.action.Redisplay";

	//activity results
	public static final int REQUEST_EMAIL_SEND = 1001;
	public static final int REQUEST_SELECT_SMS = 1002;

	/**
	 * The MIME type of {@link #CONTENT_URI} providing a directory of banks.
	 */
	public static final String CONTENT_TYPE = "vnd.android.cursor.dir/bankdroid.smskey.bank";

	/**
	 * The MIME type of a {@link #CONTENT_URI} sub-directory of a single bank.
	 */
	public static final String CONTENT_ITEM_TYPE = "vnd.android.cursor.item/bankdroid.smskey.bank";

	/**
	 * The content:// style URL for this table.
	 */
	public static final Uri CONTENT_URI = Uri.parse("content://bankdroid.smskey.Bank/banks");

}
