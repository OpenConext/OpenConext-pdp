/** @jsx React.DOM */

App.Help.PolicyRevisionsHelpEn = React.createClass({
  render: function () {
    return (
        <div className="form-element about">
          <h1>What are policy revisions?</h1>

          <p>Every time a policy gets updated a copy of the previous state is stored as a revision of the
            new  policy. By comparing revisions of a policy with each other and with the most current
            policy we are able to display an audit log of all changes made to a policy.</p>

          <h2>Retention</h2>

          <p>When a policy is deleted then all of the revisions of that policy - if any - are also deleted.</p>
        </div>

    );
  }
});
