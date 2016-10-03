import React from "react";
import I18n from "i18n-js";
import Select2Selector from "../components/select2_selector";
import IdentityHelpNl from "../help/identity_help_nl";
import IdentityHelpEn from "../help/identity_help_en";

import { getIdentityProviders } from "../api";

class Identity extends React.Component {

  constructor() {
    super();

    this.state = {
      identityProviders: [],
      unspecifiedNameId: "",
      displayName: ""
    };
  }

  componentWillMount() {
    this.setState(Object.assign({}, this.props.identity));
    getIdentityProviders()
    .then(identityProviders => this.setState({ identityProviders }));
  }

  parseEntities(entities) {
    return entities.map(entity => {
      return { value: entity.entityId, display: I18n.entityName(entity) };
    });
  }

  handleChangeIdentityProvider(newValue) {
    this.setState({ idpEntityId: newValue });
  }

  submitForm() {
    this.context.changeIdentity(this.state.idpEntityId, this.state.unspecifiedNameId, this.state.displayName);
  }

  clearIdentity() {
    this.context.clearIdentity();
  }

  isValidState() {
    const inValid = _.isEmpty(this.state.idpEntityId) || _.isEmpty(this.state.unspecifiedNameId) || _.isEmpty(this.state.displayName);
    return !inValid;
  }

  handleOnChangeUnspecifiedNameId(e) {
    this.setState({ unspecifiedNameId: e.target.value });
  }

  handleOnChangeDisplayName(e) {
    this.setState({ displayName: e.target.value });
  }

  renderUnspecifiedNameId() {
    const workflow = _.isEmpty(this.state.unspecifiedNameId) ? "failure" : "success";
    return (
      <div>
        <div className={"form-element " + workflow}>
          <p className="label">{I18n.t("identity.unspecifiedNameId")}</p>
          <input type="text" name="name" className="form-input" value={this.state.unspecifiedNameId}
            onChange={this.handleOnChangeUnspecifiedNameId.bind(this)} placeholder={I18n.t("identity.unspecifiedNameIdPlaceholder")}/>
          <em className="note"><sup>*</sup>{I18n.t("identity.unspecifiedNameIdInfo")} </em>
        </div>
        <div className="bottom"></div>
      </div>
    );
  }

  renderDisplayName() {
    const workflow = _.isEmpty(this.state.displayName) ? "failure" : "success";
    return (
      <div>
        <div className={"form-element " + workflow}>
          <p className="label">{I18n.t("identity.displayName")}</p>
          <input type="text" name="name" className="form-input" value={this.state.displayName}
            onChange={this.handleOnChangeDisplayName.bind(this)}/>
          <em className="note"><sup>*</sup>{I18n.t("identity.displayNameInfo")} </em>
        </div>
        <div className="bottom"></div>
      </div>
    );
  }

  renderIdentityProvider() {
    const workflow = _.isEmpty(this.state.idpEntityId) ? "failure" : "success";
    return (
      <div>
        <div className={"form-element " + workflow}>
          <p className="label">{I18n.t("identity.idpEntityId")}</p>

          <Select2Selector
            defaultValue={this.state.idpEntityId}
            placeholder={I18n.t("identity.idpEntityIdPlaceHolder")}
            select2selectorId={"identityProvider"}
            options={this.parseEntities(this.state.identityProviders)}
            multiple={false}
            handleChange={this.handleChangeIdentityProvider.bind(this)}/>
        </div>
        <div className="bottom"></div>
      </div>
    );
  }

  renderActions() {
    const classNameSubmit = this.isValidState() ? "" : "disabled";
    return (
      <div className="form-element">
        <a className={classNameSubmit + " submit c-button"} href="#"
          onClick={this.submitForm.bind(this)}>{I18n.t("identity.submit")}</a>
        <a className="c-button white right" href="#"
          onClick={this.clearIdentity.bind(this)}>{I18n.t("identity.clear")}</a>
      </div>
    );
  }

  render() {
    return (
      <div className="l-center mod-policy-detail">
        <div className="l-split-left form-element-container box">
          <p className="form-element form-title sub-container">{I18n.t("identity.title")}<em className="sub-element">{ I18n.t("identity.subTitle")}</em></p>
          {this.renderIdentityProvider()}
          {this.renderUnspecifiedNameId()}
          {this.renderDisplayName()}
          {this.renderActions()}
        </div>
        <div className="l-split-right form-element-container box">
          {this.renderAboutPage()}
        </div>
      </div>
    );
  }

  renderAboutPage() {
    return I18n.locale === "en" ? <IdentityHelpEn/> : <IdentityHelpNl/>;
  }
}

Identity.contextTypes = {
  changeIdentity: React.PropTypes.func,
  clearIdentity: React.PropTypes.func
}

export default Identity;
