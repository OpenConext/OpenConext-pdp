App.Controllers.History = {

  initialize: function() {
    page("/history", this.loadActions.bind(this), this.index.bind(this));
  },

  index: function(ctx) {
    App.render(App.Pages.History({key: "history", actions: ctx.actions}));
  },

  loadActions: function(ctx, next) {
    $.get(App.apiUrl("/actions"), function(data) {
      ctx.actions = data.payload;
      next();
    });
  }
}
