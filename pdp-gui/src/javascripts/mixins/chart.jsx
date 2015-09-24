/** @jsx React.DOM */

App.Mixins.Chart = {
  getInitialState: function() {
    return {
      error: false
    }
  },

  shouldComponentUpdate: function(nextProps, nextState) {
    // only rerender when the error state is changed
    return nextState.error != this.state.error;
  },

  componentDidMount: function() {
    this.chart = new StatsChart({
      lang: I18n.locale,
      spId: this.state.chart.spId,
      idpId: this.state.chart.idpId,
      accessToken: App.currentUser.statsToken,

      chartElement: this.refs.chart.getDOMNode(),
      periodElement: this.refs.period.getDOMNode(),
      downloadElement: this.refs.download.getDOMNode(),
      titleElement: this.refs.title.getDOMNode(),
      legendElement: this.refs.legend && this.refs.legend.getDOMNode(),

      onError: this.handleError
    });
  },

  handleError: function() {
    this.setState({error: true});
  },

  componentWillUnmount: function() {
    this.chart.destroy();
    this.chart = null;
  },

  renderError: function() {
    if (this.state.error) {
      return <span dangerouslySetInnerHTML={{ __html: I18n.t("application_usage_panel.error_html") }} />;
    }
  },

  renderLegend: function() {
    return (
      <div ref="legend" />
    );
  },

  renderPeriodSelect: function() {
    return (
      <div ref="period" />
    );
  },

  renderDownloadButton: function() {
    return (
      <div ref="download" />
    );
  },

  renderTitle: function() {
    return (
      <div ref="title" />
    );
  },

  renderChart: function() {
    return (
      <div className="body">
        {this.renderError()}
        <div className="chart-container">
          <div className="chart" ref="chart" />
        </div>
      </div>
    );
  },
}
