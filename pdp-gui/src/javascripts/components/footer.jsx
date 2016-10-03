import React from "react";
import I18n from "i18n-js";

class Footer extends React.Component {
  render() {
    return (
      <div className="mod-footer">
        <ul>
          <li dangerouslySetInnerHTML={{__html: I18n.t("footer.surfnet_html") }}></li>
          <li dangerouslySetInnerHTML={{__html: I18n.t("footer.terms_html") }}></li>
          <li dangerouslySetInnerHTML={{__html: I18n.t("footer.contact_html") }}></li>
        </ul>
      </div>
    );
  }
}

export default Footer;
