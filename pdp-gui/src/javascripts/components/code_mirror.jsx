/** @jsx React.DOM */

App.Components.CodeMirror = React.createClass({

  componentDidMount () {
    var node = document.getElementById("code_mirror_textarea");
    this.codeMirror = CodeMirror.fromTextArea(node, this.props.options);
    this.codeMirror.on('change', this.codeMirrorValueChanged);
    this.codeMirror.on('focus', this.focusChanged.bind(this, true));
    this.codeMirror.on('blur', this.focusChanged.bind(this, false));
    this._currentCodemirrorValue = this.props.value;
  },

  componentWillUnmount () {
    if (this.codeMirror) {
      this.codeMirror.toTextArea();
    }
  },

  componentWillReceiveProps (nextProps) {
    if (this.codeMirror && this._currentCodemirrorValue !== nextProps.value) {
      this.codeMirror.setValue(nextProps.value);
    }
  },

  focus () {
    if (this.codeMirror) {
      this.codeMirror.focus();
    }
  },

  focusChanged (focused) {
    this.setState({
      isFocused: focused
    });
    this.props.onFocusChange && this.props.onFocusChange(focused);
  },

  codeMirrorValueChanged (doc, change) {
    var newValue = doc.getValue();
    this._currentCodemirrorValue = newValue;
    this.props.onChange && this.props.onChange(newValue);
  },

  render () {
    return (
        <div className="code-mirror">
          <textarea id="code_mirror_textarea" name={this.props.path} defaultValue={this.props.value}
                    autoComplete="off"/>
        </div>
    );
  }


});
