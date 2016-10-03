import React from "react";

class Navigation extends React.Component {

  componentDidUpdate() {
    if (this.props.loading) {
      if (!this.spinner) {
        this.spinner = new Spinner({
          lines: 25, // The number of lines to draw
          length: 25, // The length of each line
          width: 4, // The line thickness
          radius: 20, // The radius of the inner circle
          color: '#4DB3CF', // #rgb or #rrggbb or array of colors
        }).spin(this.refs.spinner.getDOMNode());
      }
    } else {
      this.spinner = null;
    }
  }

  render() {
    return (
      <div className="mod-navigation">
        <ul>
          {this.renderItem("/policies", "policies")}
          {this.renderItem("/violations", "violations")}
          {this.renderItem("/conflicts", "conflicts")}
          {this.renderItem("/decisions", "decisions")}
          {this.renderItem("/playground", "playground")}
          {this.renderItem("/identity", "identity")}
          {this.renderItem("/new-policy", "new_policy", true)}
        </ul>

        {this.renderSpinner()}
      </div>
    );
  }

  renderItem(href, value, right) {
    var classNameLi = (this.props.active == value ? "active" : "");
    var classNameA = "";
    if (right) {
      classNameLi = classNameLi + " action";
      classNameA = "action";
    }
    return (
      <li className={classNameLi}><a href={href} className={classNameA}>{I18n.t("navigation." + value)}</a></li>
    );
  }

  renderSpinner() {
    if (this.props.loading) {
      return <div className="spinner" ref="spinner" />;
    }
  }
}

export default Navigation;
