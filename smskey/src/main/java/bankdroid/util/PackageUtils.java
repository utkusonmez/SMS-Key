package bankdroid.util;

import android.content.Context;
import android.content.pm.PackageManager;
import org.androidannotations.annotations.EBean;
import org.androidannotations.annotations.RootContext;

@EBean
public class PackageUtils {

	// @formatter:off
	@RootContext Context contex;
	// @formatter:on

	public String getAppVersion() {
		try {
			return contex.getPackageManager().getPackageInfo(contex.getPackageName(), 0).versionName;
		} catch (PackageManager.NameNotFoundException e) {
			return "";
		}
	}
}
