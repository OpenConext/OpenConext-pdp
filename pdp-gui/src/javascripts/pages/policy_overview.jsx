import React from "react";
import I18n from "i18n-js";

class PolicyOverview extends React.Component {

  constructor() {
    super();

    this.state = { data: [] };
  }

  destroyDataTable() {
    $("#policies_table").DataTable().destroy();
  }

  initDataTable() {
    $.fn.dataTable.ext.order["dom-checkbox"] = function(settings, col) {
      return this.api().column(col, { order: "index" }).nodes().map((td, i) => {
        return $("input", td).prop("checked") ? "1" : "0";
      });
    };
    $("#policies_table").DataTable({
      paging: true,
      language: {
        search: "_INPUT_",
        searchPlaceholder: I18n.t("policies.search"),
        lengthMenu: I18n.t("datatable.lengthMenu"),
        zeroRecords: I18n.t("datatable.zeroRecords"),
        infoEmpty: I18n.t("datatable.infoEmpty"),
        info: I18n.t("datatable.info"),
        paginate: {
          first: I18n.t("datatable.paginate_first"),
          previous: I18n.t("datatable.paginate_previous"),
          next: I18n.t("datatable.paginate_next"),
          last: I18n.t("datatable.paginate_last")
        }
      },
      columnDefs: [
        { targets: [3,6], orderDataType: "dom-checkbox" },
        { targets: [8], orderable: false }
      ]
    });
  }

  componentWillReceiveProps(nextProps) {
    this.destroyDataTable();
    if (!_.isEmpty(this.props) && this.props.flash !== nextProps.flash) {
      this.setState({ hideFlash: false });
    }
  }

  componentDidUpdate(prevProps, prevState) {
    if (!$.fn.DataTable.isDataTable("#policies_table")) {
      this.initDataTable();
    }
    // not the react way, but we don't control datatables as we should
    if (this.props.policies.length === 0) {
      $("#policies_table_paginate").hide();
    } else {
      $("#policies_table_paginate").show();
    }
  }

  componentDidMount() {
    this.initDataTable();
  }

  componentWillUnmount() {
    this.destroyDataTable();
  }

  handleDeletePolicyDetail(policy) {
    return function(e) {
      e.preventDefault();
      e.stopPropagation();
      if (confirm(I18n.t("policies.confirmation", { policyName: policy.name }))) {
        App.Controllers.Policies.deletePolicy(policy);
      }
    };
  }

  handleShowPolicyDetail(policy) {
    return function(e) {
      e.preventDefault();
      e.stopPropagation();
      page("/policy/:id", { id: policy.id });
    };
  }

  handleShowViolations(policy) {
    return function(e) {
      e.preventDefault();
      e.stopPropagation();
      page("/violations/:id", { id: policy.id });
    };
  }

  handleShowRevisions(policy) {
    return function(e) {
      e.preventDefault();
      e.stopPropagation();
      page("/revisions/:id", { id: policy.id });
    };
  }

  closeFlash() {
    this.setState({ hideFlash: true });
  }

  renderFlash() {
    const flash = this.props.flash;
    if (flash && !this.state.hideFlash) {
      return (
          <div className="flash"><p>{flash}</p><a href="#" onClick={this.closeFlash}><i
              className="fa fa-remove"></i></a></div>
      );
    }
  }

  renderViolationsLink(policy) {
    if (policy.numberOfViolations === 0) {
      return (<span>0</span>);
    } else {
      return (<a href={page.uri("/violations/:id",{ id: policy.id })}
                 onClick={this.handleShowViolations(policy)}>{policy.numberOfViolations}</a>);
    }
  }

  renderRevisionsLink(policy) {
    const numberOfRevisions = (policy.numberOfRevisions + 1);
    return (<a href={page.uri("/revisions/:id",{ id:policy.id })}
                 onClick={this.handleShowRevisions(policy)}>{numberOfRevisions}</a>);
  }

  renderIdpNames(identityProviderNames) {
    return identityProviderNames.map(name => {
      return (<p key={name}>{name}</p>);
    });
  }

  renderControls(policy) {

    if (policy.actionsAllowed) {
      return (
          <div>
            <a href={page.uri("/policy/:id", { id: policy.id })} onClick={this.handleShowPolicyDetail(policy)}
               data-tooltip={I18n.t("policies.edit")}> <i className="fa fa-edit"></i>
            </a>
            <a href="#" data-tooltip={I18n.t("policies.delete")} onClick={this.handleDeletePolicyDetail(policy)}>
              <i className="fa fa-remove"></i>
            </a>
          </div>);
    }
  }

  render() {
    const renderRows = this.props.policies.map((policy, index) => {
      return (
          <tr key={policy.id}>
            <td>{policy.name}</td>
            <td>{policy.description}</td>
            <td>{policy.serviceProviderName}</td>
            <td className='policy_is_activated_sr'><input type="checkbox" defaultChecked={policy.activatedSr}
                                                          disabled="true"/></td>
            <td>{this.renderIdpNames(policy.identityProviderNames)}</td>
            <td className='policy_violations'>{this.renderViolationsLink(policy)}</td>
            <td className='policy_is_active'><input type="checkbox" defaultChecked={policy.active}
                                                          disabled="true"/></td>
            <td className='policy_revisions'>{this.renderRevisionsLink(policy)}</td>
            <td className="policy_controls">{this.renderControls(policy)}</td>
          </tr>);
    });

    return (
        <div className="mod-policy-overview">
          {this.renderFlash()}
          <div className='table-responsive'>
            <table className='table table-bordered box' id='policies_table'>
              <thead>
              <tr className='success'>
                <th className='policy_name_col'>{I18n.t("policies.name")}</th>
                <th className='policy_description_col'>{I18n.t("policies.description")}</th>
                <th className='policy_sp_col'>{I18n.t("policies.serviceProviderId")}</th>
                <th className='policy_is_activated_sr'>{I18n.t("policies.activatedSr")}</th>
                <th className='policy_idps_col'>{I18n.t("policies.identityProviderIds")}</th>
                <th className='policy_violations'>{I18n.t("policies.violations")}</th>
                <th className='policy_is_active'>{I18n.t("policies.isActive")}</th>
                <th className='policy_revisions'>{I18n.t("policies.revisions")}</th>
                <th className='policy_controls'></th>
              </tr>
              </thead>
              <tbody>
              {renderRows}
              </tbody>
            </table>
          </div>
        </div>
    );
  }
}

export default PolicyOverview;
