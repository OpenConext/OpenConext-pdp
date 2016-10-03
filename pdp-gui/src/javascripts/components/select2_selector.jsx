import React from "react";

class Select2Selector extends React.Component {

  getInitialState() {
    const initialValue = this.props.defaultValue || (this.props.multiple ? [] : "");
    return {
      value: initialValue
    };
  }

  componentDidMount() {
    const rootNode = $("[data-select2selector-id=\"" + this.props.select2selectorId + "\"]");
    const allowClear = this.props.multiple ? false : true;
    rootNode.select2({
      width: "100%",
      placeholder: this.props.placeholder,
      allowClear: allowClear,
      forceBelow: true
    });
    const initialValue = this.props.defaultValue || (this.props.multiple ? [] : "");
    rootNode.val(initialValue).trigger("change");
    //This is not the react way, but this react version does not support native Select2 ports
    rootNode.on("change", this.handleChange);
  }

  componentWillUnmount() {
    const rootNode = $("[data-select2selector-id=\"" + this.props.select2selectorId + "\"]");
    rootNode.select2("destroy");
  }

  handleChange(e) {
    const newValue = this.props.multiple ? $("[data-select2selector-id=\"" + this.props.select2selectorId + "\"]").val() : e.target.value;
    this.props.handleChange(newValue);
  }

  render() {
    const initialValue = this.props.defaultValue || (this.props.multiple ? [] : "");
    const renderOption = this.props.options.map((option, index) => {
      return (<option key={option.value} value={option.value}>{option.display}</option>);
    });
    const multiple = this.props.multiple ? { multiple: "multiple" } : {};
    return (
        <div>
          <select id="lang" value={initialValue} data-select2selector-id={this.props.select2selectorId} {...multiple}>
            <option></option>
            {renderOption}
          </select>
        </div>
    );
  }
}

export default Select2Selector;
