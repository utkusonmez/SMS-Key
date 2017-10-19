package pl.pamsoft.smskey.backend;

import bankdroid.smskey.github.CommConstants;
import bankdroid.smskey.github.CreateIssueRequest;
import bankdroid.smskey.github.CreateIssueResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

import javax.annotation.PostConstruct;
import java.util.Collections;

import static org.springframework.http.HttpMethod.POST;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

@RestController
public class CreateIssueController {

	private final static Logger LOG = LoggerFactory.getLogger(CreateIssueController.class);
	private String ghEndpoint = null == System.getenv("SMSKEY_GH_ENDPOINT")
			? "https://api.github.com/repos/pmajkutewicz/SMS-Key/issues"
			: System.getenv("SMSKEY_GH_ENDPOINT");

	@Autowired
	private RestTemplate restTemplate;

	private HttpHeaders ghHeaders;

	@PostConstruct
	public void init() {
		ghHeaders = new HttpHeaders();
		ghHeaders.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON));
		ghHeaders.set("Authorization", String.format("token %s", System.getenv("SMSKEY_GH_TOKEN")));
	}

	@PostMapping(value = "/createIssue", produces = APPLICATION_JSON_VALUE, consumes = APPLICATION_JSON_VALUE)
	public ResponseEntity<CreateIssueResponse> createIssue(@RequestBody CreateIssueRequest request,
														   @RequestHeader(CommConstants.HEADER_NAME) String secHeader) {
		LOG.info("Request: {}, with token {}", request, secHeader);

		if (!CommConstants.HEADER_VALUE.equals(secHeader)) {
			return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
		}

		ResponseEntity<CreateIssueResponse> ghResponse =
			restTemplate.exchange(ghEndpoint, POST, new HttpEntity<>(request, ghHeaders), CreateIssueResponse.class);

		if (HttpStatus.CREATED == ghResponse.getStatusCode()) {
			LOG.info("For {} request we got response: {}", request, ghResponse);
			return new ResponseEntity<>(ghResponse.getBody(), HttpStatus.CREATED);
		} else {
			LOG.warn("For {} request we got error {}.", request, ghResponse.getStatusCode());
			return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
		}

	}
}
