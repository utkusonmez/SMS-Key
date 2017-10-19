package pl.pamsoft.smskey.backend;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.http.client.BufferingClientHttpRequestFactory;
import org.springframework.http.client.ClientHttpRequestFactory;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.web.client.RestTemplate;

import java.util.Collections;

@SpringBootApplication
@ComponentScan
@EnableAutoConfiguration
public class BackendApplication {

	private final static Logger LOG = LoggerFactory.getLogger(BackendApplication.class);

	public static void main(String[] args) {
		String token = System.getenv("SMSKEY_GH_TOKEN");
		if (null == token) {
			throw new RuntimeException("Missing SMSKEY_GH_TOKEN env.");
		}
		LOG.info("Using GH token: {}", token);

		String ghEndpoint = System.getenv("SMSKEY_GH_ENDPOINT");
		if (null != ghEndpoint) {
			LOG.info("GH Endpoint overwritten: {}", ghEndpoint);
		}

		SpringApplication.run(BackendApplication.class, args);
	}

	@Bean
	public RestTemplate getRestClient() {
		RestTemplate restTemplate = new RestTemplate(new BufferingClientHttpRequestFactory(getClientHttpRequestFactory()));
		restTemplate.getMessageConverters().add(new MappingJackson2HttpMessageConverter());
		restTemplate.setInterceptors(Collections.singletonList((request, body, execution) -> execution.execute(request, body)));
		return restTemplate;
	}

	private ClientHttpRequestFactory getClientHttpRequestFactory() {
		int timeout = 2000;
		SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
		factory.setReadTimeout(timeout);
		factory.setConnectTimeout(timeout);
		return factory;
	}
}
