App.Controllers.Identity = {

  initialize: function() {
    page("/identity",
        this.loadIdentityProviders.bind(this),
        this.identity.bind(this)
    );
  },

  loadIdentityProviders: function(ctx, next) {
    $.get(App.apiUrl("/internal/identityProviders"), data => {
      ctx.identityProviders = data;
      next();
    });
  },

  identity: function(ctx) {
    App.render(App.Pages.Identity({ key: "identity", identityProviders: ctx.identityProviders, identity: App.getIdentity() }));
  }

};
