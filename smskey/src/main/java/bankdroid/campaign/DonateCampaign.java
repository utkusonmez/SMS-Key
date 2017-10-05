package bankdroid.campaign;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.view.View;
import bankdroid.smskey.Codes;
import bankdroid.smskey.R;

import java.util.Date;

public class DonateCampaign implements Campaign {

	@Override
	public View getView(final Context context) {
		return View.inflate(context, R.layout.campaign_donate, null);
	}

	@Override
	public boolean isActive(final Date lastShown, final int numberOfAppearance, final int hitCount, final int codeCount) {
		return hitCount == 0 && (codeCount > 150 && (codeCount % 17 == 0));
	}

	@Override
	public void hit(final Context context) {
		context.startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(Codes.URL_PAYPAL)));
	}
}
