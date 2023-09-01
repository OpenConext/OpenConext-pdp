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

    validateIPAddress = index => e => {
        preventProp(e);
        const theNotation = this.state.cidrNotations.find(notation => notation.index === index);
        ipInfo(theNotation.ipAddress, theNotation.prefix).then(ipInfo => {
            const cidrNotations = [...this.state.cidrNotations] || [];
            const notation = cidrNotations.find(notation => notation.index === index);
            notation.invalid = !ipInfo.networkAddress;
            if (ipInfo.networkAddress) {
                notation.ipInfo = ipInfo;
                notation.prefix = ipInfo.prefix;
            } else {
                notation.ipInfo = undefined;
            }
            this.props.setCidrNotationsState({cidrNotations: cidrNotations});
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

    handleNegateNotation = e => {
        this.props.setNegateCidrNotationsState(e.target.checked);
    };

    handleCidrsPrefixChanged = index => e => {
        preventProp(e);
        const cidrNotations = [...this.state.cidrNotations].map(notation => {
            if (notation.index === index) {
                notation.prefix = e.target.value;
                ipInfo(notation.ipAddress, e.target.value).then(ipInfo => {
                    const newCidrNotations = [...this.state.cidrNotations];
                    const indexNotation = newCidrNotations.find(notation => notation.index === index);
                    if (ipInfo.networkAddress) {
                        indexNotation.ipInfo = ipInfo;
                    }
                    this.props.setCidrNotationsState({cidrNotations: newCidrNotations});
                });
            }
            return notation;
        });
        this.props.setCidrNotationsState({cidrNotations: cidrNotations});
    };

    handleRemoveCidrNotation = index => e => {
        preventProp(e);
        const cidrNotations = [...this.state.cidrNotations].filter(notation => {
            return !(notation.index === index);
        });
        this.props.setCidrNotationsState({cidrNotations: cidrNotations});
    };

    renderIpInfo = ipInfo => <section className="ip-info">
        <div>
            <span className="label">{I18n.t("policy_cidr.networkAddress")}</span>
            <span>{ipInfo.networkAddress}</span>
        </div>
        <div>
            <span className="label">{I18n.t("policy_cidr.broadcastAddress")}</span>
            <span>{ipInfo.broadcastAddress}</span>
        </div>
        {ipInfo.ipv4 &&
            <div>
                <span className="label">{I18n.t("policy_cidr.capacity")}</span>
                <span>{parseInt(ipInfo.capacity).toLocaleString("nl")}</span>
            </div>}
    </section>;

    getPrefixes = notation => {
        if (notation.invalid || !notation.ipInfo || isEmpty(notation.ipInfo.networkAddress)) {
            return [];
        }
        return notation.ipInfo.ipv4 ? ipv4Prefixes : ipv6Prefixes;
    };

    renderCidrNotation = notation => {
        const prefixValues = this.getPrefixes(notation);
        const id = `negate_${notation.index}`;
        return (
            <div key={notation.index}>
                <div className="cidr-container">
                    <input type="text" className="form-input ip-address" value={notation.ipAddress}
                           onChange={this.handleCidrsIPAddressChanged(notation.index)}
                           onBlur={this.validateIPAddress(notation.index)}/>
                    <span className="slash">/</span>
                    <select className="prefix" value={notation.prefix}
                            onChange={this.handleCidrsPrefixChanged(notation.index)}>
                        {prefixValues.map(prefix => <option value={prefix} key={prefix}>{prefix}</option>)}
                    </select>
                    <a href="#" onClick={this.handleRemoveCidrNotation(notation.index)}
                       className="remove inner-right">
                        <i className="fa fa-remove"></i>
                    </a>
                </div>
                {notation.invalid && <span className="invalid">{I18n.t("policy_cidr.invalid")}</span>}
                {notation.ipInfo && this.renderIpInfo(notation.ipInfo)}
            </div>);
    };

    render() {
        const {cidrNotations} = this.state;
        const {loa} = this.props;

        return (
            <div className="all-cidrs">
                {cidrNotations.length > 0 && <div className="negate">
                    <input type="checkbox"
                           id={"negate_cidr"}
                           name={"negate_cidr"}
                           checked={loa.negateCidrNotation}
                           onChange={this.handleNegateNotation}/>
                    <label htmlFor={"negate_cidr"}>{I18n.t("policy_cidr.negate")}</label>
                </div>}
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
    setNegateCidrNotationsState: React.PropTypes.func,
    loa: React.PropTypes.shape({
        cidrs: React.PropTypes.arrayOf(React.PropTypes.shape({}))
    })
};

export default PolicyCidrs;
