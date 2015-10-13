/** @jsx React.DOM */

App.Components.LanguageSelector = React.createClass({
  render: function () {
    return (
        <ul className="language">
          {[
            this.renderLocaleChooser("en"),
            this.renderLocaleChooser("nl")
          ]}
        </ul>
    );
  },

  renderLocaleChooser: function (locale) {
    return (
        <li key={locale}>
          <a
              href="#"
              className={I18n.currentLocale() == locale ? "selected" : ""}
              title={I18n.t("select_locale", {locale: locale})}
              onClick={this.handleChooseLocale(locale)}>
            {I18n.t("code", {locale: locale})}
          </a>
        </li>
    );
  },

  handleChooseLocale: function (locale) {
    return function (e) {
      e.preventDefault();
      e.stopPropagation();
      I18n.locale = locale;
      //var search = App.Utils.QueryParameter.removeQueryParameter("lang");
      //search = App.Utils.QueryParameter.addQueryParameter("lang", locale);
      var loc = window.location.href;
      var index = loc.indexOf("?");
      loc = loc.substring(0, index === -1 ? loc.length : index) + "?lang=" + locale;
      window.location.href = loc;
    }.bind(this);
  }
});
