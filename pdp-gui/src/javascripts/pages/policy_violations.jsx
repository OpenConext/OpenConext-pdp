import React from "react";
import I18n from "i18n-js";

class PolicyViolations extends React.Component {

  constructor() {
    super();

    this.state = { data: [] };
  }

  destroyDataTable() {
    $("#violations_table").DataTable().destroy();
  }

  initDataTable() {
    $.fn.dataTable.ext.order["locale-date"] = function(settings, col) {
      return this.api().column(col, { order: "index" }).nodes().map((td, i) => {
        //use the milliseconds to sort
        return moment(td.textContent, "DD-MM-YYYY hh:mm").valueOf();
      });
    };
    $.fn.dataTable.ext.order["dom-checkbox"] = function(settings, col) {
      return this.api().column(col, { order: "index" }).nodes().map((td, i) => {
        return $("input", td).prop("checked") ? "1" : "0";
      });
    };
    $("#violations_table").DataTable({
      paging: true,
      language: {
        search: "_INPUT_",
        searchPlaceholder: I18n.t("violations.search"),
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
        { targets: [3], orderDataType: "locale-date" },
        { targets: [2], orderDataType: "dom-checkbox" },
        { targets: [4], orderable: false }
      ],
      order: [[3, "desc"]]
    });
  }

  componentWillReceiveProps(nextProps) {
    this.destroyDataTable();
  }

  componentDidUpdate(prevProps, prevState) {
    if (!$.fn.DataTable.isDataTable("#violations_table")) {
      this.initDataTable();
    }
    window.scrollTo(0, 0);
    // not the react way, but we don't control datatables as we should
    if (this.props.violations.length === 0) {
      $("#violations_table_paginate").hide();
    } else {
      $("#violations_table_paginate").show();
    }
  }

  componentDidMount() {
    this.initDataTable();
  }

  componentWillUnmount() {
    this.destroyDataTable();
  }

  handleShowViolationDetail(violation) {
    return function(e) {
      e.preventDefault();
      e.stopPropagation();
      this.setState({ violation: violation, tab: "request" });
    }.bind(this);
  }

  getEntityName(id, type) {
    const name = id;
    const entities = this.props[type].filter(entity => {
      return entity.entityId === id;
    });
    if (!_.isEmpty(entities)) {
      const entity = entities[0];
      return I18n.entityName(entity);
    }
    return name;
  }

  parseEntityId(attributes, type) {
    const idpAttr = attributes.filter(attr => {
      return attr.AttributeId === type;
    });
    const idpValues = idpAttr.map(attr => {
      return attr.Value;
    });
    return idpValues.length === 1 ? idpValues[0] : undefined;
  }

  handleTabChange(tab) {
    return function(e) {
      e.preventDefault();
      e.stopPropagation();
      this.setState({ tab: tab });
    }.bind(this);
  }

  renderStatus(response, policyName) {
    const decision = response.Response[0].Decision;
    const status = App.Controllers.PolicyViolations.determineStatus(decision);
    return (
      <div className={"response-status " + status}>
        <i className={"fa fa-"+status + " " + status}></i>
        <section>
          <p className="status">{decision}</p>

          <p className="details">{I18n.t("violations.policyName") + "'" + policyName + "'"}</p>
        </section>
      </div>
    );
  }

  renderAboutPage() {
    return I18n.locale === "en" ? <App.Help.PolicyViotaltionsHelpEn/> : <App.Help.PolicyViotaltionsHelpNl/>;
  }

  renderViolationsDetail() {
    const violation = this.state.violation;
    if (violation) {
      const request = JSON.parse(violation.jsonRequest);
      const response = JSON.parse(violation.response);
      return (
        <div>
          {this.renderStatus(response, violation.policyName)}
          {this.renderTabs()}
          {this.renderRequestDetails(request)}
          {this.renderResponseDetails(response)}
        </div>
      );
    } else {
      return this.renderAboutPage();
    }
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

  renderRequestDetails(request) {
    const selectedTab = (this.state.tab || "request");
    //request is JSON very poorly formatted
    const requestJson = JSON.stringify(request, null, 3);
    if (selectedTab === "request") {
      const options = {
        mode: { name: "javascript", json: true },
        lineWrapping: true,
        lineNumbers: true,
        scrollbarStyle: null,
        readOnly: true
      };
      return (
        <App.Components.CodeMirror value={requestJson}
          options={options} uniqueId="code_mirror_textarea_violation_request"/>
      );
    }
  }

  renderResponseDetails(response) {
    const selectedTab = (this.state.tab || "request");
    const responseJson = JSON.stringify(response, null, 3);

    if (selectedTab === "response") {
      const options = {
        mode: { name: "javascript", json: true },
        lineWrapping: true,
        lineNumbers: true,
        scrollbarStyle: null,
        readOnly: true
      };
      return (
        <App.Components.CodeMirror value={responseJson}
          options={options} uniqueId="code_mirror_textarea_violation_response"/>
      );
    }
  }

  renderTable() {
    const renderRows = this.props.violations.map((violation, index) => {
      const request = JSON.parse(violation.jsonRequest).Request;
      const idp = this.parseEntityId(request.Resource.Attribute, "IDPentityID");
      const idpName = this.getEntityName(idp, "identityProviders");
      const sp = this.parseEntityId(request.Resource.Attribute, "SPentityID");
      const spName = this.getEntityName(sp, "serviceProviders");

      const response = JSON.parse(violation.response).Response[0];
      const decision = response.Decision;
      const d = new Date(violation.created);
      const selected = this.state.violation && this.state.violation.id === violation.id ? "selected" : "";
      return (
        <tr key={violation.id} className={selected}>
          <td>{idpName}<br/>{spName}</td>
          <td>{decision}</td>
          <td className='violation_is_playground'><input type="checkbox" defaultChecked={violation.playground}
              disabled="true"/></td>
          <td>{d.getDate() + "-" + (d.getMonth() + 1) + "-" + d.getFullYear() + " " + d.getHours() + ":" + (d.getMinutes() < 10 ? ("0" + d.getMinutes()) : d.getMinutes())}</td>
          <td className="violation_controls">
            <a href="#" onClick={this.handleShowViolationDetail(violation)}
              data-tooltip="Detail">
              <i className="fa fa-eye"></i>
            </a>
          </td>
        </tr>);
    });
    return (
      <table className='table table-bordered box' id='violations_table'>
        <thead>
          <tr className='success'>
            <th className='violation_providers'>{I18n.t("violations.table.sp_idp")}</th>
            <th className='violation_decision'>{I18n.t("violations.table.decision")}</th>
            <th className='violation_is_playground'>{I18n.t("violations.table.is_playground")}</th>
            <th className='violation_policy_created'>{I18n.t("violations.table.created")}</th>
            <th className='violation_controls'></th>
          </tr>
        </thead>
        <tbody>
          {renderRows}
        </tbody>
      </table>
    );
  }

  render() {

    return (
      <div className="l-center mod-violations">
        <div className="l-split-left">
          {this.renderTable()}
        </div>
        <div className="l-split-right form-element-container box">
          {this.renderViolationsDetail()}
        </div>
      </div>
    );
  }

}

export default PolicyViolations;
