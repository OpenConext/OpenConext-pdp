App.Controllers.Policies = {

  initialize: function () {
    page("/policies",
        this.loadPolicies.bind(this),
        this.overview.bind(this)
    );

    page("/policy/:id/",
        this.loadPolicy.bind(this),
        this.detail.bind(this)
    );
  },

  loadPolicies: function (ctx, next) {
    $.get(App.apiUrl("/internal/policies"), function (data) {
      ctx.policies = data;
      next();
    });
  },

  loadPolicy: function (ctx, next) {
    $.get(App.apiUrl("/internal/policies/" + ctx.params.id), function (data) {
      ctx.policy = data;
      next();
    });
  },

  overview: function (ctx) {
    App.render(App.Pages.PolicyOverview({key: "policies", policies: ctx.policies}));
  },

  detail: function (ctx) {
    App.render(App.Pages.PolicyDetail({
      key: "policy",
      policy: ctx.policy
    }));
  }

}
