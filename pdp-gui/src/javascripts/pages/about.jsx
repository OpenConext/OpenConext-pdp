/** @jsx React.DOM */

App.Pages.About = React.createClass({

  render: function () {
    return (
        <div className="l-center">
          <div className="l-middle mod-about no-left box">
          <p className="description-header">Welcome to the SURFconext Policy Administration Point (PAP)</p>
          <p/>
          <p>With the PAP you can maintain XACML policies to configure fine-grained access rules for your Institution and the connected Service Providers.</p>
        </div>
      </div>
    )
  }
});
