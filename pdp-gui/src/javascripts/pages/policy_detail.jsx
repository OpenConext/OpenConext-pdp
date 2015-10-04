/** @jsx React.DOM */
var MySelect = React.createClass({
  getInitialState: function () {
    return {
      value: 'select'
    }
  },
  propTypes: {
    // Array of <option /> components to be placed into the select
    children: React.PropTypes.array,

    name: React.PropTypes.string,
    multiple: React.PropTypes.bool,

    // The initial selected value; one of the option children should have a
    // matching value="..."
    defaultValue: React.PropTypes.string,

    // Callback executed when the selected value changes; receives a single
    // jQuery event object `e` from select2; `e.target` refers to the real
    // <select> element and `e.val` refers to the new selected value
    onChange: React.PropTypes.func
  },

  change: function (event) {
    this.setState({value: event.target.value});
  },
  componentDidMount: function () {
    var rootNode = $('[data-id]');
    $(rootNode).select2();

    if (this.props.defaultValue != null) {
      rootNode.select2("val", this.props.defaultValue);
    }

    rootNode.on("change", this._handleChange);

  },

  _handleChange: function (e) {
    var newValue = $('[data-id]').val();
    //this.state.value = value;
    this.setState({value: newValue});
    //this.props.onChange && this.props.onChange(e);
  },


  render: function () {
    return (
        <div>
          <select id="lang" data-id="whatever" onChange={this.change} value={this.state.value}>
            <option value="select">Select</option>
            <option value="Java">Java</option>
            <option value="C++">C++</option>
          </select>

          <p></p>

          <p>{this.state.value}</p>
        </div>
    );
  }
});

App.Pages.PolicyDetail = React.createClass({


  render: function () {
    var policy = this.props.policy;
    return (
        <div className="l-center">
          <div className="l-middle mod-about no-left box">
            <p></p>

            <p/>

            <p>With the PAP you can maintain XACML policies to configure fine-grained access rules for your Institution
              and the connected Service Providers.</p>
          </div>
          <MySelect />
        </div>
    )
  }

});
