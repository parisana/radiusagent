package com.radiusagent.gitissues.domains;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * @author Parisana
 */
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Getter
/**
 * Represents the object type to be used in query parameters for github api.
 * */
public class CustomQueryObj {
	private String userName;
	private String repoName;
	@Builder.Default
	private String from ="*";  // The date in string, with * as the default in-case none is provided
	@Builder.Default
	private String till = "*";
}
