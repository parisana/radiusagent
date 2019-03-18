## Radius Agent Backend
The project is made as per an assignment of Radius-Agent.
It doesn't have authentication and uses the github api to retrieve issues information.

##Usage:
hit the uri: /search
    with query parameter: githubUrl=<any public github repo>

## Returns:
    Mono<Map<String, String>>
    example: {"_0":"5410","_1":"6","_2":"18","_3":"5386", "error<optional>":"<error message>"}

    here _0 corresponds to : Total number of open issues
        _1 corresponds to : Number of open issues that were opened in the last 24 hours
        _2 corresponds to : Number of open issues that were opened more than 24 hours ago but less than 7 days ago
        _3 corresponds to : Number of open issues that were opened more than 7 days ago


## [Deployed on: https://agile-lowlands-93991.herokuapp.com]

## Improvements:
Error handling to return proper error codes and messages.

## Request rate
As per GitHub api restrictions, there must be an average of 2 seconds between each request. Or else you'll receive 403 error.