/** @jsx React.DOM */
App.Pages.Playground = React.createClass({

  componentDidMount: function () {

    var editor = CodeMirror.fromTextArea(document.getElementById("request_json"), {
      matchBrackets: true,
      autoCloseBrackets: true,
      mode: {name: "javascript", json: true},
      lineWrapping: true,
      lineNumbers: true,
      scrollbarStyle: null
    });
    
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
    return this.props.pdpRequest;
  },

  componentWillReceiveProps: function (nextProps) {
    this.state = nextProps.pdpRequest;
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

  clearForm: function () {
    page("/playground");
  },

  submitForm: function () {
    var self = this;
    /*
     * Create JSON policy request to render in the right and
     * ask the controller to post this
     * ask the controller to post a json-policy request and s
     */
    //App.Controllers.Policies.saveOrUpdatePolicy(this.state, function (jqxhr) {
    //  jqxhr.isConsumed = true;
    //  this.setState({flash: jqxhr.responseJSON.details.name});
    //}.bind(this));
  },

  isValidPdpRequest: function () {
    var pdpRequest = this.state;
    var emptyAttributes = pdpRequest.attributes.filter(function (attr) {
      return _.isEmpty(attr.value);
    });
    var validClassName = (_.isEmpty(pdpRequest.attributes) || emptyAttributes.length > 0) ? "failure" : "success";
    var inValid = _.isEmpty(pdpRequest.serviceProviderId)
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
    return (
        <div>
          <div className="form-element split success">
            <p className="label">Identity Providers</p>

            <App.Components.Select2Selector
                defaultValue={pdpRequest.identityProviderIds}
                placeholder={"Select the Identity Providers - zero or more"}
                select2selectorId={"identityProvider"}
                options={this.parseEntities(this.props.identityProviders)}
                multiple={true}
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

  renderStatus: function () {
    //if (this.state.response) {
    var status = "check" //remove
    return (
        <div className={"response-status " + status}>
          <i className={"fa fa-"+status + " " + status}></i>
          <section>
            <p className="status">Deny</p>

            <p className="details">Statuscode dfigusf kgsdf gsdfgsdfgbdfgh</p>
          </section>
        </div>
    );
    //}
  }
  ,

  renderActions: function (pdpRequest) {
    var classNameSubmit = this.isValidPdpRequest() ? "" : "disabled";
    return (
        <div className="form-element split no-pad-right">
          <a className={classNameSubmit + " large c-button"} href="#"
             onClick={this.submitForm}><i className="fa fa-refresh"></i>Check policy again</a>
          <a className="c-button cancel" href="#" onClick={this.clearForm}>Clear</a>

          <div className="adventurous">
            <i className="fa fa-location-arrow"></i>

            <p>Feeling adventurous?</p>
            <em>You can directly edit the raw source on the right.</em>
          </div>
        </div>
    );
  }
  ,

  handleJsonRequestChange: function() {

  },

  renderJsonRequest: function () {
    var selectedTab = (this.state.tab || "request");
    if (selectedTab === "request") {
      var planets = [{name: 'Earth', order: 3, stats: {life: true, mass: 5.9736 * Math.pow(10, 24)}}, {
        name: 'Saturn',
        order: 6,
        stats: {life: null, mass: 568.46 * Math.pow(10, 24)}
      }];
      //<textarea id="request_json" value={ JSON.stringify(planets, null, 3)}
      //          onChange={this.handleJsonRequestChange}/>

      return (
          <div>
            <div className="align-center">
              <a className="c-button full" href="#"
                 onClick={this.submitForm}><i className="fa fa-refresh"></i>
                Reload to apply changes made below</a>
            </div>
            <textarea id="request_json">
              {JSON.stringify(planets, null, 3)}
                      </textarea>
          </div>
      )
    }
  },

  renderJsonResponse: function () {
    var selectedTab = (this.state.tab || "request");
    if (selectedTab === "response") {
      var account = {active: true, codes: [48348, 28923, 39080], city: "London"};
      return (
          <pre className="json" dangerouslySetInnerHTML={{__html: App.Utils.Json.prettyPrint(account)}}></pre>
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
                <a href="#" >request.json</a>
              </li>
              <li className={response}onClick={this.handleTabChange("response")}>
                <i className="fa fa-file-o"></i>
                <a href="#" >response.json</a>
              </li>
            </ul>
          </div>
        </div>
    );
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
          <div className="l-split-right form-element-container box">
            {this.renderStatus()}
            {this.renderTabs()}
            {this.renderJsonRequest()}
            {this.renderJsonResponse()}
          </div>
        </div>
    )
  }

});

