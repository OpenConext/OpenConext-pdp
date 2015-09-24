/** @jsx React.DOM */

App.Components.OverviewPanel = React.createClass({
  render: function () {
    return (
      <div className="l-middle">
        <div className="mod-title">
          <h1>{this.props.app.name}</h1>
        </div>

        <div className="mod-connection">
          {this.renderConnection()}
          <App.Components.LicenseInfo app={this.props.app} onSwitchPanel={this.props.onSwitchPanel}/>
        </div>

        {this.renderWikiUrl()}

        <div className="mod-description">
          <h2>{I18n.t("overview_panel.description")}</h2>
          {this.renderDescription()}
        </div>

        {this.renderNormenKader()}

        {this.renderSingleTenantService()}

        <App.Components.Screenshots screenshotUrls={this.props.app.screenshotUrls}/>
      </div>
    );
  },

  renderWikiUrl: function () {
    if (this.props.app.wikiUrl) {
      return (
        <div className="mod-title">
          <h3
            dangerouslySetInnerHTML={{ __html: I18n.t("overview_panel.wiki_info_html", { link: this.props.app.wikiUrl }) }}/>
        </div>
      );
    }
  },

  renderNormenKader: function () {
    var html = (this.props.app.normenkaderPresent && this.props.app.normenkaderUrl) ?
      I18n.t("overview_panel.normen_kader_html", {name: this.props.app.name, link: this.props.app.normenkaderUrl}) :
      I18n.t("overview_panel.no_normen_kader_html", {name: this.props.app.name});
    return (
      <div className="mod-description">
        <h2>{I18n.t("overview_panel.normen_kader")}</h2>
        <h3
          dangerouslySetInnerHTML={{ __html: html }}/>
      </div>);
  },

  renderSingleTenantService: function() {
    if (this.props.app.exampleSingleTenant) {
      return (
        <div className="mod-description">
        <h2>{I18n.t("overview_panel.single_tenant_service")}</h2>
        <h3
          dangerouslySetInnerHTML={{ __html: I18n.t("overview_panel.single_tenant_service_html", {name: this.props.app.name}) }}/>
      </div>);
    }
  },

  renderDescription: function () {
    var hasText = function (value) {
      return value && value.trim().length > 0;
    };
    if (hasText(this.props.app.enduserDescription)) {
      return <p dangerouslySetInnerHTML={{ __html: this.props.app.enduserDescription}}/>;
    } else if (hasText(this.props.app.institutionDescription)) {
      return <p dangerouslySetInnerHTML={{ __html: this.props.app.institutionDescription}}/>;
    } else if (hasText(this.props.app.description)) {
      return <p dangerouslySetInnerHTML={{ __html: this.props.app.description}}/>;
    } else {
      return <p>{I18n.t("overview_panel.no_description")}</p>;
    }
  },

  renderConnection: function () {
    return this.props.app.connected ? this.renderHasConnection() : this.renderNoConnection();
  },

  renderHasConnection: function () {
    if (App.currentUser.dashboardAdmin) {
      var disconnect = <p><a href="#"
                             onClick={this.props.onSwitchPanel("how_to_connect")}>{I18n.t("overview_panel.disconnect")}</a>
      </p>;
    }

    return (
      <div className="technical yes split">
        <i className="fa fa-chain"/>

        <h2>{I18n.t("overview_panel.has_connection")}</h2>
        {disconnect}
      </div>
    );
  },

  renderNoConnection: function () {
    if (App.currentUser.dashboardAdmin) {
      var connect = <p><a href="#"
                          onClick={this.props.onSwitchPanel("how_to_connect")}>{I18n.t("overview_panel.how_to_connect")}</a>
      </p>;
    }

    return (
      <div className="technical unknown split">
        <i className="fa fa-chain-broken"/>

        <h2>{I18n.t("overview_panel.no_connection")}</h2>
        {connect}
      </div>
    );
  }
});
