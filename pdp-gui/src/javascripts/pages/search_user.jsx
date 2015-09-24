/** @jsx React.DOM */

App.Pages.SearchUser = React.createClass({
  mixins: [React.addons.LinkedStateMixin],

  getInitialState: function() {
    return {
      search: ""
    }
  },

  render: function () {
    return (
      <div className="l-mini">
        <div className="mod-super-user">
          <h1>{I18n.t("search_user.switch_identity")}</h1>
          <form className="search">
            <fieldset>
              <input
                type="search"
                valueLink={this.linkState("search")}
                placeholder={I18n.t("search_user.search")} />
            </fieldset>
          </form>
          <table>
            <thead>
              <tr>
                <th>{I18n.t("search_user.name")}</th>
                <th className="center percent_25">{I18n.t("search_user.switch_to")}</th>
              </tr>
            </thead>
            <tbody>
              {this.filteredIdps().map(this.renderItem)}
            </tbody>
          </table>
        </div>
      </div>
    );
  },

  renderItem: function(idp) {
    return (
      <tr key={idp.name}>
        <td>{idp.name}</td>
        <td className="center">
          {
            this.props.roles.map(function(role) {
              return this.renderSwitchToRole(idp, role);
            }.bind(this))
          }
        </td>
      </tr>
    );
  },

  renderSwitchToRole: function(idp, role) {
    return (
      <a key={role} href="#" className="c-button" onClick={this.handleSwitchToUser(idp, role)}>
        {I18n.t("search_user.switch." + role.toLowerCase())}
      </a>
    );
  },

  handleSwitchToUser: function(idp, role) {
    return function(e) {
      e.preventDefault();
      e.stopPropagation();
      App.Controllers.User.switchToIdp(idp, role, function() {
        page("/");
      });
    }
  },

  filteredIdps: function() {
    return this.props.idps.filter(this.filterBySearchQuery);
  },

  filterBySearchQuery: function(idp) {
    return idp.name.toLowerCase().indexOf(this.state.search.toLowerCase()) >= 0;
  }
});
