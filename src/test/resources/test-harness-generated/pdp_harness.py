from __future__ import annotations

"""
This module provides a test harness for the PDP (Policy Decision Point) system.
It contains classes for constructing policies, requests and responses in a
consistent JSON format that can be used to test the PDP behavior.

See generate-tests.py for examples of how to use these classes to
create test cases by:

1. Creating a PDPPolicy with allow/deny rules, attributes and entity IDs
2. Creating a PDPRequest with attributes to test against the policy
3. Creating a PDPTest that combines policy and request with expected decision
4. Writing the test files (policy.json, request.json, response.json) to disk

The generated test files can then be executed by the Java PolicyHarnessTest.
"""

import copy
import json
import random
import datetime
from dataclasses import dataclass, field
from enum import StrEnum
from pathlib import Path
from zoneinfo import ZoneInfo

from jinja2 import Template, StrictUndefined


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
            self.id = ''.join(random.choices("0123456789abcdef", k=16))

    @property
    def flat_attributes(self):
        flat = [
            (name, value)
            for name, values in self.attributes.items()
            for value in values
        ]
        return flat

    def write_json(self, path: Path):
        path.write_text(self.to_json())

    def to_json(self) -> str:
        """Render this policy as normalized JSON using a Jinja2 template."""
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
                    {#- TODO: the groupID is required for more advanced attribute combinations
                              (see https://github.com/OpenConext/OpenConext-manage/issues/579) #}
                    "groupID": 0
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
        json_str = template.render(p=self)

        # check if the JSON is correct and reformat
        return json.dumps(json.loads(json_str), indent=4)


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

    def write_json(self, path: Path):
        path.write_text(self.to_json())

    def to_json(self) -> str:
        """Render this request as normalized JSON using a Jinja2 template."""

        request_template = """
        {
            "Request": {
                "AccessSubject": {
                    "Attribute": [
                        {%- for attr_name, value in r.flat_attributes -%}
                            {%- if not loop.first %},{% endif %}
                            {
                                "AttributeId": "urn:mace:dir:attribute-def:{{ attr_name }}",
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
        json_str = template.render(r=self)

        # check if the JSON is correct and reformat
        return json.dumps(json.loads(json_str), indent=4)


class PDPDecision(StrEnum):
    Permit = "Permit"
    Deny = "Deny"
    NotApplicable = "NotApplicable"


@dataclass
class PDPResponse:
    policy: PDPPolicy
    decision: PDPDecision

    def write_json(self, path: Path):
        path.write_text(self.to_json())

    def to_json(self) -> str:
        """Render this response as normalized JSON using a Jinja2 template."""
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
                {%- elif r.decision == 'Permit' %}
                {#- TODO: this is a bit annoying, because typically you don't really want to
                          have to specify which rule matches exactly #}
                {#- TODO: also unclear what whould be in here, exactly.  Even for complex rules,
                          this still contains only 1 attribute #}
                "Category" : [ {
                    "CategoryId" : "urn:mace:dir:attribute-def:{{ r.policy.flat_attributes[0][0] }}",
                    "Attribute" : [ {
                        "AttributeId" : "urn:mace:dir:attribute-def:{{ r.policy.flat_attributes[0][0] }}",
                        "Value" : "{{ r.policy.flat_attributes[0][1] }}",
                        "DataType" : "http://www.w3.org/2001/XMLSchema#string"
                    } ]
                } ],
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
        json_str = template.render(r=self)

        # check if the JSON is correct and reformat
        return json.dumps(json.loads(json_str), indent=4)


@dataclass
class PDPTest:
    name: str
    policy: PDPPolicy
    request: PDPRequest
    # need to specify either one of these:
    response: PDPResponse | None = None
    decision: PDPDecision | None = None

    def __post_init__(self):
        if self.response is None and self.decision is None:
            raise ValueError("either response or decision must be specified")
        if self.response is None:
            self.response = PDPResponse(self.policy, self.decision)

    def copy(self) -> PDPTest:
        return copy.deepcopy(self)

    def write(self, basedir: Path = Path('.')):
        output_dir = basedir / self.name
        print(f"writing to {output_dir.absolute()}")

        output_dir.mkdir(parents=True, exist_ok=True)
        for f in output_dir.glob("*"):
            f.unlink()

        self.policy.write_json(output_dir / "policy.json")
        self.request.write_json(output_dir / "request.json")
        self.response.write_json(output_dir / "response.json")
