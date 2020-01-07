import React from "react";
import I18n from "i18n-js";

class SessionExpired extends React.Component {
    render() {
        return (
            <div className="mod-session-expired">
                <h1>{I18n.t("session_expired.title")}</h1>
                <p dangerouslySetInnerHTML={{__html: I18n.t("session_expired.description_html")}}/>
            </div>
        );
    }
}

export default SessionExpired;
