import React from "react";
import I18n from "i18n-js";

class PolicyConflicts extends React.Component {

  getInitialState() {
    return {
      conflicts: this.props.conflicts,
      hideInactive: false
    };
  }

  handleOnChangeIsActive(e) {
    this.setState({ hideInactive: !this.state.hideInactive });
  }

  renderAboutPage() {
    return I18n.locale === "en" ? <App.Help.PolicyConflictsHelpEn/> : <App.Help.PolicyConflictsHelpNl/>;
  }

  renderOverview() {
    return (<div>
      <div className="filters">
        <input type="checkbox" id="hideInactive" name="hideInactive" checked={this.state.hideInactive}
               onChange={this.handleOnChangeIsActive}/>
        <label htmlFor="isActive">{I18n.t("conflicts.hide_inactive")}</label>
        <em className="note"><sup>*</sup>{I18n.t("conflicts.hide_inactive_note")} </em>
      </div>
      <p className="form-element title">{I18n.t("conflicts.title")}</p>
      {this.renderConflicts()}
    </div>);
  }

  renderConflicts() {
    const serviceProviderNames = Object.keys(this.props.conflicts);
    if (_.isEmpty(serviceProviderNames)) {
      return (<div className={"form-element split sub-container"}>{I18n.t("conflicts.no_conflicts")}</div>);
    } else {
      return serviceProviderNames.map((sp, index) => {
        return this.renderConflict(sp, index);
      });

    }
  }

  renderConflict(sp, index) {
    const policies = this.props.conflicts[sp];
    if (this.state.hideInactive && policies.filter(policy => {
      return policy.activatedSr && policy.active;
    }).length < 2) {
      return;
    }
    return (
      <div key={sp}>
        <div className="form-element split sub-container" >
          <h3>{I18n.t("conflicts.service_provider") + " : " + sp}</h3>
          <div>
            {this.renderPolicies(policies, index)}
          </div>
        </div>
        <div className="bottom"></div>
      </div>
    );

  }

  handleShowPolicyDetail(policy) {
    return function(e) {
      e.preventDefault();
      e.stopPropagation();
      page("/policy/:id", { id: policy.id });
    };
  }

  renderPolicies(policies, index) {
    return (
      <table className='table table-bordered dataTable' id={"conflicts_table_"+index}>
        <thead>
        <tr className='success'>
          <th className='conflict_policy_name'>{I18n.t("conflicts.table.name")}</th>
          <th className='conflict_idps'>{I18n.t("conflicts.table.idps")}</th>
          <th className='conflict_is_active'>{I18n.t("policies.isActive")}</th>
          <th className='conflict_is_activated_sr'>{I18n.t("policies.activatedSr")}</th>
          <th className='conflict_controls'></th>
        </tr>
        </thead>
        <tbody>
        { policies.map(policy => {
          return this.renderPolicyRow(policy);
        })}
        </tbody>
      </table>);
  }

  renderPolicyRow(policy) {
    return (
      <tr key={policy.id}>
        <td>{policy.name}</td>
        <td>{policy.identityProviderNames.join(", ")}</td>
        <td className='conflict_is_active'><input type="checkbox" defaultChecked={policy.active}
                                                disabled="true"/></td>
        <td className="conflict_is_activated_sr"><input type="checkbox" defaultChecked={policy.activatedSr}
                                                      disabled="true"/></td>
        <td className="conflict_controls">
          <a href={page.uri("/policy/:id", { id: policy.id })} onClick={this.handleShowPolicyDetail(policy)}
             data-tooltip={I18n.t("policies.edit")}> <i className="fa fa-edit"></i>
          </a>
        </td>
      </tr>);
  }

  render() {
    return (
      <div className="l-center mod-conflicts">
        <div className="l-split-left form-element-container box">
          {this.renderOverview()}
        </div>
        <div className="l-split-right form-element-container box">
          {this.renderAboutPage()}
        </div>
      </div>
    );
  }

}

export default PolicyConflicts;
