package bankdroid.smskey.github;

import java.util.Collection;

public class CreateIssueRequest {
	private final String title;
	private final String body;
	private final Collection<String> labels;

	public CreateIssueRequest(String title, String body, Collection<String> labels) {
		this.title = title;
		this.body = body;
		this.labels = labels;
	}
}
