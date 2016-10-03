App.Controllers.About = {

  initialize: function() {
    page("/about",
        this.about.bind(this)
    );
  },

  about: function(ctx) {
    App.render(App.Pages.About());
  }
};
