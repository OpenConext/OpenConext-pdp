from __future__ import annotations

from pathlib import Path
from pdp_harness import PDPPolicy, PDPRequest, PDPResponse, PDPDecision


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
    # create a new directory for the generated tests
    base_dir = Path(__file__).parent

    test_dir = base_dir / "my_first_test"
    test_dir.mkdir(exist_ok=True)

    policy = PDPPolicy(
        idp_entityids=["http://idp1"],
        sp_entityids=["http://sp1"],
        is_sp_negated=False,
        attributes={
            "eduPersonAffiliation": ["member", "staff"],
        }
    )
    request = PDPRequest(
        idp_entityid="http://idp1",
        sp_entityid="http://sp1",
        attributes={"eduPersonAffiliation": ["member"]}
    )
    response = PDPResponse(
        policy=policy,
        decision=PDPDecision.Permit
    )

    print(f"writing to {test_dir}")
    for f in test_dir.glob("*"): f.unlink()
    policy.write_json(test_dir / "policy.json")
    request.write_json(test_dir / "request.json")
    response.write_json(test_dir / "response.json")


def main():
    generate_harnass_tests()


if __name__ == '__main__':
    main()
