package bankdroid.smskey.activities;

import android.widget.TextView;
import bankdroid.smskey.R;
import bankdroid.util.PackageUtils;
import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.Bean;
import org.androidannotations.annotations.EActivity;
import org.androidannotations.annotations.ViewById;

@EActivity(R.layout.about)
public class AboutActivity extends MenuActivity {

	// @formatter:off
	@ViewById(R.id.versionId) TextView versionId;
	@Bean PackageUtils packageUtils;
	// @formatter:on

	@AfterViews
	void setVersionNumber() {
		versionId.setText(packageUtils.getAppVersion());
	}
}
