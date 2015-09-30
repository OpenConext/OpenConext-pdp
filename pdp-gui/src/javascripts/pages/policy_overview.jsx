/** @jsx React.DOM */

App.Pages.PolicyOverview = React.createClass({

  getInitialState: function () {
    return {data: []}
  },

  componentDidMount: function () {
    var self = this;
    $('#policies_table').DataTable({
      'ajax': {
        url: '/pdp/api/internal/policies',
        dataSrc: ''
      },
      'columns': [
        {'data': 'name'},
        {'data': 'description'},
        {'data': 'serviceProviderId'},
        {'data': 'identityProviderIds'},
        {
          'searchable': false, 'orderable': false, 'data': null, 'render': function (data, type, row) {
          return '<a class="red" href="/policy/' + row.id + '"><i class="fa fa-remove"></i></a>' +
              '<a href="/policy/' + row.id + '"><i class="fa fa-edit"></i></a>';
        }
        }
      ]
    });
  },

  render: function () {
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

            </table>
          </div>
        </div>
    )
  }
});
