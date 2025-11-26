from __future__ import annotations

import os
from pathlib import Path
from pdp_harness import PDPPolicy, PDPRequest, PDPResponse, PDPDecision, PDPTest

# very simple test; generate policy, requests and responses and write them to the screen
# use for debugging
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


# generate some simple tests
def generate_tests():
    # write all tests to this directory
    os.chdir(Path(__file__).parent)

    test1 = PDPTest(
        name="simple_attr_allow",
        policy=PDPPolicy(
            idp_entityids=["http://idp1"],
            sp_entityids=["http://sp1"],
            is_sp_negated=False,
            attributes={
                "eduPersonAffiliation": ["member", "staff"],
            }
        ),
        request=PDPRequest(
            idp_entityid="http://idp1",
            sp_entityid="http://sp1",
            attributes={"eduPersonAffiliation": ["member"]}
        ),
        decision=PDPDecision.Permit
    )
    test1.write()

    # copy the test and adjust the attribute to give a Deny
    test2 = test1.copy("simple_attr_deny")
    test2.request.attributes = {"eduPersonAffiliation":  ["notmember"]}
    test2.response.decision = PDPDecision.Deny
    test2.write()

    # copy the test and adjust the SP to give a NotApplicable
    test3 = test1.copy("simple_attr_na")
    test3.request.sp_entityid = "http://sp2"
    test3.response.decision = PDPDecision.NotApplicable
    test3.write()

    # copy the test and adjust the response to fail the test
    test4 = test1.copy("simple_attr_fail")
    test4.response.decision = PDPDecision.NotApplicable
    test4.write()


def main():
    generate_tests()


if __name__ == '__main__':
    main()
