/** @jsx React.DOM */

App.Components.ApplicationUsagePanel = React.createClass({
  mixins: [
    React.addons.LinkedStateMixin,
    App.Mixins.Chart,
  ],

  getInitialState: function() {
    return {
      chart: {
        idpId: "=" + btoa("entity=" + App.currentIdp().id + "|inst=" + App.currentIdp().institutionId),
        spId: "=" + btoa("entity=" + this.props.app.spEntityId+ "|active=1")
      }
    }
  },

  render: function() {
    return (
      <div className="l-middle">
        <div className="mod-title">
          <h1>{I18n.t("application_usage_panel.title")}</h1>
        </div>

        <div className="mod-usage">
          <div className="mod-usage">
            <div className="header">
              {this.renderTitle()}
              <div className="options">
                {this.renderPeriodSelect()}
                {this.renderDownloadButton()}
              </div>
            </div>
            {this.renderChart()}
          </div>
        </div>
      </div>
    );
  }
});
