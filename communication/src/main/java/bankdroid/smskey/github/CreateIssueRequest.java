package bankdroid.smskey.github;

import java.util.Collection;

public class CreateIssueRequest {
	private String title;
	private String body;
	private Collection<String> labels;

	public CreateIssueRequest() {
	}

	public CreateIssueRequest(String title, String body, Collection<String> labels) {
		this.title = title;
		this.body = body;
		this.labels = labels;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public String getBody() {
		return body;
	}

	public void setBody(String body) {
		this.body = body;
	}

	public Collection<String> getLabels() {
		return labels;
	}

	public void setLabels(Collection<String> labels) {
		this.labels = labels;
	}

	@Override
	public String toString() {
		final StringBuilder sb = new StringBuilder("CreateIssueRequest{");
		sb.append("title='").append(title).append('\'');
		sb.append(", body='").append(body).append('\'');
		sb.append('}');
		return sb.toString();
	}
}
