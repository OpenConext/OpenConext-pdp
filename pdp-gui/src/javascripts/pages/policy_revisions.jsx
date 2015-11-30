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
      <p>{JSON.stringify(this.props.revisions, null, 4)}</p>
    </div>);
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
