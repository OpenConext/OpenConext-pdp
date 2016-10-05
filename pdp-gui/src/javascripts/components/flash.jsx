import React from "react";

import { emitter, getFlash } from "../utils/flash";

class Flash extends React.Component {
  constructor() {
    super();

    this.state = {
      flash: null
    };

    this.callback = flash => this.setState({ flash: flash });
  }

  componentWillMount() {
    this.setState({ flash: getFlash() });
    emitter.addListener("flash", this.callback);
  }

  componentWillUnmount() {
    emitter.removeListener("flash", this.callback);
  }

  closeFlash() {
    this.setState({ flash: null });
  }

  render() {
    const { flash } = this.state;

    if (flash) {
      return (
        <div className="flash">
          <p className={flash.type} dangerouslySetInnerHTML={{ __html: flash.message }}></p>
          <a className="close" href="#" onClick={() => this.closeFlash()}>
            <i className="fa fa-remove"></i>
          </a>
        </div>
      );
    }

    return null;
  }
}

export default Flash;
