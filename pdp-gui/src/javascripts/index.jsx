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

import {getIdentityProviders, getScopedIdentityProviders, getServiceProviders, getUserData, reportError} from "./api";
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
            currentUser: null,
            loading: true,
            scopedIdentityProviders: [],
            serviceProviders: [],
            identityProviders: []
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

    componentDidMount() {
        Promise.all([getScopedIdentityProviders(), getServiceProviders(), getIdentityProviders()]).then(result => {
            this.setState({
                currentUser: this.props.currentUser,
                loading: false,
                scopedIdentityProviders: result[0],
                serviceProviders: result[1],
                identityProviders: result[2]
            });
        });
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
        const {loading, serviceProviders, identityProviders, scopedIdentityProviders} = this.state;

        if (loading) {
            return null; // render null when app is not ready yet
        }
        return (
            <Router>
                <div>
                    <div className="l-header">
                        <Header/>
                        {this.renderNavigation()}
                    </div>

                    <Match exactly pattern="/" render={() => {
                        return <Redirect to="/policies"/>;
                    }}/>
                    <Match exactly pattern="/identity"
                           render={props => {
                               return <Identity identityProviders={identityProviders} {...props}/>;
                           }}/>
                    <Match exactly pattern="/policies"
                           render={props => {
                               return <PolicyOverview {...props}/>;
                           }}/>
                    <Match exactly pattern="/revisions/:id"
                           render={props => {
                               return <PolicyRevisions {...props}/>;
                           }}/>
                    <Match exactly pattern="/new-policy"
                           render={props => {
                               return <PolicyDetail identityProviders={scopedIdentityProviders}
                                                    serviceProviders={serviceProviders} {...props}/>;
                           }}/>
                    <Match exactly pattern="/new-step-policy"
                           render={props => {
                               return <PolicyDetail identityProviders={scopedIdentityProviders}
                                                    serviceProviders={serviceProviders} {...props}/>;
                           }}/>
                    <Match exactly pattern="/policy/:id"
                           render={props => {
                               return <PolicyDetail identityProviders={this.state.scopedIdentityProviders}
                                                    serviceProviders={this.state.serviceProviders} {...props}/>;
                           }}/>
                    <Match exactly pattern="/violations"
                           render={props => {
                               return <PolicyViolations identityProviders={this.state.identityProviders}
                                                        serviceProviders={this.state.serviceProviders} {...props}/>;
                           }}/>
                    <Match exactly pattern="/violations/:id"
                           render={props => {
                               return <PolicyViolations identityProviders={this.state.identityProviders}
                                                        serviceProviders={this.state.serviceProviders} {...props}/>;
                           }}/>
                    <Match exactly pattern="/conflicts"
                           render={props => {
                               return <PolicyConflicts {...props}/>;
                           }}/>
                    <Match exactly pattern="/playground"
                           render={props => {
                               return <Playground serviceProviders={serviceProviders}
                                                  identityProviders={identityProviders} {...props}/>;
                           }}/>
                    <Miss component={NotFound}/>
                    <Footer/>
                </div>
            </Router>
        );
    }

    renderNavigation() {
        return <Navigation/>;
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
    render(<NotFound/>, document.getElementById("app"));
    throw e;
}).then(currentUser => {
    if (!currentUser) {
        render(<NotFound/>, document.getElementById("app"));
    } else {
        render(<App currentUser={currentUser}/>, document.getElementById("app"));
    }
});
