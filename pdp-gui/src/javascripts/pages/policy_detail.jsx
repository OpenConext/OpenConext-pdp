import React from "react";
import I18n from "i18n-js";

class PolicyDetail extends React.Component {

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

    const state = this.props.policy;
    if (state.id === undefined || state.id === null) {
      state.active = true;
    }
    this.state = Object.assign({}, state);
  }

  toggleDenyRule(e) {
    const partialState = { denyRule: !this.state.denyRule };
    if (!this.state.denyRule) {
      partialState.allAttributesMustMatch = true;
    }
    partialState.description = this.buildAutoFormattedDescription(partialState);
    this.setState(partialState);
  }

  provideProviderNames(partialState) {
    const identityProvidersIds = partialState.identityProviderIds !== undefined ? partialState.identityProviderIds : this.state.identityProviderIds;
    if (_.isEmpty(identityProvidersIds)) {
      this.state.identityProviderNames = [];
    } else {
      //we can safely do it like this - as nothing should be updated
      this.state.identityProviderNames = identityProvidersIds.map(idp => {
        return I18n.entityName(_.find(this.props.identityProviders, "entityId", idp));
      });

    }
    const serviceProviderId = partialState.serviceProviderId !== undefined ? partialState.serviceProviderId : this.state.serviceProviderId;
    if (_.isEmpty(serviceProviderId)) {
      this.state.serviceProviderName = null;
    } else {
      this.state.serviceProviderName = I18n.entityName(_.find(this.props.serviceProviders, "entityId", serviceProviderId));
    }
  }

  parseEntities(entities) {
    const options = entities.map(entity => {
      return { value: entity.entityId, display: I18n.entityName(entity) };
    });
    return options;
  }

  handleChangeServiceProvider(newValue) {
    const partialState = { serviceProviderId: newValue };
    partialState.description = this.buildAutoFormattedDescription(partialState);
    this.setState(partialState);
  }


  handleChangeIdentityProvider(newValue) {
    const partialState = { identityProviderIds: newValue };
    partialState.description = this.buildAutoFormattedDescription(partialState);
    const scopeSPs = App.currentUser.policyIdpAccessEnforcementRequired && _.isEmpty(newValue);
    const serviceProviders = scopeSPs ? this.parseEntities(App.currentUser.spEntities) : this.parseEntities(this.props.serviceProviders);
    if (scopeSPs) {
      if (this.state.serviceProviderId && !_.any(serviceProviders, "value", this.state.serviceProviderId)) {
        //Unfortunately we have to set the current value manually as the integration with select2 is done one-way
        const select2ServiceProvider = $("[data-select2selector-id=\"serviceProvider\"]");
        select2ServiceProvider.val("").trigger("change");
      }
      partialState.spDataChanged = true;
    }
    this.setState(partialState);
  }

  cancelForm() {
    if (confirm(I18n.t("policy_detail.confirmation"))) {
      page("/policies");
    }
  }

  deletePolicy(policy) {
    return function(e) {
      e.preventDefault();
      e.stopPropagation();
      if (confirm(I18n.t("policies.confirmation", { policyName: policy.name }))) {
        App.Controllers.Policies.deletePolicy(policy);
      }
    };
  }

  submitForm() {
    const self = this;
    App.Controllers.Policies.saveOrUpdatePolicy(this.state, jqxhr => {
      jqxhr.isConsumed = true;
      this.setState({ flash: jqxhr.responseJSON.details.name });
    });
  }

  isValidPolicy() {
    const policy = this.state;
    const emptyAttributes = policy.attributes.filter(attr => {
      return _.isEmpty(attr.value);
    });
    const inValid = _.isEmpty(policy.name) || _.isEmpty(policy.description) || _.isEmpty(policy.serviceProviderId)
      || _.isEmpty(policy.attributes) || emptyAttributes.length > 0 || _.isEmpty(policy.denyAdvice) || _.isEmpty(policy.denyAdviceNl);
    return !inValid;
  }

  handleOnChangeName(e) {
    this.setState({ name: e.target.value });
  }

  handleOnChangeDescription(e) {
    this.setState({ description: e.target.value });
  }

  handleOnChangeAutoFormat(e) {
    const partialState = { autoFormat: !this.state.autoFormat };
    if (partialState.autoFormat) {
      partialState.savedDescription = this.state.description;
      this.provideProviderNames(partialState);
      partialState.description = App.Utils.AutoFormat.description(this.state);
    } else {
      partialState.description = this.state.savedDescription || "";
    }
    this.setState(partialState);
  }

  handleOnChangeIsActive(e) {
    this.setState({ active: !this.state.active });
  }

  buildAutoFormattedDescription(partialState) {
    if (this.state.autoFormat) {
      this.provideProviderNames(partialState);
      //we don't want to merge the partialState and this.state before the update
      const policy = {
        identityProviderNames: this.state.identityProviderNames,
        serviceProviderName: this.state.serviceProviderName,
        attributes: partialState.attributes || this.state.attributes,
        denyRule: partialState.denyRule !== undefined ? partialState.denyRule : this.state.denyRule,
        allAttributesMustMatch: partialState.allAttributesMustMatch !== undefined ? partialState.allAttributesMustMatch : this.state.allAttributesMustMatch
      };
      return App.Utils.AutoFormat.description(policy);
    } else {
      return this.state.description;
    }
  }

  handleOnDenyAdvice(e) {
    this.setState({ denyAdvice: e.target.value });
  }

  handleOnDenyAdviceNl(e) {
    this.setState({ denyAdviceNl: e.target.value });
  }

  renderName(policy) {
    const workflow = _.isEmpty(policy.name) ? "failure" : "success";
    return (
      <div>
        <div className={"form-element "+workflow}>
          <p className="label">{I18n.t("policy_detail.name")}</p>
          <input type="text" name="name" className="form-input" value={policy.name}
            onChange={this.handleOnChangeName}/>
        </div>
        <div className="bottom"></div>
      </div>
    );
  }

  renderDescription(policy) {
    const workflow = _.isEmpty(policy.description) ? "failure" : "success";
    return (
      <div>
        <div className={"form-element "+workflow}>
          <p className="label">{I18n.t("policy_detail.description")}</p>
          <textarea rows="2" name="description" className="form-input" value={policy.description}
            onChange={this.handleOnChangeDescription}/>
          <input type="checkbox" id="autoFormatDescription" name="autoFormatDescription"
            onChange={this.handleOnChangeAutoFormat}/>
          <label className="note" htmlFor="autoFormatDescription">{I18n.t("policy_detail.autoFormat")}</label>
        </div>
        <div className="bottom"></div>
      </div>
    );
  }

  renderActive(policy) {
    if (!App.currentUser.policyIdpAccessEnforcementRequired) {
      return (
        <div>
          <div className={"form-element success"}>
            <p className="label">{I18n.t("policy_detail.isActive")}</p>
            <input type="checkbox" id="isActive" name="isActive" checked={policy.active}
              onChange={this.handleOnChangeIsActive}/>
            <label htmlFor="isActive">{I18n.t("policy_detail.isActiveDescription")}</label>
            <em className="note"><sup>*</sup>{I18n.t("policy_detail.isActiveInfo")} </em>
          </div>
          <div className="bottom"></div>
        </div>
      );
    }
  }

  renderDenyAdvice(policy) {
    const workflow = (_.isEmpty(policy.denyAdvice) || _.isEmpty(policy.denyAdviceNl)) ? "failure" : "success";
    return (
      <div className={"form-element "+workflow}>
        <p className="label before-em">{I18n.t("policy_detail.deny_message")}</p>
        <em>{I18n.t("policy_detail.deny_message_info")}</em>
        <input type="text" name="denyMessage" className="form-input" value={policy.denyAdvice}
          onChange={this.handleOnDenyAdvice}/>

        <p className="label">{I18n.t("policy_detail.deny_message_nl")}</p>
        <input type="text" name="denyMessageNl" className="form-input" value={policy.denyAdviceNl}
          onChange={this.handleOnDenyAdviceNl}/>

        <div className="bottom"></div>
      </div>
    );
  }

  renderServiceProvider(policy) {
    const workflow = _.isEmpty(policy.serviceProviderId) ? "failure" : "success";
    const scopeSPs = App.currentUser.policyIdpAccessEnforcementRequired && _.isEmpty(policy.identityProviderIds);
    const serviceProviders = scopeSPs ? this.parseEntities(App.currentUser.spEntities) : this.parseEntities(this.props.serviceProviders);
    return (
      <div>
        <div className={"form-element " + workflow}>
          <p className="label">{I18n.t("policies.serviceProviderId")}</p>
          <App.Components.Select2Selector
            defaultValue={policy.serviceProviderId}
            placeholder={I18n.t("policy_detail.sp_placeholder")}
            select2selectorId={"serviceProvider"}
            options={serviceProviders}
            dataChanged={policy.spDataChanged}
            handleChange={this.handleChangeServiceProvider}/>
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
  }

  renderIdentityProvider(policy) {
    return (
      <div>
        <div className="form-element success">
          <p className="label">{I18n.t("policies.identityProviderIds")}</p>

          <App.Components.Select2Selector
            defaultValue={policy.identityProviderIds}
            placeholder={I18n.t("policy_detail.idps_placeholder")}
            select2selectorId={"identityProvider"}
            options={this.parseEntities(this.props.identityProviders)}
            dataChanged={false}
            multiple={true}
            handleChange={this.handleChangeIdentityProvider}/>
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

            <div id="ios_checkbox" className={classNameSelected + " ios-ui-select"} onClick={this.toggleDenyRule}>
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
      const partialState = { allAttributesMustMatch: allAttributesMustMatch };
      partialState.description = this.buildAutoFormattedDescription(partialState);
      this.setState(partialState);
    }.bind(this);
  }

  renderRule(value, selected) {
    const className = value + " " + (selected ? "selected" : "");
    if (this.state.denyRule) {
      return (
        <li key={value}>
          <span className={className}>{value}</span>
        </li>
      );
    } else {
      return (
        <li key={value}>
          <a href="#" className={className} onClick={this.handleChooseRule(value)}>{value}</a>
        </li>
      );
    }
  }

  handleShowRevisions(policy) {
    return function(e) {
      e.preventDefault();
      e.stopPropagation();
      page("/revisions/:id", { id: policy.id });
    };
  }

  setAttributeState(newAttributeState) {
    newAttributeState.description = this.buildAutoFormattedDescription(newAttributeState);
    this.setState(newAttributeState);
  }

  renderAttributes(policy) {
    //we need state changes from the child component
    return (<App.Components.PolicyAttributes
      policy={this.state}
      allowedAttributes={this.props.allowedAttributes}
      setAttributeState={this.setAttributeState}/>);
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
  }

  closeFlash() {
    this.setState({ flash: undefined });
  }

  renderFlash() {
    if (this.state.flash) {
      return (
        <div className="flash full"><p className="error">{this.state.flash}</p><a href="#"
            onClick={this.closeFlash}><i
              className="fa fa-remove"></i></a>
        </div>
      );
    }
  }

  renderActions(policy) {
    const classNameSubmit = this.isValidPolicy() ? "" : "disabled";
    return (
      <div className="form-element">
        <a className={classNameSubmit + " submit c-button"} href="#"
          onClick={this.submitForm}>{I18n.t("policy_detail.submit")}</a>
        <a className="c-button cancel" href="#" onClick={this.cancelForm}>{I18n.t("policy_detail.cancel")}</a>
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
  }

  renderRevisionsLink(policy) {
    const numberOfRevisions = (policy.numberOfRevisions + 1);
    if (policy.id) {
      return (<a className="c-button cancel pull-right" href={page.uri("/revisions/:id",{ id:policy.id })}
        onClick={this.handleShowRevisions(policy)}>{I18n.t("policies.revisions")}</a>);
    }
  }


  createdDate(policy) {
    const created = moment(policy.created);
    created.locale(I18n.locale);
    return created.format("LL");
  }

  render() {
    const policy = this.state;
    const title = policy.id ? I18n.t("policy_detail.update_policy") : I18n.t("policy_detail.create_policy");
    //var classTitle = policy.id
    const created = moment(policy.created);
    created.locale(I18n.locale);
    const date = created.format("LLLL");
    const subtitle = policy.id ? I18n.t("policy_detail.sub_title", {
      displayName: policy.userDisplayName,
      created: this.createdDate(policy)
    }) : "";
    const activatedSR = policy.id ?
      (policy.activatedSr ? I18n.t("policy_detail.activated_true") : I18n.t("policy_detail.activated_false")) : "";
    return (
      <div className="l-center mod-policy-detail">
        {this.renderFlash()}
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

  renderAboutPage() {
    return I18n.locale === "en" ? <App.Help.PolicyDetailHelpEn/> : <App.Help.PolicyDetailHelpNl/>;
  }


}


export default PolicyDetail;
