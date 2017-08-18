import React from "react";
import I18n from "i18n-js";

import groupBy from "lodash/groupBy";
import isEmpty from "lodash/isEmpty";

import {preventProp} from "../lib/util";

class PolicyAttributes extends React.Component {

    constructor() {
        super();
        this.state = {};
    }

    markAttributes(policy) {
        policy.attributes = policy.attributes.map((attr, index) => {
            attr.index = index;
            return attr;
        });
        return policy;
    }

    componentWillMount() {
        this.setState(this.markAttributes(this.props.policy));
    }

    componentWillReceiveProps(nextProps) {
        this.setState(this.markAttributes(nextProps.policy));
    }

    addAttribute(attrName) {
        const attributes = this.state.attributes || [];
        const index = attributes.length + 1;
        attributes.push({name: attrName, value: "", index: index});
        this.props.setAttributeState({attributes: attributes});
    }

    removeAttribute(name) {
        let attributes = this.state.attributes || [];
        attributes = this.state.attributes.filter(attribute => {
            return attribute.name !== name;
        });
        this.props.setAttributeState({attributes: attributes});
    }

    handleAttributeValueChanged(attrName, index) {
        return function (e) {
            preventProp(e);
            //change attribute value
            const attributes = this.state.attributes.map(attr => {
                if (attr.name === attrName && attr.index === index) {
                    attr.value = e.target.value;
                }
                return attr;
            });
            this.props.setAttributeState({attributes: attributes});
        }.bind(this);
    }

    handleRemoveAttributeValue(attrName, index) {
        return function (e) {
            preventProp(e);
            //remove attribute value
            const attributes = this.state.attributes.filter(attr => {
                return !(attr.name === attrName && attr.index === index);
            });
            this.props.setAttributeState({attributes: attributes});
        }.bind(this);
    }

    handleNewAttributeValue(attrName) {
        return function (e) {
            preventProp(e);
            //change attribute value
            this.addAttribute(attrName);
        }.bind(this);
    }

    handleNewAttribute(e) {
        preventProp(e);
        //change attribute value
        const attrName = e.target.value;
        this.addAttribute(attrName);
    }

    handleRemoveAttribute(attrName) {
        return function (e) {
            preventProp(e);
            //change attribute value
            this.removeAttribute(attrName);
        }.bind(this);
    }

    renderAttributeInfo(attrName, index) {
        if (index !== 0) {
            return null;
        }
        if ("urn:collab:sab:surfnet.nl" === attrName) {
            return (<em className="attribute-value"><sup>*</sup>{I18n.t("policy_attributes.sab_info")}</em>);
        } else if ("urn:collab:group:surfteams.nl" === attrName) {
            return (<em className="attribute-value"><sup>*</sup>{I18n.t("policy_attributes.group_info")}</em>);
        }

        return null;
    }

    renderAttributeValue(attrName, attribute, index) {
        const className = this.renderAttributeInfo(attrName, index) === undefined ? "" : "before-em";
        return (
            <div className="value-container" key={"div-" + attrName + "-" + attribute.index}>
                <input type="text" name="value" className={"form-input " + className}
                       key={attrName + "-" + attribute.index}
                       value={attribute.value}
                       placeholder={I18n.t("policy_attributes.attribute_value_placeholder")}
                       onChange={this.handleAttributeValueChanged(attrName, attribute.index)}/>
                {this.renderAttributeInfo(attrName, index)}
                <a href="#" className="remove" onClick={this.handleRemoveAttributeValue(attrName, attribute.index)}>
                    <i className="fa fa-remove"></i>
                </a>

            </div>
        );
    }

    render() {
        const policy = this.state;
        const grouped = groupBy(policy.attributes, attr => {
            return attr.name;
        });
        const attrNames = Object.keys(grouped);
        const allowedAttributes = this.props.allowedAttributes.filter(attr => {
            return isEmpty(grouped[attr.AttributeId]);
        });
        const self = this;
        const emptyAttributes = policy.attributes.filter(attr => {
            return isEmpty(attr.value);
        });
        const validClassName = (isEmpty(policy.attributes) || emptyAttributes.length > 0) ? "failure" : "success";
        const css = this.props.css || "";
        const className = this.props.innerAttributes ? "" : "form-element " + css + " " + validClassName;
        return (
            <div className={className}>
                {
                    attrNames.map(attrName => {
                        return (
                            <div key={attrName}>

                                  <p className="label">{I18n.t("policy_attributes.attribute")}</p>

                                <div className="attribute-container">
                                    <input type="text" name="attribute" className="form-input disabled" value={attrName}
                                           disabled="disabled"/>
                                    <a href="#" onClick={self.handleRemoveAttribute(attrName)} className="remove">
                                        <i className="fa fa-remove"></i>
                                    </a>
                                </div>
                                <div className="attribute-values">
                                    <p className="label">{I18n.t("policy_attributes.values")}</p>
                                    {
                                        grouped[attrName].map((attribute, index) => {
                                            return self.renderAttributeValue(attrName, attribute, index);
                                        })
                                    }
                                    <a href="#" onClick={self.handleNewAttributeValue(attrName)} className="plus">
                                        <i className="fa fa-plus"></i>
                                        {I18n.t("policy_attributes.new_value")}
                                    </a>
                                </div>
                            </div>
                        );
                    })
                }
                {!this.props.innerAttributes &&
                <p className="label">{I18n.t("policy_attributes.attribute")}</p>}
                <select value="" onChange={self.handleNewAttribute.bind(self)}>
                    <option value="" disabled="disabled">{I18n.t("policy_attributes.new_attribute")}</option>
                    {
                        allowedAttributes.map(allowedAttribute => {
                            return (<option value={allowedAttribute.AttributeId}
                                            key={allowedAttribute.AttributeId}>{allowedAttribute.AttributeId}</option>);
                        })
                    }
                </select>
            </div>);

    }
}

PolicyAttributes.propTypes = {
    css: React.PropTypes.string,
    setAttributeState: React.PropTypes.func,
    allowedAttributes: React.PropTypes.arrayOf(React.PropTypes.shape({
        name: React.PropTypes.string
    })),
    policy: React.PropTypes.shape({
        attributes: React.PropTypes.arrayOf(React.PropTypes.shape({}))
    }),
    innerAttributes: React.PropTypes.boolean
};

export default PolicyAttributes;
