/** @jsx React.DOM */

App.Components.LicenseInfo = React.createClass({
  getDefaultProps: function () {
    return {
      split: true
    }
  },

  render: function () {
    switch (this.props.app.licenseStatus) {
      case "HAS_LICENSE_SURFMARKET":
      case "HAS_LICENSE_SP":
        return this.renderHasLicense();
      case "NO_LICENSE":
        return this.renderNoLicense();
      case "NOT_NEEDED":
        return this.renderNoLicenseNeeded();
      case "UNKNOWN":
        return this.renderUnknownLicense();
    }
  },

  renderSplitClass: function (classNames) {
    if (this.props.split) {
      return classNames + " split";
    } else {
      return classNames;
    }
  },

  renderHasLicense: function () {
    var licenseStatus = this.props.app.licenseStatus === "HAS_LICENSE_SURFMARKET" ? I18n.t("license_info.has_license_surfmarket") : I18n.t("license_info.has_license_sp");
    var license = this.props.app.license;
    var licenseInfo = (license && license.endDate) ? I18n.t("license_info.valid", {date: I18n.strftime(new Date(license.endDate), "%-d %B %Y")}) : "";
    return (
      <div className={this.renderSplitClass("license yes")}>
        <i className="fa fa-file-text-o"></i>
        <h2>{licenseStatus}</h2>
        <p>{licenseInfo}</p>
      </div>
    );
  },

  renderNoLicense: function () {
    var link;
    if (this.props.onSwitchPanel) {
      link = <p><a href="#" onClick={this.props.onSwitchPanel("license_info")}>{I18n.t("license_info.license_info")}</a></p>;
    }
    return (
      <div className={this.renderSplitClass("license no")}>
        <i className="fa fa-file-text-o"></i>
        <h2>{I18n.t("license_info.no_license")}</h2>
        {link}
      </div>
    );
  },

  renderNoLicenseNeeded: function () {
    return (
      <div className={this.renderSplitClass("license no-needed")}>
        <i className="fa fa-file-text-o"></i>
        <h2>{I18n.t("license_info.no_license_needed")}</h2>
      </div>
    );
  },

  renderUnknownLicense: function () {
    var link;
    if (this.props.onSwitchPanel) {
      link = <p><a href="#"
                       onClick={this.props.onSwitchPanel("license_info")}>{I18n.t("license_info.license_unknown_info")}</a>
      </p>;
    }
    return (
      <div className={this.renderSplitClass("license unknown")}>
        <i className="fa fa-file-text-o"></i>
        <h2>{I18n.t("license_info.unknown_license")}</h2>
        {link}
      </div>
    );
  },
});
