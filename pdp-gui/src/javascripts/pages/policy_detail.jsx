/** @jsx React.DOM */
App.Pages.PolicyDetail = React.createClass({

  getInitialState: function () {
    var policy = this.props.policy;
    if (_.isEmpty(policy)) {
      policy = {attributes: [], denyRule: false};
    }
    return policy;
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

  submitForm: function (policy) {
    App.Controllers.Policies.saveOrUpdatePolicy(policy);
  },

  addAttribute: function (name, value) {
    this.state.attributes = this.state.attributes || [];
    this.state.attributes.push({"name": name, "value": value});
  },

  removeAttribute: function (name, value) {
    this.state.attributes = this.state.attributes || [];

    this.state.attributes = this.state.attributes.filter(function (attribute) {
      return attribute.name !== name && attribute.value !== value;
    });
  },

  isValidPolicy: function () {
    var policy = this.state;
    var inValid = _.isEmpty(policy.name) || _.isEmpty(policy.description) || _.isEmpty(policy.serviceProviderId)
        || _.isEmpty(policy.attributes) || _.isEmpty(policy.denyAdvice);
    return !inValid;
  },

  renderServiceProvider: function (policy) {
    var workflow = _.isEmpty(policy.serviceProviderId) ? "failure" : "success";
    return (
        <div>
          <div className={"form-element "+workflow}>
            <p className="label">Service Provider</p>
            <App.Components.Select2Selector
                defaultValue={policy.serviceProviderId}
                placeholder={"Select a Service Provider (required)"}
                select2selectorId={"serviceProvider"}
                options={this.parseEntities(this.props.serviceProviders, policy.serviceProviderId)}
                handleChange={this.handleChangeServiceProvider}/>
          </div>
          <div className="bottom"></div>
        </div>
    );
  },

  renderIdentityProvider: function (policy) {
    return (
        <div>
          <div className="form-element success">
            <p className="label">Identity Provider(s)</p>
            <App.Components.Select2Selector
                defaultValue={this.state.identityProviderIds}
                placeholder={"Select a Identity Provider (zero or more)"}
                select2selectorId={"identityProvider"}
                options={this.parseEntities(this.props.identityProviders, this.state.identityProviderIds)}
                multiple={true}
                handleChange={this.handleChangeIdentityProvider}/>
          </div>
          <div className="bottom"></div>
        </div>
    );
  },

  renderDenyPermitRule: function (policy) {
    var classNameSelected = policy.denyRule ? "checked" : "";
    var policyPermit = policy.denyRule ? "Deny" : "Permit";
    return (
        <div>
          <div className="form-element success" onClick={this.toggleDenyRule}>
            <p className="label">Access</p>

            <div id="ios_checkbox" className={classNameSelected + " ios-ui-select"}>
              <div className="inner"></div>
              <p>{policyPermit}</p>
            </div>
          </div>
          <div className="bottom"></div>
        </div>
    );
  },

  renderActions: function (policy) {
    var classNameSubmit = this.isValidPolicy() ? "" : "disabled";
    return (
        <div className="form-element">
          <a className={classNameSubmit + " submit c-button"} href="#" onClick={this.submitForm}>Submit</a>
          <a className="c-button cancel" href="#" onClick={this.cancelForm}>Cancel</a>
        </div>
    );
  },

  render: function () {
    var policy = this.state;
    return (
        <div className="l-center mod-policy-detail">
          <div className="l-middle form-element-container box">

            <p className="form-element form-title">Create new policy</p>
            {this.renderServiceProvider(policy)}
            {this.renderIdentityProvider(policy)}
            {this.renderDenyPermitRule(policy)}
            {this.renderActions(policy)}
          </div>
        </div>
    )
  }

});
