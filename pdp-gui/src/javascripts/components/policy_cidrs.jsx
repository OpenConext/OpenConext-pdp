import React from "react";
import I18n from "i18n-js";
import isEmpty from "lodash/isEmpty";

import {ipInfo} from "../api";
import {preventProp} from "../lib/util";

const ipv4Prefixes = [...Array(33).keys()].filter(i => i > 7);
const ipv6Prefixes = [...Array(129).keys()].filter(i => i > 31 && i % 8 === 0);

class PolicyCidrs extends React.Component {

    constructor() {
        super();
        this.state = {};
    }

    markCidrNotations(loa) {
        loa.cidrNotations = loa.cidrNotations.map((notation, index) => {
            notation.index = index;
            return notation;
        });
        return loa;
    }

    componentWillMount() {
        this.setState(this.markCidrNotations(this.props.loa));
    }

    componentWillReceiveProps(nextProps) {
        this.setState(this.markCidrNotations(nextProps.loa));
    }

    addCidrNotation = e => {
        preventProp(e);
        const cidrNotations = this.state.cidrNotations || [];
        const index = cidrNotations.length + 1;
        cidrNotations.push({ipAddress: "", prefixLength: undefined, index: index});
        this.props.setCidrNotationsState({cidrNotations: cidrNotations});
    };

    removeCidrNotation(index) {
        let cidrNotations = this.state.cidrNotations || [];
        cidrNotations = cidrNotations.filter(notation => {
            return notation.index !== index;
        });
        this.props.setCidrNotationsState({cidrNotations: cidrNotations});
    }

    validateIPAddress = index => e => {
        preventProp(e);
        const ipAddress = this.state.cidrNotations.find(notation => notation.index === index).ipAddress;
        ipInfo(ipAddress).then(ipInfo => {
            const cidrNotations = this.state.cidrNotations || [];
            const notation = cidrNotations.find(notation => notation.index === index);
            notation.invalid = !ipInfo.networkAddress;
            if (ipInfo.networkAddress) {
                notation.ipInfo = ipInfo;
            }
        });
    };

    handleCidrsIPAddressChanged = index => e => {
        preventProp(e);
        const cidrNotations = this.state.cidrNotations.map(notation => {
            if (notation.index === index) {
                notation.ipAddress = e.target.value;
            }
            return notation;
        });
        this.props.setCidrNotationsState({cidrNotations: cidrNotations});
    };

    handleCidrsPrefixChanged = index => e => {
        preventProp(e);
        const cidrNotations = this.state.cidrNotations.map(notation => {
            if (notation.index === index) {
                notation.prefix = e.target.value;
            }
            return notation;
        });
        this.props.setCidrNotationsState({cidrNotations: cidrNotations});
    };

    handleRemoveCidrNotation(index) {
        return function (e) {
            preventProp(e);
            const cidrNotations = this.state.cidrNotations.filter(notation => {
                return !(notation.index === index);
            });
            this.props.setCidrNotationsState({cidrNotations: cidrNotations});
        }.bind(this);
    }

    getPrefixes = notation => {
        if (notation.invalid || !notation.ipInfo || isEmpty(notation.ipInfo.networkAddress)) {
            return [];
        }
        return notation.ipInfo.ipv4 ? ipv4Prefixes : ipv6Prefixes;
    };

    renderCidrNotation = notation => {
        const className = notation.invalid ? "failure" : "success";
        const prefixValues = this.getPrefixes(notation);
        return (
            <div className={`form-element cidr-container ${className}`} key={notation.index}>
                <input type="text" className="ip-address" value={notation.ipAddress}
                       onChange={this.handleCidrsIPAddressChanged(notation.index)}
                       onBlur={this.validateIPAddress(notation.index)}/>
                <span className="slash">/</span>
                <select value={notation.prefix} onChange={this.handleCidrsPrefixChanged(notation.index)}>
                    {prefixValues.map(prefix => <option value={prefix} key={prefix}>{prefix}</option>)}
                </select>
                <a href="#" onClick={this.handleRemoveCidrNotation(notation.index)} className="remove">
                    <i className="fa fa-remove"></i>
                </a>
            </div>);
    };

    render() {
        const cidrNotations = this.state.cidrNotations;
        return (
            <div>
                {cidrNotations.map(notation => this.renderCidrNotation(notation))}
                <a href="#" onClick={this.addCidrNotation} className="plus">
                    <i className="fa fa-plus"></i>
                    {I18n.t("policy_cidr.add_cidr")}
                </a>
            </div>
        );
    }
}

PolicyCidrs.propTypes = {
    setCidrNotationsState: React.PropTypes.func,
    loa: React.PropTypes.shape({
        cidrs: React.PropTypes.arrayOf(React.PropTypes.shape({}))
    })
};

export default PolicyCidrs;
