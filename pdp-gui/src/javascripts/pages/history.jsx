/** @jsx React.DOM */

App.Pages.History = React.createClass({
  mixins: [App.Mixins.SortableTable("history", "requestDate", true)],

  render: function() {
    return (
      <div className="l-mini">

        <div className="mod-history">
          <h1>{I18n.t("history.title")}</h1>

          <table>
            <thead>
              <tr>
                {this.renderSortableHeader("percent_15", "requestDate")}
                {this.renderSortableHeader("percent_15", "userName")}
                {this.renderSortableHeader("percent_25", "type")}
                {this.renderSortableHeader("percent_20", "jiraKey")}
                {this.renderSortableHeader("percent_25", "status")}
              </tr>
            </thead>
            <tbody>
            {this.sort(this.props.actions).map(this.renderAction)}
            </tbody>
          </table>
        </div>
      </div>
    );
  },

  renderAction: function(action) {
    return (
      <tr key={action.id}>
        <td className="percent_15">{new Date(action.requestDate).format("dd-MM-yyyy")}</td>
        <td className="percent_15">{action.userName}</td>
        <td className="percent_25">{I18n.t("history.action_types." + action.type, {serviceName: action.spName})}</td>
        <td className="percent_20">{action.jiraKey}</td>
        <td className="percent_25">{I18n.t("history.statusses." + action.status)}</td>
      </tr>
    );
  },

  convertRequestDateForSort: function(value) {
    return Date.parse(value);
  }
});
