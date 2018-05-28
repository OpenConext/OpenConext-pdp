import React from "react";
import I18n from "i18n-js";
import isEmpty from "lodash/isEmpty";

import {getPolicies, getSamlAllowedAttributes, postPdpRequest} from "../api";
import determineStatus from "../utils/status";

import SelectWrapper from "../components/select_wrapper";
import PolicyAttributes from "../components/policy_attributes";
import PolicyPlaygroundHelpEn from "../help/policy_playground_help_en";
import PolicyPlaygroundHelpNl from "../help/policy_playground_help_nl";
import CodeMirror from "../components/code_mirror";
import PolicyRevisions from "./policy_revisions";

class Playground extends React.Component {

    constructor() {
        super();

        this.state = {
            clientId: "EngineBlock",
            allowedSamlAttributes: [],
            policies: [],
            pdpRequest: {
                attributes: []
            }
        };
    }

    componentWillMount() {
        getPolicies().then(policies => this.setState({policies}));
        getSamlAllowedAttributes().then(allowedSamlAttributes => this.setState({allowedSamlAttributes}));
    }

    parseEntities(entities) {
        return entities.map(entity => ({value: entity.entityId, display: I18n.entityName(entity)}));
    }

    handleChangePolicy(newValue) {
        if (newValue) {
            const policy = this.state.policies.filter(policy => {
                return policy.id === parseInt(newValue);
            })[0];
            const idp = policy.identityProviderIds && policy.identityProviderIds.length > 0 ?
                policy.identityProviderIds[0] : null;

            const isReg = policy.type === "reg";
            let attributes = [];
            if (isReg) {
                attributes = attributes.concat(policy.attributes);
            } else {
                policy.loas.forEach(loa => {
                    attributes = attributes.concat(loa.attributes);
                    loa.cidrNotations.forEach(notation => {
                        attributes = attributes.concat([{
                            name: "urn:mace:surfnet.nl:collab:xacml-attribute:ip-address",
                            value: notation.ipAddress
                        }]);
                    });
                });
            }
            this.setState({
                pdpRequest: {
                    ...this.state.pdpRequest,
                    selectedPolicy: newValue,
                    identityProviderId: idp,
                    serviceProviderId: policy.serviceProviderId,
                    clientId: isReg ? "EngineBlock" : "Stepup",
                    attributes: attributes
                }
            });
        }
    }

    parsePolicies(policies) {
        const options = policies.map(policy => {
            return {value: policy.id, display: policy.name.trim()};
        });
        options.sort((p1, p2) => {
            return p1.display.localeCompare(p2.display);
        });
        return options;
    }

    handleChangeServiceProvider(newValue) {
        this.setState({pdpRequest: {...this.state.pdpRequest, serviceProviderId: newValue}});
    }


    handleChangeIdentityProvider(newValue) {
        this.setState({pdpRequest: {...this.state.pdpRequest, identityProviderId: newValue}});
    }

    clearForm() {
        this.setState({pdpRequest: {attributes: []}});
    }

    replayRequest() {
        postPdpRequest(JSON.parse(this.state.decisionRequestJson))
            .then(response => this.setState({responseJSON: response, tab: "response"}))
            .catch(response => this.setState({responseJSON: response, tab: "response"}));
    }

    submitForm() {
        return function () {
            const idp = this.state.pdpRequest.identityProviderId;
            const sp = this.state.pdpRequest.serviceProviderId;
            const clientId = this.state.pdpRequest.clientId;
            const decisionRequest = {
                Request: {
                    AccessSubject: {Attribute: []},
                    Resource: {
                        Attribute: [
                            {AttributeId: "SPentityID", Value: sp},
                            {AttributeId: "IDPentityID", Value: idp},
                            {AttributeId: "ClientID", Value: clientId},
                        ]
                    }
                }
            };
            const attributes = this.state.pdpRequest.attributes.map(attr => {
                return {AttributeId: attr.name, Value: attr.value};
            });
            decisionRequest.Request.AccessSubject.Attribute = attributes;
            postPdpRequest(decisionRequest).then(response => {
                this.setState({
                    decisionRequest: decisionRequest,
                    decisionRequestJson: JSON.stringify(decisionRequest, null, 3),
                    responseJSON: response,
                    tab: "response"
                });
            });
        }.bind(this);
    }

    isValidPdpRequest() {
        const {pdpRequest} = this.state;
        const emptyAttributes = pdpRequest.attributes.filter(attr => {
            return isEmpty(attr.value);
        });
        const inValid = isEmpty(pdpRequest.serviceProviderId) || isEmpty(pdpRequest.identityProviderId)
            || isEmpty(pdpRequest.attributes) || emptyAttributes.length > 0;
        return !inValid;
    }

    renderPolicies() {
        return (
            <div>
                <div className="form-element split success">
                    <p className="label before-em">{I18n.t("playground.policy")}</p>
                    <em className="label">{I18n.t("playground.policy_info")}</em>
                    <SelectWrapper
                        defaultValue={this.state.pdpRequest.selectedPolicy}
                        placeholder={I18n.t("playground.policy_search")}
                        options={this.parsePolicies(this.state.policies)}
                        handleChange={this.handleChangePolicy.bind(this)}/>
                </div>
                <div className="bottom"></div>
            </div>
        );
    }

