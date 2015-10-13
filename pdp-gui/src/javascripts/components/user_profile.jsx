/** @jsx React.DOM */

App.Components.UserProfile = React.createClass({

  render: function () {
    return (
        <ul className="user-profile">
          {this.renderRole()}
          {this.renderIdps()}
          {this.renderSps()}
        </ul>
    );
  },

  renderRole: function () {
    return (
        <li>
          <h2>{I18n.t("header.role")}</h2>
          <ul>
            <li className="user-profile-entity">
              {I18n.t("profile." + App.currentUser.principal.authorities[0].authority)}
            </li>
          </ul>
        </li>
    );
  },

  renderIdps: function () {
    return (
        <li>
          <h2>{I18n.t("header.idps")}</h2>
          <ul>
            {App.currentUser.principal.idpEntities.map(this.renderItem)}
          </ul>
        </li>
    );
  },

  renderSps: function () {
    if (!_.isEmpty(App.currentUser.principal.spEntities)) {
      return (
          <li>
            <h2>{I18n.t("header.sps")}</h2>
            <ul>
              {App.currentUser.principal.spEntities.map(this.renderItem)}
            </ul>
          </li>
      );
    }
  },

  renderItem: function (entity) {
    return (
        <li key={entity.entityId} className="user-profile-entity">
          <span>{entity.nameEn}</span>
        </li>
    );
  }


});
