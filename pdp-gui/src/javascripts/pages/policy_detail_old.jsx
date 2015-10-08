/** @jsx React.DOM */
App.Pages.PolicyDetailOld = React.createClass({

  markAttributes: function (policy) {
    policy.attributes = policy.attributes.map(function (attr, index) {
      attr.index = index;
      return attr;
    });
    return policy;
  },

  componentWillUpdate: function () {
    var node = this.getDOMNode();
    this.shouldScrollBottom = node.scrollTop + node.offsetHeight === node.scrollHeight;
  },

  componentDidUpdate: function () {
    if (this.shouldScrollBottom) {
      var node = this.getDOMNode();
      node.scrollTop = node.scrollHeight
    }
  },

  getInitialState: function () {
    return this.markAttributes(this.props.policy);
  },

  componentWillReceiveProps: function (nextProps) {
    this.state = this.markAttributes(nextProps.policy);
  },

  toggleDenyRule: function (e) {
    this.setState({denyRule: !this.state.denyRule});
  },

  parseEntities: function (entities) {
    var options = entities.map(function (entity) {
      return {value: entity.entityId, display: entity.nameEn};
    });
    return options;
  },

  handleChangeServiceProvider: function (newValue) {
    this.setState({serviceProviderId: newValue});
  },


  handleChangeIdentityProvider: function (newValue) {
    this.setState({identityProviderIds: newValue});
  },

  cancelForm: function () {
    if (confirm("Are your sure you want to leave this page?")) {
      page("/policies");
    }
  },

  submitForm: function () {
    var self = this;
    if (this.isValidPolicy()) {
      //remove attributes with empty value
      App.Controllers.Policies.saveOrUpdatePolicy(this.state, function (jqxhr) {
        jqxhr.isConsumed = true;
        this.setState({flash: jqxhr.responseJSON.details.name});
      }.bind(this));
    } else {
      this.setState({allFieldsRequired: true});
    }
  },

  isValidPolicy: function () {
    var policy = this.state;
    var inValid = _.isEmpty(policy.name) || _.isEmpty(policy.description) || _.isEmpty(policy.serviceProviderId)
        || _.isEmpty(policy.attributes) || _.isEmpty(policy.denyAdvice);
    return !inValid;
  },

  handleOnChangeName: function (e) {
    this.setState({name: e.target.value});
  },

  handleOnChangeDescription: function (e) {
    this.setState({description: e.target.value});
  },

  handleOnDenyAdvice: function (e) {
    this.setState({denyAdvice: e.target.value});
  },

  renderNameDescription: function (policy) {
    var workflow = _.isEmpty(policy.name) || _.isEmpty(policy.description) ? "failure" : "success";
    return (
        <div>
          <div className={"form-element "+workflow}>
            <p className="label">Name</p>
            <input type="text" name="name" className="form-input" value={policy.name}
                   onChange={this.handleOnChangeName}/>

            <p className="label">Description</p>
          <textarea cols="5" name="description" className="form-input" value={policy.description}
                    onChange={this.handleOnChangeDescription}/>
          </div>
          <div className="bottom"></div>
        </div>
    );
  }
  ,
  renderDenyAdvice: function (policy) {
    var workflow = _.isEmpty(policy.denyAdvice) ? "failure" : "success";
    return (
        <div className={"form-element "+workflow}>
          <p className="label before-em">Deny message</p>
          <em>This is the message displayed to the user if access is denied based on this policy.</em>
          <input type="text" name="denyMessage" className="form-input" value={policy.denyAdvice}
                 onChange={this.handleOnDenyAdvice}/>

          <div className="bottom"></div>
        </div>
    );
  }
  ,

  renderServiceProvider: function (policy) {
    var workflow = _.isEmpty(policy.serviceProviderId) ? "failure" : "success";
    return (
        <div>
          <div className={"form-element " + workflow}>
            <p className="label">Service Provider</p>
            <App.Components.Select2Selector
                defaultValue={policy.serviceProviderId}
                placeholder={"Select the Service Provider - required"}
                select2selectorId={"serviceProvider"}
                options={this.parseEntities(this.props.serviceProviders)}
                handleChange={this.handleChangeServiceProvider}/>
          </div>
          <div className="bottom"></div>
        </div>
    );
  }
  ,

  renderIdentityProvider: function (policy) {
    return (
        <div>
          <div className="form-element success">
            <p className="label">Identity Providers</p>

            <App.Components.Select2Selector
                defaultValue={policy.identityProviderIds}
                placeholder={"Select the Identity Providers - zero or more"}
                select2selectorId={"identityProvider"}
                options={this.parseEntities(this.props.identityProviders)}
                multiple={true}
                handleChange={this.handleChangeIdentityProvider}/>
          </div>
          <div className="bottom"></div>
        </div>
    );
  }
  ,
  addAttribute: function (attrName) {
    var attributes = this.state.attributes || [];
    var index = attributes.length + 1;
    attributes.push({name: attrName, value: "", index: index});
    this.setState({attributes: attributes});
  },

  removeAttribute: function (name) {
    var attributes = this.state.attributes || [];
    attributes = this.state.attributes.filter(function (attribute) {
      return attribute.name !== name;
    });
    this.setState({attributes: attributes});
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
      this.setState({attributes: attributes});
    }.bind(this);
  },

  handleRemoveAttributeValue: function (attrName, index) {
    return function (e) {
      this.preventProp(e);
      //remove attribute value
      var attributes = this.state.attributes.filter(function (attr) {
        return !(attr.name === attrName && attr.index === index);
      });
      this.setState({attributes: attributes});
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

  renderAttributes: function (policy) {
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

  renderDenyPermitRule: function (policy) {
    var classNameSelected = policy.denyRule ? "checked" : "";
    var policyPermit = policy.denyRule ? "Deny" : "Permit";
    return (
        <div>
          <div className="form-element success">
            <div className="column-3 first" onClick={this.toggleDenyRule}>
              <p className="label">Access</p>

              <div id="ios_checkbox" className={classNameSelected + " ios-ui-select"}>
                <div className="inner"></div>
                <p>{policyPermit}</p>
              </div>
            </div>
            <div className="column-3 middle">
              <p className="info">Permit</p>
              <em>Permit polices enforce that a only a successful match of the attributes defined will result in a Permit. No match will result in a Deny.
              </em>
            </div>
            <div className="column-3">
              <p className="info">Deny</p>
              <em>Deny polices are less common to use. If the attributes in the policy match those of the person trying to login then this will result in a Deny. No match will result in a Permit.
              </em>
            </div>
          </div>
          <div className="bottom"></div>
        </div>
    );
  }
  ,

  handleChooseRule: function (value) {
    return function (e) {
      e.preventDefault();
      e.stopPropagation();
      this.setState({allAttributesMustMatch: value === "AND"});
    }.bind(this);
  },

  renderRule: function (value, selected) {
    var className = value + " " + (selected ? "selected" : "");
    return (
        <li key={value}>
          <a href="#" className={className} onClick={this.handleChooseRule(value)}>{value}</a>
        </li>
    );
  },

  renderLogicalRule: function (policy) {
    var allAttributesMustMatch = policy.allAttributesMustMatch;
    return (
        <div>
          <div className="form-element success">
            <div className="column-3 first">
              <p className="label">Rule</p>
              <ul className="logical-rule">
                {[
                  this.renderRule("AND", allAttributesMustMatch),
                  this.renderRule("OR", !allAttributesMustMatch)
                ]}
              </ul>
            </div>
            <div className="column-3 middle">
              <p className="info">AND</p>
              <em>Policies with a logical AND rule enforce that all attributes defined must match those of the person trying to login.</em>
            </div>
            <div className="column-3">
              <p className="info">OR</p>
              <em>Polices defined with a logical OR only require one of the attributes to match the attributes of the person requesting access.</em>
            </div>
            <em className="note"><sup>*</sup> Note that attribute values with the same attribute name always be evaluated with the logical OR meaning only one matching value is required for this attribute.</em>
          </div>
          <div className="bottom"></div>
        </div>
    );
  },

  closeFlash: function () {
    this.setState({flash: undefined});
  }
  ,

  renderFlash: function () {
    if (this.state.flash) {
      return (
          <div className="flash"><p className="error">{this.state.flash}</p><a href="#" onClick={this.closeFlash}><i className="fa fa-remove"></i></a>
          </div>
      );
    }
  }
  ,

  renderActions: function (policy) {
    var classNameSubmit = this.isValidPolicy() ? "" : "disabled";
    return (
        <div className="form-element">
          <a className={classNameSubmit + " submit c-button"} href="#" onClick={this.submitForm}>Submit</a>
          <a className="c-button cancel" href="#" onClick={this.cancelForm}>Cancel</a>
        </div>
    );
  }
  ,

  render: function () {
    var policy = this.state;
    var title = policy.id ? "Update policy" : "Create new policy";
    return (
        <div className="l-center mod-policy-detail">
          {this.renderFlash()}
          <div className="l-middle form-element-container box">

            <p className="form-element form-title">{title}</p>
            {this.renderNameDescription(policy)}
            {this.renderDenyPermitRule(policy)}
            {this.renderServiceProvider(policy)}
            {this.renderIdentityProvider(policy)}
            {this.renderLogicalRule(policy)}
            {this.renderAttributes(policy)}
            {this.renderDenyAdvice(policy)}
            {this.renderActions(policy)}
          </div>
        </div>
    )
  }

});

