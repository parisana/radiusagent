package com.radiusagent.gitissues.services;

import com.radiusagent.gitissues.domains.CustomQueryObj;
import com.radiusagent.gitissues.domains.CustomReturnKeyForGithubApi;
import com.radiusagent.gitissues.domains.GitHubApiResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Arrays;
import java.util.Collections;

/**
 * @author Parisana
 */
@Component
@Slf4j
public class GitSearchService {

    private final WebClient webClient;

    public GitSearchService(WebClient webClient) {
        this.webClient = webClient;
    }

    /**
     * @param userName, the username of the github repo
     * @param repoName , the repo name of the github repo
     * @return key:value as string where key is one of the CustomReturnKeyForGithubApi enums and value is an integer.
     * */
    public Mono<String> getTotalOpenIssuesCount(String userName, String repoName){

        final CustomQueryObj build = CustomQueryObj.builder()
                .userName(userName)
                .repoName(repoName)
                .build();
        return getCountOfOpenIssue(build, CustomReturnKeyForGithubApi._0);

    }

    /**
     * @param userName, the username of the github repo
     * @param repoName , the repo name of the github repo
     * @return key:value as string where key is one of the CustomReturnKeyForGithubApi enums and value is an integer.
     * */
    public Mono<String> getLast24HrsOpenIssuesCount(String userName, String repoName) {

        final Instant now = Instant.now().truncatedTo(ChronoUnit.SECONDS);
        final CustomQueryObj customQueryObj = CustomQueryObj.builder()
                .userName(userName)
                .repoName(repoName)
                .from(now.minus(1, ChronoUnit.DAYS).toString())
                .build();
        return getCountOfOpenIssue(customQueryObj, CustomReturnKeyForGithubApi._1);
    }

    /**
     * @param userName, the username of the github repo
     * @param repoName , the repo name of the github repo
     * @return key:value as string where key is one of the CustomReturnKeyForGithubApi enums and value is an integer.
     * */
    public Mono<String> getLast7DaysExceptLast24HrsOpenIssuesCount(String userName, String repoName) {

        final Instant now = Instant.now().truncatedTo(ChronoUnit.SECONDS);
        final CustomQueryObj customQueryObj = CustomQueryObj.builder()
                .userName(userName)
                .repoName(repoName)
                .from(now.minus(7, ChronoUnit.DAYS).toString())
                .till(now.minus(1, ChronoUnit.DAYS).toString())
                .build();
        return getCountOfOpenIssue(customQueryObj, CustomReturnKeyForGithubApi._2);
    }

    /**
     * @param userName, the username of the github repo
     * @param repoName , the repo name of the github repo
     * @return key:value as string where key is one of the CustomReturnKeyForGithubApi enums and value is an integer.
     * */
    public Mono<String> getAllExceptLast7DaysOpenIssuesCount(String userName, String repoName) {

        final Instant now = Instant.now().truncatedTo(ChronoUnit.SECONDS);
        final CustomQueryObj customQueryObj = CustomQueryObj.builder()
                .userName(userName)
                .repoName(repoName)
                .till(now.minus(7, ChronoUnit.DAYS).toString())
                .build();
        return getCountOfOpenIssue(customQueryObj, CustomReturnKeyForGithubApi._3);
    }

    /**
     * @return total open issues count based on the customQueryObj
     * */
    private Mono<String> getCountOfOpenIssue(CustomQueryObj customQueryObj, CustomReturnKeyForGithubApi key) {
        MultiValueMap<String, String> attributes = new LinkedMultiValueMap<>();
        attributes.put("page", Collections.singletonList("1"));
        attributes.put("per_page", Collections.singletonList("1"));
        attributes.put("q", Collections.singletonList("repo:" + customQueryObj.getUserName() + "/" + customQueryObj.getRepoName() +
                "+is:open+is:issue"+
                "+created:" + customQueryObj.getFrom() + ".." + customQueryObj.getTill()));

        return webClient
                .get()
                .uri(uriBuilder -> uriBuilder
                        .scheme("https")
                        .host("api.github.com")
                        .path("/search/issues")
                        .queryParams(attributes)
                        .build())
                .retrieve().bodyToMono(GitHubApiResponse.class).map(gitHubApiResponse -> key.name()+":"+gitHubApiResponse.getTotalCount())
                .onErrorResume(e-> {
                    log.info(e.getMessage());
                    return Mono.just("error:" + e.getMessage());
                });
    }

}
