/** @jsx React.DOM */

App.Help.PolicyPlaygroundHelpEn = React.createClass({
  render: function () {
    return (

        <div className="form-element about">
          <h1>How to use the Policy Playground?</h1>

          <p>With the SURFconext Policy Administration Point (PAP) you can maintain <a
              href="https://en.wikipedia.org/wiki/XACML"
              target="_blank">XACML</a> policies to configure fine-grained access rules for your Institution and the
            connected Service Providers.</p>

          <p>This playground can be used to test your policies. Newly created / updated or deleted policies are directly
            testable.</p>

          <p>By selecting one of the policies you can to test against this policy without selecting the correct input
            parameters.</p>

          <h2>Service Provider (SP) and Identity Provider (IdP)</h2>

          <p>Select the SP you have defined in your policy. Although you may have created the policy with no IdP or more
            then one, it
            is required to select one here. It's value will be ignored if you have chosen no IdP in your policy.</p>

          <h2>Attributes</h2>

          <p>The attributes you add and their values end up in the policy decision request that is issued to the Policy
            Definition Point (PDP).
            In this way you can test the multiple outcomes of enforcing your policies</p>

          <p>Note that if you use the attribute <em>urn:collab:group:surfteams.nl</em> and you want to test a match then
            you have to fill in the fully qualified team name inclusing the surfteams prefix.</p>

          <h2>Results</h2>

          <p>There are four possible results:</p>
          <ul>
            <li><span>Permit</span> - There was at least one applicable policy found and the Permit rule matched the
              attributes in the request
            </li>
            <li><span>Deny</span> - There was at least one applicable policy found and the attributes did not match</li>
            <li><span>Not Applicable</span> - No policy was found for the selected SP and IdP</li>
            <li><span>Indeterminate</span> - A required attribute by the Policy was not present. This can only happen
              with Deny rules.
            </li>
          </ul>
          <p>When the outcome is Permit or Not Applicable you would have been granted access.</p>
        </div>

    );
  }
});
