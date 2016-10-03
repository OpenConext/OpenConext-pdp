import React from "react";
import I18n from "i18n-js";

class PolicyRevisions extends React.Component {

  componentWillUpdate() {
    const node = this.getDOMNode();
    this.shouldScrollBottom = node.scrollTop + node.offsetHeight === node.scrollHeight;
  }

  componentDidUpdate() {
    if (this.shouldScrollBottom) {
      const node = this.getDOMNode();
      node.scrollTop = node.scrollHeight;
    }
  }

  constructor() {
    super();

    this.state = { data: [] };
  }

  renderAttributesDiff(prev, curr) {
    const attrPrevGrouped = _.groupBy(prev.attributes, attr => {
      return attr.name;
    });

    const attrCurrGrouped = _.groupBy(curr.attributes, attr => {
      return attr.name;
    });

    const attrResult = _.transform(attrCurrGrouped, (result, attributes, attrName) => {
      if (attrPrevGrouped.hasOwnProperty(attrName)) {
        //find out the diff in values
        const prevValues = _.pluck(attrPrevGrouped[attrName], "value");
        const currValues = _.pluck(attributes, "value");

        const deleted = _.difference(prevValues, currValues).map(deletedValue => {
          return { value: deletedValue, status: "prev" };
        });
        const added = _.difference(currValues, prevValues).map(addedValue => {
          return { value: addedValue, status: "curr" };
        });
        const unchanged = currValues.filter(value => {
          return prevValues.indexOf(value) !== -1;
        }).map(unchangedValue => {
          return { value: unchangedValue, status: "no-change" };
        });
        const newValues = deleted.concat(added).concat(unchanged);
        const anyValuesChanged = newValues.filter(val => {
          return val.status == "prev" || val.status === "curr";
        }).length > 0;

        result[attrName] = { values: newValues, status: "no-change", anyValuesChanged: anyValuesChanged };
      } else {
        // these are the added attributes that are in curr and not in prev
        result[attrName] = { values: attributes.map(attribute => {
          return { value: attribute.value, status: "curr" };
        }), status: "curr" };
      }

    });
    const prevNames = Object.keys(attrPrevGrouped);

    // add the deleted attributes that are in prev and not in curr
    prevNames.forEach(name => {
      if (!attrResult.hasOwnProperty(name)) {
        attrResult[name] = { values: attrPrevGrouped[name].map(attribute => {
          return { value: attribute.value, status: "prev" };
        }), status: "prev" };
      }
    });
    const attributesUnchanged = _.values(attrResult).filter(attribuut => {
      return (attribuut.status === "prev" || attribuut.status === "curr") && attribuut.values.filter(value => {
        return value.value === "prev" || value.value === "curr";
      }).length === 0;
    }).length === 0 ;
    const attributeNames = Object.keys(attrResult);
    return (
        <div>
          <div
              className={"diff-element " + (attributesUnchanged ? "no-change" : "changed")}>
            <p className="label">{I18n.t("revisions.attributes")}</p>
            {
                attributeNames.map(attributeName => {
                  return (
                    <div key={attributeName}>
                      <div className="attribute-container">
                        <span className={"diff "+attrResult[attributeName].status}>{attributeName}</span>
                      </div>
                      <div className={"attribute-values-container " + (attrResult[attributeName].status === "no-change"
                                          && attrResult[attributeName].anyValuesChanged ? "diff-element changed" : "")}>
                        <p className="label">{I18n.t("policy_attributes.values")}</p>
                        {
                            attrResult[attributeName].values.map(value => {
                              return (
                                <div className="value-container"
                                     key={attributeName + "-" + attrResult[attributeName].status +"-" + value.value+"-" +value.status}>
                                  <span className={"diff "+value.status}>{value.value}</span>
                                </div>
                                    );
                            })
                            }
                      </div>
                    </div>);
                })
                }
          </div>
          <div className="diff-element-seperator"></div>
        </div>);
  }

  renderDiff(prev, curr) {
    const properties = ["name", "description", "denyRule", "serviceProviderName", "identityProviderNames",
      "allAttributesMustMatch", "attributes", "denyAdvice", "denyAdviceNl", "active"
    ];
    //means someone if looking at the first initial revision
    if (!prev) {
      prev = { attributes: [] };
    }

    const renderPropertyDiff = function(prev, curr, name) {
      if (name === "attributes") {
        return this.renderAttributesDiff(prev, curr);
      } else {
        return (<div>
          <div className={"diff-element " + this.classNamePropertyDiff(prev[name], curr[name])}>
            <p className="label">{I18n.t("revisions."+name)}</p>
            {this.renderPropertyDiff(prev[name], curr[name])}
          </div>
          <div className="diff-element-seperator"></div>
        </div>);
      }
    }.bind(this);

    return (
        <section>
          {this.renderTopDiff(prev,curr)}
          <div className="form-element about">
            <div className="diff-panel">
              {
                  properties.map(prop => {
                    return renderPropertyDiff(prev, curr, prop);
                  })
                  }
            </div>
          </div>
        </section>
    );
  }

