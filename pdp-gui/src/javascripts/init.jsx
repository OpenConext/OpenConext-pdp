/** @jsx React.DOM */

var App = {
  Components: {},
  Help: {},
  Pages: {},
  Controllers: {},
  Utils: {},


  store: {}, // in memory key/value store, to save temporary settings

  initialize: function () {
    this.determineLanguage();

    $(document).ajaxError(this.ajaxError.bind(this));
    $(document).ajaxStart(this.showSpinner.bind(this));
    $(document).ajaxStop(this.hideSpinner.bind(this));
    $(document).ajaxComplete(this.checkSessionExpired.bind(this));

    this.setupAjaxSend();

    this.fetchUserData(function (user) {
      this.currentUser = user;
      for (controller in App.Controllers) {
        App.Controllers[controller].initialize();
      }

      page("/", this.rootPath.bind(this));
      page("*", this.actionNotFound.bind(this));

      page.start();
    }.bind(this));
  },

  actionNotFound: function () {
    this.render(App.Pages.NotFound());
  },

  rootPath: function () {
    page.redirect("/policies");
  },

  render: function (page) {
    if (this.mainComponent) {
      //stupid hack to ensure non-controlled components are updated. the performance penalty is to neglect
      this.mainComponent.setProps({
        page: App.Pages.Empty()
      });
      this.mainComponent.setProps({
        page: page
      });
    } else {
      this.mainComponent = React.renderComponent(App.Components.Main({page: page}), document.getElementById("app"));
    }

  },

  apiUrl: function (value, params) {
    return page.uri(BASE_URL + value, params);
  },

  setupAjaxSend: function () {
    $(document).ajaxSend(function (event, jqxhr, settings) {
      jqxhr.setRequestHeader("Content-Type", "application/json");
      jqxhr.setRequestHeader("X-CSRF-TOKEN", this.crsfToken);

      if (this.store.identity) {
        jqxhr.setRequestHeader("X-IDP-ENTITY-ID", this.store.identity.idpEntityId);
        jqxhr.setRequestHeader("X-UNSPECIFIED-NAME-ID", this.store.identity.unspecifiedNameId);
        jqxhr.setRequestHeader("X-DISPLAY-NAME", this.store.identity.displayName);
        jqxhr.setRequestHeader("X_IMPERSONATE", "true");
      }

    }.bind(this));
  },

  fetchUserData: function (callback) {
    var self = this;
    $.get(App.apiUrl("/internal/users/me"), function (data) {
      if (!data) {
        self.render(App.Pages.NotFound());
      } else {
        callback(data);
      }
    }).fail(function (data) {
      self.render(App.Pages.NotFound());
    });
  },

  showSpinner: function () {
    if (this.mainComponent) {
      this.mainComponent.setProps({loading: true});
    }
  },

  hideSpinner: function () {
    if (this.mainComponent) {
      this.mainComponent.setProps({loading: false});
    }
  },

  checkSessionExpired: function (event, xhr) {
    //do not handle anything other then 200 and 302 as the others are handled by ajaxError
    if (xhr.getResponseHeader("X-SESSION-ALIVE") !== "true" && (xhr.status === 0 || xhr.status === 200 || xhr.status === 302)) {
      var newLoc = window.location.protocol + "//" + window.location.host + "/policies";
      window.location.href = newLoc;
    }
    var csrfToken = xhr.getResponseHeader("X-CSRF-TOKEN");
    if (!_.isEmpty(csrfToken)) {
      this.crsfToken = csrfToken;
    }

  },

  determineLanguage: function () {
    var parameterByName = App.Utils.QueryParameter.getParameterByName("lang");
    if (_.isEmpty(parameterByName)) {
      parameterByName = Cookies.get("lang");
    }
    I18n.locale = parameterByName ? parameterByName : "en";
  },

  ajaxError: function (event, xhr) {
    if (xhr.isConsumed) {
      return;
    }
    switch (xhr.status) {
      case 404:
        App.actionNotFound();
        break;
      default:
        this.render(App.Pages.ServerError());
        console.error("Ajax request failed");
    }
  },

  setFlash: function (message) {
    this.store.flash = message;
  },

  getFlash: function () {
    var message = this.store.flash;
    this.store.flash = undefined;
    return message;
  },

  getIdentity: function () {
    return this.store.identity || {};
  },

  changeIdentity: function (idpEntityId, unspecifiedNameId, displayName) {
    this.store.identity = {idpEntityId: idpEntityId, unspecifiedNameId: unspecifiedNameId, displayName: displayName};
    this.initialize();
    this.rootPath();
  },

  clearIdentity: function () {
    delete this.store.identity;
    this.initialize();
    this.rootPath();
  }

};
