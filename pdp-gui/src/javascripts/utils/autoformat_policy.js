import isEmpty from "lodash/isEmpty";
import groupBy from "lodash/groupBy";

const AutoFormat = {

    addQuotes: function (str) {
        return isEmpty(str) ? str : `'${str}'`;
    },

    attributes: function (passedAttributes, allAttributesMustMatch) {
        let attributes = passedAttributes;
        const otherAttr = attributes.filter(attr => {
            return attr.name !== "urn:collab:group:surfteams.nl";
        });
        if (otherAttr.length === 0) {
            return ".";
        }
        attributes = groupBy(otherAttr, attr => {
            return attr.name;
        });
        const attributeNames = Object.keys(attributes);
        const length = attributeNames.length;
        const lines = attributeNames.map((attributeName, index) => {
            const values = attributes[attributeName].map(attribute => {
                const negated = attribute.negated ? " NOT " : "";
                return negated + this.addQuotes(attribute.value);
            }).join(" or ");
            const logical = index === (length - 1) ? "" : allAttributesMustMatch ? " and " : " or ";

            const result = "he/she has the value " + values + " for attribute '" + attributeName + "'" + logical;
            return result;
        });
        return lines.join("");

    },

    cidrNotations: function (passedCidrNotations, allAttributesMustMatch, hasAttributes) {
        if (passedCidrNotations.length === 0) {
            return "";
        }
        const lines = passedCidrNotations.map((notation, index) => {
            const res = (index === 0 && hasAttributes) ? (allAttributesMustMatch ? " and" : " or") : "";
            const negate = notation.negate ? "not " : "";
            return res + " with an IP address " + negate + "in the range " + this.addQuotes(notation.ipAddress + "/" + notation.prefix);
        });
        return lines.join(" or");
    },

    description: function (policy) {
        const idps = isEmpty(policy.identityProviderNames) ? "" : " from " + policy.identityProviderNames.map(this.addQuotes).join(" or ");
        const sp = this.addQuotes(policy.serviceProviderNames.join(", ")) || "?";
        const attrs = policy.attributes || [];
        const teamMembershipAttr = attrs.filter(attr => {
            return attr.name === "urn:collab:group:surfteams.nl";
        });
        const teamMembership = teamMembershipAttr.length > 0 ? " he/she is a member of the team " + teamMembershipAttr
            .map(attr => this.addQuotes(attr.value)).join(" or ") : "";

        const and = teamMembershipAttr.length === 0 || teamMembershipAttr.length === attrs.length ? "" : policy.allAttributesMustMatch ? " and" : " or";
        const only = policy.denyRule ? "not" : "only";

        const attributes = this.attributes(attrs, policy.allAttributesMustMatch);

        const loas = policy.loas || [];
        const loasTxt = loas.map(loa => {
            const attrLoa = this.attributes(loa.attributes || [], loa.allAttributesMustMatch);
            let txt = " is required to authenticate with LoA " + this.addQuotes(loa.level);
            if (attrLoa !== ".") {
                txt = txt + " when " + attrLoa;
            }
            txt = txt + this.cidrNotations(loa.cidrNotations, loa.allAttributesMustMatch, loa.attributes.length > 0);
            return txt;
        }).join(" and he /she ");

        //we can't use JS templates as the backtick breaks the uglification. Will be resolved when we upgrade the build tooling
        let description;
        if (policy.type === "step") {
            description = "A user" + idps + loasTxt + " when accessing " + sp;
        } else {
            description = "A user" + idps + " is " + only + " allowed to access " + sp + " when" + teamMembership + and + " " + attributes;
        }


        return description;
    }
};

export default AutoFormat;
