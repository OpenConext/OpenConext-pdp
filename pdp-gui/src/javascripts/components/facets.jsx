/** @jsx React.DOM */

App.Components.Facets = React.createClass({
  render: function () {
    var facets = this.props.facets;

    return (
      <div className="mod-filters">
        <h1>{this.renderTitle()}</h1>
        {facets.map(this.renderFacet)}
        <fieldset>
          {this.renderTotals()}
        </fieldset>
        <fieldset>
          {this.renderDownloadButton()}
        </fieldset>
      </div>
    );
  },

  renderTitle: function () {
    return (
      <span>
        {I18n.t("facets.title")}
        {this.renderResetFilters()}
      </span>
    );
  },

  renderResetFilters: function () {
    if (this.props.filteredCount < this.props.totalCount) {
      return (
        <small>
          <a href="#" onClick={this.handleResetFilters}>{I18n.t("facets.reset")}</a>
        </small>
      );
    }
  },

  renderTotals: function () {
    var count = this.props.filteredCount;
    var total = this.props.totalCount;

    if (count == total) {
      return I18n.t("facets.totals.all", {total: total})
    } else {
      return I18n.t("facets.totals.filtered", {count: count, total: total})
    }
  },

  renderFacet: function (facet) {
    return (
      <fieldset key={facet.name}>
        <a href="#" onClick={this.handleFacetToggle(facet)}>
          {this.renderDropDownIndicator(facet)}
        </a>

        <h2>{facet.name}</h2>
        {this.renderFacetOptions(facet)}
      </fieldset>
    );
  },

  renderFacetOptions: function (facet) {
    if (!this.props.hiddenFacets[facet.name]) {
      return (
        facet.values.map(function (value) {
          return this.renderFacetValue(facet, value);
        }.bind(this)));
    }
  },

  handleFacetToggle: function (facet) {
    return function (e) {
      e.stopPropagation();
      this.props.onHide(facet);
    }.bind(this);
  },

  renderDropDownIndicator: function (facet) {
    if (this.props.hiddenFacets[facet.name]) {
      return <i className="fa fa-caret-down float-right"/>;
    } else {
      return <i className="fa fa-caret-up float-right"/>;
    }
  },

  renderFacetValue: function (facet, facetValue) {
    var type = facet.oneOptionAllowed ? "radio" : "checkbox";
    var facetName = facet.searchValue || facet.name;
    var value = facetValue.searchValue || facetValue.value;

    return (
      <label key={facetValue.value} className={facetValue.count === 0 ? "greyed-out" : ""}>
        <input
          checked={Array.isArray(this.props.selectedFacets[facetName]) && this.props.selectedFacets[facetName].indexOf(value) > -1}
          type={type}
          onChange={this.handleSelectFacet(facetName, value)}/>
        {facetValue.value} ({facetValue.count})
      </label>
    );
  },

  renderDownloadButton: function () {
    return (
      <a href="#" className={"c-button" + (this.props.filteredCount <= 0 ? " disabled" : "")}
         onClick={this.handleDownload}>{I18n.t("facets.download")}</a>
    );
  },

  handleDownload: function (e) {
    e.preventDefault();
    e.stopPropagation();
    this.props.onDownload();
  },

  handleSelectFacet: function (facet, facetValue) {
    return function (e) {
      e.stopPropagation();
      this.props.onChange(facet, facetValue, e.target.checked);
    }.bind(this);
  },

  handleResetFilters: function (e) {
    e.preventDefault();
    e.stopPropagation();
    this.props.onReset();
  }


});
