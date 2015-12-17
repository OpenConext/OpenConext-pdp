/** @jsx React.DOM */

App.Components.Header = React.createClass({
  getInitialState: function () {
    return {
      dropDownActive: false
    }
  },

  render: function () {
    //renderMeta was removed because this is not implemented yet
    return (
        <div className="mod-header">
          <h1 className="title"><a href="/">{I18n.t("header.title")}</a></h1>
          {this.renderMeta()}
        </div>
    );
  },

  renderMeta: function () {
    return (
        <div className="meta">
          <div className="name">
            {this.renderProfileLink()}
            {this.renderDropDown()}
          </div>
          <App.Components.LanguageSelector />
          <ul className="links">
            <li dangerouslySetInnerHTML={{__html: I18n.t("header.links.help_html") }}></li>
            {this.renderExitLogout()}
            <li>
              <a href="https://github.com/OpenConext/OpenConext-pdp" target="_blank">
                <img src="/images/github.png"/>
              </a>
            </li>

          </ul>
        </div>
    );
  },

  renderProfileLink: function () {
    return (
        <span>
          {I18n.t("header.welcome")}&nbsp;
          <a href="#" onClick={this.handleToggle}>
            {App.currentUser.displayName}
            {this.renderDropDownIndicator()}
          </a>
        </span>
    );
  },

  renderDropDownIndicator: function () {
    if (this.state.dropDownActive) {
      return <i className="fa fa-caret-up"/>;
    } else {
      return <i className="fa fa-caret-down"/>;
    }
  },

  renderDropDown: function () {
    if (this.state.dropDownActive) {
      return (
          <div>
            <App.Components.UserProfile />
          </div>
      );
    }
  },

  renderExitLogout: function () {
    return (
        <li><a href="#" onClick={this.stop}>{I18n.t("header.links.logout")}</a></li>
    );
  },

  stop: function() {
    var node = document.getElementById("app");
    React.unmountComponentAtNode(node);
    React.renderComponent(App.Pages.Logout(), node);
  },

  handleToggle: function (e) {
    e.preventDefault();
    e.stopPropagation();
    this.setState({dropDownActive: !this.state.dropDownActive});
  }
});
