## Provision policies

To provision policies on start-up put them in this folder. The format must be according the
PdpPolicyDefinition bean:

```
{
	"name": "Name",
	"description": "Omschrijving",
	"serviceProviderId": "https://teams.surfconext.nl",
	"attributes": [{
		"name": "urn:collab:group:surfteams.nl",
		"value": "urn:collab:group:surfteams.nl:nl:surfnet:diensten:team_name"
	}],
	"denyAdvice": "The message for the user",
	"denyAdviceNl": "De melding voor de gebruiker",
	"active": true,
	"policyId": "urn:surfconext:xacml:policy:id:name",
	"type": "reg"
}
```