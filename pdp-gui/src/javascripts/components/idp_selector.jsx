/** @jsx React.DOM */

App.Components.IDPSelector = React.createClass({
  getInitialState: function() {
    return {
      activeIdp: (App.currentUser.switchedToIdp || App.currentUser.currentIdp).id
    }
  },

  render: function () {
    if (App.currentUser.institutionIdps.length > 0) {
      return (
        <li className="select-idp">
          <h2>{I18n.t("header.switch_idp")}</h2>
          {this.renderMenu()}
        </li>
      );
    } else {
      return null;
    }
  },

  renderMenu: function() {
    return (
      <ul>
        {App.currentUser.institutionIdps.map(this.renderItem)}
      </ul>
    );
  },

  renderItem: function(idp) {
    return (
      <li key={idp.id}>
        <a href="#" onClick={this.handleChooseIdp(idp)}>
          {this.renderActiveIndicator(idp)}
          {idp.name}
        </a>
      </li>
    );
  },

  renderActiveIndicator: function(idp) {
    if (this.state.activeIdp == idp.id) {
      return (
        <i className="fa fa-caret-right" />
      );
    } else {
      return "";
    }
  },

  handleChooseIdp: function(idp) {
    return function(e) {
      e.preventDefault();
      e.stopPropagation();
      App.Controllers.User.switchToIdp(idp, null, function() {
        this.setState({ activeIdp: idp.id });
        page.replace(window.history.state.path);
      }.bind(this));
    }.bind(this)
  }
});
