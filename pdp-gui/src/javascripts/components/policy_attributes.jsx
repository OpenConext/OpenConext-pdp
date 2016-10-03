import React from "react";
import I18n from "i18n-js";

class PolicyAttributes extends React.Component {

  markAttributes(policy) {
    policy.attributes = policy.attributes.map((attr, index) => {
      attr.index = index;
      return attr;
    });
    return policy;
  }

  componentWillMount() {
    return this.markAttributes(this.props.policy);
  }

  componentWillReceiveProps(nextProps) {
    this.state = this.markAttributes(nextProps.policy);
  }

  addAttribute(attrName) {
    const attributes = this.state.attributes || [];
    const index = attributes.length + 1;
    attributes.push({ name: attrName, value: "", index: index });
    this.props.setAttributeState({ attributes: attributes });
  }

  removeAttribute(name) {
    let attributes = this.state.attributes || [];
    attributes = this.state.attributes.filter(attribute => {
      return attribute.name !== name;
    });
    this.props.setAttributeState({ attributes: attributes });
  }

  preventProp(e) {
    e.preventDefault();
    e.stopPropagation();
  }

  handleAttributeValueChanged(attrName, index) {
    return function(e) {
      this.preventProp(e);
      //change attribute value
      const attributes = this.state.attributes.map(attr => {
        if (attr.name === attrName && attr.index === index) {
          attr.value = e.target.value;
        }
        return attr;
      });
      this.props.setAttributeState({ attributes: attributes });
    }.bind(this);
  }

  handleRemoveAttributeValue(attrName, index) {
    return function(e) {
      this.preventProp(e);
      //remove attribute value
      const attributes = this.state.attributes.filter(attr => {
        return !(attr.name === attrName && attr.index === index);
      });
      this.props.setAttributeState({ attributes: attributes });
    }.bind(this);
  }

  handleNewAttributeValue(attrName) {
    return function(e) {
      this.preventProp(e);
      //change attribute value
      this.addAttribute(attrName);
    }.bind(this);
  }

  handleNewAttribute(e) {
    this.preventProp(e);
    //change attribute value
    const attrName = e.target.value;
    this.addAttribute(attrName);
  }

  handleRemoveAttribute(attrName) {
    return function(e) {
      this.preventProp(e);
      //change attribute value
      this.removeAttribute(attrName);
    }.bind(this);
  }

  renderAttributeInfo(attrName, index) {
    if (index !== 0) {
      return;
    }
    if ("urn:collab:sab:surfnet.nl" === attrName) {
      return (<em className="attribute-value"><sup>*</sup>{I18n.t("policy_attributes.sab_info")}</em>);
    } else if ("urn:collab:group:surfteams.nl" === attrName) {
      return (<em className="attribute-value"><sup>*</sup>{I18n.t("policy_attributes.group_info")}</em>);
    }
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
    const grouped = _.groupBy(policy.attributes, attr => {
      return attr.name;
    });
    const attrNames = Object.keys(grouped);
    const allowedAttributes = this.props.allowedAttributes.filter(attr => {
      return _.isEmpty(grouped[attr.AttributeId]);
    });
    const self = this;
    const emptyAttributes = policy.attributes.filter(attr => {
      return _.isEmpty(attr.value);
    });
    const validClassName = (_.isEmpty(policy.attributes) || emptyAttributes.length > 0) ? "failure" : "success";
    const css = this.props.css || "";
    return (
        <div className={"form-element "+css+" "+validClassName}>
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
          <p className="label">{I18n.t("policy_attributes.attribute")}</p>
          <select value="" onChange={self.handleNewAttribute}>
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

export default PolicyAttributes;
