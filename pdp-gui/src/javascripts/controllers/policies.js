App.Controllers.Policies = {

  initialize: function () {
    page("/policies",
        this.loadPolicies.bind(this),
        this.overview.bind(this)
    );

    page("/policies/:id/",
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
    if (ctx.init && ctx.init.policy) {
      ctx.policy = ctx.init.policy;
      next();
    } else {
      $.get(App.apiUrl("/internal/policies/id/" + ctx.params.id), function (data) {
        ctx.policy = data;
        //TODO App.apiUrl("/pdpPolicyViolations/search/findByAssociatedAdviceId?associatedAdviceId=http%3A%2F%2Fexample.com%2Fadvice%2FreasonForDeny
        next();
      });
    }
  },

  overview: function (ctx) {
    App.render(App.Pages.PolicyOverview({key: "policies", policies: ctx.policies}));
  },

  detail: function (ctx) {
    App.render(App.Pages.PolicyDetail({
      key: "policies",
      policies: ctx.policies,
      policy: ctx.policy
    }));
  }

}
