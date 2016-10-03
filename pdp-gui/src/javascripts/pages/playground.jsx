import React from "react";
import I18n from "i18n-js";

class Playground extends React.Component {

  componentWillUpdate() {
    const node = this.getDOMNode();
    this.shouldScrollBottom = node.scrollTop + node.offsetHeight === node.scrollHeight;
  }

  componentDidUpdate() {
    if (this.shouldScrollBottom) {
      const node = this.getDOMNode();
      node.scrollTop = node.scrollHeight;
    }
  }

  constructor() {
    super();

    this.state = Object.assign({}, this.props.pdpRequest);
  }

  parseEntities(entities) {
    const options = entities.map(entity => {
      return { value: entity.entityId, display: I18n.entityName(entity) };
    });
    return options;
  }

  handleChangePolicy(newValue) {
    if (newValue) {
      const policy = this.props.policies.filter(policy => {
        return policy.id === parseInt(newValue);
      })[0];

      const identityProviderId = _.isEmpty(policy.identityProviderIds) ? App.currentUser.idpEntities[0].entityId : policy.identityProviderIds[0];

      this.setState({
        attributes: policy.attributes
      });
      //Unfortunately we have to set the visual representation manually as the integration with select2 is done one-way
      $("[data-select2selector-id=\"serviceProvider\"]").val(policy.serviceProviderId).trigger("change");
      $("[data-select2selector-id=\"identityProvider\"]").val(identityProviderId).trigger("change");
    }
  }

  parsePolicies(policies) {
    const options = policies.map(policy => {
      return { value: policy.id, display: policy.name.trim() };
    });
    options.sort((p1, p2) => {
      return p1.display.localeCompare(p2.display);
    });
    return options;
  }

  handleChangeServiceProvider(newValue) {
    this.setState({ serviceProviderId: newValue });
  }


  handleChangeIdentityProvider(newValue) {
    this.setState({ identityProviderId: newValue });
  }

  clearForm() {
    page("/playground");
  }

  replayRequest() {
    App.Controllers.Playground.postPdpRequest(this.state.decisionRequestJson,
        jqxhr => {
          this.setState({ responseJSON: jqxhr.responseJSON, tab: "response" });
        },
        jqxhr => {
          jqxhr.isConsumed = true;
          this.setState({ responseJSON: jqxhr.responseJSON, tab: "response" });
        });
  }

  submitForm() {
    return function(e) {
      const idp = this.state.identityProviderId;
      const sp = this.state.serviceProviderId;
      const decisionRequest = {
        Request: {
          ReturnPolicyIdList: true,
          CombinedDecision: false,
          AccessSubject: { Attribute: [] },
          Resource: { Attribute: [{ AttributeId: "SPentityID", Value: sp }, { AttributeId: "IDPentityID", Value: idp }] }
        }
      };
      const attributes = this.state.attributes.map(attr => {
        return { AttributeId: attr.name, Value: attr.value };
      });
      decisionRequest.Request.AccessSubject.Attribute = attributes;
      const json = JSON.stringify(decisionRequest);
      App.Controllers.Playground.postPdpRequest(json, jqxhr => {
        this.setState({
          decisionRequest: decisionRequest,
          decisionRequestJson: JSON.stringify(decisionRequest, null, 3),
          responseJSON: jqxhr.responseJSON,
          tab: "response"
        });
      }, jgxhr => {
        jqxhr.isConsumed = true;
        console.log(jgxhr.responseJSON.details);
      });
    }.bind(this);
  }

  isValidPdpRequest() {
    const pdpRequest = this.state;
    const emptyAttributes = pdpRequest.attributes.filter(attr => {
      return _.isEmpty(attr.value);
    });
    const inValid = _.isEmpty(pdpRequest.serviceProviderId) || _.isEmpty(pdpRequest.identityProviderId)
        || _.isEmpty(pdpRequest.attributes) || emptyAttributes.length > 0;
    return !inValid;
  }

  renderPolicies() {
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
  }

  renderServiceProvider(pdpRequest) {
    const workflow = _.isEmpty(pdpRequest.serviceProviderId) ? "failure" : "success";
    return (
        <div>
          <div className={"form-element split " + workflow}>
            <p className="label">{I18n.t("policies.serviceProviderId")}</p>
            <App.Components.Select2Selector
                defaultValue={pdpRequest.serviceProviderId}
                placeholder={I18n.t("policy_detail.sp_placeholder")}
                select2selectorId={"serviceProvider"}
                options={this.parseEntities(this.props.serviceProviders)}
                handleChange={this.handleChangeServiceProvider}/>
          </div>
          <div className="bottom"></div>
        </div>
    );
  }

  renderIdentityProvider(pdpRequest) {
    const workflow = _.isEmpty(pdpRequest.identityProviderId) ? "failure" : "success";
    return (
        <div>
          <div className={"form-element split " + workflow}>
            <p className="label">{I18n.t("policies.identityProviderId")}</p>

            <App.Components.Select2Selector
                defaultValue={pdpRequest.identityProviderId}
                placeholder={I18n.t("playground.idp_placeholder")}
                select2selectorId={"identityProvider"}
                options={this.parseEntities(this.props.identityProviders)}
                handleChange={this.handleChangeIdentityProvider}/>
          </div>
          <div className="bottom"></div>
        </div>
    );
  }

