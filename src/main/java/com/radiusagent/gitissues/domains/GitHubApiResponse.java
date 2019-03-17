package com.radiusagent.gitissues.domains;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.Arrays;

/**
 * @author Parisana
 */
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class GitHubApiResponse{
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
