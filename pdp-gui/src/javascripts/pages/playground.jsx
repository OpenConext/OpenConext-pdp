/** @jsx React.DOM */
App.Pages.Playground = React.createClass({

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
    return this.props.pdpRequest;
  },

  parseEntities: function (entities) {
    var options = entities.map(function (entity) {
      return {value: entity.entityId, display: entity.nameEn};
    });
    return options;
  },

  handleChangePolicy: function (newValue) {
    if (newValue) {
      var policy = this.props.policies.filter(function (policy) {
        return policy.id === parseInt(newValue);
      })[0];

      var identityProviderId = _.isEmpty(policy.identityProviderIds) ? this.props.identityProviders[0].entityId : policy.identityProviderIds[0];
      this.setState({
        attributes: policy.attributes
      });
      //Unfortunately we have to set the visual representation manually as the integration with select2 is done one-way
      $('[data-select2selector-id="serviceProvider"]').val(policy.serviceProviderId).trigger("change");
      $('[data-select2selector-id="identityProvider"]').val(identityProviderId).trigger("change");
    }
  },

  parsePolicies: function (policies) {
    var options = policies.map(function (policy) {
      return {value: policy.id, display: policy.name};
    });
    return options;
  },

  handleChangeServiceProvider: function (newValue) {
    this.setState({serviceProviderId: newValue});
  },


  handleChangeIdentityProvider: function (newValue) {
    this.setState({identityProviderId: newValue});
  },

  clearForm: function () {
    page("/playground");
  },

  replayRequest: function () {
    var decisionRequest = JSON.parse(this.state.decisionRequestJson);
    App.Controllers.Playground.postPdpRequest(decisionRequest, function (jqxhr) {
      this.setState({
        responseJSON: jqxhr.responseJSON,
        tab: "response"
      })
    }.bind(this));
  },

  submitForm: function () {
    return function (e) {
      var idp = this.state.identityProviderId;
      var sp = this.state.serviceProviderId;
      var decisionRequest = {
        Request: {
          ReturnPolicyIdList: true,
          CombinedDecision: false,
          AccessSubject: {Attribute: []},
          Resource: {Attribute: [{AttributeId: "SPentityID", Value: sp}, {AttributeId: "IDPentityID", Value: idp}]}
        }
      };
      var attributes = this.state.attributes.map(function (attr) {
        return {AttributeId: attr.name, Value: attr.value};
      });
      decisionRequest.Request.AccessSubject.Attribute = attributes;

      App.Controllers.Playground.postPdpRequest(decisionRequest, function (jqxhr) {
        console.log(jqxhr.responseJSON);
        this.setState({
          decisionRequest: decisionRequest,
          decisionRequestJson: JSON.stringify(decisionRequest, null, 3),
          responseJSON: jqxhr.responseJSON,
          tab: "response"
        })
      }.bind(this));
    }.bind(this);
  },

  isValidPdpRequest: function () {
    var pdpRequest = this.state;
    var emptyAttributes = pdpRequest.attributes.filter(function (attr) {
      return _.isEmpty(attr.value);
    });
    var inValid = _.isEmpty(pdpRequest.serviceProviderId) || _.isEmpty(pdpRequest.identityProviderId)
        || _.isEmpty(pdpRequest.attributes) || emptyAttributes.length > 0;
    return !inValid;
  },

  renderPolicies: function () {
    return (
        <div>
          <div className="form-element split success">
            <p className="label before-em">{I18n.t("playground.policy")}</p>
            <em className="label">{I18n.t("playground.policy_info")}</em>
            <App.Components.Select2Selector
                defaultValue=""
                placeholder={I18n.t("playground.policy_search")}
                select2selectorId={"policy"}
                options={this.parsePolicies(this.props.policies)}
                handleChange={this.handleChangePolicy}/>
          </div>
          <div className="bottom"></div>
        </div>
    );
  },

  renderServiceProvider: function (pdpRequest) {
    var workflow = _.isEmpty(pdpRequest.serviceProviderId) ? "failure" : "success";
    return (
        <div>
          <div className={"form-element split " + workflow}>
            <p className="label">Service Provider</p>
            <App.Components.Select2Selector
                defaultValue={pdpRequest.serviceProviderId}
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

  renderIdentityProvider: function (pdpRequest) {
    var workflow = _.isEmpty(pdpRequest.identityProviderId) ? "failure" : "success";
    return (
        <div>
          <div className={"form-element split " + workflow}>
            <p className="label">Identity Provider</p>

            <App.Components.Select2Selector
                defaultValue={pdpRequest.identityProviderId}
                placeholder={"Select the Identity Provider - required"}
                select2selectorId={"identityProvider"}
                options={this.parseEntities(this.props.identityProviders)}
                handleChange={this.handleChangeIdentityProvider}/>
          </div>
          <div className="bottom"></div>
        </div>
    );
  },

  setAttributeState: function (newAttributeState) {
    this.setState(newAttributeState);
  },

  renderAttributes: function (pdpRequest) {
    //we need state changes from the child component
    return (<App.Components.PolicyAttributes
        policy={this.state}
        allowedAttributes={this.props.allowedSamlAttributes}
        setAttributeState={this.setAttributeState}
        css="split"/>);
  },

  renderStatus: function (responseJSON) {
    var response = responseJSON.Response[0];
    var decision = response.Decision;
    var statusCode = response.Status.StatusCode.Value;
    var status = App.Controllers.PolicyViolations.determineStatus(decision);
    return (
        <div className={"response-status " + status}>
          <i className={"fa fa-"+status + " " + status}></i>
          <section>
            <p className="status">{decision}</p>

            <p className="details">{"StatusCode: " + "'" + statusCode + "'"}</p>
          </section>
        </div>
    );
  },

  renderActions: function (pdpRequest) {
    var classNameSubmit = this.isValidPdpRequest() ? "" : "disabled";
    return (
        <div className="form-element split no-pad-right">
          <a className={classNameSubmit + " large c-button"} href="#"
             onClick={this.submitForm(this)}><i className="fa fa-refresh"></i>Check policies again</a>
          <a className="c-button cancel" href="#" onClick={this.clearForm}>Clear</a>

          <div className="adventurous">
            <i className="fa fa-location-arrow"></i>

            <p>Feeling adventurous?</p>
            <em>You can directly edit the raw source on the right.</em>
          </div>
        </div>
    );
  },

  updateJsonRequest: function (newJson) {
    this.setState({decisionRequestJson: newJson});
  },

  renderJsonRequest: function (decisionRequest) {
    var selectedTab = (this.state.tab || "request");
    if (selectedTab === "request") {
      var options = {
        mode: {name: "javascript", json: true},
        lineWrapping: true,
        lineNumbers: true,
        scrollbarStyle: null
      }
      return (
          <div>
            <div className="align-center">
              <a className="c-button full" href="#" onClick={this.replayRequest}>
                <i className="fa fa-refresh"></i>Reload to apply changes made below</a>
            </div>
            <App.Components.CodeMirror value={this.state.decisionRequestJson} onChange={this.updateJsonRequest}
                                       options={options} uniqueId="code_mirror_textarea_request"/>
          </div>
      )
    }
  },

  renderJsonResponse: function (responseJSON) {

    var selectedTab = (this.state.tab || "request");
    if (selectedTab === "response") {
      var options = {
        mode: {name: "javascript", json: true},
        lineWrapping: true,
        lineNumbers: true,
        scrollbarStyle: null,
        readOnly: true
      }
      return (
          <App.Components.CodeMirror value={JSON.stringify(responseJSON, null, 3)} options={options}
                                     uniqueId="code_mirror_textarea_response"/>
      )
    }
  },

  handleTabChange: function (tab) {
    return function (e) {
      e.preventDefault();
      e.stopPropagation();
      this.setState({tab: tab});
    }.bind(this);
  },

  renderTabs: function () {
    var selectedTab = (this.state.tab || "request");
    var request = (selectedTab == "request" ? "selected" : "");
    var response = (selectedTab == "response" ? "selected" : "");
    return (
        <div>
          <div>
            <ul className="tabs">
              <li className={request} onClick={this.handleTabChange("request")}>
                <i className="fa fa-file-o"></i>
                <a href="#">request.json</a>
              </li>
              <li className={response} onClick={this.handleTabChange("response")}>
                <i className="fa fa-file-o"></i>
                <a href="#">response.json</a>
              </li>
            </ul>
          </div>
        </div>
    );
  },
  renderAboutPage: function () {
    return (
        <div className="about form-element">
          <h1>How to use the Policy Playground?</h1>

          <p>With the SURFconext Policy Administration Point (PAP) you can maintain <a
              href="https://en.wikipedia.org/wiki/XACML"
              target="_blank">XACML</a> policies to configure fine-grained access rules for your Institution and the
            connected Service Providers.</p>

          <p>This playground can be used to test your policies. Newly created / updated or deleted polices are directly
            testable.</p>

          <h2>Service Provider (SP) and Identity Provider (IdP)</h2>

          <p>Select the SP you have defined in your policy. Although you may have created the policy with no IdP or more
            then one, it
            is required to select one here. It's value will be ignored if you have chosen no IdP in your policy.</p>

          <h2>Attributes</h2>

          <p>The attributes you add and their values end up in the policy decision request that is issued to the Policy
            Definition Point (PDP).
            In this way you can test the multiple outcomes of enforcing your policies</p>

          <p>Note that if you use the attribute <em>urn:collab:group:surfteams.nl</em> and you want to test a match then
            you have to fill in the fully qualified team name inclusing the surfteams prefix.</p>

          <h2>Results</h2>

          <p>There are four possible results:</p>
          <ul>
            <li><span>Permit</span> - There was at least one applicable policy found and the Permit rule matched the
              attributes in the request
            </li>
            <li><span>Deny</span> - There was at least one applicable policy found and the attributes did not match</li>
            <li><span>Not Applicable</span> - No policy was found for the selected SP and IdP</li>
            <li><span>Indeterminate</span> - A required attribute by the Policy was not present. This can only happen
              with Deny rules.
            </li>
          </ul>
          <p>When the outcome is Permit or Not Applicable you would have been granted access.</p>
        </div>);
  },

  renderRequestResponsePanel: function () {
    var decisionRequest = this.state.decisionRequest;
    var responseJSON = this.state.responseJSON;
    if (decisionRequest && responseJSON) {
      return (
          <div className="l-split-right form-element-container box">
            {this.renderStatus(responseJSON)}
            {this.renderTabs()}
            {this.renderJsonRequest()}
            {this.renderJsonResponse(responseJSON)}
          </div>
      );
    } else {
      return (
          <div className="l-split-right form-element-container box">
            {this.renderAboutPage()}
          </div>
      );
    }
  },


  render: function () {
    var pdpRequest = this.state;
    return (
        <div className="l-center mod-playground">
          <div className="l-split-left form-element-container box">

            <p className="form-element playground-title">POLICY PLAYGROUND</p>
            {this.renderPolicies()}
            {this.renderServiceProvider(pdpRequest)}
            {this.renderIdentityProvider(pdpRequest)}
            {this.renderAttributes(pdpRequest)}
            {this.renderActions(pdpRequest)}
          </div>
          {this.renderRequestResponsePanel()}
        </div>
    )
  }

});

