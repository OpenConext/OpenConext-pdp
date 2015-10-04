/** @jsx React.DOM */

var App = {
  Components: {},
  Pages: {},
  Controllers: {},
  Mixins: {},

  store: {}, // in memory key/value store, to save temporary settings

  initialize: function () {
    var parameterByName = this.getParameterByName("lang");
    I18n.locale = parameterByName ? parameterByName : "en";
    $(document).ajaxError(this.ajaxError.bind(this));
    $(document).ajaxStart(this.showSpinner.bind(this));
    $(document).ajaxStop(this.hideSpinner.bind(this));

    $(document).ajaxSend(function (event, jqxhr, settings) {
      jqxhr.setRequestHeader("Content-Type", "application/json");
    }.bind(this));

    $.ajaxSetup({
      contentType: "application/json"
    });

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
      this.mainComponent.setProps({
        page: page
      });
    } else {
      this.mainComponent = React.renderComponent(App.Components.Main({page: page}), document.getElementById("app"));
    }
  },

  stop: function () {
    var node = document.getElementById("app");
    React.unmountComponentAtNode(node);
    React.renderComponent(App.Pages.Logout(), node);
  },

  apiUrl: function (value, params) {
    return page.uri(BASE_URL + value, params);
  },

  renderYesNo: function (value) {
    var word = value ? "yes" : "no";
    return <td className={word}>{I18n.t("boolean." + word)}</td>;
  },

  fetchUserData: function (callback) {
    var self = this;
    $.get(App.apiUrl("/internal/users/me" + window.location.search), function (data) {
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

  ajaxError: function (event, xhr) {
    switch (xhr.status) {
      case 404:
        App.actionNotFound();
        break;
      default:
        this.render(App.Pages.ServerError());
        console.error("Ajax request failed");
    }
  },

  getParameterByName: function(name) {
  name = name.replace(/[\[]/, "\\[").replace(/[\]]/, "\\]");
  var regex = new RegExp("[\\?&]" + name + "=([^&#]*)"),
      results = regex.exec(location.search);
  return results === null ? "" : decodeURIComponent(results[1].replace(/\+/g, " "));
}
};
