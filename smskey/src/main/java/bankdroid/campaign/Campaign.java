package bankdroid.campaign;

import java.util.Date;

import android.content.Context;
import android.view.View;

public interface Campaign {
	boolean isActive(Date lastShown, int numberOfAppearance, int hitCount, int codeCount);

	View getView(Context context);

	void hit(Context context);
}
