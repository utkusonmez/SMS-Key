package bankdroid.campaign;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.Handler;
import android.os.Message;
import android.preference.PreferenceManager;
import android.util.DisplayMetrics;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.RelativeLayout;
import android.widget.RelativeLayout.LayoutParams;
import bankdroid.smskey.Codes;
import bankdroid.smskey.R;
import bankdroid.util.ErrorLogger;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class CampaignManager implements OnClickListener {
	private final static List<Campaign> campaigns = new ArrayList<Campaign>();
	private final static long ANIMATION_DELAY = 500;

	static {
		campaigns.add(new DonateCampaign());
		campaigns.add(new FacebookCampaign());
		campaigns.add(new MarketRateCampaign());
		campaigns.add(new VisitBlogCampaign());
	}

	private final Context context;
	private final SharedPreferences preferences;
	private Campaign campaign;

	public CampaignManager(final Context context) {
		super();
		this.context = context;
		preferences = PreferenceManager.getDefaultSharedPreferences(context);
	}

	public static void resetCampaign(final Context context) {
		final SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
		final Editor edit = preferences.edit();

		for (final Campaign campaign : campaigns) {
			final String key = campaign.getClass().getName();
			if (preferences.contains(key)) {
				edit.remove(key);
			}
		}

		edit.commit();
	}

	public void show(final RelativeLayout parent) {
		if (campaign != null) { // some campaign is already displayed
			return;
		}

		final View view;
		try {
			campaign = getActiveCampaign();
			if (campaign == null) {
				return;
			}

			view = campaign.getView(context);
		} catch (final Exception e) {
			ErrorLogger.logError(context, e, "PREPCAMPAIGN");
			return;
		}

		final Handler handler = new Handler() {
			@Override
			public void handleMessage(final Message msg) {
				try {
					super.handleMessage(msg);

					if (msg.what == 1) {
						//calculate 50dp instead of 50px
						final DisplayMetrics dm = context.getResources().getDisplayMetrics();
						final int pixelSize = (int) (50f * dm.density);

						final LayoutParams params = new LayoutParams(LayoutParams.FILL_PARENT, pixelSize);
						params.addRule(RelativeLayout.ALIGN_PARENT_TOP, 0);
						params.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM, RelativeLayout.TRUE);
						params.addRule(RelativeLayout.ALIGN_PARENT_LEFT, RelativeLayout.TRUE);
						params.addRule(RelativeLayout.ALIGN_PARENT_RIGHT, RelativeLayout.TRUE);

						final Animation animation = AnimationUtils.loadAnimation(context, R.anim.slide_in_bottom);
						view.startAnimation(animation);

						parent.addView(view, params);

						//set up onclicklisteners
						view.setOnClickListener(CampaignManager.this);
					}
				} catch (final Exception e) {
					ErrorLogger.logError(context, e, "SHOWCAMPAIGN");
				}
			}
		};
		handler.sendEmptyMessageDelayed(1, ANIMATION_DELAY);
	}

	private Campaign getActiveCampaign() {
		final int codeCount = preferences.getInt(Codes.PREF_CODE_COUNT, 0);
		for (final Campaign campaign : campaigns) {
			final CampaignStatus status = loadCampaignStatus(campaign);

			if (campaign.isActive(status.getLastShown(), status.getNumberOfShow(), status.getHitCount(), codeCount)) {
				status.setNumberOfShow(status.getNumberOfShow() + 1);
				status.setLastShown(new Date());
				saveCampaignStatus(campaign, status);

				return campaign;
			}
		}
		return null;
	}

	private void saveCampaignStatus(final Campaign campaign, final CampaignStatus status) {
		final String statusString = status.toString();
		final String key = campaign.getClass().getName();
		final Editor editor = preferences.edit();
		editor.putString(key, statusString);
		editor.commit();
	}

	private CampaignStatus loadCampaignStatus(final Campaign campaign) {
		final String key = campaign.getClass().getName();

		String statusString = null;
		if (preferences.contains(key)) {
			statusString = preferences.getString(key, null);
		}

		CampaignStatus status = null;
		if (statusString != null) {
			status = CampaignStatus.fromString(statusString);
		} else {
			status = new CampaignStatus();
		}

		return status;
	}

	@Override
	public void onClick(final View v) {
		final CampaignStatus status = loadCampaignStatus(campaign);
		status.setHitCount(status.getHitCount() + 1);
		saveCampaignStatus(campaign, status);

		campaign.hit(context);
	}
}
