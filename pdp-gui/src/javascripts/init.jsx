/** @jsx React.DOM */

var App = {
  Components: {},
  Pages: {},
  Controllers: {},
  Mixins: {},

  store: {}, // in memory key/value store, to save temporary settings

  initialize: function () {
    I18n.locale = "en";
    $(document).ajaxError(this.ajaxError.bind(this));
    $(document).ajaxStart(this.showSpinner.bind(this));
    $(document).ajaxStop(this.hideSpinner.bind(this));
    $(document).ajaxComplete(this.checkSessionExpired.bind(this));

    this.fetchUserData(function (user) {
      this.currentUser = user;

      this.currentUser.statsToken = this.fetchStatsToken();
      if (!this.currentUser.statsToken) {
        return this.authorizeStats();
      }

      $(document).ajaxSend(function (event, jqxhr, settings) {
        if (settings.url.indexOf(STATS_HOST) < 0) {
          jqxhr.setRequestHeader("X-IDP-ENTITY-ID", this.currentIdpId());
        }
      }.bind(this));

      for (controller in App.Controllers) {
        App.Controllers[controller].initialize();
      }

      page("/", this.rootPath.bind(this));
      page("*", this.actionNotFound.bind(this));

      if (this.superUserNotSwitched()) {
        page.start({dispatch: false});
        page("/users/search");
      } else {
        page.start();
      }
    }.bind(this));
  },

  actionNotFound: function () {
    this.render(App.Pages.NotFound());
  },

  superUserNotSwitched: function () {
    return this.currentUser.superUser && !this.currentUser.switchedToIdp;
  },

  pageRequested: function () {
    //the redirect_uri goes to /, but we have stored the requested href in the state parameter
    var locationHash = window.location.hash.substr(1);
    var url = locationHash.substr(locationHash.indexOf("state=")).split("&")[0].split("=")[1];
    if (!url) {
      return url;
    }
    var parser = document.createElement('a');
    parser.href = decodeURIComponent(url);
    return parser.pathname
  },

  rootPath: function () {
    if (this.superUserNotSwitched()) {
      page.redirect("/users/search");
    } else {
      var requestedPage = this.pageRequested();
      if (requestedPage) {
        page.redirect(requestedPage);
      } else {
        page.redirect("/apps");
      }
    }
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

  authorizeStats: function () {
    window.location = this.currentUser.statsUrl + "&state=" + window.location;
  },

  fetchStatsToken: function () {
    var locationHash = window.location.hash.substr(1);
    return locationHash.substr(locationHash.indexOf("access_token=")).split("&")[0].split("=")[1];
  },

  fetchUserData: function (callback) {

    var redirectTo403Server = function () {
      window.location = window.location.protocol + "//" + window.location.host + "/dashboard/api/forbidden";
    };

    $.get(App.apiUrl("/users/me" + window.location.search), function (data) {
      if (!data.payload) {
        redirectTo403Server();
        return;
      }
      I18n.locale = data.language;
      callback(data.payload);
    }).fail(function (data) {
      redirectTo403Server();
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

  checkSessionExpired: function (event, xhr) {
    //do not handle anything other then 200 and 302 as the others are handled by ajaxError
    if (xhr.getResponseHeader("sessionAlive") !== "success" && (xhr.status === 0 || xhr.status === 200 || xhr.status === 302)) {
      if (window.location.hostname === "localhost") {
        window.location.href = window.location.protocol + "//" + window.location.host + "/dashboard/api/home?redirectTo=" + window.location.pathname;
      } else {
        window.location.href = window.location.protocol + "//" + window.location.host + "/apps";
      }
    }
  },

  currentIdpId: function () {
    return this.currentIdp().id;
  },

  currentIdp: function () {
    if (this.currentUser.superUser && this.currentUser.switchedToIdp) {
      return this.currentUser.switchedToIdp;
    } else {
      return (this.currentUser.switchedToIdp || this.currentUser.currentIdp);
    }
  }
};
