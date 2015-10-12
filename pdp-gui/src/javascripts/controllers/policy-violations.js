App.Controllers.PolicyViolations = {

  initialize: function () {
    page("/violations",
        this.loadViolations.bind(this),
        this.loadIdentityProviders.bind(this),
        this.loadServiceProviders.bind(this),
        this.violations.bind(this)
    );

    page("/violations/:policyId",
        this.loadViolations.bind(this),
        this.loadIdentityProviders.bind(this),
        this.loadServiceProviders.bind(this),
        this.violations.bind(this)
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

  loadViolations: function (ctx, next) {
    var url = ctx.params.policyId ?
        App.apiUrl("/internal/violations/:policyId", {policyId: encodeURIComponent(ctx.params.policyId)}) : App.apiUrl("/internal/violations");
    $.get(url, function (data) {
      ctx.violations = data;
      next();
    });
  },

  violations: function (ctx) {
    App.render(App.Pages.PolicyViolations({
      key: "violations",
      violations: ctx.violations,
      identityProviders: ctx.identityProviders,
      serviceProviders: ctx.serviceProviders
    }));
  },

  determineStatus: function (decision) {
    switch (decision) {
      case "Permit":
      {
        return "check";
      }
      case "Indeterminate":
      case "Deny":
      {
        return "remove";
      }
      case "NotApplicable":
      {
        return "question"
      }
      default:
      {
        throw "Unknown decision" + decision;
      }
    }
  },

}
