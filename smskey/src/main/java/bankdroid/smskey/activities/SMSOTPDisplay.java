package bankdroid.smskey.activities;

import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.Point;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.media.AudioAttributes;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.text.ClipboardManager;
import android.util.Log;
import android.view.Display;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.ScrollView;
import android.widget.TextView;
import bankdroid.smskey.BankManager;
import bankdroid.smskey.Codes;
import bankdroid.smskey.CountDown;
import bankdroid.smskey.CountDown.CountDownListener;
import bankdroid.smskey.Eula;
import bankdroid.smskey.Formatters;
import bankdroid.smskey.Message;
import bankdroid.smskey.R;
import bankdroid.smskey.SMSReceiver;
import bankdroid.util.CodeUtils;
import bankdroid.util.QRUtils;
import com.google.zxing.WriterException;
import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.Bean;
import org.androidannotations.annotations.Click;
import org.androidannotations.annotations.EActivity;
import org.androidannotations.annotations.Extra;
import org.androidannotations.annotations.OptionsItem;
import org.androidannotations.annotations.OptionsMenu;
import org.androidannotations.annotations.ViewById;
import org.androidannotations.annotations.res.ColorRes;

import java.util.Calendar;
import java.util.List;

import static java.lang.String.format;



/**
 * This view as able to display SMS one time passwords processed by {@link SMSReceiver}. Besides displayed the codes
 * it provides several conveniences services:
 * <ul>
 * <li>Display code in large letters for the better readability</li>
 * <li>Display a copy button to copy the code into the clipboard. In this way it is easy to
 * copy and paste it into the appropriate field in the Browser</li>
 * <li>create menu: clear, preferences, bank list</li>
 * <li>handle preferences</li>
 * <li>improved design</li>
 * <li>use notifications instead of direct pop-up based on user preferences</li>
 * <li>clear SMS based on preferences (that may be problematic. There is no good tip for it on forums.</li>
 * <li>display a different activity on start up, from where various actions can be started.</li>
 * <li>displays list of banks and their settings</li>
 * <li>let the user to register new banks, store settings in DB</li>
 * <li>displays a count-down to indicate when the OTP will expire</li>
 * <li>German translations</li>
 * <li>displays transaction signing security warning</li>
 * <li>split code into group of numbers based on user preference</li>
 * </ul>
 */
@EActivity(R.layout.sod)
@OptionsMenu(R.menu.sodmenu)
public class SMSOTPDisplay extends MenuActivity implements Codes, CountDownListener, SensorEventListener {
	private static final int FORCE_THRESHOLD = 900;

	// @formatter:off
	@ViewById(R.id.codeButton) TextView codeButton;
	@ViewById(R.id.receivedAt) TextView receivedAt;
	@ViewById(R.id.messageBody) TextView messageBody;
	@ViewById(R.id.originatingAddress) TextView originatingAddress;
	@ViewById(R.id.countDown) TextView countDownView;
	@ViewById(R.id.securityWarning) TextView securityWarning;
	@ViewById(R.id.qrCode) ImageView qrCode;
	@ViewById(R.id.mainPanel) ScrollView mainPanel;
	@Bean CodeUtils codeUtils;
	@Bean QRUtils qrUtils;
	@ColorRes(R.color.backgroundEnd) int backgroundColor;
	@Extra(BANKDROID_SMSKEY_MESSAGE) Message intentMessage;
	//@formatter:on

	private Message message;
	private CountDown countDown;
	private SensorManager sensorManager;
	private Sensor sensor;
	private long lastUpdate = -1;
	private float lastX, lastY, lastZ;
	private SharedPreferences settings;
	private MediaPlayer mediaPlayer;

	@AfterViews
	void init() {
		Eula.show(this);

		settings = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
		final boolean unlockScreen = settings.getBoolean(PREF_UNLOCK_SCREEN, DEFAULT_UNLOCK_SCREEN);
		if (unlockScreen && Build.VERSION.SDK_INT >= 5) {
			getWindow().addFlags(WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED);
			getWindow().addFlags(WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON);
		}

		final boolean shakeToCopy = settings.getBoolean(PREF_SHAKE_TO_COPY, true);
		if (shakeToCopy) {
			this.sensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
			final List<Sensor> sensors = sensorManager.getSensorList(Sensor.TYPE_ACCELEROMETER);
			if (sensors.size() > 0) {
				sensor = sensors.get(0);
			}
		}
	}

