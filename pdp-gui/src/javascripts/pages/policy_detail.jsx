/** @jsx React.DOM */
App.Pages.PolicyDetail = React.createClass({

  getInitialState: function () {
    return this.props.policy;
  },

  componentWillReceiveProps: function (nextProps) {
    this.state = nextProps.policy;
  },

  toggleDenyRule: function (e) {
    this.setState({denyRule: !this.state.denyRule});
  },

  parseEntities: function (entities, filterValues) {
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
      App.Controllers.Policies.saveOrUpdatePolicy(this.state, function (jqxhr) {
        jqxhr.isConsumed = true;
        this.setState({flash: jqxhr.responseJSON.details.name});
      }.bind(this));
    } else {
      this.setState({allFieldsRequired: true});
    }
  },

  addAttribute: function (name, value) {
    var attributes = this.state.attributes || [];
    attributes.push({"name": name, "value": value});
    this.setState({attributes: attributes});
  },

  removeAttribute: function (name, value) {
    var attributes = this.state.attributes || [];
    attributes = this.state.attributes.filter(function (attribute) {
      return attribute.name !== name && attribute.value !== value;
    });
    this.setState({attributes: attributes});
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
          <em>info text</em>
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
                options={this.parseEntities(this.props.serviceProviders, policy.serviceProviderId)}
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
                options={this.parseEntities(this.props.identityProviders, policy.identityProviderIds)}
                multiple={true}
                handleChange={this.handleChangeIdentityProvider}/>
          </div>
          <div className="bottom"></div>
        </div>
    );
  }
  ,

  handleAttributeValueChanged: function (attrName, value) {
    return function (e) {
      e.preventDefault();
      e.stopPropagation();
      //change attribute value
      console.log(e.target.value);
    }.bind(this);
  },

  handleNewAttribute: function (e) {
    e.preventDefault();
    e.stopPropagation();
    //change attribute value
    console.log(e.target.value);
  },

  handleNewAttributeValue: function (e) {
    e.preventDefault();
    e.stopPropagation();
    //change attribute value
    console.log(e.target.value);
  },

  renderAllowedAttributes: function (policy) {
    var grouped = _.groupBy(policy.attributes, function (attr) {
      return attr.name;
    });
    var attrNames = Object.keys(grouped);
    var allowedAttributes = this.props.allowedAttributes;
    var self = this;
    return (
        <div className="form-element success">
          {
            attrNames.map(function (attrName, index) {
              return (
                  <div>
                    <p className="label">Attribute</p>

                    <div className="attribute-container">
                      <input type="text" name="attribute" className="form-input disabled" value={attrName}
                             disabled="disabled"/>
                      <a href="#" className="remove"><i className="fa fa-remove"></i></a>
                    </div>
                    <div className="attribute-values">
                      <p className="label">Values(s)</p>
                      {
                        grouped[attrName].map(function (attribute) {
                          return (
                              <div className="value-container">
                                <input type="text" name="value" className="form-input" value={attribute.value}
                                       onChange={self.handleAttributeValueChanged(attrName, attribute.value)}/>
                                <a href="#" className="remove"><i className="fa fa-remove"></i></a>
                              </div>
                          )
                        })
                      }
                      <input type="text" name="value" className="form-input" value=""
                             placeholder="Add a new value..."
                             onChange={self.handleNewAttributeValue}/>
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
                return (<option value={allowedAttribute.AttributeId}>{allowedAttribute.AttributeId}</option>);
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
            <div className="column-3" onClick={this.toggleDenyRule}>
              <p className="label">Access</p>

              <div id="ios_checkbox" className={classNameSelected + " ios-ui-select"}>
                <div className="inner"></div>
                <p>{policyPermit}</p>
              </div>
            </div>
            <div className="column-3">
              <p className="info">Permit</p>
              <em>ldkfglsdfng lkdsfngl sn dfglk ndsf lkg nsdflkgn dsflkgn </em>
            </div>
            <div className="column-3">
              <p className="info">Deny</p>
              <em>dfjb bskdfb kjdfg kjdsfg </em>
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
            <div className="column-3">
              <p className="label">Rule</p>
              <ul className="logical-rule">
                {[
                  this.renderRule("AND", allAttributesMustMatch),
                  this.renderRule("OR", !allAttributesMustMatch)
                ]}
              </ul>
            </div>
            <div className="column-3">
              <p className="info">AND</p>
              <em>ldkfglsdfng lkdsfngl sn dfglk ndsf lkg nsdflkgn dsflkgn </em>
            </div>
            <div className="column-3">
              <p className="info">OR</p>
              <em>dfjb bskdfb kjdfg kjdsfg </em>
            </div>
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
          <div className="flash"><p className="error">{this.state.flash}</p><a href="#" onClick={this.closeFlash}>X</a>
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
            {this.renderAllowedAttributes(policy)}
            {this.renderDenyAdvice(policy)}
            {this.renderActions(policy)}
          </div>
        </div>
    )
  }

});

