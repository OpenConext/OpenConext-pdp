App.Controllers.Apps = {

  initialize: function () {
    page("/apps",
      this.loadFacets.bind(this),
      this.loadApps.bind(this),
      this.overview.bind(this)
    );

    page("/apps/:id/:active_panel?",
      this.loadApp.bind(this),
      this.detail.bind(this)
    );
  },

  loadApps: function (ctx, next) {
    $.get(App.apiUrl("/services"), function (data) {
      ctx.apps = data.payload;
      next();
    });
  },

  loadApp: function (ctx, next) {
    if (ctx.init && ctx.init.app && ctx.init.institutions) {
      ctx.app = ctx.init.app;
      ctx.institutions = ctx.init.institutions;
      next();
    } else {
      $.get(App.apiUrl("/services/id/" + ctx.params.id), function (data) {
        ctx.app = data.payload;
        $.get(App.apiUrl("/services/idps", {spEntityId: ctx.app.spEntityId}), function (data) {
          ctx.institutions = data.payload;
          next();
        });
      });
    }
  },

  loadFacets: function (ctx, next) {
    $.get(App.apiUrl("/facets"), function (data) {
      ctx.facets = data.payload;
      next();
    });
  },

  overview: function (ctx) {
    // We need to sanitize the categories data for each app to ensure the facet totals are correct
    var unknown = {value: I18n.t("facets.unknown")};
    ctx.facets.forEach(function (facet) {
      ctx.apps.forEach(function (app) {
        app.categories = app.categories || [];
        var appCategory = app.categories.filter(function (category) {
          return category.name === facet.name;
        });
        if (appCategory.length === 0) {
          app.categories.push({name: facet.name, values: [unknown]});
          var filtered = facet.values.filter(function (facetValue) {
            return facetValue.value === unknown.value;
          });
          facet.values.push(unknown);
        }
      });
    });
    App.render(App.Pages.AppOverview({key: "apps", apps: ctx.apps, facets: ctx.facets}));
  },

  detail: function (ctx) {
    App.render(App.Pages.AppDetail({
      key: "apps",
      apps: ctx.apps,
      app: ctx.app,
      institutions: ctx.institutions,
      activePanel: ctx.params.active_panel
    }));
  },

  makeConnection: function (app, comments, callback) {
    if (App.currentUser.dashboardAdmin) {
      $.post(
        App.apiUrl("/services/id/" + app.id + "/connect"),
        {comments: comments, spEntityId: app.spEntityId},
        function () {
          if (callback) callback();
        });
    }
  },

  disconnect: function (app, comments, callback) {
    if (App.currentUser.dashboardAdmin) {
      $.post(App.apiUrl("/services/id/" + app.id + "/disconnect"), {
        comments: comments,
        spEntityId: app.spEntityId
      }, function () {
        if (callback) callback();
      });
    }
  },

  downloadOverview: function (apps) {
    var ids = apps.map(function (app) {
      return app.id;
    });

    window.open(App.apiUrl("/services/download", {idpEntityId: App.currentIdpId(), id: ids}));
  }
}
