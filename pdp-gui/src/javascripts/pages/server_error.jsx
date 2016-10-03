import React from "react";
import I18n from "i18n-js";

class ServerError extends React.Component {
  render() {
    return (
      <div className="mod-not-found">
        <h1>{I18n.t("server_error.title")}</h1>
        <p dangerouslySetInnerHTML={{ __html: I18n.t("server_error.description_html") }} />
      </div>
    );
  }
}

export default ServerError;
