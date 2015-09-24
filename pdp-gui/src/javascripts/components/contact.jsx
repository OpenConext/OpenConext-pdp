/** @jsx React.DOM */

App.Components.Contact = React.createClass({
  render: function() {
    return (
      <div className="contact">
        <h2>{I18n.t("contact.email")}</h2>
        <address>
          <a href={"mailto:" + this.props.email}>{this.props.email}</a>
        </address>
      </div>
    );
  }
});
