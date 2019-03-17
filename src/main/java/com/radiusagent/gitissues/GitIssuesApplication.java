package com.radiusagent.gitissues;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.reactive.function.client.ClientRequest;
import org.springframework.web.reactive.function.client.ExchangeFilterFunction;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.net.MalformedURLException;
import java.net.URL;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Arrays;
import java.util.Collections;
import java.util.Map;
import java.util.function.Function;

@Slf4j
@SpringBootApplication
public class GitIssuesApplication {

	public static void main(String[] args) {
		SpringApplication.run(GitIssuesApplication.class, args);
	}

	@Bean
	public WebClient webClient(){
		return WebClient.builder().filter(logRequest()).baseUrl("https://api.github.com").build();

	}

	private static ExchangeFilterFunction logRequest() {
		return ExchangeFilterFunction.ofRequestProcessor(new Function<ClientRequest, Mono<ClientRequest>>() {
			@Override
			public Mono<ClientRequest> apply(ClientRequest clientRequest) {
				log.info("Request: {} {}", clientRequest.method(), clientRequest.url());
				clientRequest.attributes().forEach((k, v)-> log.info("key: " + k + " : " + v));
				clientRequest.headers().forEach((name, values) -> values.forEach(value -> log.info("{}={}", name, value)));
				return Mono.just(clientRequest);
			}
		});
	}

	// https://api.github.com/search/issues?page=1&per_page=1&rel=next&q=repo:rpcs3/rpcs3+is:public+is:issue+created:2018-03-06..2019-03-12&sort=created

}

@RestController
@Slf4j
class SimpleController{
	private final WebClient webClient;

	public SimpleController(WebClient webClient){
		this.webClient = webClient;
	}

	@GetMapping("/search")
	public Flux<Map<String, Integer>> index(@RequestParam String githubUrl){
		final URL receivedGitRepoUrl;
		try {
			receivedGitRepoUrl = new URL(githubUrl);
			final String path = receivedGitRepoUrl.getPath();
			final String[] split = Arrays.stream(path.split("/")).filter(str->!str.isBlank()).toArray(String[]::new);
			final String userName = split[0];
			final String repoName = split[1];
			return Flux.concat(getTotalOpenIssuesCount(userName, repoName), getLast24HrsOpenIssuesCount(userName, repoName),
					getLast7DaysExceptLast24HrsOpenIssuesCount(userName, repoName), getAllExceptLast7DaysOpenIssuesCount(userName, repoName));
		} catch (MalformedURLException e) {
			throw new RuntimeException(e);
		}
	}
	private Mono<Map<String, Integer>> getTotalOpenIssuesCount(String userName, String repoName){

		final CustomQueryObj build = CustomQueryObj.builder()
				.userName(userName)
				.repoName(repoName)
				.build();
		return getCountOfOpenIssue(build, CustomReturnKeyForGithubApi._0);

	}

	private Mono<Map<String, Integer>> getLast24HrsOpenIssuesCount(String userName, String repoName) {

		final Instant now = Instant.now().truncatedTo(ChronoUnit.SECONDS);
		final CustomQueryObj customQueryObj = CustomQueryObj.builder()
				.userName(userName)
				.repoName(repoName)
				.from(now.minus(1, ChronoUnit.DAYS).toString())
				.build();
		return getCountOfOpenIssue(customQueryObj, CustomReturnKeyForGithubApi._1);
	}

	private Mono<Map<String, Integer>> getLast7DaysExceptLast24HrsOpenIssuesCount(String userName, String repoName) {

		final Instant now = Instant.now().truncatedTo(ChronoUnit.SECONDS);
		final CustomQueryObj customQueryObj = CustomQueryObj.builder()
				.userName(userName)
				.repoName(repoName)
				.from(now.minus(7, ChronoUnit.DAYS).toString())
				.till(now.minus(1, ChronoUnit.DAYS).toString())
				.build();
		return getCountOfOpenIssue(customQueryObj, CustomReturnKeyForGithubApi._2);
	}

	private Mono<Map<String, Integer>> getAllExceptLast7DaysOpenIssuesCount(String userName, String repoName) {

		final Instant now = Instant.now().truncatedTo(ChronoUnit.SECONDS);
		final CustomQueryObj customQueryObj = CustomQueryObj.builder()
				.userName(userName)
				.repoName(repoName)
				.till(now.minus(7, ChronoUnit.DAYS).toString())
				.build();
		return getCountOfOpenIssue(customQueryObj, CustomReturnKeyForGithubApi._3);
	}

	private Mono<Map<String, Integer>> getCountOfOpenIssue(CustomQueryObj customQueryObj, CustomReturnKeyForGithubApi key) {
		MultiValueMap<String, String> attributes = new LinkedMultiValueMap<>();
		attributes.put("is", Arrays.asList("public", "issue"));
		attributes.put("page", Collections.singletonList("1"));
		attributes.put("per_page", Collections.singletonList("1"));
		attributes.put("q", Collections.singletonList("repo:" + customQueryObj.getUserName() + "/" +
				customQueryObj.getRepoName() + "+created:" + customQueryObj.getFrom() + ".." + customQueryObj.getTill()));

		return webClient
				.get()
				.uri(uriBuilder -> uriBuilder
						.scheme("https")
						.host("api.github.com")
						.path("/search/issues")
						.queryParams(attributes)
						.build())
				.retrieve().bodyToMono(GitHubApiResponse.class).map(gitHubApiResponse -> Map.of(key.name(), gitHubApiResponse.getTotalCount()))
				.onErrorResume(e-> {
					log.info(e.getMessage());
					return Mono.just(Map.of("error", key.ordinal()));
				});
	}
}

enum CustomReturnKeyForGithubApi{
//	_0 for total issues count, _1 for issues count between last 7 days excluding last 24 hrs, etc etc
	_0, _1, _2, _3
}

@NoArgsConstructor
@AllArgsConstructor
@Builder
@Getter
class CustomQueryObj {
	private String userName;
	private String repoName;
	@Builder.Default
	private String from ="*";  // The date in string, with * as the default in-case none is provided
	@Builder.Default
	private String till = "*";
}

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
class GitHubApiResponse{
	@JsonProperty("total_count")
	private int totalCount;
	@JsonProperty("incomplete_results")
	private boolean incompleteResults;
	@JsonIgnore
	private Object[] items;

	@Override
	public String toString() {
		return "GitHubApiResponse{" +
				"totalCount=" + totalCount +
				", incomplete_results=" + incompleteResults +
				", items=" + Arrays.toString(items) +
				'}';
	}
}

