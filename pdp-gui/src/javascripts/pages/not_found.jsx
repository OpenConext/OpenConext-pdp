import React from "react";

class NotFound extends React.Component {
  render() {
    return (
      <div className="mod-not-found">
        <h1>{I18n.t("not_found.title")}</h1>
        <p dangerouslySetInnerHTML={{ __html: I18n.t("not_found.description_html") }} />
      </div>
    );
  }
}

export default NotFound;