  setAttributeState(newAttributeState) {
    this.setState(newAttributeState);
  }

  renderAttributes(pdpRequest) {
    //we need state changes from the child component
    return (<App.Components.PolicyAttributes
        policy={this.state}
        allowedAttributes={this.props.allowedSamlAttributes}
        setAttributeState={this.setAttributeState}
        css="split"/>);
  }

  renderStatus(responseJSON) {
    let decision, statusCode, status;
    if (responseJSON.Response) {
      const response = responseJSON.Response[0];
      decision = response.Decision;
      statusCode = response.Status.StatusCode.Value;
      status = App.Controllers.PolicyViolations.determineStatus(decision);
    } else {
      decision = "Error";
      statusCode = "Unexpected error occured";
      status = "remove";
    }
    return (
        <div className={"response-status " + status}>
          <i className={"fa fa-"+status + " " + status}></i>
          <section>
            <p className="status">{decision}</p>

            <p className="details">{"Status code: " + "'" + statusCode + "'"}</p>
          </section>
        </div>
    );
  }

  renderAdventurous() {
    const decisionRequest = this.state.decisionRequest;
    const responseJSON = this.state.responseJSON;
    if (decisionRequest && responseJSON) {
      return (<div className="adventurous">
        <i className="fa fa-location-arrow"></i>

        <p>{I18n.t("playground.adventurous_title")}</p>
        <em>{I18n.t("playground.adventurous_text")}</em>
      </div>);
    }
  }

  renderActions(pdpRequest) {
    const classNameSubmit = this.isValidPdpRequest() ? "" : "disabled";
    return (
        <div className="form-element split no-pad-right">
          <a className={classNameSubmit + " large c-button"} href="#"
             onClick={this.submitForm(this)}><i className="fa fa-refresh"></i>{I18n.t("playground.check_policies")}</a>
          <a className="c-button cancel" href="#" onClick={this.clearForm}>{I18n.t("playground.clear_policies")}</a>
          {this.renderAdventurous()}
        </div>
    );
  }

  updateJsonRequest(newJson) {
    this.setState({ decisionRequestJson: newJson });
  }

  renderJsonRequest(decisionRequest) {
    const selectedTab = (this.state.tab || "request");
    if (selectedTab === "request") {
      const options = {
        mode: { name: "javascript", json: true },
        lineWrapping: true,
        lineNumbers: true,
        scrollbarStyle: null
      };
      return (
          <div>
            <div className="align-center">
              <a className="c-button full" href="#" onClick={this.replayRequest}>
                <i className="fa fa-refresh"></i>{I18n.t("playground.reload_policy")}</a>
            </div>
            <App.Components.CodeMirror value={this.state.decisionRequestJson} onChange={this.updateJsonRequest}
                                       options={options} uniqueId="code_mirror_textarea_request"/>
          </div>
      );
    }
  }

  renderJsonResponse(responseJSON) {

    const selectedTab = (this.state.tab || "request");
    if (selectedTab === "response") {
      const options = {
        mode: { name: "javascript", json: true },
        lineWrapping: true,
        lineNumbers: true,
        scrollbarStyle: null,
        readOnly: true
      };
      return (
          <App.Components.CodeMirror value={JSON.stringify(responseJSON, null, 3)} options={options}
                                     uniqueId="code_mirror_textarea_response"/>
      );
    }
  }

  handleTabChange(tab) {
    return function(e) {
      e.preventDefault();
      e.stopPropagation();
      this.setState({ tab: tab });
    }.bind(this);
  }

  renderTabs() {
    const selectedTab = (this.state.tab || "request");
    const request = (selectedTab == "request" ? "selected" : "");
    const response = (selectedTab == "response" ? "selected" : "");
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
  }

  renderRequestResponsePanel() {
    const decisionRequest = this.state.decisionRequest;
    const responseJSON = this.state.responseJSON;
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
      return (<div className="l-split-right form-element-container box">
        {this.renderAboutPage()}
      </div>);
    }
  }

  renderAboutPage() {
    return I18n.locale === "en" ? <App.Help.PolicyPlaygroundHelpEn/> : <App.Help.PolicyPlaygroundHelpNl/>;
  }

  render() {
    const pdpRequest = this.state;
    return (
        <div className="l-center mod-playground">
          <div className="l-split-left form-element-container box">

            <p className="form-element title">POLICY PLAYGROUND</p>
            {this.renderPolicies()}
            {this.renderServiceProvider(pdpRequest)}
            {this.renderIdentityProvider(pdpRequest)}
            {this.renderAttributes(pdpRequest)}
            {this.renderActions(pdpRequest)}
          </div>
          {this.renderRequestResponsePanel()}
        </div>
    );
  }

}


export default Playground;
