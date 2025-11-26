from __future__ import annotations

import json
from dataclasses import dataclass, field, asdict, InitVar
from enum import StrEnum

from jinja2 import Template, StrictUndefined
import uuid
import datetime
from zoneinfo import ZoneInfo


MARTY_DATE = datetime.datetime(2015, 10, 21, 7, 28, 0, tzinfo=ZoneInfo("America/Los_Angeles"))


@dataclass
class PDPPolicy:
    id: str | None = None
    idp_entityids: list[str] = field(default_factory=list)
    sp_entityids: list[str] = field(default_factory=list)
    attributes: dict[str, list[str]] = field(default_factory=dict)
    is_sp_negated: bool = False
    is_deny: bool = False
    is_and: bool = False
    created: datetime.datetime = MARTY_DATE

    def __post_init__(self):
        if self.id is None:
            self.id = str(uuid.uuid4())

    @property
    def flat_attributes(self):
        flat = [
            (name, value)
            for name, values in self.attributes.items()
            for value in values
        ]
        return flat


@dataclass
class PDPRequest:
    idp_entityid: str
    sp_entityid: str
    attributes: dict[str, list[str]] = field(default_factory=dict)

    @property
    def flat_attributes(self):
        flat = [
            (name, value)
            for name, values in self.attributes.items()
            for value in values
        ]
        return flat


class PDPDecision(StrEnum):
    Permit = "Permit"
    Deny = "Deny"
    NotApplicable = "NotApplicable"


@dataclass
class PDPResponse:
    policy: PDPPolicy
    decision: PDPDecision


def render_policy(policy: PDPPolicy) -> str:
    policy_template = """
    {
        "id": "{{ p.id }}",
        "policyId": "urn:surfconext:xacml:policy:id:{{ p.id }}",
        "name": "{{ p.id }}",
        "description": "This is the description of the policy",
        "serviceProviderIds": {{ p.sp_entityids | list | tojson }},
        "serviceProvidersNegated": {{ p.is_sp_negated | tojson }},
        "identityProviderIds": {{ p.idp_entityids | list | tojson }},
        "attributes": [
            {%- for attr_name, attr_value in p.flat_attributes -%}
            {%- if not loop.first %},{% endif %}
            {
                "name": "urn:mace:dir:attribute-def:{{ attr_name }}",
                "value": "{{ attr_value }}",
                "negated": false,
                "groupID": 0 {#- TODO: the groupID is required for more advanced attribute combinations (see https://github.com/OpenConext/OpenConext-manage/issues/579) #}
            }
            {%- endfor -%}
        ],
        "denyRule": {{ p.is_deny | tojson }},
        "allAttributesMustMatch": {{ p.is_and | tojson }},
        "created": "{{ p.created.isoformat() }}",
        "denyAdvice": "NOT ALLOWED",
        "denyAdviceNl": "MAG NIET",
        "active": true,
        "actionsAllowed": false,
        "type": "reg",
        "revisionNbr": 0,
        "activatedSr": false
    }
    """
    template = Template(policy_template, undefined=StrictUndefined)
    json_str = template.render(p=policy)

    # check if the json is correct and reformat:
    return json.dumps(json.loads(json_str), indent=4)


def render_request(request: PDPRequest) -> str:
    request_template = """
    {
        "Request": {
            "AccessSubject": {
                "Attribute": [
                    {%- for attr_name, value in r.flat_attributes -%}
                        {%- if not loop.first %},{% endif %}
                        {
                            "AttributeId": "{{ attr_name }}",
                            "Value": "{{ value }}"
                        }
                    {%- endfor -%}
                ]
            },
            "Resource": {
                "Attribute": [{
                    "AttributeId": "SPentityID",
                    "Value": "{{ r.sp_entityid }}"
                },{
                    "AttributeId": "IDPentityID",
                    "Value": "{{ r.idp_entityid }}"
                },{
                    "AttributeId": "ClientID",
                    "Value": "EngineBlock"
                }]
           }
        }
    }
    """
    template = Template(request_template, undefined=StrictUndefined)
    json_str = template.render(r=request)

    # check if the json is correct and reformat:
    return json.dumps(json.loads(json_str), indent=4)


def render_response(response: PDPResponse) -> str:
    response_template = """
    {
        "Response": [{
            "Status": {
                "StatusCode": { "Value": "urn:oasis:names:tc:xacml:1.0:status:ok" }
            },
            {%- if r.decision == 'Deny' %}
            "AssociatedAdvice": [
                {
                   "AttributeAssignment": [
                      {
                         "Category": "urn:oasis:names:tc:xacml:3.0:attribute-category:resource",
                         "AttributeId": "DenyMessage:en",
                         "Value": "NOT ALLOWED",
                         "DataType": "http://www.w3.org/2001/XMLSchema#string"
                      },
                      {
                         "Category": "urn:oasis:names:tc:xacml:3.0:attribute-category:resource",
                         "AttributeId": "DenyMessage:nl",
                         "Value": "MAG NIET",
                         "DataType": "http://www.w3.org/2001/XMLSchema#string"
                      },
                      {
                         "Category": "urn:oasis:names:tc:xacml:3.0:attribute-category:resource",
                         "AttributeId": "IdPOnly",
                         "Value": true,
                         "DataType": "http://www.w3.org/2001/XMLSchema#boolean"
                      }
                   ],
                   "Id": "urn:surfconext:xacml:policy:id:{{ r.policy.id }}"
                }
            ],
            {%- endif %}
            "PolicyIdentifier": {
                "PolicySetIdReference": [{
                    "Version": "1.0",
                    "Id": "urn:openconext:pdp:root:policyset"
                }]
                {%- if r.decision != 'NotApplicable' -%},
                "PolicyIdReference": [{
                    "Version": "1",
                    "Id": "urn:surfconext:xacml:policy:id:{{ r.policy.id }}"
                }]
                {%- endif -%}
            },
            "Decision": "{{ r.decision }}"
        }]
    }
    """
    template = Template(response_template, undefined=StrictUndefined)
    json_str = template.render(r=response)

    # check if the json is correct and reformat:
    return json.dumps(json.loads(json_str), indent=4)


def test():
    policy = PDPPolicy(
        idp_entityids=["http://idp1"],
        sp_entityids=["http://sp1"],
        is_sp_negated=False,
        attributes={
            "eduPersonAffiliation": ["member", "staff"],
            "eduPersonPrincipalName": ["foo@example.com"]
        },
        is_deny=False,
        is_and=True
    )
    print("Policy:")
    print(render_policy(policy))

    request = PDPRequest(
        idp_entityid="http://idp1",
        sp_entityid="http://sp1",
        attributes={
            "urn:mace:dir:attribute-def:eduPersonAffiliation": ["member", "staff"]
        }
    )
    print("Request:")
    print(render_request(request))

    response_permit = PDPResponse(
        policy=policy,
        decision=PDPDecision.Permit
    )
    response_na = PDPResponse(
        policy=policy,
        decision=PDPDecision.NotApplicable
    )
    response_deny = PDPResponse(
        policy=policy,
        decision=PDPDecision.Deny
    )
    print("Response permit:")
    print(render_response(response_permit))
    print("Response na:")
    print(render_response(response_na))
    print("Response deny:")
    print(render_response(response_deny))


def main():
    test()


if __name__ == '__main__':
    main()
