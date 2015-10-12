/** @jsx React.DOM */

App.Components.CodeMirror = React.createClass({

  componentDidMount: function () {
    var node = document.getElementById(this.props.uniqueId);
    this.codeMirror = CodeMirror.fromTextArea(node, this.props.options);
    this._currentCodemirrorValue = this.props.value;
    if (!this.props.options.readOnly) {
      this.codeMirror.on('change', this.codeMirrorValueChanged);
    }
  },

  componentWillUnmount: function () {
    if (this.codeMirror) {
      this.codeMirror.toTextArea();
    }
  },

  componentWillReceiveProps: function (nextProps) {
    if (this.codeMirror && this._currentCodemirrorValue !== nextProps.value) {
      this.codeMirror.setValue(nextProps.value);
    }
  },

  codeMirrorValueChanged: function (doc, change) {
    var newValue = doc.getValue();
    this._currentCodemirrorValue = newValue;
    this.props.onChange && this.props.onChange(newValue);
  },

  render: function () {
    return (
        <div className="code-mirror">
          <textarea id={this.props.uniqueId} defaultValue={this.props.value} autoComplete="off"/>
        </div>
    );
  }


});
