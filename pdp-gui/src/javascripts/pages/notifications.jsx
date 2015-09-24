/** @jsx React.DOM */

App.Pages.Notifications = React.createClass({
  render: function() {
    var notificationMessage = this.props.notificationMessage;
    return (
      <div className="l-mini">

        <div className="mod-notifications">
          <h1>{I18n.t("notifications.title")}</h1>

          <p>
            {notificationMessage.messageKeys.map(this.renderNotificationMessage)}
          </p>
          <br />
          <table>
            <thead>
              <tr>
                <th className="percent_60">{I18n.t("notifications.name")}</th>
                <th className="percent_20">{I18n.t("notifications.license")}</th>
                <th className="percent_20">{I18n.t("notifications.connection")}</th>
              </tr>
            </thead>
            <tbody>
              {notificationMessage.arguments.sort(function(l, r) {
                return l.name.localeCompare(r.name)
              }).map(this.renderNotification)}
            </tbody>
          </table>

        </div>
      </div>
      );
  },

  renderNotificationMessage: function(messageKey) {
    return <p key={messageKey}>{I18n.t(messageKey)}</p>
  },

  renderNotification: function(notificationArgument) {
    return (
      <tr key={notificationArgument.id}>
        <td>
          <a href={"/apps/" + notificationArgument.id}>
            {notificationArgument.name}
          </a>
        </td>
        {App.renderYesNo(notificationArgument.license)}
        {App.renderYesNo(notificationArgument.connected)}
      </tr>
      );
  }

});
