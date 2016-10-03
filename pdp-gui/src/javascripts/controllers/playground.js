App.Controllers.Playground = {

  initialize: function() {
    page("/playground",
        this.loadIdentityProviders.bind(this),
        this.loadServiceProviders.bind(this),
        this.loadSamlAllowedAttributes.bind(this),
        this.loadPolicies.bind(this),
        this.playground.bind(this)
    );
  },

  loadPolicies: function(ctx, next) {
    $.get(App.apiUrl("/internal/policies"), data => {
      ctx.policies = data;
      next();
    });
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

  loadSamlAllowedAttributes: function(ctx, next) {
    $.get(App.apiUrl("/internal/saml-attributes"), data => {
      ctx.allowedSamlAttributes = data;
      next();
    });
  },

  playground: function(ctx) {
    App.render(App.Pages.Playground({
      key: "playground",
      pdpRequest: { attributes:[] },
      identityProviders: ctx.identityProviders,
      serviceProviders: ctx.serviceProviders,
      allowedSamlAttributes: ctx.allowedSamlAttributes,
      policies: ctx.policies
    }
      ));
  },

  postPdpRequest: function(json, callBack, failureCallback) {
    const jqxhr = $.ajax({
      url: App.apiUrl("/internal/decide/policy"),
      type: "POST",
      data: json
    }).done(() => {
      callBack(jqxhr);
    }).fail(() => {
      failureCallback(jqxhr);
    });
  }

};
