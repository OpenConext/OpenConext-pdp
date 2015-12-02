/** @jsx React.DOM */

App.Pages.PolicyRevisions = React.createClass({

  getInitialState: function () {
    return {data: []}
  },

  renderDiff: function (prev, curr) {
    var prevDenyRule = prev.denyRule ? I18n.t("revisions.deny") : I18n.t("revisions.permit");
    var currDenyRule = curr.denyRule ? I18n.t("revisions.deny") : I18n.t("revisions.permit");

    var prevAttrRule = prev.allAttributesMustMatch ? I18n.t("revisions.logical_and") : I18n.t("revisions.logical_or");
    var currAttrRule = curr.allAttributesMustMatch ? I18n.t("revisions.logical_and") : I18n.t("revisions.logical_or");

    return (
        <section>
          {this.renderTopDiff(prev,curr)}
          <div className="form-element about">
            <div className="diff-panel">
              <div className={"diff-element " + this.classNamePropertyDiff(prev.name, curr.name)}>
                <p className="label">{I18n.t("policy_detail.name")}</p>
                {this.renderPropertyDiff(prev.name, curr.name)}
              </div>
              <div className="diff-element-seperator"></div>
              <div className={"diff-element " + this.classNamePropertyDiff(prev.description, curr.description)}>
                <p className="label">{I18n.t("policy_detail.description")}</p>
                {this.renderPropertyDiff(prev.description, curr.description)}
              </div>
              <div className="diff-element-seperator"></div>
              <div className={"diff-element " + this.classNamePropertyDiff(prevDenyRule, currDenyRule)}>
                <p className="label">{I18n.t("policy_detail.access")}</p>
                {this.renderPropertyDiff(prevDenyRule, currDenyRule)}
              </div>
              <div className="diff-element-seperator"></div>
              <div
                  className={"diff-element " + this.classNamePropertyDiff(prev.serviceProviderName, curr.serviceProviderName)}>
                <p className="label">{I18n.t("policies.serviceProviderId")}</p>
                {this.renderPropertyDiff(prev.serviceProviderName, curr.serviceProviderName)}
              </div>
              <div className="diff-element-seperator"></div>
              <div
                  className={"diff-element " + this.classNamePropertyDiff(prev.identityProviderNames.join(", "), curr.identityProviderNames.join(", "))}>
                <p className="label">{I18n.t("policies.identityProviderIds")}</p>
                {this.renderPropertyDiff(prev.identityProviderNames.join(", "), curr.identityProviderNames.join(", "))}
              </div>
              <div className="diff-element-seperator"></div>
              <div className={"diff-element " + this.classNamePropertyDiff(prevAttrRule, currAttrRule)}>
                <p className="label">{I18n.t("policy_detail.rule")}</p>
                {this.renderPropertyDiff(prevAttrRule, currAttrRule)}
              </div>
              <div className="diff-element-seperator"></div>
              <div className={"diff-element " + this.classNamePropertyDiff(prev.attributes, curr.attributes)}>
                <p className="label">{I18n.t("revisions.attributes")}</p>
              </div>
              <div className="diff-element-seperator"></div>
              <div className={"diff-element " + this.classNamePropertyDiff(prev.denyAdvice, curr.denyAdvice)}>
                <p className="label">{I18n.t("policy_detail.deny_message")}</p>
                {this.renderPropertyDiff(prev.denyAdvice, curr.denyAdvice)}
              </div>
              <div className="diff-element-seperator"></div>
              <div className={"diff-element " + this.classNamePropertyDiff(prev.denyAdviceNl, curr.denyAdviceNl)}>
                <p className="label">{I18n.t("policy_detail.deny_message_nl")}</p>
                {this.renderPropertyDiff(prev.denyAdviceNl, curr.denyAdviceNl)}
              </div>
            </div>
          </div>
        </section>
    );
  },

  renderTopDiff: function (prev, curr) {
    return (
    <div className="top-diff"
         dangerouslySetInnerHTML={{__html: I18n.t("revisions.changes_info_html", {userDisplayName: curr.userDisplayName , createdDate: this.createdDate(curr), currRevisionNbr: curr.revisionNbr,prevRevisionNbr: prev.revisionNbr}) }}></div>


    );
  },

  renderPropertyDiff: function (prev, curr) {
    if (prev !== curr) {
      return (<div>
        <span className="diff curr">{curr}</span>
        <span className="diff prev">{prev}</span>
      </div>)
    } else {
      return (<span className="diff no-change">{curr}</span>)
    }
  },

  classNamePropertyDiff: function (prev, curr) {
    return prev !== curr ? "changed" : "no-change";
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
    var renderRevision = this.renderRevision;
    return this.props.revisions.map(function (revision, index) {
      return renderRevision(revision, index);
    });
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
