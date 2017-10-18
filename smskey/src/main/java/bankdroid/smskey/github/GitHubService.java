package bankdroid.smskey.github;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.Headers;
import retrofit2.http.POST;

public interface GitHubService {

	@Headers(CommConstants.HEADER_NAME + ": " + CommConstants.HEADER_VALUE)
	@POST("/createIssue")
	Call<CreateIssueResponse> createIssue(@Body CreateIssueRequest issue);

}
