import React from "react";
import I18n from "i18n-js";

class LanguageSelector extends React.Component {
  render() {
    return (
        <ul className="language">
          {[
            this.renderLocaleChooser("en"),
            this.renderLocaleChooser("nl")
          ]}
        </ul>
    );
  }

  renderLocaleChooser(locale) {
    return (
        <li key={locale}>
          <a
              href="#"
              className={I18n.currentLocale() == locale ? "selected" : ""}
              title={I18n.t("select_locale", { locale: locale })}
              onClick={this.handleChooseLocale(locale)}>
            {I18n.t("code", { locale: locale })}
          </a>
        </li>
    );
  }

  handleChooseLocale(locale) {
    return function(e) {
      e.preventDefault();
      e.stopPropagation();
      Cookies.set("lang", locale, { expires: 356, secure: document.location.protocol.endsWith("https") });
      I18n.locale = locale;
      const newSearch = App.Utils.QueryParameter.replaceQueryParameter("lang", locale);
      window.location.search = newSearch;
    }.bind(this);
  }
}

export default LanguageSelector;
