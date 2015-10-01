/** @jsx React.DOM */

App.Pages.PolicyOverview = React.createClass({

  getInitialState: function () {
    return {data: []}
  },

  componentDidMount: function () {
    var self = this;
    $('#policies_table').DataTable({});
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
              <a href={page.uri("/policy/:id", {id: policy.id})} onClick={self.handleShowPolicyDetail(policy)}>
                <i className="fa fa-edit"></i>
            </a>
            <a className="red" href="#"><i className="fa fa-remove"></i></a></td>
          </tr>)
    });

    return (
        <div>
          <div className="new-policy"><a className="c-button" href="#">New Policy</a></div>
          <div className='table-responsive'>
            <table className='table table-bordered' id='policies_table'>
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