    renderServiceProvider(pdpRequest) {
        const workflow = isEmpty(pdpRequest.serviceProviderId) ? "failure" : "success";
        return (
            <div>
                <div className={"form-element split " + workflow}>
                    <p className="label">{I18n.t("policies.serviceProviderId")}</p>
                    <SelectWrapper
                        defaultValue={pdpRequest.serviceProviderId}
                        placeholder={I18n.t("policy_detail.sp_placeholder")}
                        options={this.parseEntities(this.props.serviceProviders)}
                        handleChange={this.handleChangeServiceProvider.bind(this)}/>
                </div>
                <div className="bottom"></div>
            </div>
        );
    }

    renderClientId(pdpRequest) {
        const workflow = isEmpty(pdpRequest.serviceProviderId) ? "failure" : "success";
        return (
            <div>
                <div className={"form-element split " + workflow}>
                    <p className="label">{I18n.t("policies.serviceProviderId")}</p>
                    <SelectWrapper
                        defaultValue={pdpRequest.serviceProviderId}
                        placeholder={I18n.t("policy_detail.sp_placeholder")}
                        options={this.parseEntities(this.props.serviceProviders)}
                        handleChange={this.handleChangeServiceProvider.bind(this)}/>
                </div>
                <div className="bottom"></div>
            </div>
        );
    }

    renderIdentityProvider(pdpRequest) {
        const workflow = isEmpty(pdpRequest.identityProviderId) ? "failure" : "success";
        return (
            <div>
                <div className={"form-element split " + workflow}>
                    <p className="label">{I18n.t("policies.identityProviderId")}</p>

                    <SelectWrapper
                        defaultValue={pdpRequest.identityProviderId}
                        placeholder={I18n.t("playground.idp_placeholder")}
                        options={this.parseEntities(this.props.identityProviders)}
                        handleChange={this.handleChangeIdentityProvider.bind(this)}/>
                </div>
                <div className="bottom"></div>
            </div>
        );
    }

    setAttributeState(newAttributeState) {
        this.setState({pdpRequest: {...this.state.pdpRequest, ...newAttributeState}});
    }

    renderAttributes(pdpRequest) {
        //we need state changes from the child component
        return (<PolicyAttributes
            policy={pdpRequest}
            allowedAttributes={this.state.allowedSamlAttributes}
            setAttributeState={this.setAttributeState.bind(this)}
            css="split"/>);
    }

    renderStatus(responseJSON) {
        let decision, statusCode, status;
        if (responseJSON.Response) {
            const response = responseJSON.Response[0];
            decision = response.Decision;
            statusCode = response.Status.StatusCode.Value;
            status = determineStatus(decision);
        } else {
            decision = "Error";
            statusCode = "Unexpected error occured";
            status = "remove";
        }
        return (
            <div className={"response-status " + status}>
                <i className={"fa fa-" + status + " " + status}></i>
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

        return null;
    }

    renderActions() {
        const classNameSubmit = this.isValidPdpRequest() ? "" : "disabled";
        return (
            <div className="form-element split no-pad-right">
                <a className={classNameSubmit + " large c-button"} href="#"
                   onClick={this.submitForm(this)}><i
                    className="fa fa-refresh"></i>{I18n.t("playground.check_policies")}</a>
                <a className="c-button cancel" href="#"
                   onClick={this.clearForm.bind(this)}>{I18n.t("playground.clear_policies")}</a>
                {this.renderAdventurous()}
            </div>
        );
    }

    updateJsonRequest(newJson) {
        this.setState({decisionRequestJson: newJson});
    }

    renderJsonRequest() {
        const selectedTab = (this.state.tab || "request");
        if (selectedTab === "request") {
            const options = {
                mode: {name: "javascript", json: true},
                lineWrapping: true,
                lineNumbers: true,
                scrollbarStyle: null
            };
            return (
                <div>
                    <div className="align-center">
                        <a className="c-button full" href="#" onClick={this.replayRequest.bind(this)}>
                            <i className="fa fa-refresh"></i>{I18n.t("playground.reload_policy")}</a>
                    </div>
                    <CodeMirror value={this.state.decisionRequestJson} onChange={this.updateJsonRequest.bind(this)}
                                options={options} uniqueId="code_mirror_textarea_request"/>
                </div>
            );
        }

        return null;
    }

    renderJsonResponse(responseJSON) {
        const selectedTab = (this.state.tab || "request");
        if (selectedTab === "response") {
            const options = {
                mode: {name: "javascript", json: true},
                lineWrapping: true,
                lineNumbers: true,
                scrollbarStyle: null,
                readOnly: true
            };
            return (
                <CodeMirror value={JSON.stringify(responseJSON, null, 3)} options={options}
                            uniqueId="code_mirror_textarea_response"/>
            );
        }

        return null;
    }

    handleTabChange(tab) {
        return function (e) {
            e.preventDefault();
            e.stopPropagation();
            this.setState({tab: tab});
        }.bind(this);
    }

    renderTabs() {
        const selectedTab = (this.state.tab || "request");
        const request = (selectedTab === "request" ? "selected" : "");
        const response = (selectedTab === "response" ? "selected" : "");
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
        }

        return (
            <div className="l-split-right form-element-container box">
                {this.renderAboutPage()}
            </div>
        );
    }

    renderAboutPage() {
        return I18n.locale === "en" ? <PolicyPlaygroundHelpEn/> : <PolicyPlaygroundHelpNl/>;
    }

    render() {
        const {pdpRequest} = this.state;
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

Playground.contextTypes = {
    currentUser: React.PropTypes.object,
    router: React.PropTypes.object,
};
Playground.propTypes = {
    identityProviders: React.PropTypes.array,
    serviceProviders: React.PropTypes.array
};


export default Playground;
