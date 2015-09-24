/** @jsx React.DOM */

App.Components.Main = React.createClass({
  render: function () {
    return (
      <div>
        <div className="l-header">
          <App.Components.Header />
          {this.renderNavigation()}
        </div>

        {this.props.page}

        <App.Components.Footer />
      </div>
    );
  },

  renderNavigation: function() {
    if (!App.superUserNotSwitched()) {
      return <App.Components.Navigation active={this.props.page.props.key} loading={this.props.loading} />;
    }
  }
});
