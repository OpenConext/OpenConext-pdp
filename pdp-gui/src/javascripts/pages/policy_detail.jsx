/** @jsx React.DOM */
var MySelect = React.createClass({

  getInitialState: function () {
    return {
      value: ''
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
    rootNode.select2({
      width: '100%',
      placeholder: "Select a Service Provider",
      allowClear: true
    });

    if (this.props.defaultValue != null) {
      rootNode.select2("val", this.props.defaultValue);
    }

    rootNode.on("change", this._handleChange);

  },
  componentWillUnmount: function () {
    var rootNode = $('[data-id]');
    rootNode.select2("destroy");
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
            <option></option>
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

  getInitialState: function () {
    return this.props.policy;
  },

  toggleDenyRule: function (e) {
    this.setState({denyRule: !this.state.denyRule});

  },

  render: function () {
    var policy = this.state;
    var policyPermit = policy.denyRule ? "Deny" : "Permit";
    var classNameSelected = policy.denyRule ? "checked" : "";
    return (
        <div className="l-center mod-policy-detail">
          <div className="l-middle form-element-container box">

            <p className="form-element form-title">Create new policy</p>

            <div className="form-element success">
              <p className="label">Service Provider</p>
              <MySelect />
            </div>
            <div className="bottom"></div>
            <div className="form-element failure">
              <p className="label">Identity Provider</p>
              <MySelect />
            </div>
            <div className="bottom"></div>
            <div className="form-element failure" onClick={this.toggleDenyRule}>
              <p className="label">Access</p>

              <div id="ios_checkbox" className={classNameSelected + " ios-ui-select"}>
                <div className="inner"></div>
                <p>{policyPermit}</p>
              </div>
            </div>
            <div className="bottom"></div>
            <div className="actions">
              <a className="c-button" href="#">Submit</a>
            </div>
          </div>
        </div>
    )
  }

});
