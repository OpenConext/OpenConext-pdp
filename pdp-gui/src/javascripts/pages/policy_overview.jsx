/** @jsx React.DOM */

App.Pages.PolicyOverview = React.createClass({

  getInitialState: function () {
    return {data: []}
  },

  componentDidMount: function () {
    var self = this;
    $('#policies_table').DataTable({
      paging: false,
      language: {
        search: "_INPUT_",
        searchPlaceholder: "Search policies..."
      },
      columnDefs: [ {
        targets: [ 4 ],
        orderable: false
      } ]

    });
  },

  handleShowPolicyDetail: function (policy) {
    return function (e) {
      e.preventDefault();
      e.stopPropagation();
      page("/policy/:id", {id: policy.id});
    }
  },

  render: function () {
    var self = this;
    var renderRows = this.props.policies.map(function (policy, index) {
      return (
          <tr key={policy.id}>
            <td>{policy.name}</td>
            <td>{policy.description}</td>
            <td>{policy.serviceProviderId}</td>
            <td>{policy.identityProviderIds}</td>
            <td>
              <a href="#" data-tooltip="View"><i className="fa fa-eye"></i></a>
              <a className="orange" href={page.uri("/policy/:id", {id: policy.id})} onClick={self.handleShowPolicyDetail(policy)}
                 data-tooltip="Edit">
                <i className="fa fa-edit"></i>
            </a>
            <a className="red" href="#" data-tooltip="Delete"><i className="fa fa-remove"></i></a></td>
          </tr>)
    });

    return (
        <div className="mod-policy-overview">
          <div className='table-responsive'>
            <table className='table table-bordered box' id='policies_table'>
              <thead>
              <tr className='success'>
                <th className='policy_name_col'>{I18n.t('policies.name')}</th>
                <th className='policy_description_col'>{I18n.t('policies.description')}</th>
                <th className='policy_sp_col'>{I18n.t('policies.serviceProviderId')}</th>
                <th className='policy_idps_col'>{I18n.t('policies.identityProviderIds')}</th>
                <th className='policy_controls'></th>
              </tr>
              </thead>
              <tbody>
              {renderRows}
              </tbody>
            </table>
          </div>
        </div>
    )
  }
});
