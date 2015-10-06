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
      $.get(App.apiUrl("/internal/policies/:id", {id: ctx.params.id}), function (data) {
        ctx.policy = data;
        next();
      });
    } else {
      ctx.policy = {};
      next();
    }
  },

  overview: function (ctx) {
    App.render(App.Pages.PolicyOverview({key: "policies", policies: ctx.policies, flash: App.getFlash()}));
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

  saveOrUpdatePolicy: function (policy, failureCallback) {
    var type = policy.id ? "PUT" : "POST";
    var json = JSON.stringify(policy);
    var jqxhr = $.ajax({
      url: App.apiUrl("/internal/policies"),
      type: type,
      data: json
    }).done(function () {
      App.setFlash("Policy '" + policy.name + "' was successfully created");
      page("/policies");
    }).fail(function () {
      failureCallback(jqxhr);
    });
  },

  deletePolicy: function (policy) {
    $.ajax({
      url: App.apiUrl("/internal/policies/:id", {id: policy.id}),
      type: 'DELETE'
    }).done(function () {
      App.setFlash("Policy '" + policy.name + "' was successfully deleted");
      page("/policies");
    });
  }


};
