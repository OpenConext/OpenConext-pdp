/** @jsx React.DOM */

App.Pages.PolicyViolations = React.createClass({

  getInitialState: function () {
    return {data: []}
  },

  destroyDataTable: function () {
    $('#violations_table').DataTable().destroy();
  },

  initDataTable: function () {
    $('#violations_table').DataTable({
      paging: false,
      language: {
        search: "_INPUT_",
        searchPlaceholder: I18n.t("violations.search")
      },
      columnDefs: [{
        targets: [3],
        orderable: false
      }]
    });
  },

  componentWillReceiveProps: function (nextProps) {
    this.destroyDataTable();
  },

  componentDidUpdate: function (prevProps, prevState) {
    if (!$.fn.DataTable.isDataTable('#violations_table')) {
      this.initDataTable();
    }
    window.scrollTo(0, 0);

  },

  componentDidMount: function () {
    this.initDataTable();
  },

  componentWillUnmount: function () {
    this.destroyDataTable();
  },

  handleShowViolationDetail: function (violation) {
    return function (e) {
      e.preventDefault();
      e.stopPropagation();
      this.setState({violation: violation, tab: "request"});
    }.bind(this);
  },

  getEntityName: function (id, type) {
    var name = id;
    var entities = this.props[type].filter(function (entity) {
      return entity.entityId === id;
    });
    if (!_.isEmpty(entities)) {
      var entity = entities[0];
      return I18n.entityName(entity);
    }
    return name;
  },

  parseEntityId: function (attributes, type) {
    var idpAttr = attributes.filter(function (attr) {
      return attr.AttributeId === type;
    });
    var idpValues = idpAttr.map(function (attr) {
      return attr.Value;
    });
    return idpValues.length === 1 ? idpValues[0] : undefined;
  },

  handleTabChange: function (tab) {
    return function (e) {
      e.preventDefault();
      e.stopPropagation();
      this.setState({tab: tab});
    }.bind(this);
  },

  renderStatus: function (response, policyName) {
    var decision = response.Response[0].Decision;
    var status = App.Controllers.PolicyViolations.determineStatus(decision);
    return (
        <div className={"response-status " + status}>
          <i className={"fa fa-"+status + " " + status}></i>
          <section>
            <p className="status">{decision}</p>

            <p className="details">{I18n.t("violations.policyName") + "'" + policyName + "'"}</p>
          </section>
        </div>
    );
  },

  renderAboutPage: function () {
    return I18n.locale === "en" ? <App.Help.PolicyViotaltionsHelpEn/> : <App.Help.PolicyViotaltionsHelpNl/>;
  },

  renderViolationsDetail: function () {
    var violation = this.state.violation;
    if (violation) {
      var request = JSON.parse(violation.jsonRequest);
      var response = JSON.parse(violation.response);
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
  },

  renderTabs: function () {
    var selectedTab = (this.state.tab || "request");
    var request = (selectedTab == "request" ? "selected" : "");
    var response = (selectedTab == "response" ? "selected" : "");
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
  },

  renderRequestDetails: function (request) {
    var selectedTab = (this.state.tab || "request");
    //request is JSON very poorly formatted
    var requestJson = JSON.stringify(request, null, 3);
    if (selectedTab === "request") {
      var options = {
        mode: {name: "javascript", json: true},
        lineWrapping: true,
        lineNumbers: true,
        scrollbarStyle: null,
        readOnly: true
      }
      return (
          <App.Components.CodeMirror value={requestJson}
                                     options={options} uniqueId="code_mirror_textarea_violation_request"/>
      )
    }
  },

  renderResponseDetails: function (response) {
    var selectedTab = (this.state.tab || "request");
    var responseJson = JSON.stringify(response, null, 3);

    if (selectedTab === "response") {
      var options = {
        mode: {name: "javascript", json: true},
        lineWrapping: true,
        lineNumbers: true,
        scrollbarStyle: null,
        readOnly: true
      }
      return (
          <App.Components.CodeMirror value={responseJson}
                                     options={options} uniqueId="code_mirror_textarea_violation_response"/>
      )
    }
  },

  renderTable: function () {
    var renderRows = this.props.violations.map(function (violation, index) {
      var request = JSON.parse(violation.jsonRequest).Request;
      var idp = this.parseEntityId(request.Resource.Attribute, "IDPentityID");
      var idpName = this.getEntityName(idp, "identityProviders");
      var sp = this.parseEntityId(request.Resource.Attribute, "SPentityID");
      var spName = this.getEntityName(sp, "serviceProviders");
      var nameId = this.parseEntityId(request.AccessSubject.Attribute, "urn:oasis:names:tc:SAML:1.1:nameid-format:unspecified");

      var response = JSON.parse(violation.response).Response[0];
      var decision = response.Decision;
      var d = new Date(violation.created);
      var selected = this.state.violation && this.state.violation.id === violation.id ? "selected" : "";
      return (
          <tr key={violation.id} className={selected}>
            <td>{idpName}<br/>{spName}</td>
            <td>{decision}</td>
            <td>{d.getDate() + "-" + (d.getMonth() + 1) + "-" + d.getFullYear() + " " + d.getHours() + ":" + (d.getMinutes() < 10 ? ("0" + d.getMinutes() ) : d.getMinutes())}</td>
            <td className="violation_controls">
              <a href="#" onClick={this.handleShowViolationDetail(violation)}
                 data-tooltip="Detail">
                <i className="fa fa-eye"></i>
              </a>
            </td>
          </tr>)
    }.bind(this));
    return (
        <table className='table table-bordered box' id='violations_table'>
          <thead>
          <tr className='success'>
            <th className='violation_providers'>{I18n.t("violations.table.sp_idp")}</th>
            <th className='violation_decision'>{I18n.t("violations.table.decision")}</th>
            <th className='violation_policy_created'>{I18n.t("violations.table.created")}</th>
            <th className='violation_controls'></th>
          </tr>
          </thead>
          <tbody>
          {renderRows}
          </tbody>
        </table>
    );
  },

  render: function () {

    return (
        <div className="l-center mod-violations">
          <div className="l-split-left">
            {this.renderTable()}
          </div>
          <div className="l-split-right form-element-container box">
            {this.renderViolationsDetail()}
          </div>
        </div>
    )
  }

});