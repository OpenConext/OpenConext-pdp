/** @jsx React.DOM */

App.Pages.PolicyRevisions = React.createClass({

  getInitialState: function () {
    return {data: []}
  },

  getEntityName: function (id, type) {
    var name = id;
    var entities = this.props[type].filter(function (entity) {
      return entity.entityId === id;
    });
    if (!_.isEmpty(entities)) {
      var entity = entities[0];
      return I18n.entityName(entity);
    }
    return name;
  },

  parseEntityId: function (attributes, type) {
    var idpAttr = attributes.filter(function (attr) {
      return attr.AttributeId === type;
    });
    var idpValues = idpAttr.map(function (attr) {
      return attr.Value;
    });
    return idpValues.length === 1 ? idpValues[0] : undefined;
  },

  renderDiff: function(prev, next) {
    //TODO
  },

  renderComparePanel: function () {
    var prev = this.state.prev;
    var next = this.state.next;
    if (prev && next) {
      return this.renderDiff(prev, next);
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

  renderRevisions: function() {
    this.props.revisions.sort(function(rev1, rev2){
      return rev2.created - rev1.created;
    });
    var renderRevision = this.renderRevision;
    return this.props.revisions.map(function (revision, index) {
      return renderRevision(revision, index);
    });
  },

  renderRevision: function (revision, index) {
    var className = index === 0 ? "success" : "failure";
    return (
        <div>
          <div className={"form-element split " + className}>
            <p className="label before-em">{revision.name}</p>
            <em className="label">{revision.userDisplayName + " - " + new Date(revision.created)}</em>
            <i className="revision fa fa-arrow-right"></i>
          </div>
          <div className="bottom"></div>
        </div>
    );
  },

  render: function () {
    return (
        <div className="l-center mod-playground">
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
