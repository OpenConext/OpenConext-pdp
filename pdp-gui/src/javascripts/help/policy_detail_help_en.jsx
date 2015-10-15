/** @jsx React.DOM */

App.Help.PolicyDetailHelpEn = React.createClass({
  render: function () {
    return (
        <div className="form-element about">
          <h1>How to create Policies?</h1>

          <p>Access policies define when a user is allowed to login to a Service based on the user attributes provided
            by
            the Institution and
            other attribute providers.</p>

          <h2>Access</h2>

          <p>We strongly recommend choosing the Permit access over the Deny access.</p>

          <p>The algorithm we use to determine if someone
            is allowed access based on the rules in the policy is 'first-applicable'. This means that the first rule
            which holds true
            is the rule determining the result - e.g. Deny or Permit.
          </p>

          <p>More information about the implications of a Deny access can be found
            <a target="_blank" href="http://docs.oasis-open.org/xacml/3.0/xacml-3.0-core-spec-os-en.html#_Toc325047268"> here</a>.
          </p>

          <h2>Service</h2>

          <p>The Service determines for which Service this policy applies. You can only link policies to one
            Service</p>

          <h2>Institution</h2>

          <p>The Institution determines for which Institution this policy applies. The Institution in this context is
            the Identity Provider which authenticated the user.
            You can link policies to zero or more Institutions</p>

          <h2>Rule</h2>

          <p>Choose if all the attributes defined in this policy must match the attributes of the user or that one
            match is sufficient for a 'Permit'</p>

          <h2>Attributes</h2>

          <p>The attributes and their values actually define if a user is granted access to the service or not.</p>

          <p>Special care must be taken when you choose a group-name as a required attribute. The value must be
            the fully qualified group name where the user is a member of. Please consult the responsible
            technical product manager how to retrieve the fully qualified name of a certain group / team.</p>

        </div>
    );
  }
});
