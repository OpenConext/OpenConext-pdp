import React from "react";
import isArray from "lodash/isArray";

import Select from "react-select";

class SelectWrapper extends React.Component {
    onChange(val) {
        if (isArray(val)) {
            return this.props.handleChange(val.map(v => v.value), val.map(v => v.label));
        }

        return this.props.handleChange(val.value, val.label);
    }

    render() {
        const defaultValue = this.props.defaultValue || (this.props.multiple ? [] : "");
        const data = this.props.options.map(option => ({label: option.display, value: option.value}));
        const minimumResultsForSearch = this.props.minimumResultsForSearch || 7;

        return (
            <Select
                value={defaultValue}
                options={data}
                multi={this.props.multiple}
                onChange={val => this.onChange(val)}
                style={{width: "100%"}}
                placeholder={this.props.placeholder}
                searchable={data.length >= minimumResultsForSearch}
                clearable={false}
            />
        );
    }
}

SelectWrapper.propTypes = {
    handleChange: React.PropTypes.func.isRequired,
    defaultValue: React.PropTypes.oneOfType([
        React.PropTypes.string,
        React.PropTypes.arrayOf(React.PropTypes.string),
        React.PropTypes.number
    ]),
    multiple: React.PropTypes.bool,
    options: React.PropTypes.arrayOf(React.PropTypes.shape({
        display: React.PropTypes.string,
        value: React.PropTypes.oneOfType([
            React.PropTypes.string,
            React.PropTypes.number
        ])
    })),
    placeholder: React.PropTypes.string,
    minimumResultsForSearch: React.PropTypes.number
};

export default SelectWrapper;
