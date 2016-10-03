import React from "react";
import I18n from "i18n-js";

class Identity extends React.Component {

  constructor() {
    super();

    this.state = Object.assign({}, this.props.identity);
  }

  parseEntities(entities) {
    return entities.map(function (entity) {
      return {value: entity.entityId, display: I18n.entityName(entity)};
    });
  }

  handleChangeIdentityProvider(newValue) {
    this.setState({idpEntityId: newValue});
  }

  cancelForm() {
    page("/policies");
  }

  submitForm() {
    App.changeIdentity(this.state.idpEntityId, this.state.unspecifiedNameId, this.state.displayName);
  }

  clearIdentity() {
    App.clearIdentity();
  }

  isValidState() {
    var inValid = _.isEmpty(this.state.idpEntityId) || _.isEmpty(this.state.unspecifiedNameId) || _.isEmpty(this.state.displayName);
    return !inValid;
  }

  handleOnChangeUnspecifiedNameId(e) {
    this.setState({unspecifiedNameId: e.target.value});
  }

  handleOnChangeDisplayName(e) {
    this.setState({displayName: e.target.value});
  }

  renderUnspecifiedNameId() {
    var workflow = _.isEmpty(this.state.unspecifiedNameId) ? "failure" : "success";
    return (
        <div>
          <div className={"form-element " + workflow}>
            <p className="label">{I18n.t("identity.unspecifiedNameId")}</p>
            <input type="text" name="name" className="form-input" value={this.state.unspecifiedNameId}
                   onChange={this.handleOnChangeUnspecifiedNameId} placeholder={I18n.t("identity.unspecifiedNameIdPlaceholder")}/>
            <em className="note"><sup>*</sup>{I18n.t("identity.unspecifiedNameIdInfo")} </em>
          </div>
          <div className="bottom"></div>
        </div>
    );
  }

  renderDisplayName() {
    var workflow = _.isEmpty(this.state.displayName) ? "failure" : "success";
    return (
        <div>
          <div className={"form-element " + workflow}>
            <p className="label">{I18n.t("identity.displayName")}</p>
            <input type="text" name="name" className="form-input" value={this.state.displayName}
                   onChange={this.handleOnChangeDisplayName}/>
            <em className="note"><sup>*</sup>{I18n.t("identity.displayNameInfo")} </em>
          </div>
          <div className="bottom"></div>
        </div>
    );
  }

  renderIdentityProvider() {
    var workflow = _.isEmpty(this.state.idpEntityId) ? "failure" : "success";
    return (
        <div>
          <div className={"form-element " + workflow}>
            <p className="label">{I18n.t("identity.idpEntityId")}</p>

            <App.Components.Select2Selector
                defaultValue={this.state.idpEntityId}
                placeholder={I18n.t("identity.idpEntityIdPlaceHolder")}
                select2selectorId={"identityProvider"}
                options={this.parseEntities(this.props.identityProviders)}
                multiple={false}
                handleChange={this.handleChangeIdentityProvider}/>
          </div>
          <div className="bottom"></div>
        </div>
    );
  }

  renderActions() {
    var classNameSubmit = this.isValidState() ? "" : "disabled";
    return (
        <div className="form-element">
          <a className={classNameSubmit + " submit c-button"} href="#"
             onClick={this.submitForm}>{I18n.t("identity.submit")}</a>
          <a className="c-button cancel" href="#" onClick={this.cancelForm}>{I18n.t("identity.cancel")}</a>
          <a className="c-button white right" href="#"
             onClick={this.clearIdentity}>{I18n.t("identity.clear")}</a>
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
    )
  }

  renderAboutPage: function () {
    return I18n.locale === "en" ? <App.Help.IdentityHelpEn/> : <App.Help.IdentityHelpNl/>;
  }


}


export default Identity;
