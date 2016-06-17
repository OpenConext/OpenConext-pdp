App.Controllers.PolicyRevisions = {

  initialize: function () {
    page("/revisions/:id",
        this.loadRevisions.bind(this),
        this.revisions.bind(this)
    );
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
      revisions: ctx.revisions
    }));
  }

};
