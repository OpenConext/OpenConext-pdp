require("../stylesheets/application.sass");
require("es6-promise").polyfill();
require("isomorphic-fetch");
require("lodash");

import "./locale/en";
import "./locale/nl";
import React from "react";
import { render } from "react-dom";
import Router from "react-router/BrowserRouter";
import Match from "react-router/Match";
import Miss from "react-router/Miss";
import Cookies from "js-cookie";
import I18n from "i18n-js";

import { getUserData } from "./api";
import QueryParameter from "./utils/query-parameters";

import NotFound from "./pages/not_found";
import Footer from "./components/footer";
import Header from "./components/header";
import Navigation from "./components/navigation";

class App extends React.Component {
  getChildContext() {
    return {
      currentUser: this.props.currentUser
    };
  }

  render() {
    return (
      <Router>
        <div>
          <div className="l-header">
            <Header />
            {this.renderNavigation()}
          </div>
          <Footer />
        </div>
      </Router>
    )
  }

  renderNavigation() {
    return <Navigation />;
  }
}

App.childContextTypes = {
  currentUser: React.PropTypes.object,
  router: React.PropTypes.object
};

function determineLanguage() {
  let parameterByName = QueryParameter.getParameterByName("lang");

  if (_.isEmpty(parameterByName)) {
    parameterByName = Cookies.get("lang");
  }

  I18n.locale = parameterByName ? parameterByName : "en";
}

determineLanguage();

getUserData().catch(e => {
  render(<NotFound />, document.getElementById("app"));
  throw e;
}).then(currentUser => {
  if (!currentUser) {
    render(<NotFound />, document.getElementById("app"));
  } else {
    render(<App currentUser={currentUser} />, document.getElementById("app"));
  }
});
