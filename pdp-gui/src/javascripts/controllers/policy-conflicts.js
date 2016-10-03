App.Controllers.PolicyConflicts = {

  initialize: function() {
    page("/conflicts",
        this.loadConflicts.bind(this),
        this.conflicts.bind(this)
    );
  },

  loadConflicts: function(ctx, next) {
    const url = App.apiUrl("/internal/conflicts");
    $.get(url, data => {
      ctx.conflicts = data;
      next();
    });
  },

  conflicts: function(ctx) {
    App.render(App.Pages.PolicyConflicts({
      key: "conflicts",
      conflicts: ctx.conflicts
    }));
  }

};
