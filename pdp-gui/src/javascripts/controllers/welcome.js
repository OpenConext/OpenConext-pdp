App.Controllers.Welcome = {

  initialize: function () {
    page("/welcome",
        this.welcome.bind(this)
    );
  },

  welcome: function (ctx) {
    App.render(App.Pages.Welcome());
  }
}
