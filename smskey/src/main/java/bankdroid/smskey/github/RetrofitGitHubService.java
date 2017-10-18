package bankdroid.smskey.github;

import android.content.pm.PackageManager;
import android.os.AsyncTask;
import android.os.Build;
import android.util.Log;
import android.widget.TextView;
import bankdroid.smskey.Codes;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import retrofit2.Call;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.adapter.rxjava.RxJavaCallAdapterFactory;
import retrofit2.converter.gson.GsonConverterFactory;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static java.lang.String.format;

public class RetrofitGitHubService extends AsyncTask<String, Void, String> implements Codes {
	private final String title = "Support for new bank";
	private final Set<String> labels = new HashSet<>(Collections.singletonList("new_bank"));

	private final TextView textViewFeedback;

	public RetrofitGitHubService(TextView textViewFeedbackId) {
		this.textViewFeedback = textViewFeedbackId;
	}

	/**
	 * First arg is phone number,
	 * second one is message content
	 * third is app version
	 */
	@Override
	protected String doInBackground(String... args) {
		Retrofit retrofit = new Retrofit.Builder()
			.addConverterFactory(GsonConverterFactory.create())
			.addCallAdapterFactory(RxJavaCallAdapterFactory.create())
			.baseUrl(CommConstants.ENDPOINT_HOST).build();

		GitHubService service = retrofit.create(GitHubService.class);
		String msg = createMessage(args[0], args[1]);
		labels.add(format("app%s", args[2]));
		Call<CreateIssueResponse> issue = service.createIssue(new CreateIssueRequest(title, msg, labels));

		try {
			Response<CreateIssueResponse> execute = issue.execute();
			Log.d(TAG, "isSuccessful:" + execute.isSuccessful());
			Log.d(TAG, "code:" + execute.code());
			if (!execute.isSuccessful()) {
				Log.e(TAG, "isSuccessful:" + execute.errorBody().string());
				return "Invalid API usage.";
			} else {
				return execute.body().getHtmlUrl();
			}
		} catch (IOException e) {
			Log.e(TAG, e.getMessage(), e);
			return "There was an error: " + e.getMessage();
		}
	}

	private String createMessage(String address, String message) {
		HashMap<String, String> data = new HashMap<>();
		data.put("address", address);
		data.put("message", message);
		data.put("osVersion", format("Android SDK: %s (%s)", Build.VERSION.SDK_INT, Build.VERSION.RELEASE));

		StringBuilder sb = new StringBuilder("Please add support for following message:\n");
		sb.append("`");
		sb.append(new GsonBuilder().setPrettyPrinting().create().toJson(data));
		sb.append("`");
		return sb.toString();
	}

	@Override
	protected void onPostExecute(String result) {
		textViewFeedback.setText(result);
	}

}
