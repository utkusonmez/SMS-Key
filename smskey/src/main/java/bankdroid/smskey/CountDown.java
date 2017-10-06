package bankdroid.smskey;

import android.os.Handler;
import android.os.Message;
import android.util.Log;

public class CountDown implements Runnable, Codes {

	private static final int TICK = 1111;
	private static final int STOP = 1112;
	private final CountDownListener listener;
	private int remainingSec;
	private boolean forceStopped = false;
	private Handler handler;
	public CountDown(final CountDownListener listener, final int remainingSec) {
		this.listener = listener;
		this.remainingSec = remainingSec;
	}

	public void start() {
		handler = new Handler() {
			@Override
			public void handleMessage(final Message msg) {
				super.handleMessage(msg);

				if (msg.what == TICK) {
					listener.tick(remainingSec);
				} else if (msg.what == STOP) {
					listener.stop();
				}
			}
		};

		new Thread(this).start();
	}

	public void forceStop() {
		forceStopped = true;
	}

	@Override
	public void run() {
		while (remainingSec > 0 && !forceStopped) {
			try {
				Thread.sleep(1000);
			} catch (final InterruptedException e) {
				Log.d(TAG, "Count down is interrupted unexpectedly.", e);
				break;
			}
			remainingSec--;
			final Message message = Message.obtain(handler, TICK);
			handler.sendMessage(message);
		}

		final Message message = Message.obtain(handler, STOP);
		handler.sendMessage(message);
	}

	public interface CountDownListener {
		void tick(int remainingSec);

		void stop();
	}

}
