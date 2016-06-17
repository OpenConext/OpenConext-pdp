/** @jsx React.DOM */

App.Help.PolicyConflictsHelpEn = React.createClass({
  render: function () {
    return (
        <div className="form-element about">
          <h1>What are policy conflicts?</h1>

          <p>If two or more policies are configured for the same Service Provider and either there are no
            Identity Providers configured or there is at least one Identity Provider that is configured in both policies then those policies
            are considered conflicting.</p>

          <h2>Consequences</h2>

          <p>Conflicting policies can influence each other because if one of the policies results in
            a negative decision then the user is not allowed access while the user might be allowed access
            based on the other - conflicting - policies.</p>
        </div>

    );
  }
});
