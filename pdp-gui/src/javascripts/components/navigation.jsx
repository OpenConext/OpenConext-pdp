import React from "react";
import I18n from "i18n-js";

import Spinner from "spin.js";
import spinner from "../lib/spin";

import Link from "react-router/Link";

class Navigation extends React.Component {
    constructor() {
        super();

        this.state = {
            loading: false
        };
    }

    componentWillMount() {
        spinner.onStart = () => this.setState({loading: true});
        spinner.onStop = () => this.setState({loading: false});
    }

    componentDidUpdate() {
        if (this.state.loading) {
            if (!this.spinner) {
                this.spinner = new Spinner({
                    lines: 25, // The number of lines to draw
                    length: 25, // The length of each line
                    width: 4, // The line thickness
                    radius: 20, // The radius of the inner circle
                    color: "#4DB3CF", // #rgb or #rrggbb or array of colors
                }).spin(this.spinnerNode);
            }
        } else {
            this.spinner = null;
        }
    }

    render() {
        return (
            <div className="mod-navigation">
                <ul>
                    {this.renderItem("/policies", "policies")}
                    {this.renderItem("/violations", "violations")}
                    {this.renderItem("/conflicts", "conflicts")}
                    {this.renderItem("/loas", "loas")}
                    {this.renderItem("/decisions", "decisions")}
                    {this.renderItem("/playground", "playground")}
                    {this.renderItem("/identity", "identity")}
                    {this.renderAction("/new-step-policy", "new_ssa_policy")}
                    {this.renderAction("/new-policy", "new_policy")}
                </ul>

                {this.renderSpinner()}
            </div>
        );
    }

    renderAction(href, value) {
        return (
            <li className="action"><Link to={href} className={"action"}
                                         activeClassName="active">{I18n.t("navigation." + value)}</Link></li>
        );
    }

    renderItem(href, value) {
        return (
            <li><Link to={href} activeClassName="active">{I18n.t("navigation." + value)}</Link></li>
        );
    }

    renderSpinner() {
        if (this.state.loading) {
            return <div className="spinner" ref={spinner => this.spinnerNode = spinner}/>;
        }

        return null;
    }
}

export default Navigation;
