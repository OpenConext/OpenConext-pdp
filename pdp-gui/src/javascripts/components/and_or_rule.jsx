import React from "react";
import I18n from "i18n-js";

import {preventProp} from "../lib/util";

class AndOrRule extends React.Component {

    handleChooseRule = value => e => {
        preventProp(e);
        this.props.toggleRule(value);
    };

    renderRule(value, selected) {
        const className = value + " " + (selected ? "selected" : "");
        if (this.props.policy.denyRule) {
            return (
                <li key={value}>
                    <span className={className}>{value}</span>
                </li>
            );
        }
        return (
            <li key={value}>
                <a href="#" className={className} onClick={this.handleChooseRule(value)}>{value}</a>
            </li>
        );
    }

    renderLogicalRule(policy) {
        const allAttributesMustMatch = policy.allAttributesMustMatch;
        const classNameAnd = !policy.allAttributesMustMatch ? "not-selected" : "";
        const classNameOr = policy.allAttributesMustMatch ? "not-selected" : "";
        const className = this.props.hideDetails ? "and-or-rule" : "form-element success";
        const ruleClassName = this.props.hideDetails ? "" : "column-3 first";
        return (
            <div>
                <div className={className}>
                    <div className={ruleClassName}>
                        <p className="label">{I18n.t("policy_detail.rule")}</p>
                        <ul className={`logical-rule ${this.props.hideDetails ? "hide-details" : ""}`}>
                            {[
                                this.renderRule(I18n.t("policy_detail.rule_and"), allAttributesMustMatch),
                                this.renderRule(I18n.t("policy_detail.rule_or"), !allAttributesMustMatch)
                            ]}
                        </ul>
                    </div>
                    {!this.props.hideDetails && <div className="column-3 middle">
                        <p className={"info " + classNameAnd}>{I18n.t("policy_detail.rule_and")}</p>
                        <em className={classNameAnd}>{I18n.t("policy_detail.rule_and_info")}</em>
                    </div>}
                    {!this.props.hideDetails && <div className="column-3">
                        <p className={"info " + classNameOr}>{I18n.t("policy_detail.rule_or")}</p>
                        <em className={classNameOr}>{I18n.t("policy_detail.rule_or_info")}</em>
                    </div>}
                    {!this.props.hideDetails &&
                    <em className="note"><sup>*</sup>{I18n.t("policy_detail.rule_info_add")} </em>}
                    {this.renderDenyRuleNote()}
                </div>
                {!this.props.hideDetails && <div className="bottom"></div>}
            </div>
        );
    }

    renderDenyRuleNote() {
        if (this.props.policy.denyRule) {
            return (<em><sup>*</sup> {I18n.t("policy_detail.rule_info_add_2")}</em>);
        }

        return null;
    }

    render() {
        const {policy} = this.props;
        return this.renderLogicalRule(policy);
    }

}

AndOrRule.propTypes = {
    policy: React.PropTypes.object,
    toggleRule: React.PropTypes.func,
    hideDetails: React.PropTypes.bool
};

export default AndOrRule;
