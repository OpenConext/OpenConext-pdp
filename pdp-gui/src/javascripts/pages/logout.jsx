import React from "react";

class Logout extends React.Component {
  render: function () {
    return (
      <div className="mod-logout">
        {I18n.t("logout.title")}<br />
        <span dangerouslySetInnerHTML={{ __html: I18n.t("logout.description_html")}} />
      </div>
    );
  }
}

export default Logout;
