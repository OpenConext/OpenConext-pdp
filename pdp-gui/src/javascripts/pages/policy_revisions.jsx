/** @jsx React.DOM */

App.Pages.PolicyRevisions = React.createClass({

  getInitialState: function () {
    return {data: []}
  },

  renderAttributesDiff: function (prev, curr) {
    var attrPrevGrouped = _.groupBy(prev.attributes, function (attr) {
      return attr.name;
    });

    var attrCurrGrouped = _.groupBy(curr.attributes, function (attr) {
      return attr.name;
    });

    var attrResult = _.transform(attrCurrGrouped, function (result, attributes, attrName) {
      if (attrPrevGrouped.hasOwnProperty(attrName)) {
        var prevValues = _.pluck(attrPrevGrouped[attrName], 'value');
        var currValues = _.pluck(attributes, 'value');

        var deleted = _.difference(prevValues, currValues).map(function(deletedValue){
          return {value: deletedValue, status: "prev"};
        });
        var added = _.difference(currValues, prevValues).map(function(addedValue){
          return {value: addedValue, status: "curr"};
        });
        var unchanged = currValues.filter(function(value){
          return prevValues.indexOf(value) !== -1;
        }).map(function(unchangedValue){
          return {value: unchangedValue, status: "no-change"};
        });

        result[attrName] = {values: deleted.concat(added).concat(unchanged), status: "no-change"};
      } else {
        // these are the added attributes that are in curr and not in prev
        result[attrName] = {values: attributes.map(function(attribute){
          return {value: attribute.value, status: "curr"};
        }), status: "curr"}
      }

    });
    var prevNames = Object.keys(attrPrevGrouped);

    // add the deleted attributes that are in prev and not in curr
    prevNames.forEach(function (name) {
      if (!attrResult.hasOwnProperty(name)) {
        attrResult[name] = {values: attrPrevGrouped[name].map(function(attribute){
          return {value: attribute.value, status: "prev"};
        }), status: "prev"}
      }
    });
    var attributesUnchanged = _.values(attrResult).filter(function(attribuut){
      return (attribuut.status === "prev" || attribuut.status === "curr") && attribuut.values.filter(function(value){
            return value.value === "prev" || value.value === "curr";
          }).length === 0;
    }).length === 0 ;
    var attributeNames = Object.keys(attrResult);
    return (
        <div>
          <div
              className={"diff-element " + (attributesUnchanged ? "no-change" : "changed")}>
            <p className="label">{I18n.t("revisions.attributes")}</p>
            {
                attributeNames.map(function (attributeName) {
                    return (
                    <div key={attributeName}>
                      <div className="attribute-container">
                        <span className={"diff "+attrResult[attributeName].status}>{attributeName}</span>
                      </div>
                      <div className="attribute-values-container">
                        <p className="label">{I18n.t("policy_attributes.values")}</p>
                        {
                            attrResult[attributeName].values.map(function (value) {
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
  },

  renderDiff: function (prev, curr) {
    var properties = ["name", "description", "denyRule", "serviceProviderName", "identityProviderNames",
      "allAttributesMustMatch", "attributes", "denyAdvice", "denyAdviceNl"
    ];
    //means someone if looking at the first initial revision
    if (!prev) {
      prev = {attributes: []};
    }

    var renderPropertyDiff = function (prev, curr, name) {
      if (name === "attributes") {
        return this.renderAttributesDiff(prev, curr);
      } else {
        return (<div>
          <div className={"diff-element " + this.classNamePropertyDiff(prev[name], curr[name])}>
            <p className="label">{I18n.t("revisions."+name)}</p>
            {this.renderPropertyDiff(prev[name], curr[name])}
          </div>
          <div className="diff-element-seperator"></div>
        </div>)
      }
    }.bind(this);

    return (
        <section>
          {this.renderTopDiff(prev,curr)}
          <div className="form-element about">
            <div className="diff-panel">
              {
                  properties.map(function(prop){
                      return renderPropertyDiff(prev, curr, prop);
                      })
                  }
            </div>
          </div>
        </section>
    );
  },

  renderTopDiff: function (prev, curr) {
    if (prev.revisionNbr !== undefined && prev.revisionNbr !== curr.revisionNbr) {
      return (
          <div className="top-diff" dangerouslySetInnerHTML={{__html:
            I18n.t("revisions.changes_info_html",
              { userDisplayName: curr.userDisplayName , createdDate: this.createdDate(curr), currRevisionNbr: curr.revisionNbr, prevRevisionNbr: prev.revisionNbr }
            )
          }}>
          </div>
      );
    }
    return (
        <div className="top-diff" dangerouslySetInnerHTML={{__html:
          I18n.t("revisions.changes_first_html",
            { userDisplayName: curr.userDisplayName ,createdDate: this.createdDate(curr), currRevisionNbr: curr.revisionNbr }
          )
        }}>
        </div>
    );
  },

  renderPropertyDiff: function (prev, curr) {
    var previous = _.isArray(prev) ? prev.join(", ") : prev;
    var current = _.isArray(curr) ? curr.join(", ") : curr;
    if (previous === current) {
      return (<span className="diff no-change">{current.toString()}</span>)
    } else if (previous === undefined) {
      return <span className="diff curr">{current.toString()}</span>
    } else {
      return (<div>
        <span className="diff prev">{previous.toString()}</span>
        <span className="diff curr">{current.toString()}</span>
      </div>)
    }
  },

  classNamePropertyDiff: function (prev, curr) {
    var previous = _.isArray(prev) ? prev.join(", ") : prev;
    var current = _.isArray(curr) ? curr.join(", ") : curr;
    return previous !== current ? "changed" : "no-change";
  },

  handleCompare: function (revision) {
    return function (e) {
      e.preventDefault();
      e.stopPropagation();
      var prev = this.props.revisions.filter(function (rev) {
        return rev.revisionNbr === (revision.revisionNbr - 1)
      });
      this.setState({curr: revision});
      this.setState({prev: prev[0]});
    }.bind(this);
  },

  renderComparePanel: function () {
    var prev = this.state.prev;
    var curr = this.state.curr;
    if (prev || curr) {
      return this.renderDiff(prev, curr);
    } else {
      return this.renderAboutPage();
    }
  },

  renderAboutPage: function () {
    return I18n.locale === "en" ? <App.Help.PolicyRevisionsHelpEn/> : <App.Help.PolicyRevisionsHelpNl/>;
  },

  renderOverview: function () {
    return ( <div>
      <p className="form-element title">{I18n.t("revisions.title")}</p>
      {this.renderRevisions()}
    </div>);
  },

  renderRevisions: function () {
    this.props.revisions.sort(function (rev1, rev2) {
      return rev2.created - rev1.created;
    });
    return this.props.revisions.map(function (revision, index) {
      return this.renderRevision(revision, index);
    }.bind(this));
  },

  createdDate: function (revision) {
    var created = moment(revision.created);
    created.locale(I18n.locale);
    return created.format('LL');
  },

  renderRevision: function (revision, index) {
    var className = index === 0 ? "success" : "failure";
    return (
        <div key={index}>
          <div className={"form-element split sub-container " + className}>
            {this.renderRevisionMetadata(revision)}
            <a className="c-button white compare" href="#" onClick={this.handleCompare(revision)}>&lt; &gt;</a>
          </div>
          <div className="bottom"></div>
        </div>
    );
  },

  renderRevisionMetadata: function (revision) {
    return (<div><p className="label before-em">{revision.name}</p>
      <p className="before-em">{I18n.t("revisions.revision") + " " + revision.revisionNbr}</p>
      <p className="before-em smaller">{I18n.t("policy_detail.sub_title", {displayName: revision.userDisplayName, created: this.createdDate(revision)})}</p>
    </div>)
  },

  render: function () {
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

});
