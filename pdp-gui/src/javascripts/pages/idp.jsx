/** @jsx React.DOM */

App.Pages.MyIdp = React.createClass({
  render: function () {
    var roles = Object.keys(this.props.roles);
    return (
      <div className="l-mini">
        <div className="mod-idp">
          <h1>{I18n.t("my_idp.title")}</h1>

          <p dangerouslySetInnerHTML={{__html: I18n.t("my_idp.sub_title_html") }}></p>
          {this.renderRoles(roles)}
          {this.renderLicenseContactPersons(this.props.licenseContactPersons)}
        </div>
      </div>
    );
  },

  renderRoles: function (roles) {
    return (
      <table>
        <thead>
        <tr>
          <th className="percent_50">{I18n.t("my_idp.role")}</th>
          <th className="percent_50">{I18n.t("my_idp.users")}</th>
        </tr>
        </thead>
        <tbody>
        {roles.map(this.renderRole)}
        </tbody>
      </table>
    );
  },

  renderRole: function (role) {
    var names = this.props.roles[role].map(function (r) {
      return r.firstName + " " + r.surname
    }).sort().join(", ");
    var roleName = I18n.t("my_idp")[role];
    return (
      <tr key={role}>
        <td>{roleName}</td>
        <td>{names}</td>
      </tr>
    );
  },

  renderLicenseContactPerson: function (licenseContactPerson) {
    return (
      <tr>
        <td>{licenseContactPerson.name}</td>
        <td>{licenseContactPerson.email}</td>
        <td>{licenseContactPerson.phone}</td>
      </tr>
    );
  },

  renderLicenseContactPersons: function (licenseContactPersons) {
    if (licenseContactPersons && licenseContactPersons.length > 0) {
      return (
        <div>
          <p className="next" dangerouslySetInnerHTML={{__html: I18n.t("my_idp.license_contact_html") }}></p>
          <table>
            <thead>
            <tr>
              <th className="percent_35">{I18n.t("my_idp.license_contact_name")}</th>
              <th className="percent_35">{I18n.t("my_idp.license_contact_email")}</th>
              <th className="percent_35">{I18n.t("my_idp.license_contact_phone")}</th>
            </tr>
            </thead>
            <tbody>
            {licenseContactPersons.map(this.renderLicenseContactPerson)}
            </tbody>
          </table>
        </div>
      );
    }
  }

});