  renderTopDiff(prev, curr) {
    if (prev.revisionNbr !== undefined && prev.revisionNbr !== curr.revisionNbr) {
      return (
          <div className="top-diff" dangerouslySetInnerHTML={{ __html:
            I18n.t("revisions.changes_info_html",
              { userDisplayName: curr.userDisplayName , authenticatingAuthorityName: curr.authenticatingAuthorityName, createdDate: this.createdDate(curr), currRevisionNbr: curr.revisionNbr, prevRevisionNbr: prev.revisionNbr }
            )
          }}>
          </div>
      );
    }
    return (
        <div className="top-diff" dangerouslySetInnerHTML={{ __html:
          I18n.t("revisions.changes_first_html",
            { userDisplayName: curr.userDisplayName ,authenticatingAuthorityName: curr.authenticatingAuthorityName, createdDate: this.createdDate(curr), currRevisionNbr: curr.revisionNbr }
          )
        }}>
        </div>
    );
  }

  renderPropertyDiff(prev, curr) {
    const previous = _.isArray(prev) ? prev.join(", ") : prev;
    const current = _.isArray(curr) ? curr.join(", ") : curr;
    if (previous === current) {
      return (<span className="diff no-change">{current.toString()}</span>);
    } else if (previous === undefined) {
      return <span className="diff curr">{current.toString()}</span>;
    } else {
      return (<div>
        <span className="diff prev">{previous.toString()}</span>
        <span className="diff curr">{current.toString()}</span>
      </div>);
    }
  }

  classNamePropertyDiff(prev, curr) {
    const previous = _.isArray(prev) ? prev.join(", ") : prev;
    const current = _.isArray(curr) ? curr.join(", ") : curr;
    return previous !== current ? "changed" : "no-change";
  }

  handleCompare(revision) {
    return function(e) {
      e.preventDefault();
      e.stopPropagation();
      const prev = this.props.revisions.filter(rev => {
        return rev.revisionNbr === (revision.revisionNbr - 1);
      });
      this.setState({ curr: revision });
      this.setState({ prev: prev[0] });
    }.bind(this);
  }

  renderComparePanel() {
    const prev = this.state.prev;
    const curr = this.state.curr;
    if (prev || curr) {
      return this.renderDiff(prev, curr);
    } else {
      return this.renderAboutPage();
    }
  }

  renderAboutPage() {
    return I18n.locale === "en" ? <App.Help.PolicyRevisionsHelpEn/> : <App.Help.PolicyRevisionsHelpNl/>;
  }

  renderOverview() {
    return (<div>
      <p className="form-element title">{I18n.t("revisions.title")}</p>
      {this.renderRevisions()}
    </div>);
  }

  renderRevisions() {
    this.props.revisions.sort((rev1, rev2) => {
      return rev2.created - rev1.created;
    });
    return this.props.revisions.map((revision, index) => {
      return this.renderRevision(revision, index);
    });
  }

  createdDate(revision) {
    const created = moment(revision.created);
    created.locale(I18n.locale);
    return created.format("LLLL");
  }

  renderRevision(revision, index) {
    const className = index === 0 ? "success" : "failure";
    const linkClassName = this.state.curr && this.state.curr.revisionNbr === revision.revisionNbr ? "selected" : "";
    return (
        <div key={index}>
          <div className={"form-element split sub-container " + className}>
            {this.renderRevisionMetadata(revision)}
            <a className={"c-button white compare "+linkClassName} href="#" onClick={this.handleCompare(revision)}>&lt; &gt;</a>
          </div>
          <div className="bottom"></div>
        </div>
    );
  }

  renderRevisionMetadata(revision) {
    return (<div><p className="label before-em">{revision.name}</p>
      <p className="before-em">{I18n.t("revisions.revision") + " " + revision.revisionNbr}</p>
      <p className="before-em smaller">{I18n.t("policy_detail.sub_title", { displayName: revision.userDisplayName, created: this.createdDate(revision) })}</p>
    </div>);
  }

  render() {
    return (
        <div className="l-center mod-revisions">
          <div className="l-split-left form-element-container box">
            {this.renderOverview()}
          </div>
          <div className="l-split-right form-element-container box">
            {this.renderComparePanel()}
          </div>
        </div>
    );
  }

}

export default PolicyRevisions;
