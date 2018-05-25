import "react-select/dist/react-select.css";
import "datatables/media/css/jquery.dataTables.css";
import "../stylesheets/application.sass";
import {polyfill} from "es6-promise";
import isEmpty from "lodash/isEmpty";
import "isomorphic-fetch";
import "lodash";

import React from "react";
import {render} from "react-dom";
import Router from "react-router/BrowserRouter";
import Match from "react-router/Match";
import Redirect from "react-router/Redirect";
import Miss from "react-router/Miss";
import Cookies from "js-cookie";
import I18n from "i18n-js";

import {getUserData, reportError} from "./api";
import QueryParameter from "./utils/query-parameters";
import {changeIdentity, clearIdentity} from "./lib/identity";

import Identity from "./pages/identity";
import NotFound from "./pages/not_found";
import Footer from "./components/footer";
import Header from "./components/header";
import Navigation from "./components/navigation";
import PolicyOverview from "./pages/policy_overview";
import PolicyRevisions from "./pages/policy_revisions";
import PolicyDetail from "./pages/policy_detail";
import PolicyViolations from "./pages/policy_violations";
import PolicyConflicts from "./pages/policy_conflicts";
import Playground from "./pages/playground";

import "./locale/en";
import "./locale/nl";

polyfill();

class App extends React.Component {
    constructor() {
        super();
        this.state = {
            currentUser: null
        };
        window.onerror = (msg, url, line, col, err) => {
            const info = err || {};
            const response = info.response || {};
            const error = {
                userAgent: navigator.userAgent,
                message: msg,
                url: url,
                line: line,
                col: col,
                error: info.message,
                stack: info.stack,
                targetUrl: response.url,
                status: response.status
            };
            reportError(error);
        };

    }

    componentWillMount() {
        this.setState({currentUser: this.props.currentUser});
    }

    getChildContext() {
        return {
            currentUser: this.state.currentUser,
            changeIdentity: this.changeIdentity.bind(this),
            clearIdentity: this.clearIdentity.bind(this)
        };
    }

    changeIdentity(idpEntityId, unspecifiedNameId, displayName) {
        changeIdentity({idpEntityId, unspecifiedNameId, displayName});
        getUserData().then(currentUser => this.setState({currentUser}));
    }

    clearIdentity() {
        clearIdentity();
        this.setState({currentUser: this.props.currentUser});
    }

    render() {
        return (
            <Router>
                <div>
                    <div className="l-header">
                        <Header />
                        {this.renderNavigation()}
                    </div>

                    <Match exactly pattern="/" render={() => {
                        return <Redirect to="/policies"/>;
                    }}/>
                    <Match exactly pattern="/identity" component={Identity}/>
                    <Match exactly pattern="/policies" component={PolicyOverview}/>
                    <Match exactly pattern="/revisions/:id" component={PolicyRevisions}/>
                    <Match exactly pattern="/new-policy" component={PolicyDetail}/>
                    <Match exactly pattern="/new-step-policy" component={PolicyDetail}/>
                    <Match exactly pattern="/policy/:id" component={PolicyDetail}/>
                    <Match exactly pattern="/violations" component={PolicyViolations}/>
                    <Match exactly pattern="/violations/:id" component={PolicyViolations}/>
                    <Match exactly pattern="/conflicts" component={PolicyConflicts}/>
                    <Match exactly pattern="/playground" component={Playground}/>
                    <Miss component={NotFound}/>
                    <Footer />
                </div>
            </Router>
        );
    }

    renderNavigation() {
        return <Navigation />;
    }
}

App.childContextTypes = {
    currentUser: React.PropTypes.object,
    router: React.PropTypes.object,
    changeIdentity: React.PropTypes.func,
    clearIdentity: React.PropTypes.func
};

App.propTypes = {
    currentUser: React.PropTypes.shape({})
};

(function determineLanguage() {
    let parameterByName = QueryParameter.getParameterByName("language");

    if (isEmpty(parameterByName)) {
        parameterByName = Cookies.get("language");
    }

    I18n.locale = parameterByName || "en";
})();

getUserData().catch(e => {
    render(<NotFound />, document.getElementById("app"));
    throw e;
}).then(currentUser => {
    if (!currentUser) {
        render(<NotFound />, document.getElementById("app"));
    } else {
        render(<App currentUser={currentUser}/>, document.getElementById("app"));
    }
});
