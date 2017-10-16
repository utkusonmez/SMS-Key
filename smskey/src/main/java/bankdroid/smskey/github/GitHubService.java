package bankdroid.smskey.github;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.Headers;
import retrofit2.http.POST;

public interface GitHubService {

	@Headers("Authorization: token 4b225025429989f08962f6b33c788d9260fc978e")
	@POST("/repos/pmajkutewicz/SMS-Key/issues")
	Call<CreateIssueResponse> createIssue(@Body CreateIssueRequest issue);

}
