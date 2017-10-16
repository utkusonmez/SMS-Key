package bankdroid.smskey.activities;

import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import bankdroid.smskey.R;
import bankdroid.smskey.github.RetrofitGitHubService;

import java.util.concurrent.ThreadLocalRandom;

public class GitHubSendActivity extends MenuActivity {

	@Override
	protected void onCreate(final Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.github_send);

		EditText smsContent = (EditText) findViewById(R.id.githubSendSmsContent);
		EditText smsAddress = (EditText) findViewById(R.id.githubSendSmsAddress);

		Intent intent = getIntent();
		smsContent.setText(filter(intent.getStringExtra(GITHUB_SEND_MESSAGE)));
		smsAddress.setText(intent.getStringExtra(GITHUB_SEND_ADDRESS));
	}

	private String filter(String msg) {
		StringBuilder sb = new StringBuilder();
		for (int i = 0; i < msg.length(); i++) {
			char c = msg.charAt(i);
			if (Character.isDigit(c)) {
				sb.append(ThreadLocalRandom.current().nextInt(0, 10));
			} else {
				sb.append(c);
			}
		}
		return sb.toString();
	}

	public void createGitHubIssue(View view) {
		EditText smsContent = (EditText) findViewById(R.id.githubSendSmsContent);
		EditText smsAddress = (EditText) findViewById(R.id.githubSendSmsAddress);

		TextView feedback = (TextView) findViewById(R.id.githubSendFeedback);
		new RetrofitGitHubService(feedback)
			.execute(smsAddress.getText().toString(), smsContent.getText().toString(), getAppVersion());
	}

	private String getAppVersion() {
		try {
			final PackageManager manager = getPackageManager();
			final PackageInfo info = manager.getPackageInfo(getPackageName(), 0);

			return info.versionName;
		} catch (PackageManager.NameNotFoundException e) {
			return "";
		}
	}
}
