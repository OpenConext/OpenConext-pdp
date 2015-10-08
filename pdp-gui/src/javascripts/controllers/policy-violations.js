App.Controllers.PolicyViolations = {

  initialize: function () {
    page("/violations",
        this.violations.bind(this)
    );
  },

  violations: function (ctx) {
    App.render(App.Pages.PolicyViolations());
  }
}
