App.Controllers.PolicyViolations = {

  initialize: function() {
    page("/violations",
        this.loadViolations.bind(this),
        this.loadIdentityProviders.bind(this),
        this.loadServiceProviders.bind(this),
        this.violations.bind(this)
    );

    page("/violations/:id",
        this.loadViolations.bind(this),
        this.loadIdentityProviders.bind(this),
        this.loadServiceProviders.bind(this),
        this.violations.bind(this)
    );
  },

  loadServiceProviders: function(ctx, next) {
    $.get(App.apiUrl("/internal/serviceProviders"), data => {
      ctx.serviceProviders = data;
      next();
    });
  },

  loadIdentityProviders: function(ctx, next) {
    $.get(App.apiUrl("/internal/identityProviders"), data => {
      ctx.identityProviders = data;
      next();
    });
  },

  loadViolations: function(ctx, next) {
    const url = ctx.params.id ?
        App.apiUrl("/internal/violations/:id", { id: ctx.params.id }) : App.apiUrl("/internal/violations");
    $.get(url, data => {
      ctx.violations = data;
      next();
    });
  },

  violations: function(ctx) {
    App.render(App.Pages.PolicyViolations({
      key: "violations",
      violations: ctx.violations,
      identityProviders: ctx.identityProviders,
      serviceProviders: ctx.serviceProviders
    }));
  },

  determineStatus: function(decision) {
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
        return "question";
      }
    default:
      {
        throw "Unknown decision" + decision;
      }
    }
  },

};
