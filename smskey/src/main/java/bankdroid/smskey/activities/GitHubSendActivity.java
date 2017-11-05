package bankdroid.smskey.activities;

import android.widget.EditText;
import android.widget.TextView;
import bankdroid.smskey.R;
import bankdroid.smskey.github.RetrofitGitHubService;
import bankdroid.util.PackageUtils;
import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.Bean;
import org.androidannotations.annotations.Click;
import org.androidannotations.annotations.EActivity;
import org.androidannotations.annotations.Extra;
import org.androidannotations.annotations.ViewById;

import java.util.concurrent.ThreadLocalRandom;

@EActivity(R.layout.github_send)
public class GitHubSendActivity extends MenuActivity {

	// @formatter:off
	@ViewById(R.id.githubSendSmsContent) EditText smsContent;
	@ViewById(R.id.githubSendSmsAddress) EditText smsAddress;
	@ViewById(R.id.githubSendFeedback) TextView feedback;
	@Extra(INTENT_GITHUB_SEND_MESSAGE) String intentMessage;
	@Extra(INTENT_GITHUB_SEND_ADDRESS) String intentAddress;
	@Bean PackageUtils packageUtils;
	// @formatter:on

	@AfterViews
	void init() {
		smsContent.setText(filter(intentMessage));
		smsAddress.setText(intentAddress);
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

	@Click(R.id.createGitHubIssueRow)
	void createGitHubIssue() {
		new RetrofitGitHubService(feedback)
			.execute(smsAddress.getText().toString(), smsContent.getText().toString(), packageUtils.getAppVersion());
	}
}
