App.Controllers.User = {
  initialize: function() {
    if (App.currentUser.superUser) {
      page(
        "/users/search",
        this.loadUsers.bind(this),
        this.searchUser.bind(this)
      );

      page("/exit", this.exitUser.bind(this));
    }
    page("/profile", this.profile.bind(this));
    page("/logout", this.logoutUser.bind(this));
  },

  loadUsers: function(ctx, next) {
    $.get(App.apiUrl("/users/super/idps"), function(data) {
      ctx.idps = data.payload.idps;
      ctx.roles = data.payload.roles;
      next();
    });
  },

  logoutUser: function() {
    $.get(App.apiUrl("/logout"), function() {
      App.stop();
    });
  },

  exitUser: function() {
    $.get(App.apiUrl("/users/me/switch-to-idp"), function(data) {
      App.currentUser.switchedToIdp = null;
      page("/");
    });
  },

  switchToIdp: function(idp, role, callback) {
    $.get(App.apiUrl("/users/me/switch-to-idp", { idpId: idp.id, role: role }), function(data) {
      App.fetchUserData(function(user) {
        App.currentUser = user;
        App.currentUser.statsToken = App.fetchStatsToken();
        if (App.currentUser.statsToken == "") {
          return App.authorizeStats();
        }
        if (callback) callback();
      });
    });
  },

  searchUser: function(ctx) {
    App.render(App.Pages.SearchUser({idps: ctx.idps, roles: ctx.roles}));
  },

  profile: function() {
    App.render(App.Pages.Profile());
  }

};
