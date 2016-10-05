import React from "react";
import I18n from "i18n-js";
import moment from "moment";
import Link from "react-router/Link";

import { createPolicy, deletePolicy, updatePolicy, getPolicy, getAllowedAttributes, getScopedIdentityProviders, getNewPolicy, getServiceProviders } from "../api";
import { setFlash } from "../utils/flash";

import AutoFormat from "../utils/autoformat_policy";
import Flash from "../components/flash";
import PolicyAttributes from "../components/policy_attributes";
import PolicyDetailHelpEn from "../help/policy_detail_help_en";
import PolicyDetailHelpNl from "../help/policy_detail_help_nl";
import Select2Selector from "../components/select2_selector";

class PolicyDetail extends React.Component {

  // componentWillUpdate() {
  //   const node = this.getDOMNode();
  //   this.shouldScrollBottom = node.scrollTop + node.offsetHeight === node.scrollHeight;
  // }
  //
  // componentDidUpdate() {
  //   if (this.shouldScrollBottom) {
  //     const node = this.getDOMNode();
  //     node.scrollTop = node.scrollHeight;
  //   }
  // }

  constructor() {
    super();

    this.state = {
      autoFormat: false,
      policy: null,
      identityProviders: [],
      serviceProviders: [],
      allowedAttributes: []
    };
  }

  componentWillMount() {
    if (!this.props.params.id) {
      getNewPolicy().then(policy => {
        if (!this.props.params.id) {
          policy.active = true;
        }

        this.setState({ policy });
      });
    } else {
      getPolicy(this.props.params.id).then(policy => this.setState({ policy }));
    }

    getScopedIdentityProviders().then(identityProviders => this.setState({ identityProviders }));
    getServiceProviders().then(serviceProviders => this.setState({ serviceProviders }));
    getAllowedAttributes().then(allowedAttributes => this.setState({ allowedAttributes }));
  }

  toggleDenyRule() {
    const partialState = { denyRule: !this.state.policy.denyRule };
    if (!this.state.policy.denyRule) {
      partialState.allAttributesMustMatch = true;
    }
    this.setState({ policy: { ...this.state.policy, ...partialState } });
  }

  provideProviderNames(partialState) {
    const identityProvidersIds = partialState.identityProviderIds !== undefined ? partialState.identityProviderIds : this.state.policy.identityProviderIds;
    let identityProviderNames = [];

    if (!_.isEmpty(identityProvidersIds)) {
      identityProviderNames = identityProvidersIds.map(idp => {
        return I18n.entityName(_.find(this.state.identityProviders, "entityId", idp));
      });
    }

    const serviceProviderId = partialState.serviceProviderId !== undefined ? partialState.serviceProviderId : this.state.policy.serviceProviderId;
    let serviceProviderName = null;

    if (!_.isEmpty(serviceProviderId)) {
      serviceProviderName = I18n.entityName(_.find(this.state.serviceProviders, "entityId", serviceProviderId));
    }

    this.setState({ policy: { ...this.state.policy, identityProviderNames, serviceProviderName } });
  }

  parseEntities(entities) {
    const options = entities.map(entity => {
      return { value: entity.entityId, display: I18n.entityName(entity) };
    });
    return options;
  }

  handleChangeServiceProvider(newValue, newLabel) {
    this.setState({ policy: { ...this.state.policy, serviceProviderId: newValue, serviceProviderName: newLabel } });
  }


  handleChangeIdentityProvider(newValue, newLabel) {
    const { currentUser } = this.context;
    const partialState = { identityProviderIds: newValue, identityProviderNames: newLabel };
    const scopeSPs = currentUser.policyIdpAccessEnforcementRequired && _.isEmpty(newValue);
    if (scopeSPs) {
      partialState.spDataChanged = true;
    }
    this.setState({ policy: { ...this.state.policy, ...partialState } });
  }

  cancelForm() {
    if (confirm(I18n.t("policy_detail.confirmation"))) {
      this.context.router.transitionTo("/policies");
    }
  }

  deletePolicy(policy) {
    return function(e) {
      e.preventDefault();
      e.stopPropagation();
      if (confirm(I18n.t("policies.confirmation", { policyName: policy.name }))) {
        deletePolicy(policy.id).then(() => {
          setFlash(I18n.t("policies.flash", { policyName: policy.name, action: I18n.t("policies.flash_deleted") }));
          this.context.router.transitionTo("/policies");
        });
      }
    }.bind(this);
  }

