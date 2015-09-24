/** @jsx React.DOM */

App.Pages.Logout = React.createClass({
  render: function () {
    return (
      <div className="mod-logout">
        {I18n.t("logout.title")}<br />
        <span dangerouslySetInnerHTML={{ __html: I18n.t("logout.description_html")}} />
      </div>
    );
  }
});
