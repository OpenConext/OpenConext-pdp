App.Controllers.MyIdp = {
  initialize: function () {
    page("/my-idp", this.loadIdpRolesWithUsers.bind(this), this.loadLicenseContactPerson.bind(this), this.myIdp.bind(this));
  },

  myIdp: function (ctx) {
    App.render(App.Pages.MyIdp({key: "my_idp", roles: ctx.roles, licenseContactPersons: ctx.licenseContactPersons}));
  },

  loadIdpRolesWithUsers: function (ctx, next) {
    $.get(App.apiUrl("/idp/current/roles"), function (data) {
      ctx.roles = data.payload;
      next();
    });
  },

  loadLicenseContactPerson: function (ctx, next) {
    $.get(App.apiUrl("/idp/licensecontactpersons"), function (data) {
      ctx.licenseContactPersons = data.payload;
      next();
    });
  }
};
