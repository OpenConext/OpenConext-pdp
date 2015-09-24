App.Controllers.Notifications = {

  initialize: function() {
    page("/notifications", this.loadNotifications.bind(this), this.index.bind(this));
  },

  index: function(ctx                     ) {
    App.render(App.Pages.Notifications({key: "notifications", notificationMessage: ctx.notificationMessage}));
  },

  loadNotifications: function(ctx, next) {
    $.get(App.apiUrl("/notifications"), function(data) {
      ctx.notificationMessage = data.payload;
      next();
    });
  }
}