  submitForm() {
    const { policy } = this.state;

    const apiCall = policy.id ? updatePolicy : createPolicy;
    const action = policy.id ? I18n.t("policies.flash_updated") : I18n.t("policies.flash_created");

    apiCall({ ...policy, description: this.renderAutoformatDescription(policy) }).then(() => {
      setFlash(I18n.t("policies.flash", { policyName: policy.name, action }));
      this.context.router.transitionTo("/policies");
    })
    .catch(e => {
      setFlash(e, "error");
    });
  }

  isValidPolicy() {
    const { policy } = this.state;
    const emptyAttributes = policy.attributes.filter(attr => {
      return _.isEmpty(attr.value);
    });
    const description = this.renderAutoformatDescription(policy);
    const inValid = _.isEmpty(policy.name) || _.isEmpty(description) || _.isEmpty(policy.serviceProviderId)
      || _.isEmpty(policy.attributes) || emptyAttributes.length > 0 || _.isEmpty(policy.denyAdvice) || _.isEmpty(policy.denyAdviceNl);
    return !inValid;
  }

  handleOnChangeName(e) {
    this.setState({ policy: { ...this.state.policy, name: e.target.value } });
  }

  handleOnChangeDescription(e) {
    this.setState({ policy: { ...this.state.policy, description: e.target.value } });
  }

  handleOnChangeAutoFormat() {
    this.setState({ autoFormat: !this.state.autoFormat });
  }

  handleOnChangeIsActive() {
    this.setState({ policy: { ...this.state.policy, active: !this.state.policy.active } });
  }

  handleOnDenyAdvice(e) {
    this.setState({ policy: { ...this.state.policy, denyAdvice: e.target.value } });
  }

  handleOnDenyAdviceNl(e) {
    this.setState({ policy: { ...this.state.policy, denyAdviceNl: e.target.value } });
  }

  renderName(policy) {
    const workflow = _.isEmpty(policy.name) ? "failure" : "success";
    return (
      <div>
        <div className={"form-element "+workflow}>
          <p className="label">{I18n.t("policy_detail.name")}</p>
          <input type="text" name="name" className="form-input" value={policy.name || ""}
            onChange={this.handleOnChangeName.bind(this)}/>
        </div>
        <div className="bottom"></div>
      </div>
    );
  }

  renderDescription(policy) {
    const description = this.renderAutoformatDescription(policy);
    const workflow = _.isEmpty(description) ? "failure" : "success";
    return (
      <div>
        <div className={"form-element "+workflow}>
          <p className="label">{I18n.t("policy_detail.description")}</p>
          <textarea rows="2" name="description" className="form-input" value={description}
            onChange={this.handleOnChangeDescription.bind(this)}/>
          <input type="checkbox" id="autoFormatDescription" name="autoFormatDescription" value={this.state.autoFormat}
            onChange={this.handleOnChangeAutoFormat.bind(this)}/>
          <label className="note" htmlFor="autoFormatDescription">{I18n.t("policy_detail.autoFormat")}</label>
        </div>
        <div className="bottom"></div>
      </div>
    );
  }

  renderAutoformatDescription(policy) {
    if (this.state.autoFormat) {
      return AutoFormat.description(policy);
    }
    return policy.description || "";
  }

  renderActive(policy) {
    const { currentUser } = this.context;

    if (!currentUser.policyIdpAccessEnforcementRequired) {
      return (
        <div>
          <div className={"form-element success"}>
            <p className="label">{I18n.t("policy_detail.isActive")}</p>
            <input type="checkbox" id="isActive" name="isActive" checked={policy.active}
              onChange={this.handleOnChangeIsActive.bind(this)}/>
            <label htmlFor="isActive">{I18n.t("policy_detail.isActiveDescription")}</label>
            <em className="note"><sup>*</sup>{I18n.t("policy_detail.isActiveInfo")} </em>
          </div>
          <div className="bottom"></div>
        </div>
      );
    }

    return null;
  }

  renderDenyAdvice(policy) {
    const workflow = (_.isEmpty(policy.denyAdvice) || _.isEmpty(policy.denyAdviceNl)) ? "failure" : "success";
    return (
      <div className={"form-element "+workflow}>
        <p className="label before-em">{I18n.t("policy_detail.deny_message")}</p>
        <em>{I18n.t("policy_detail.deny_message_info")}</em>
        <input type="text" name="denyMessage" className="form-input" value={policy.denyAdvice || ""}
          onChange={this.handleOnDenyAdvice.bind(this)}/>

        <p className="label">{I18n.t("policy_detail.deny_message_nl")}</p>
        <input type="text" name="denyMessageNl" className="form-input" value={policy.denyAdviceNl || ""}
          onChange={this.handleOnDenyAdviceNl.bind(this)}/>

        <div className="bottom"></div>
      </div>
    );
  }

