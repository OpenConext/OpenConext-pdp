/** @jsx React.DOM */

App.Pages.PolicyOverview = React.createClass({

  getInitialState: function () {
    return {data: []}
  },

  destroyDataTable: function () {
    $('#policies_table').DataTable().destroy();
  },

  initDataTable: function () {
    $('#policies_table').DataTable({
      paging: false,
      language: {
        search: "_INPUT_",
        searchPlaceholder: I18n.t("policies.search")
      },
      columnDefs: [{
        targets: [5],
        orderable: false
      }]
    });
  },

  componentWillReceiveProps: function (nextProps) {
    this.destroyDataTable();
    if (!_.isEmpty(this.props) && this.props.flash !== nextProps.flash) {
      this.setState({hideFlash: false});
    }
  },

  componentDidUpdate: function (prevProps, prevState) {
    if (!$.fn.DataTable.isDataTable('#policies_table')) {
      this.initDataTable();
    }

  },

  componentDidMount: function () {
    this.initDataTable();
  },

  componentWillUnmount: function () {
    this.destroyDataTable();
  },

  handleDeletePolicyDetail: function (policy) {
    return function (e) {
      e.preventDefault();
      e.stopPropagation();
      if (confirm("Are your sure you want to remove policy '" + policy.name + "'?")) {
        App.Controllers.Policies.deletePolicy(policy);
      }
    }
  }
  ,

  handleShowPolicyDetail: function (policy) {
    return function (e) {
      e.preventDefault();
      e.stopPropagation();
      page("/policy/:id", {id: policy.id});
    }
  },

  handleShowViolations: function (policy) {
    return function (e) {
      e.preventDefault();
      e.stopPropagation();
      page("/violations/:policyId", {policyId: encodeURIComponent(policy.policyId)});
    }
  },

  closeFlash: function () {
    this.setState({hideFlash: true});
  },

  renderFlash: function () {
    var flash = this.props.flash;
    if (flash && !this.state.hideFlash) {
      return (
          <div className="flash"><p>{flash}</p><a href="#" onClick={this.closeFlash}><i
              className="fa fa-remove"></i></a></div>
      );
    }
  }
  ,
  renderViolationsLink: function (policy) {
    if (policy.numberOfViolations === 0) {
      return (<span>0</span>);
    } else {
      return (<a href={page.uri("/violations/:policyId",{policyId:encodeURIComponent(policy.policyId)})}
          onClick={this.handleShowViolations(policy)}>{policy.numberOfViolations}</a>);
    }
  },

  render: function () {
    var renderRows = this.props.policies.map(function (policy, index) {
      return (
          <tr key={policy.id}>
            <td>{policy.name}</td>
            <td>{policy.description}</td>
            <td>{policy.serviceProviderName}</td>
            <td>{policy.identityProviderNames}</td>
            <td className='policy_violations'>{this.renderViolationsLink(policy)}</td>
            <td className="policy_controls">
              <a href={page.uri("/policy/:id", {id: policy.id})} onClick={this.handleShowPolicyDetail(policy)}
                 data-tooltip="Edit">
                <i className="fa fa-edit"></i>
              </a>
              <a href="#" data-tooltip="Delete" onClick={this.handleDeletePolicyDetail(policy)}><i
                  className="fa fa-remove"></i></a></td>
          </tr>)
    }.bind(this));

    return (
        <div className="mod-policy-overview">
          {this.renderFlash()}
          <div className='table-responsive'>
            <table className='table table-bordered box' id='policies_table'>
              <thead>
              <tr className='success'>
                <th className='policy_name_col'>{I18n.t('policies.name')}</th>
                <th className='policy_description_col'>{I18n.t('policies.description')}</th>
                <th className='policy_sp_col'>{I18n.t('policies.serviceProviderId')}</th>
                <th className='policy_idps_col'>{I18n.t('policies.identityProviderIds')}</th>
                <th className='policy_violations'>{I18n.t('policies.violations')}</th>
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
})
;
