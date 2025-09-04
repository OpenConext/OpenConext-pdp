### Test Harness

This directory contains folders. Each folder contains a minimal of three files:
- policy.json
- request.json
- response.json

The `policy.json` is PdPpolicy in the JSON format as stored in Manage. In the details of a policy in Manage, there is a
JSON tab where you can copy the JSON.

The `request.json` is the data send from EB to PdP and contains the SP, IdP and user attributes.

The `response.json` is the expected response from PdP. You can capture examples in the policy playground in Manage
for both the requests and responses.

With `mvn test` all the tests are run, with `mvn test -Dtest=PolicyHarnessTest#test -Dpolicy={policy_dir_name}` only the policy in
directory `policy_dir_name` is tested.

If you want to test with multiple - possible conflicting - policies, you can add multiple policy_XYZ.json in
the directory, and these will all be used in the test.
