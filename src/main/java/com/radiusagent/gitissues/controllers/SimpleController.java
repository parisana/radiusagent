package com.radiusagent.gitissues.controllers;

import com.radiusagent.gitissues.services.GitSearchService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.Arrays;
import java.util.Map;

/**
 * @author Parisana
 */
@RestController
@Slf4j
class SimpleController{

	private final GitSearchService gitSearchService;

	SimpleController(GitSearchService gitSearchService) {
		this.gitSearchService = gitSearchService;
	}

	@CrossOrigin(origins = "*")
	@GetMapping("/search")
	/**
	 * @param githubUrl, the public github url for which the search query is to be performed, should follow rfc2396 syntax ex: https://github.com/user/repo
	 * */
	public Mono<Map<String, String>> index(@RequestParam String githubUrl){
		final URL receivedGitRepoUrl;
		try {
			receivedGitRepoUrl = new URL(githubUrl);
			final String path = receivedGitRepoUrl.getPath();
			final String[] split = Arrays.stream(path.split("/")).filter(str->!str.isBlank()).toArray(String[]::new);
			final String userName = split[0];
			final String repoName = split[1];
			return Flux.concat(gitSearchService.getTotalOpenIssuesCount(userName, repoName), gitSearchService.getLast24HrsOpenIssuesCount(userName, repoName),
					gitSearchService.getLast7DaysExceptLast24HrsOpenIssuesCount(userName, repoName),
					gitSearchService.getAllExceptLast7DaysOpenIssuesCount(userName, repoName))
					.collectMap(s -> s.split(":")[0], s -> s.split(":")[1]);
		} catch (MalformedURLException e) {
			throw new RuntimeException(e);
		}
	}

}
