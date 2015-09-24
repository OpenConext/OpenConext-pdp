/** @jsx React.DOM */

App.Components.Screenshots = React.createClass({
  render: function() {
    var screenshotsUrls = this.props.screenshotUrls;
    if(!screenshotsUrls) {
      screenshotsUrls = [];
    }
    return (
      <div className="mod-screenshots">
        {screenshotsUrls.map(this.renderScreenshot)}
      </div>
    );
  },

  renderScreenshot: function(screenshot, index) {
    return (
      <a key={index} href="#">
        <img src={screenshot} />
      </a>
    );
  }
});
