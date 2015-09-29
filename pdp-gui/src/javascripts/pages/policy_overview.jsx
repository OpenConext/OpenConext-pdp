/** @jsx React.DOM */

App.Pages.PolicyOverview = React.createClass({

  getInitialState: function () {
    return {data: []}
  },

  componentDidMount: function () {
    var self = this;
    $('#policies_table').DataTable({
      "ajax": {
        url: "/pdp/api/internal/policies",
        dataSrc: ''
      },
      "columns": [
        {"data": "name"},
        {"data": "description"},
        {"data": "serviceProviderId"},
        {"data": "identityProviderIds"}
      ]
    });
  },

  //componentDidUpdate: function () {
  //  $('#policies_table').dataTable({
  //    "sPaginationType": "full_numbers",
  //    "bAutoWidth": false,
  //    "bDestroy": true,
  //  });
  //},

  render: function () {
    //var x = this.props.policies.map(function (policy, index) {
    //  return
    //  <tr>
    //    <td>{index + 1}</td>
    //    <td>{policy.name}</td>
    //    <td>{policy.description}</td>
    //    <td>{policy.serviceProviderId}</td>
    //  </tr>
    //});
    //return (
    //    <div className="table-responsive">
    //      <table className="table table-bordered" id="policies_table">
    //        <thead>
    //        <tr class="success">
    //          <td>{I18n.t("policies.name")}</td>
    //          <td>{I18n.t("policies.description")}</td>
    //          <td>{I18n.t("policies.serviceProviderId")}</td>
    //        </tr>
    //        </thead>
    //        <tbody>
    //        {x}
    //        </tbody>
    //      </table>
    //    </div>
    //)
    return (
        <div className="table-responsive">
          <table className="table table-bordered" id="policies_table">
            <thead>
            <tr className="success">
              <th className="policy_name_col">{I18n.t("policies.name")}</th>
              <th className="policy_description_col">{I18n.t("policies.description")}</th>
              <th className="policy_sp_col">{I18n.t("policies.serviceProviderId")}</th>
              <th className="policy_idps_col">{I18n.t("policies.identityProviderIds")}</th>
            </tr>
            </thead>

          </table>
        </div>
    )
  }
});
