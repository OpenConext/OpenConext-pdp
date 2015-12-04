App.Utils.AutoFormat = {

  addQuotes: function (str) {
    return (_.isEmpty(str)) ? str : "'" + str + "'";
  },

  attributes: function (attributes, allAttributesMustMatch) {
    var otherAttr = attributes.filter(function (attr) {
      return attr.name !== "urn:collab:group:surfteams.nl"
    });
    if (otherAttr.length === 0) {
      return ".";
    }
    var attributes = _.groupBy(otherAttr, function (attr) {
      return attr.name;
    });
    var attributeNames = Object.keys(attributes);
    var length = attributeNames.length;
    var lines = attributeNames.map(function (attributeName, index) {
      var values = attributes[attributeName].map(function (attribute) {
        return this.addQuotes(attribute.value);
      }.bind(this)).join(" or ");
      var logical = index === (length - 1) ? "" : allAttributesMustMatch ? " and " : " or ";
      var result = "he/ she has the value " + values + " for attribute '" + attributeName + "'" + logical;
      return result;
    }.bind(this));
    return lines.join("");

  },

  description: function (policy) {
    var idps = _.isEmpty(policy.identityProviderNames) ? "" : " from " + policy.identityProviderNames.map(this.addQuotes).join(" or ");
    var sp = this.addQuotes(policy.serviceProviderName) || "?";
    var attrs = policy.attributes || [];
    var teamMembershipAttr = attrs.filter(function (attr) {
      return attr.name === "urn:collab:group:surfteams.nl";
    });
    var teamMembership = teamMembershipAttr.length > 0 ? " he/ she is a member of the team " + teamMembershipAttr.map(function (attr) {
      return this.addQuotes(attr.value);
    }.bind(this)).join(" or ") : "";

    var and = teamMembershipAttr.length === 0 || teamMembershipAttr.length === attrs.length ? "" : policy.allAttributesMustMatch ? " and" : " or";
    var only = policy.denyRule ? "not" : "only";

    var attributes = this.attributes(attrs, policy.allAttributesMustMatch);
    //we can't use JS templates as the backtick breaks the uglification. Will be resolved when we upgrade the build tooling
    var description = "A user " + idps + "is " + only + " allowed to access " + sp + " when" + teamMembership + and + " " + attributes;

    return description;
  }


};
