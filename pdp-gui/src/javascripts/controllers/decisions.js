App.Controllers.Decisions = {

  initialize: function() {
    page("/decisions",
        this.loadDecisions.bind(this),
        this.decisions.bind(this)
    );
  },

  loadDecisions: function(ctx, next) {
    const url = App.apiUrl("/internal/decisions?daysAgo=365");
    $.get(url, data => {
      ctx.decisions = data;
      next();
    });
  },

  decisions: function(ctx) {
    App.render(App.Pages.Decisions({
      key: "decisions",
      decisions: ctx.decisions
    }));
  }

};
