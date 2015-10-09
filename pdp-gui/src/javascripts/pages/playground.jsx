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

  handleChangeServiceProvider: function (newValue) {
    this.setState({serviceProviderId: newValue});
  },


  handleChangeIdentityProvider: function (newValue) {
    this.setState({identityProviderId: newValue});
  },

  clearForm: function () {
    page("/playground");
  },

  replayRequest: function() {
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
    var validClassName = (_.isEmpty(pdpRequest.attributes) || emptyAttributes.length > 0) ? "failure" : "success";
    var inValid = _.isEmpty(pdpRequest.serviceProviderId) || _.isEmpty(pdpRequest.identityProviderId)
        || _.isEmpty(pdpRequest.attributes) || emptyAttributes.length > 0;
    return !inValid;
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

  determineStatus: function (decision) {
    switch (decision) {
      case "Permit":
      {
        return "check";
      }
      case "Indeterminate":
      case "Deny":
      {
        return "remove";
      }
      case "NotApplicable":
      {
        return "question"
      }
      default:
      {
        throw "Unknown decision" + decision;
      }
    }
  },

  renderStatus: function (responseJSON) {
    var response = responseJSON.Response[0];
    var decision = response.Decision;
    var statusCode = response.Status.StatusCode.Value;
    var status = this.determineStatus(decision);
    return (
        <div className={"response-status " + status}>
          <i className={"fa fa-"+status + " " + status}></i>
          <section>
            <p className="status">{decision}</p>

            <p className="details">{"StatusCode" + "'" + statusCode + "'"}</p>
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
                                       options={options}/>
          </div>
      )
    }
  },

  renderJsonResponse: function (responseJSON) {
    var selectedTab = (this.state.tab || "request");
    if (selectedTab === "response") {
      return (
          <pre className="json" dangerouslySetInnerHTML={{__html: App.Utils.Json.prettyPrint(responseJSON)}}></pre>
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

  renderTabs: function (pdpRequest) {
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
          <h1>How to use the Policy Playground</h1>

          <h2>Service Provider</h2>

          <p>todo</p>

          <h2>Identity Provider</h2>

          <p>todo</p>

          <h2>Attributes</h2>

          <p>todo</p>

          <p>fully qualified team name and dummy Teams PIP</p>

          <h2>Results</h2>

          <p>todo</p>

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

