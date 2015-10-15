/** @jsx React.DOM */

App.Help.PolicyViotaltionsHelpEn = React.createClass({
  render: function () {
    return (
        <div className="form-element about">
          <h1>What are unauthorized logins?</h1>

          <p>Every time a negative decision is sent back from the Policy Decision Point (PDP) to the Policy
            Enforcement
            Point (PEP) this will
            result in a forbidden access to the designated Service Provider.</p>

          <h2>Unauthorized decisions</h2>

          <p>Every time a negative decision occurs - e.g. policy violation from the PDP - it is stored. When you want
            to look at the details of a policy violation click the view icon.</p>

          <p>For every policy violation the following (for 30 days) is stored:</p>
          <ul>
            <li>The original JSON request from the PEP</li>
            <li>The JSON response from the PDP</li>
            <li>A reference to the policy responsible for the decision</li>
            <li>The decision - e.g. Deny or Indeterminate - from the PDP</li>
          </ul>

        </div>

    );
  }
});
