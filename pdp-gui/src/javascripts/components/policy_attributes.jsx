/** @jsx React.DOM */

App.Components.PolicyAttributes = React.createClass({

  markAttributes: function (policy) {
    policy.attributes = policy.attributes.map(function (attr, index) {
      attr.index = index;
      return attr;
    });
    return policy;
  },

  getInitialState: function () {
    return this.markAttributes(this.props.policy);
  },

  componentWillReceiveProps: function (nextProps) {
    this.state = this.markAttributes(nextProps.policy);
  },

  addAttribute: function (attrName) {
    var attributes = this.state.attributes || [];
    var index = attributes.length + 1;
    attributes.push({name: attrName, value: "", index: index});
    this.props.setAttributeState({attributes: attributes});
  },

  removeAttribute: function (name) {
    var attributes = this.state.attributes || [];
    attributes = this.state.attributes.filter(function (attribute) {
      return attribute.name !== name;
    });
    this.props.setAttributeState({attributes: attributes});
  },

  preventProp: function preventProp(e) {
    e.preventDefault();
    e.stopPropagation();
  },

  handleAttributeValueChanged: function (attrName, index) {
    return function (e) {
      this.preventProp(e);
      //change attribute value
      var attributes = this.state.attributes.map(function (attr) {
        if (attr.name === attrName && attr.index === index) {
          attr.value = e.target.value;
        }
        return attr;
      });
      this.props.setAttributeState({attributes: attributes});
    }.bind(this);
  },

  handleRemoveAttributeValue: function (attrName, index) {
    return function (e) {
      this.preventProp(e);
      //remove attribute value
      var attributes = this.state.attributes.filter(function (attr) {
        return !(attr.name === attrName && attr.index === index);
      });
      this.props.setAttributeState({attributes: attributes});
    }.bind(this);
  },

  handleNewAttributeValue: function (attrName) {
    return function (e) {
      this.preventProp(e);
      //change attribute value
      this.addAttribute(attrName);
    }.bind(this);
  },

  handleNewAttribute: function (e) {
    this.preventProp(e);
    //change attribute value
    var attrName = e.target.value;
    this.addAttribute(attrName);
  },

  handleRemoveAttribute: function (attrName) {
    return function (e) {
      this.preventProp(e);
      //change attribute value
      this.removeAttribute(attrName);
    }.bind(this);
  },


  renderAttributeValue: function (attrName, attribute) {
    return (
        <div className="value-container" key={"div-" + attrName + "-" + attribute.index}>
          <input type="text" name="value" className="form-input"
                 key={attrName + "-" + attribute.index}
                 value={attribute.value}
                 placeholder="Attribute value..."
                 onChange={this.handleAttributeValueChanged(attrName, attribute.index)}/>
          <a href="#" className="remove" onClick={this.handleRemoveAttributeValue(attrName, attribute.index)}>
            <i className="fa fa-remove"></i>
          </a>
        </div>
    )
  },

  render: function () {
    var policy = this.state;
    var grouped = _.groupBy(policy.attributes, function (attr) {
      return attr.name;
    });
    var attrNames = Object.keys(grouped);
    var allowedAttributes = this.props.allowedAttributes.filter(function (attr) {
      return _.isEmpty(grouped[attr.AttributeId]);
    });
    var self = this;
    var emptyAttributes = policy.attributes.filter(function (attr) {
      return _.isEmpty(attr.value);
    });
    var validClassName = (_.isEmpty(policy.attributes) || emptyAttributes.length > 0) ? "failure" : "success";
    return (
        <div className={"form-element "+validClassName}>
          {
            attrNames.map(function (attrName, index) {
              return (
                  <div key={attrName}>
                    <p className="label">Attribute</p>

                    <div className="attribute-container">
                      <input type="text" name="attribute" className="form-input disabled" value={attrName}
                             disabled="disabled"/>
                      <a href="#" onClick={self.handleRemoveAttribute(attrName)} className="remove">
                        <i className="fa fa-remove"></i>
                      </a>
                    </div>
                    <div className="attribute-values">
                      <p className="label">Values(s)</p>
                      {
                        grouped[attrName].map(function (attribute) {
                          return self.renderAttributeValue(attrName, attribute);
                        })
                      }
                      <a href="#" onClick={self.handleNewAttributeValue(attrName)} className="plus">
                        <i className="fa fa-plus"></i>
                        Add a new value...
                      </a>
                    </div>
                  </div>
              );
            })
          }
          <p className="label">Attribute</p>
          <select value="" onChange={self.handleNewAttribute}>
            <option value="" disabled="disabled">Add new attribute....</option>
            {
              allowedAttributes.map(function (allowedAttribute) {
                return (<option value={allowedAttribute.AttributeId}
                                key={allowedAttribute.AttributeId}>{allowedAttribute.AttributeId}</option>);
              })
            }
          </select>
        </div>);

  },


});