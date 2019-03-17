The project is made as per an assignment of Radius-Agent.
It doesn't have authentication and uses the github api to retrieve issues information.

Usage:
hit the uri: /search
    with query parameter: githubUrl=<any public github repo>

Returns:
    Flux<Map<String, String>>
    example: {"_0":"5410","_1":"6","_2":"18","_3":"5386"}