	@Override
	protected void onResume() {
		super.onResume();

		if (sensor != null) {
			sensorManager.registerListener(this, sensor, SensorManager.SENSOR_DELAY_GAME);
		}

		//clear notification if there is any
		final NotificationManager nm = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);

		nm.cancel(NOTIFICATION_ID);

		if (!processIntent()) {
			if (message != null) {
				Log.d(TAG, "Restore old values");
				setValues(message);
			} else {
				Log.d(TAG, "Clear fields as there is no intent and no previously set values.");
				setValues(null);
			}
		}

		final boolean keepScreenOn = settings.getBoolean(PREF_KEEP_SCREEN_ON, DEFAULT_KEEP_SCREEN_ON);
		codeButton.setKeepScreenOn(keepScreenOn);
	}

	private void playSound() {
		final String ringtoneURI = settings.getString(PREF_NOTIFICATION_RINGTONE,
			RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION).toString());

		if (ringtoneURI != null && !"".equals(ringtoneURI)) {
			try {
				final AudioManager audioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
				if (audioManager.getStreamVolume(AudioManager.STREAM_NOTIFICATION) != 0) {
					if (mediaPlayer == null) {
						mediaPlayer = new MediaPlayer();
						mediaPlayer.setDataSource(this, Uri.parse(ringtoneURI));
						AudioAttributes audioAttributes = new AudioAttributes.Builder()
							.setLegacyStreamType(AudioManager.STREAM_NOTIFICATION).build();
						mediaPlayer.setAudioAttributes(audioAttributes);
						mediaPlayer.setLooping(false);
						mediaPlayer.prepare();
					}

					mediaPlayer.start();
				}
			} catch (final Exception e) {
				Log.e(TAG, "Failed to play notification sound.", e);
			}
		} else {
			Log.e(TAG, "Notification sound URI is not available.");
		}
	}

	private boolean processIntent() {
		//process intent
		final Intent intent = getIntent();

		//check timestamp availability to make sure, that intent is not null, and correct intent is received.
		if (intentMessage != null) {
			Log.d(TAG, "Set values based on new SMS intent.");

			//to avoid duplicate noise
			final boolean newMessage = this.message == null || !this.message.equals(intentMessage);
			setValues(intentMessage);

			if (ACTION_DISPLAY.equals(intent.getAction()))
				BankManager.updateLastMessage(getApplicationContext(), intentMessage);

			if (newMessage && settings.getBoolean(PREF_PLAY_SOUND, DEFAULT_PLAY_SOUND)
				&& intent.getBooleanExtra(BANKDROID_SMSKEY_PLAYSOUND, false)) {
				playSound();
			}

			return true;
		}

		return false;
	}

	@Override
	protected void onPause() {
		super.onPause();

		if (sensor != null) {
			sensorManager.unregisterListener(this);
		}

		if (countDown != null) {
			countDown.forceStop();
			countDown = null;
		}
	}

	@Override
	protected void onSaveInstanceState(final Bundle outState) {
		super.onSaveInstanceState(outState);

		if (message != null) {
			Log.d(TAG, format("Values going to be saved for code: %s (%s)", message.getCode(), message.getBank().getName()));
			outState.putSerializable(BANKDROID_SMSKEY_MESSAGE, message);
		}
	}

	@Override
	protected void onRestoreInstanceState(final Bundle savedInstanceState) {
		super.onRestoreInstanceState(savedInstanceState);

		if (savedInstanceState.containsKey(BANKDROID_SMSKEY_MESSAGE)) {
			message = (Message) savedInstanceState.getSerializable(BANKDROID_SMSKEY_MESSAGE);
			Log.d(TAG, format("Values restored for code: %s (%s)", message.getCode(), message.getBank().getName()));
		}
	}

	private void setValues(final Message message) {
		this.message = message;

		if (message != null) {
			Log.i(TAG, format("One time password to display from Bank = %s", message.getBank().getName()));
			final CharSequence timestampText = Formatters.getTimstampFormat().format(message.getTimestamp());

			//format code
			String code = message.getCode();
			final int splitSize = Integer.parseInt(settings.getString(PREF_SPLIT_CODE, DEFAULT_SPLIT_CODE));
			code = codeUtils.splitCode(code, splitSize);

			codeButton.setText(code);
			receivedAt.setText(getResources().getText(R.string.received_prefix).toString() + " " + timestampText);
			messageBody.setText(message.getMessage());

			//TODO try to read bank name from contact list
			originatingAddress.setText(message.getOriginatingAddress());

			updateQRCode(code);

			securityWarning.setVisibility(message.getBank().isTransactionSign(message.getMessage()) ? View.VISIBLE : View.GONE);

			if (message.getBank().getExpiry() > 0) {
				countDownView.setVisibility(View.VISIBLE);

				//calculate correct validity period from receivedAt and expiry
				final long ellapsedTime = (Calendar.getInstance().getTimeInMillis() - message.getTimestamp().getTime()) / 1000; //convert to seconds
				final int remainingTime = (int) Math.max(0, message.getBank().getExpiry() - ellapsedTime);
				countDownView.setText(getResources().getText(R.string.countdown_prefix).toString() + " "
					+ convertTime(remainingTime));

				if (remainingTime > 0) {
					countDown = new CountDown(this, remainingTime);
					countDown.start();
				}
			} else {
				countDownView.setVisibility(View.GONE);
			}
		} else { //set empty message
			codeButton.setText(getResources().getText(R.string.nocode));
			messageBody.setText("");
			originatingAddress.setText("");
			receivedAt.setVisibility(View.GONE);
			countDownView.setVisibility(View.GONE);
			securityWarning.setVisibility(View.GONE);
		}
	}

	private void updateQRCode(String code) {
		Display display = getWindowManager().getDefaultDisplay();
		Point size = new Point();
		display.getSize(size);
		int width = size.x;
		int height = size.y;
		int smaller = (int) (Math.min(width, height) * 0.60);
		try {
			Bitmap bitmap = qrUtils.encodeAsBitmap(code, smaller, smaller, backgroundColor);
			qrCode.setImageBitmap(bitmap);
		} catch (WriterException e) {
			e.printStackTrace();
		}
	}

	private CharSequence convertTime(final int expiry) {
		final int hours = expiry / 3600;
		final int minutes = (expiry % 3600) / 60;
		final int secs = expiry % 60;

		final StringBuilder builder = new StringBuilder();
		appendDigits(builder, hours).append(':');
		appendDigits(builder, minutes).append(':');
		appendDigits(builder, secs);

		return builder.toString();
	}

	private StringBuilder appendDigits(final StringBuilder builder, final int digits) {
		if (digits < 10)
			builder.append('0');
		if (digits == 0)
			builder.append('0');
		else
			builder.append(String.valueOf(digits));
		return builder;
	}

	@Override
	protected void onNewIntent(final Intent intent) {
		super.onNewIntent(intent);
		setIntent(intent);
		processIntent();
	}

	@Click(R.id.codePanel)
	public void onCopyAndClose(final View v) {
		if (message != null) {
			((ClipboardManager) getSystemService(CLIPBOARD_SERVICE)).setText(message.getCode());
		}
		finish();
	}

	@OptionsItem(R.id.menuBanks)
	void menuBanks() {
		BankListActivity_.intent(this).start();
	}

	@OptionsItem(R.id.menuClear)
	void menuClear() {
		Log.d(TAG, "Clear menu selected.");
		setValues(null);
	}

	@Override
	public void stop() {
		// do nothing
	}

	@Override
	public void tick(final int remainingSec) {
		final TextView countDown = countDownView;
		countDown.setText(getResources().getText(R.string.countdown_prefix).toString() + " "
			+ convertTime(remainingSec));
	}

	@Override
	public void onAccuracyChanged(final Sensor s, final int value) {
		//do nothing
	}

	@Override
	public void onSensorChanged(final SensorEvent event) {
		if (event.sensor.getType() != Sensor.TYPE_ACCELEROMETER || event.values.length < 3)
			return;

		final long currentTime = System.currentTimeMillis();

		if (lastUpdate < 0) {
			lastUpdate = currentTime;
			lastX = event.values[SensorManager.DATA_X];
			lastY = event.values[SensorManager.DATA_Y];
			lastY = event.values[SensorManager.DATA_Z];
		} else if ((currentTime - lastUpdate) > 100) {
			final long diffTime = (currentTime - lastUpdate);
			lastUpdate = currentTime;

			final float x = event.values[SensorManager.DATA_X];
			final float y = event.values[SensorManager.DATA_Y];
			final float z = event.values[SensorManager.DATA_Z];

			final float currentForce = Math.abs(x + y + z - lastX - lastY - lastZ) / diffTime * 10000;

			if (currentForce > FORCE_THRESHOLD) {
				//device has been shaken
				onCopyAndClose(null);
			}

			lastX = x;
			lastY = y;
			lastZ = z;
		}
	}
}
