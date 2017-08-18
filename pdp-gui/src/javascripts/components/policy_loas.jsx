import React from "react";
import I18n from "i18n-js";
import isEmpty from "lodash/isEmpty";

import PolicyAttributes from "./policy_attributes";
import PolicyCidrs from "./policy_cidrs";
import AndOrRule from "./and_or_rule";

import {preventProp} from "../lib/util";

class PolicyLoas extends React.Component {

    constructor() {
        super();
        this.state = {};
    }

    markLoas(policy) {
        policy.loas = policy.loas.map((loa, index) => {
            loa.index = index;
            return loa;
        });
        return policy;
    }

    componentWillMount() {
        this.setState(this.markLoas(this.props.policy));
    }

    componentWillReceiveProps(nextProps) {
        this.setState(this.markLoas(nextProps.policy));
    }

    addLoa(loaName) {
        const loas = this.state.loas || [];
        const index = loas.length + 1;
        loas.push({level: loaName, allAttributesMustMatch: true, attributes: [], cidrNotations: [], index: index});
        this.props.setLoasState({loas: loas});
    }

    removeLoa(loaToRemove) {
        const loas = this.state.loas || [];
        const newLoas = loas.filter(loa => {
            return loa.index !== loaToRemove.index;
        });
        this.props.setLoasState({loas: newLoas});
    }

    handleChooseRule = index => value => {
        const allAttributesMustMatch = (value === I18n.t("policy_detail.rule_and"));
        const newLoas = [...this.state.loas];
        const theLoa = newLoas.find(loa => loa.index === index);
        theLoa.allAttributesMustMatch = allAttributesMustMatch;
        this.props.setLoasState({loas: newLoas});
    };

    handleNewLoa(e) {
        preventProp(e);
        const level = e.target.value;
        this.addLoa(level);
    }

    handleRemoveLoa = level => e => {
        preventProp(e);
        this.removeLoa(level);
    };

    setAttributeState = loa => newAttributeState => {
        const newLoas = [...this.state.loas];
        const theLoa = newLoas.find(aLoa => aLoa.index === loa.index);
        theLoa.attributes = newAttributeState.attributes;
        this.props.setLoasState({loas: newLoas});
    };

    setCidrNotationsState = loa => newCidrNotationState => {
        const newLoas = [...this.state.loas];
        const theLoa = newLoas.find(aLoa => aLoa.index === loa.index);
        theLoa.cidrNotations = newCidrNotationState.cidrNotations;
        this.props.setLoasState({loas: newLoas});
    };

    isInvalidLoa = loa => {
        const emptyAttributes = loa.attributes.filter(attr => {
            return isEmpty(attr.value);
        });
        const invalidCidrNotations = loa.cidrNotations.some(notation => !notation.ipInfo || !notation.ipInfo.networkAddress);
        return (isEmpty(loa.attributes) || emptyAttributes.length > 0) && (isEmpty(loa.cidrNotations) || invalidCidrNotations);
    };

    renderLoa = (index, loa, css) => {
        const self = this;
        return (
            <div className={`form-element no-pad-right ${css} ${this.isInvalidLoa(loa) ? "failure" : "success"}`}
                 key={`${loa.level}_${index}`}>
                <p className="label">{I18n.t("policy_loas.loa")}</p>

                <div className="loa-container">
                    <input type="text" name="loa-level" className="form-input disabled" value={loa.level}
                           disabled="disabled"/>
                    <a href="#" onClick={self.handleRemoveLoa(loa)} className="remove">
                        <i className="fa fa-remove"></i>
                    </a>
                    <hr/>
                    <AndOrRule policy={loa}
                               toggleRule={this.handleChooseRule(index)}
                               hideDetails={true}/>
                    <p className="loas-and-or-rule">{I18n.t("policy_loas.and_or_rule")}</p>
                    <PolicyAttributes policy={loa}
                                      css="no-pad-right"
                                      allowedAttributes={this.props.allowedAttributes}
                                      setAttributeState={this.setAttributeState(loa)}
                                      innerAttributes={true}/>
                    <hr/>
                    <PolicyCidrs loa={loa}
                                 setCidrNotationsState={this.setCidrNotationsState(loa)}/>
                </div>
            </div>
        );
    };

    render() {
        const policy = this.state;
        const allowedLoas = this.props.allowedLoas.filter(loa =>
            policy.loas.filter(policyLoa => policyLoa.level === loa.level).length === 0
        );
        const self = this;
        const css = this.props.css || "";
        const className = (isEmpty(policy.loas) || policy.loas.some(loa => this.isInvalidLoa(loa)))
            ? "failure" : "success";
        return (
            <div className={"form-element all-loas " + css + " " + className}>
                {
                    policy.loas.map((loa, index) => this.renderLoa(index, loa, css))
                }
                <select className="new-loa" value="" onChange={self.handleNewLoa.bind(self)}>
                    <option value="" disabled="disabled">{I18n.t("policy_loas.new_loa")}</option>
                    {
                        allowedLoas.map(allowedLoa => {
                            return (<option value={allowedLoa}
                                            key={allowedLoa}>{allowedLoa}</option>);
                        })
                    }
                </select>
            </div>);

    }
}

PolicyLoas.propTypes = {
    css: React.PropTypes.string,
    setLoasState: React.PropTypes.func,
    allowedLoas: React.PropTypes.array,
    allowedAttributes: React.PropTypes.arrayOf(React.PropTypes.shape({
        name: React.PropTypes.string
    })),
    policy: React.PropTypes.shape({
        loas: React.PropTypes.arrayOf(React.PropTypes.shape({}))
    })
};

export default PolicyLoas;
