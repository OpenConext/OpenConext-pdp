/** @jsx React.DOM */

App.Pages.Profile = React.createClass({
  render: function() {
    var attributeKeys = Object.keys(App.currentUser.attributeMap);
    var roles = App.currentUser.grantedAuthorities;
    return (
      <div className="l-mini">

        <div className="mod-profile">
          <h1>{I18n.t("profile.title")}</h1>
          <p>{I18n.t("profile.sub_title")}</p>
          <h3>{I18n.t("profile.my_attributes")}</h3>
          <table>
            <thead>
              <tr>
                <th className="percent_50">{I18n.t("profile.attribute")}</th>
                <th className="percent_50">{I18n.t("profile.value")}</th>
              </tr>
            </thead>
            <tbody>
              {attributeKeys.map(this.renderAttribute)}
            </tbody>
          </table>
          <h3>{I18n.t("profile.my_roles")}</h3>
          <p>{I18n.t("profile.my_roles_description")}</p>
          <table>
            <thead>
              <tr>
                <th className="percent_50">{I18n.t("profile.role")}</th>
                <th className="percent_50">{I18n.t("profile.role_description")}</th>
              </tr>
            </thead>
            <tbody>
              {roles.map(this.renderRole)}
            </tbody>
          </table>

        </div>
      </div>
      );
  },

  renderAttribute: function(attributeKey) {
    // Use [] to get the value from I18n because attributeKey can contain (.) dot's.
    var attributeName = I18n.t("profile.attribute_map")[attributeKey]["name"]
    var attributeDescription = I18n.t("profile.attribute_map")[attributeKey]["description"]
    return (
      <tr key={attributeKey}>
        <td title={attributeDescription}>
          {attributeName}
        </td>
        <td>{App.currentUser.attributeMap[attributeKey]}</td>
      </tr>
      );
  },

  renderRole: function(role) {
    return (
      <tr key={role.authority}>
        <td>{I18n.t("profile.roles." + role.authority + ".name")}</td>
        <td>{I18n.t("profile.roles." + role.authority + ".description")}</td>
      </tr>
      );
  }

});
