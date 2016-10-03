import React from "react";

class Main extends React.Component {
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
    return <App.Components.Navigation active={this.props.page.props.key} loading={this.props.loading} />;
  }
}

export default Main;
