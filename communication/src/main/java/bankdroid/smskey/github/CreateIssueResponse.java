package bankdroid.smskey.github;

public class CreateIssueResponse {
	private int id;
	private String url;
	private String htmlUrl;
	private int number;

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public String getUrl() {
		return url;
	}

	public void setUrl(String url) {
		this.url = url;
	}

	public String getHtmlUrl() {
		return htmlUrl;
	}

	/* Neded by backend to deserialize GH response */
	/* Retrofit uses reflection, so wathc out for field names */
	public void setHtml_url(String htmlUrl) {
		this.htmlUrl = htmlUrl;
	}

	public int getNumber() {
		return number;
	}

	public void setNumber(int number) {
		this.number = number;
	}
}
