App.Controllers.PolicyRevisions = {

  initialize: function () {
    page("/revisions/:id",
        this.loadRevisions.bind(this),
        this.loadIdentityProviders.bind(this),
        this.loadServiceProviders.bind(this),
        this.revisions.bind(this)
    );
  },

  loadServiceProviders: function (ctx, next) {
    $.get(App.apiUrl("/internal/serviceProviders"), function (data) {
      ctx.serviceProviders = data;
      next();
    });
  },

  loadIdentityProviders: function (ctx, next) {
    $.get(App.apiUrl("/internal/identityProviders"), function (data) {
      ctx.identityProviders = data;
      next();
    });
  },

  loadRevisions: function (ctx, next) {
    var url = App.apiUrl("/internal/revisions/:id", {id: ctx.params.id});
    $.get(url, function (data) {
      ctx.revisions = data;
      next();
    });
  },

  revisions: function (ctx) {
    App.render(App.Pages.PolicyRevisions({
      key: "revisions",
      revisions: ctx.revisions,
      identityProviders: ctx.identityProviders,
      serviceProviders: ctx.serviceProviders
    }));
  }

};
