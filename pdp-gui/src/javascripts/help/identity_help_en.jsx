/** @jsx React.DOM */

App.Help.IdentityHelpEn = React.createClass({
  render: function () {
    return (
        <div className="form-element about">
          <h1>Identity</h1>

          <p>Users of the PDP application (this GUI) are administrators and therefore can view all policies and violations.
          Administrators can also create policies for every IdP and SP.</p>

          <h2>Trusted API</h2>

          <p>Other users of the API of the PDP - like Dashboard Institution administrators - will
            have restricted access and functionality based on the identity of the user.
            </p>

          <h2>Identity Impersonation </h2>

          <p>To test and see the implications of the restrictions you can impersonate a different
          user from a specific IdP. Based on the IdP the following restrictions are applied:</p>
          <ul>

            <li>The creation, deletion and update of a policy is only allowed (OR):
              <ul>
              <li>if the IdP(s) of the policy are a subset of the IdP(s) of the user - linked by Institution ID</li>
              <li>if the policy has no IdP's and the SP of the policy is linked to one of the IdPs of the user</li>
              </ul>
            </li>
            <li>Additionally deletion and update of policies is further restricted:
              <ul>
                <li>the IdP of the user who created the policy must equal the Idp of the user who wants to delete / update the policy</li>
              </ul>
            </li>
            <li>Policies for the overview and drop-down in the playground are filtered and only returned when (OR):
              <ul>
                <li>IdP's of the policy are empty and the SP of the policy is allowed through one of the IdP's of the user</li>
                <li>one of the IdP's of the policy equals the IdP of the user</li>
                <li>the SP of the policy is linked to one of the IdP's of the user</li>
              </ul>
            </li>
            <li>Policy violations for the overview are filtered and only returned when (OR):</li>
            <ul>
              <ul>
                <li>the IdP in the JSON request of the violation is equal to one of the IdP's of the user</li>
                <li>the SP in the JSON request of the violation is linked to one IdP's of the user</li>
              </ul>
            </ul>
            <li>Policy revisions can only be seen when the policy is shown in the overview</li>
          </ul>

        </div>

    );
  }
});