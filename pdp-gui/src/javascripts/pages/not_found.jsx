/** @jsx React.DOM */

App.Pages.NotFound = React.createClass({
  render: function () {
    return (
      <div className="mod-not-found">
        <h1>{I18n.t("not_found.title")}</h1>
        <p dangerouslySetInnerHTML={{ __html: I18n.t("not_found.description_html") }} />
      </div>
    );
  }
});
