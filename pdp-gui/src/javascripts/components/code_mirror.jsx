import React from "react";
import CodeMirror from "codemirror";

class CodeMirrorComponent extends React.Component {

  componentDidMount() {
    this.codeMirror = CodeMirror.fromTextArea(this.node, this.props.options);
    this._currentCodemirrorValue = this.props.value;
    if (!this.props.options.readOnly) {
      this.codeMirror.on("change", this.codeMirrorValueChanged.bind(this));
    }
  }

  componentWillUnmount() {
    if (this.codeMirror) {
      this.codeMirror.toTextArea();
    }
  }

  componentWillReceiveProps(nextProps) {
    if (this.codeMirror && this._currentCodemirrorValue !== nextProps.value) {
      this.codeMirror.setValue(nextProps.value);
    }
  }

  codeMirrorValueChanged(doc, change) {
    const newValue = doc.getValue();
    this._currentCodemirrorValue = newValue;
    this.props.onChange && this.props.onChange(newValue);
  }

  render() {
    return (
      <div className="code-mirror">
        <textarea ref={node => this.node = node} defaultValue={this.props.value} autoComplete="off"/>
      </div>
    );
  }


}

export default CodeMirrorComponent;
