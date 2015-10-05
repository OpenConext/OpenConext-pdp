App.Controllers.Policies = {

  initialize: function () {
    page("/policies",
        this.loadPolicies.bind(this),
        this.overview.bind(this)
    );

    page("/policy/:id",
        this.loadPolicy.bind(this),
        this.loadIdentityProviders.bind(this),
        this.loadServiceProviders.bind(this),
        this.detail.bind(this)
    );

    page("/new-policy",
        this.loadPolicy.bind(this),
        this.loadIdentityProviders.bind(this),
        this.loadServiceProviders.bind(this),
        this.detail.bind(this)
    );
  },

  loadPolicies: function (ctx, next) {
    $.get(App.apiUrl("/internal/policies"), function (data) {
      ctx.policies = data;
      next();
    });
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

  loadPolicy: function (ctx, next) {
    if (ctx.params.id) {
      $.get(App.apiUrl("/internal/policies/:id" ,{id: ctx.params.id}), function (data) {
        ctx.policy = data;
        next();
      });
    } else {
      ctx.policy = {};
      next();
    }
  },

  overview: function (ctx) {
    App.render(App.Pages.PolicyOverview({key: "policies", policies: ctx.policies}));
  },

  detail: function (ctx) {
    App.render(App.Pages.PolicyDetail({
          key: "policy",
          policy: ctx.policy,
          identityProviders: ctx.identityProviders,
          serviceProviders: ctx.serviceProviders
        }
    ))
    ;
  },

  saveOrUpdatePolicy: function(policy) {
    var type = policy.id ? "PUT" : "POST";
    $.ajax({
      url: App.apiUrl("/internal/policies"),
      type: type,
      data: policy,
      success: function(result) {
        page("/policies");
      }
    });
  },

  deletePolicy: function(policy) {
    $.ajax({
      url: App.apiUrl("/internal/policies/:id", { id: policy.id }),
      type: 'DELETE',
      success: function(result) {
        page("/policies");
      }
    });
  }


};
