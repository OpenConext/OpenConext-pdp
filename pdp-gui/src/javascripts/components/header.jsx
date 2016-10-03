import React from "react";
import I18n from "i18n-js";
import { render, unmountComponentAtNode } from "react-dom";

import githubImage from "../../images/github.png";
import LanguageSelector from "./language_selector";
import UserProfile from "./user_profile";
import Logout from "../pages/logout";

class Header extends React.Component {
  constructor() {
    super();

    this.state = {
      dropDownActive: false
    };
  }

  render() {
    //renderMeta was removed because this is not implemented yet
    return (
      <div className="mod-header">
        <h1 className="title"><a href="/">{I18n.t("header.title")}</a></h1>
        {this.renderMeta()}
      </div>
    );
  }

  renderMeta() {
    return (
      <div className="meta">
        <div className="name">
          {this.renderProfileLink()}
          {this.renderDropDown()}
        </div>
        <LanguageSelector />
        <ul className="links">
          <li dangerouslySetInnerHTML={{ __html: I18n.t("header.links.help_html") }}></li>
          {this.renderExitLogout()}
          <li>
            <a href="https://github.com/OpenConext/OpenConext-pdp" target="_blank">
              <img src={githubImage}/>
            </a>
          </li>
        </ul>
      </div>
    );
  }

  renderProfileLink() {
    const { currentUser } = this.context;

    return (
      <span>
        {I18n.t("header.welcome")}&nbsp;
        <a href="#" onClick={this.handleToggle.bind(this)}>
          {currentUser.displayName}
          {this.renderDropDownIndicator()}
        </a>
      </span>
    );
  }

  renderDropDownIndicator() {
    if (this.state.dropDownActive) {
      return <i className="fa fa-caret-up"/>;
    } else {
      return <i className="fa fa-caret-down"/>;
    }
  }

  renderDropDown() {
    if (this.state.dropDownActive) {
      return (
        <div>
          <UserProfile />
        </div>
      );
    }
  }

  renderExitLogout() {
    return (
      <li><a href="#" onClick={this.stop.bind(this)}>{I18n.t("header.links.logout")}</a></li>
    );
  }

  stop(e) {
    e.preventDefault();
    const node = document.getElementById("app");
    unmountComponentAtNode(node);
    render(<Logout />, node);
  }

  handleToggle(e) {
    e.preventDefault();
    e.stopPropagation();
    this.setState({ dropDownActive: !this.state.dropDownActive });
  }
}

Header.contextTypes = {
  currentUser: React.PropTypes.object
};

export default Header;
