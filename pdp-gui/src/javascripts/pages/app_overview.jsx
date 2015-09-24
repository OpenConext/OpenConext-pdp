/** @jsx React.DOM */

App.Pages.AppOverview = React.createClass({
  mixins: [
    React.addons.LinkedStateMixin,
    App.Mixins.SortableTable("apps.overview", "name")
  ],

  getInitialState: function () {
    return {
      search: "",
      radioButtonFacets: ["connection", "used_by_idp", "published_edugain"],
      activeFacets: App.store.activeFacets || {},
      hiddenFacets: App.store.hiddenFacets || {}
    }
  },

  render: function () {
    var filteredApps = this.filterAppsForExclusiveFilters(this.props.apps);

    if (App.currentUser.dashboardAdmin) {
      var connect = (
        <th className="percent_10 right">
          {I18n.t("apps.overview.connect")}
        </th>
      );
    }

    var facets = this.staticFacets().concat(this.props.facets);
    this.addNumbers(filteredApps, facets);
    filteredApps = this.filterAppsForInclusiveFilters(filteredApps);

    return (
      <div className="l-main">
        <div className="l-left">
          <App.Components.Facets
            facets={facets}
            selectedFacets={this.state.activeFacets}
            hiddenFacets={this.state.hiddenFacets}
            filteredCount={filteredApps.length}
            totalCount={this.props.apps.length}
            onChange={this.handleFacetChange}
            onHide={this.handleFacetHide}
            onReset={this.handleResetFilters}
            onDownload={this.handleDownloadOverview}/>
        </div>
        <div className="l-right">
          <div className="mod-app-search">
            <fieldset>
              <i className="fa fa-search"/>
              <input
                type="search"
                valueLink={this.linkState("search")}
                placeholder={I18n.t("apps.overview.search_hint")}/>

              <button type="submit">{I18n.t("apps.overview.search")}</button>
            </fieldset>
          </div>
          <div className="mod-app-list">
            <table>
              <thead>
              <tr>
                {this.renderSortableHeader("percent_25", "name")}
                {this.renderSortableHeader("percent_15", "license")}
                {this.renderSortableHeader("percent_15", "connected")}
                {connect}
              </tr>
              </thead>
              <tbody>
              {filteredApps.length > 0 ? this.sort(filteredApps).map(this.renderApp) : this.renderEmpty()}
              </tbody>
            </table>
          </div>
        </div>
      </div>
    );
  },

  renderEmpty: function () {
    return <td className="empty" colSpan="4">{I18n.t("apps.overview.no_results")}</td>;
  },

  renderApp: function (app) {
    if (App.currentUser.dashboardAdmin) {
      var connect = (
        <td className="right">
          {this.renderConnectButton(app)}
        </td>
      );
    }

    return (
      <tr key={app.id} onClick={this.handleShowAppDetail(app)}>
        <td><a href={page.uri("/apps/:id", {id: app.id})}>{app.name}</a></td>
        {this.renderLicenseStatus(app)}
        {App.renderYesNo(app.connected)}
        {connect}
      </tr>
    );
  },

  licenseStatusClassName: function (app) {
    switch (app.licenseStatus) {
      case "HAS_LICENSE_SURFMARKET":
      case "HAS_LICENSE_SP":
        return "yes"
      case "NO_LICENSE":
        return "no";
      default:
        return "";
    }
  },

  renderLicenseStatus: function (app) {
    return (
      <td
        className={this.licenseStatusClassName(app)}>{I18n.t("facets.static.license." + app.licenseStatus.toLowerCase())}</td>
    );
  },

  renderConnectButton: function (app) {
    if (!app.connected) {
      return <a onClick={this.handleShowHowToConnect(app)}
                className="c-button narrow">{I18n.t("apps.overview.connect_button")}</a>;
    }
  },

  handleShowAppDetail: function (app) {
    return function (e) {
      e.preventDefault();
      e.stopPropagation();
      page("/apps/:id", {id: app.id});
    }
  },

  handleShowHowToConnect: function (app) {
    return function (e) {
      e.preventDefault();
      e.stopPropagation();
      page("/apps/:id/how_to_connect", {id: app.id});
    }
  },

  /*
   * this.state.activeFacets is a object with facet names and the values are arrays with all select values
   */
  handleFacetChange: function (facet, facetValue, checked) {
    var selectedFacets = $.extend({}, this.state.activeFacets);
    var facetValues = selectedFacets[facet];
    if (facetValues) {
      checked ? facetValues.push(facetValue) : facetValues.splice(facetValues.indexOf(facetValue), 1);
    } else {
      facetValues = selectedFacets[facet] = [facetValue];
    }
    /*
     * Special case. For some static facets we only want one value (e.g. either 'yes' or 'no')
     */
    if (this.state.radioButtonFacets.indexOf(facet) > -1 && checked && facetValues.length === 2) {
      //we use radio buttons for one-value-facets, but we do want the ability to de-select them
      var nbr = (facetValues[0] === facetValues[1] ? 2 : 1);
      facetValues.splice(0, nbr);
    }
    this.setState({activeFacets: selectedFacets});
    App.store.activeFacets = selectedFacets;
  },


  handleFacetHide: function (facet) {
    var hiddenFacets = $.extend({}, this.state.hiddenFacets);
    if (hiddenFacets[facet.name]) {
      delete hiddenFacets[facet.name];
    } else {
      hiddenFacets[facet.name] = true;
    }
    this.setState({hiddenFacets: hiddenFacets});
    App.store.hiddenFacets = hiddenFacets;
  },

  handleResetFilters: function () {
    this.setState({
      search: "",
      activeFacets: {},
      hiddenFacets: {}
    });

    App.store.activeFacets = null;
    App.store.hiddenFacets = null;
  },

  handleDownloadOverview: function () {
    var filteredApps = this.filterAppsForInclusiveFilters(this.filterAppsForExclusiveFilters(this.props.apps));
    App.Controllers.Apps.downloadOverview(filteredApps);
  },

  filterAppsForExclusiveFilters: function (apps) {
    var filteredApps = apps.filter(this.filterBySearchQuery);
    if (!$.isEmptyObject(this.state.activeFacets)) {
      filteredApps = filteredApps.filter(this.filterConnectionFacet);
      filteredApps = filteredApps.filter(this.filterIdpService);
      filteredApps = filteredApps.filter(this.filterPublishedEdugain);
    }

    return filteredApps;
  },

  filterAppsForInclusiveFilters: function (apps) {
    var filteredApps = apps;

    if (!$.isEmptyObject(this.state.activeFacets)) {
      filteredApps = filteredApps.filter(this.filterByFacets);
      filteredApps = filteredApps.filter(this.filterLicenseFacet);
    }

    return filteredApps;
  },

  addNumbers: function (filteredApps, facets) {
    var me = this;
    var filter = function (facet, filterFunction) {
      facet.values.forEach(function (facetValue) {
        facetValue.count = filteredApps.filter(function (app) {
          return filterFunction(app, facetValue);
        }).length;
      });
    };
    facets.forEach(function (facet) {
      switch (facet.searchValue) {
        case "connection":
          filter(facet, function (app, facetValue) {
            return facetValue.searchValue === "yes" ? app.connected : !app.connected;
          });
          break;
        case "license":
          filter(facet, function (app, facetValue) {
            return app.licenseStatus === facetValue.searchValue;
          });
          break;
        case "used_by_idp":
          filter(facet, function (app, facetValue) {
            var usedByIdp = App.currentIdp().institutionId === app.institutionId;
            return facetValue.searchValue === "yes" ? usedByIdp : !usedByIdp;
          });
          break;
        case "published_edugain":
          filter(facet, function (app, facetValue) {
            var published = app.publishedInEdugain || false;
            return facetValue.searchValue === "yes" ? published : !published;
          });
          break;
        default:
          filter(facet, function (app, facetValue) {
            var categories = me.normalizeCategories(app);
            var appTags = categories[facet.name] || [];
            return appTags.indexOf(facetValue.value) > -1;
          });
      }
    });
  },

  filterBySearchQuery: function (app) {
    return app.name.toLowerCase().indexOf(this.state.search.toLowerCase()) >= 0;
  },

  filterConnectionFacet: function (app) {
    var connectionFacetValues = this.state.activeFacets["connection"] || [];
    if (connectionFacetValues.length > 0) {
      return app.connected ? connectionFacetValues[0] === "yes" : connectionFacetValues[0] === "no";
    }
    return true;
  },

  filterLicenseFacet: function (app) {
    var licenseFacetValues = this.state.activeFacets["license"] || [];
    return licenseFacetValues.length === 0 || licenseFacetValues.indexOf(app.licenseStatus) > -1;
  },

  filterIdpService: function (app) {
    var usedByIdpFacetValues = this.state.activeFacets["used_by_idp"] || [];
    if (usedByIdpFacetValues.length > 0) {
      return App.currentIdp().institutionId === app.institutionId ? usedByIdpFacetValues[0] === "yes" : usedByIdpFacetValues[0] === "no";
    }
    return true;
  },

  filterPublishedEdugain: function (app) {
    var edugainFacetValues = this.state.activeFacets["published_edugain"] || [];
    if (edugainFacetValues.length > 0) {
      return app.publishedInEdugain ? edugainFacetValues[0] === "yes" : edugainFacetValues[0] === "no";
    }
    return true;
  },

  filterByFacets: function (app) {
    var normalizedCategories = this.normalizeCategories(app);
    for (var facet in this.state.activeFacets) {
      var facetValues = this.state.activeFacets[facet] || [];
      if (normalizedCategories[facet] && facetValues.length > 0) {
        var hits = normalizedCategories[facet].filter(function (facetValue) {
          return facetValues.indexOf(facetValue) > -1;
        });
        if (hits.length === 0) {
          return false;
        }
      }
    }
    return true;
  },

  normalizeCategories: function (app) {
    var normalizedCategories = {}
    app.categories.forEach(function (category) {
      normalizedCategories[category.name] = category.values.map(function (categoryValue) {
        return categoryValue.value;
      });
    });
    return normalizedCategories;
  },

  convertLicenseForSort: function (value, app) {
    return app.licenseStatus;
  },

  convertNameForSort: function (value) {
    return value.toLowerCase();
  },

  staticFacets: function () {
    return [{
      name: I18n.t("facets.static.connection.name"),
      searchValue: "connection",
      oneOptionAllowed: true,
      values: [
        {value: I18n.t("facets.static.connection.has_connection"), searchValue: "yes"},
        {value: I18n.t("facets.static.connection.no_connection"), searchValue: "no"}
      ]
    }, {
      name: I18n.t("facets.static.used_by_idp.name"),
      searchValue: "used_by_idp",
      oneOptionAllowed: true,
      values: [
        {value: I18n.t("facets.static.used_by_idp.yes"), searchValue: "yes"},
        {value: I18n.t("facets.static.used_by_idp.no"), searchValue: "no"}
      ]
    }, {
      name: I18n.t("facets.static.published_edugain.name"),
      searchValue: "published_edugain",
      oneOptionAllowed: true,
      values: [
        {value: I18n.t("facets.static.published_edugain.yes"), searchValue: "yes"},
        {value: I18n.t("facets.static.published_edugain.no"), searchValue: "no"}
      ]
    }, {
      name: I18n.t("facets.static.license.name"),
      searchValue: "license",
      oneOptionAllowed: false,
      values: [
        {value: I18n.t("facets.static.license.has_license_surfmarket"), searchValue: "HAS_LICENSE_SURFMARKET"},
        {value: I18n.t("facets.static.license.has_license_sp"), searchValue: "HAS_LICENSE_SP"},
        {value: I18n.t("facets.static.license.no_license"), searchValue: "NO_LICENSE"},
        {value: I18n.t("facets.static.license.not_needed"), searchValue: "NOT_NEEDED"},
        {value: I18n.t("facets.static.license.unknown"), searchValue: "UNKNOWN"}
      ]
    }];
  }

});