  renderServiceProvider(policy) {
    const { currentUser } = this.context;
    const workflow = _.isEmpty(policy.serviceProviderId) ? "failure" : "success";
    const scopeSPs = currentUser.policyIdpAccessEnforcementRequired && _.isEmpty(policy.identityProviderIds);
    const serviceProviders = scopeSPs ? this.parseEntities(currentUser.spEntities) : this.parseEntities(this.state.serviceProviders);

    return (
      <div>
        <div className={"form-element " + workflow}>
          <p className="label">{I18n.t("policies.serviceProviderId")}</p>
          <Select2Selector
            defaultValue={policy.serviceProviderId}
            placeholder={I18n.t("policy_detail.sp_placeholder")}
            select2selectorId={"serviceProvider"}
            options={serviceProviders}
            dataChanged={policy.spDataChanged}
            handleChange={this.handleChangeServiceProvider.bind(this)}/>
          {this.renderScopedWarning(scopeSPs)}
        </div>
        <div className="bottom"></div>
      </div>
    );
  }

  renderScopedWarning(scopedSPs) {
    if (scopedSPs) {
      return (<em className="note"><sup>*</sup>{I18n.t("policy_detail.spScopeInfo")} </em>);
    }

    return null;
  }

  renderIdentityProvider(policy) {
    return (
      <div>
        <div className="form-element success">
          <p className="label">{I18n.t("policies.identityProviderIds")}</p>

          <Select2Selector
            defaultValue={policy.identityProviderIds}
            placeholder={I18n.t("policy_detail.idps_placeholder")}
            select2selectorId={"identityProvider"}
            options={this.parseEntities(this.state.identityProviders)}
            dataChanged={false}
            multiple={true}
            handleChange={this.handleChangeIdentityProvider.bind(this)}/>
        </div>
        <div className="bottom"></div>
      </div>
    );
  }

  renderDenyPermitRule(policy) {
    const classNameSelected = policy.denyRule ? "checked" : "";
    const classNamePermit = policy.denyRule ? "not-selected" : "";
    const classNameDeny = !policy.denyRule ? "not-selected" : "";
    const policyPermit = policy.denyRule ? I18n.t("policy_detail.deny") : I18n.t("policy_detail.permit");
    return (
      <div>
        <div className="form-element success">
          <div className="column-3 first">
            <p className="label">{I18n.t("policy_detail.access")}</p>

            <div id="ios_checkbox" className={classNameSelected + " ios-ui-select"} onClick={this.toggleDenyRule.bind(this)}>
              <div className="inner"></div>
              <p>{policyPermit}</p>
            </div>
          </div>
          <div className="column-3 middle">
            <p className={"info " + classNamePermit}>{I18n.t("policy_detail.permit")}</p>
            <em className={classNamePermit}>{I18n.t("policy_detail.permit_info")}</em>
          </div>
          <div className="column-3">
            <p className={"info "+classNameDeny}>{I18n.t("policy_detail.deny")}</p>
            <em className={classNameDeny}>{I18n.t("policy_detail.deny_info")}</em>
          </div>
        </div>
        <div className="bottom"></div>
      </div>
    );
  }

  handleChooseRule(value) {
    return function(e) {
      e.preventDefault();
      e.stopPropagation();
      const allAttributesMustMatch = (value === I18n.t("policy_detail.rule_and"));
      this.setState({ policy: { ...this.state.policy, allAttributesMustMatch } });
    }.bind(this);
  }

  renderRule(value, selected) {
    const className = value + " " + (selected ? "selected" : "");
    if (this.state.policy.denyRule) {
      return (
        <li key={value}>
          <span className={className}>{value}</span>
        </li>
      );
    }

    return (
      <li key={value}>
        <a href="#" className={className} onClick={this.handleChooseRule(value)}>{value}</a>
      </li>
    );
  }

  setAttributeState(newAttributeState) {
    this.setState({ policy: { ...this.state.policy, ...newAttributeState } });
  }

  renderAttributes(policy) {
    //we need state changes from the child component
    return (<PolicyAttributes
      policy={policy}
      allowedAttributes={this.state.allowedAttributes}
      setAttributeState={this.setAttributeState.bind(this)}/>);
  }

