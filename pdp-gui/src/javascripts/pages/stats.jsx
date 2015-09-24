/** @jsx React.DOM */

App.Pages.Stats = React.createClass({
  mixins: [
    React.addons.LinkedStateMixin,
    App.Mixins.Chart,
  ],

  getInitialState: function() {
    return {
      chart: {
        idpId: "=" + btoa("entity=" + App.currentIdp().id + "|inst=" + App.currentIdp().institutionId),
        spId: "*"
      }
    }
  },

  render: function() {
    return (
      <div className="l-main">
        <div className="l-left">
          <div className="mod-legend" ref="legend">
            <h1>{I18n.t("stats.legend")}</h1>
          </div>
        </div>
        <div className="l-right">
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
