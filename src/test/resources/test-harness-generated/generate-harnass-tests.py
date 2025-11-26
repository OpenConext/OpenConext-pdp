from __future__ import annotations

import os
from pathlib import Path
from pdp_harness import PDPPolicy, PDPRequest, PDPResponse, PDPDecision, PDPTest


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
    print(policy.to_json())

    request = PDPRequest(
        idp_entityid="http://idp1",
        sp_entityid="http://sp1",
        attributes={"eduPersonAffiliation": ["member", "staff"]}
    )
    print("Request:")
    print(request.to_json())

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
    print(response_permit.to_json())
    print("Response na:")
    print(response_na.to_json())
    print("Response deny:")
    print(response_deny.to_json())


def generate_harnass_tests():
    # write all tests to this directory
    os.chdir(Path(__file__).parent)

    policy = PDPPolicy(
        idp_entityids=["http://idp1"],
        sp_entityids=["http://sp1"],
        is_sp_negated=False,
        attributes={
            "eduPersonAffiliation": ["member", "staff"],
        }
    )

    test1 = PDPTest(
        name="simple_attr_allow",
        policy=policy,
        request=PDPRequest(
            idp_entityid="http://idp1",
            sp_entityid="http://sp1",
            attributes={"eduPersonAffiliation": ["member"]}
        ),
        decision=PDPDecision.Permit
    )
    test1.write()

    test2 = test1.copy()
    test2.policy.attributes["eduPersonAffiliation"] = ["notmember"]
    test2.policy.decision = PDPDecision.Deny
    test2.write()

    test3 = test1.copy()
    test3.request.sp_entityid = "http://sp2"
    test3.policy.decision = PDPDecision.NotApplicable
    test3.write()


def main():
    generate_harnass_tests()


if __name__ == '__main__':
    main()
