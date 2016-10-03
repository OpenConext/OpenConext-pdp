/** @jsx React.DOM */

const App = {
  Components: {},
  Help: {},
  Pages: {},
  Controllers: {},
  Utils: {},


  store: {}, // in memory key/value store, to save temporary settings

  initialize: function() {
    this.determineLanguage();

    $(document).ajaxError(this.ajaxError.bind(this));
    $(document).ajaxStart(this.showSpinner.bind(this));
    $(document).ajaxStop(this.hideSpinner.bind(this));
    $(document).ajaxComplete(this.checkSessionExpired.bind(this));

    this.setupAjaxSend();

    this.currentUser = {};

    this.fetchUserData(user => {
      this.currentUser = user;
      for (controller in App.Controllers) {
        App.Controllers[controller].initialize();
      }

      page("/", this.rootPath.bind(this));
      page("*", this.actionNotFound.bind(this));

      page.start();
    });
  },

  actionNotFound: function() {
    this.render(App.Pages.NotFound());
  },

  rootPath: function() {
    page.redirect("/policies");
  },

  render: function(page) {
    if (this.mainComponent) {
      //stupid hack to ensure non-controlled components are updated. the performance penalty is to neglect
      this.mainComponent.setProps({
        page: App.Pages.Empty()
      });
      this.mainComponent.setProps({
        page: page
      });
    } else {
      this.mainComponent = React.renderComponent(App.Components.Main({ page: page }), document.getElementById("app"));
    }

  },

  apiUrl: function(value, params) {
    return page.uri(BASE_URL + value, params);
  },

  setupAjaxSend: function() {
    $(document).ajaxSend((event, jqxhr, settings) => {
      jqxhr.setRequestHeader("Content-Type", "application/json");
      jqxhr.setRequestHeader("X-CSRF-TOKEN", this.crsfToken);

      if (this.store.identity) {
        jqxhr.setRequestHeader("X-IDP-ENTITY-ID", this.store.identity.idpEntityId);
        jqxhr.setRequestHeader("X-UNSPECIFIED-NAME-ID", this.store.identity.unspecifiedNameId);
        jqxhr.setRequestHeader("X-DISPLAY-NAME", this.store.identity.displayName);
        jqxhr.setRequestHeader("X-IMPERSONATE", "true");
      }

    });
  },

  fetchUserData: function(callback) {
    const self = this;
    $.get(App.apiUrl("/internal/users/me"), data => {
      if (!data) {
        self.render(App.Pages.NotFound());
      } else {
        callback(data);
      }
    }).fail(data => {
      //not good
      self.currentUser = {};
      self.render(App.Pages.NotFound());
    });
  },

  showSpinner: function() {
    if (this.mainComponent) {
      this.mainComponent.setProps({ loading: true });
    }
  },

  hideSpinner: function() {
    if (this.mainComponent) {
      this.mainComponent.setProps({ loading: false });
    }
  },

  checkSessionExpired: function(event, xhr) {
    //do not handle anything other then 200 and 302 as the others are handled by ajaxError
    if (xhr.getResponseHeader("X-SESSION-ALIVE") !== "true" && (xhr.status === 0 || xhr.status === 200 || xhr.status === 302)) {
      window.location.reload(true);
    }
    const csrfToken = xhr.getResponseHeader("X-CSRF-TOKEN");
    if (!_.isEmpty(csrfToken)) {
      this.crsfToken = csrfToken;
    }
  },

  determineLanguage: function() {
    let parameterByName = App.Utils.QueryParameter.getParameterByName("lang");
    if (_.isEmpty(parameterByName)) {
      parameterByName = Cookies.get("lang");
    }
    I18n.locale = parameterByName ? parameterByName : "en";
  },

  ajaxError: function(event, xhr) {
    if (xhr.isConsumed) {
      return;
    }
    switch (xhr.status) {
    case 404:
      App.actionNotFound();
      break;
    case 403:
      App.actionNotFound();
      break;
    default:
      this.render(App.Pages.ServerError());
      console.error("Ajax request failed");
    }
  },

  setFlash: function(message) {
    this.store.flash = message;
  },

  getFlash: function() {
    const message = this.store.flash;
    this.store.flash = undefined;
    return message;
  },

  getIdentity: function() {
    return this.store.identity || {};
  },

  changeIdentity: function(idpEntityId, unspecifiedNameId, displayName) {
    this.store.identity = { idpEntityId: idpEntityId, unspecifiedNameId: unspecifiedNameId, displayName: displayName };
    this.initialize();
    this.rootPath();
  },

  clearIdentity: function() {
    delete this.store.identity;
    this.initialize();
    this.rootPath();
  }

};