  renderLogicalRule(policy) {
    const allAttributesMustMatch = policy.allAttributesMustMatch;
    const classNameAnd = !policy.allAttributesMustMatch ? "not-selected" : "";
    const classNameOr = policy.allAttributesMustMatch ? "not-selected" : "";

    return (
      <div>
        <div className="form-element success">
          <div className="column-3 first">
            <p className="label">{I18n.t("policy_detail.rule")}</p>
            <ul className="logical-rule">
              {[
                this.renderRule(I18n.t("policy_detail.rule_and"), allAttributesMustMatch),
                this.renderRule(I18n.t("policy_detail.rule_or"), !allAttributesMustMatch)
              ]}
            </ul>
          </div>
          <div className="column-3 middle">
            <p className={"info "+classNameAnd}>{I18n.t("policy_detail.rule_and")}</p>
            <em className={classNameAnd}>{I18n.t("policy_detail.rule_and_info")}</em>
          </div>
          <div className="column-3">
            <p className={"info "+classNameOr}>{I18n.t("policy_detail.rule_or")}</p>
            <em className={classNameOr}>{I18n.t("policy_detail.rule_or_info")}</em>
          </div>
          <em className="note"><sup>*</sup>{I18n.t("policy_detail.rule_info_add")} </em>
          {this.renderDenyRuleNote()}
        </div>
        <div className="bottom"></div>
      </div>
    );
  }

  renderDenyRuleNote() {
    if (this.state.denyRule) {
      return (<em><sup>*</sup> {I18n.t("policy_detail.rule_info_add_2")}</em>);
    }

    return null;
  }

  renderActions(policy) {
    const classNameSubmit = this.isValidPolicy() ? "" : "disabled";
    return (
      <div className="form-element">
        <a className={classNameSubmit + " submit c-button"} href="#"
          onClick={this.submitForm.bind(this)}>{I18n.t("policy_detail.submit")}</a>
        <a className="c-button cancel" href="#" onClick={this.cancelForm.bind(this)}>{I18n.t("policy_detail.cancel")}</a>
        {this.renderDelete(policy)}
        {this.renderRevisionsLink(policy)}
      </div>
    );
  }

  renderDelete(policy) {
    if (policy.id) {
      return (
        <a className="c-button delete" href="#" onClick={this.deletePolicy(policy)}>{I18n.t("policies.delete")}</a>);
    }

    return null;
  }

  renderRevisionsLink(policy) {
    if (policy.id) {
      return (
        <Link className="c-button cancel pull-right" to={`/revisions/${policy.id}`}>
          {I18n.t("policies.revisions")}
        </Link>
      );
    }

    return null;
  }


  createdDate(policy) {
    const created = moment(policy.created);
    created.locale(I18n.locale);
    return created.format("LL");
  }

  render() {
    const { policy } = this.state;

    if (policy) {
      const title = policy.id ? I18n.t("policy_detail.update_policy") : I18n.t("policy_detail.create_policy");
      //var classTitle = policy.id
      const created = moment(policy.created);
      created.locale(I18n.locale);
      const subtitle = policy.id ? I18n.t("policy_detail.sub_title", {
        displayName: policy.userDisplayName,
        created: this.createdDate(policy)
      }) : "";
      const activatedSR = policy.id ?
        (policy.activatedSr ? I18n.t("policy_detail.activated_true") : I18n.t("policy_detail.activated_false")) : "";

      return (
        <div className="l-center mod-policy-detail">
          <Flash />
          <div className="l-split-left form-element-container box">
            <p className="form-element form-title sub-container">{title}<em className="sub-element">{subtitle}</em>
              <em className="sub-element second">{activatedSR}</em>
            </p>
            {this.renderName(policy)}
            {this.renderDenyPermitRule(policy)}
            {this.renderServiceProvider(policy)}
            {this.renderIdentityProvider(policy)}
            {this.renderLogicalRule(policy)}
            {this.renderAttributes(policy)}
            {this.renderDenyAdvice(policy)}
            {this.renderDescription(policy)}
            {this.renderActive(policy)}
            {this.renderActions(policy)}
          </div>
          <div className="l-split-right form-element-container box">
            {this.renderAboutPage()}
          </div>
        </div>
      );
    }

    return null;
  }

  renderAboutPage() {
    return I18n.locale === "en" ? <PolicyDetailHelpEn/> : <PolicyDetailHelpNl/>;
  }
}

PolicyDetail.contextTypes = {
  currentUser: React.PropTypes.object,
  router: React.PropTypes.object
};

PolicyDetail.propTypes = {
  params: React.PropTypes.shape({
    id: React.PropTypes.string
  })
};

export default PolicyDetail;
