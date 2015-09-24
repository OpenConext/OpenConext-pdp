App.Controllers.Stats = {
  initialize: function() {
    page("/statistics",
      this.loadApps.bind(this),
      this.showStats.bind(this)
    );
  },

  loadApps: function(ctx, next) {
    $.get(App.apiUrl("/services"), function(data) {
      ctx.apps = data.payload;
      next();
    });
  },

  showStats: function(ctx) {
    App.render(App.Pages.Stats({key: "stats", apps: ctx.apps}));
  }
}
