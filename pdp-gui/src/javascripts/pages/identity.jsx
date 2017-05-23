import React from "react";
import I18n from "i18n-js";
import isEmpty from "lodash/isEmpty";

import SelectWrapper from "../components/select_wrapper";
import IdentityHelpNl from "../help/identity_help_nl";
import IdentityHelpEn from "../help/identity_help_en";

import {currentIdentity} from "../lib/identity";

import {getIdentityProviders} from "../api";

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
        this.setState(Object.assign({}, currentIdentity));
        getIdentityProviders()
            .then(identityProviders => this.setState({identityProviders}));
    }

    parseEntities(entities) {
        return entities.map(entity => {
            return {value: entity.entityId, display: I18n.entityName(entity)};
        });
    }

    handleChangeIdentityProvider(newValue) {
        this.setState({idpEntityId: newValue});
    }

    submitForm() {
        this.context.changeIdentity(this.state.idpEntityId, this.state.unspecifiedNameId, this.state.displayName);
        this.context.router.transitionTo("/policies");
    }

    clearIdentity() {
        this.context.clearIdentity();
        this.setState({
            identityProviders: [],
            unspecifiedNameId: "",
            displayName: ""
        });
    }

    isValidState() {
        const inValid = isEmpty(this.state.idpEntityId) || isEmpty(this.state.unspecifiedNameId) || isEmpty(this.state.displayName);
        return !inValid;
    }

    handleOnChangeUnspecifiedNameId(e) {
        this.setState({unspecifiedNameId: e.target.value});
    }

    handleOnChangeDisplayName(e) {
        this.setState({displayName: e.target.value});
    }

    renderUnspecifiedNameId() {
        const workflow = isEmpty(this.state.unspecifiedNameId) ? "failure" : "success";
        return (
            <div>
                <div className={"form-element " + workflow}>
                    <p className="label">{I18n.t("identity.unspecifiedNameId")}</p>
                    <input type="text" name="name" className="form-input" value={this.state.unspecifiedNameId}
                           onChange={this.handleOnChangeUnspecifiedNameId.bind(this)}
                           placeholder={I18n.t("identity.unspecifiedNameIdPlaceholder")}/>
                    <em className="note"><sup>*</sup>{I18n.t("identity.unspecifiedNameIdInfo")} </em>
                </div>
                <div className="bottom"></div>
            </div>
        );
    }

    renderDisplayName() {
        const workflow = isEmpty(this.state.displayName) ? "failure" : "success";
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
        const workflow = isEmpty(this.state.idpEntityId) ? "failure" : "success";
        return (
            <div>
                <div className={"form-element " + workflow}>
                    <p className="label">{I18n.t("identity.idpEntityId")}</p>

                    <SelectWrapper
                        defaultValue={this.state.idpEntityId}
                        placeholder={I18n.t("identity.idpEntityIdPlaceHolder")}
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
                    <p className="form-element form-title sub-container">{I18n.t("identity.title")}<em
                        className="sub-element">{ I18n.t("identity.subTitle")}</em></p>
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
    clearIdentity: React.PropTypes.func,
    router: React.PropTypes.object
};

export default Identity;
