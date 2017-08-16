import React from "react";

class PolicyDetailHelpStepEn extends React.Component {
    render() {
        return (
            <div className="form-element about">
                <h1>How to create Stepup-Gateway Policies?</h1>

                <p>Access policies for the Stepup-Gateway define the Level of Assurance (LoA) for a user when he / she tries to login.
                    The decision which LoA to enforce is based on the user attributes provided by the Institution and other attribute providers.</p>

                <h2>Service</h2>

                <p>The Service determines for which Service this policy applies. You can only link policies to one
                    Service</p>

                <h2>Institution</h2>

                <p>The Institution determines for which Institution this policy applies. The Institution in this context
                    is the Identity Provider which authenticated the user. You can link policies to zero or more Institutions</p>

                <h2>Level of Assurance</h2>
                <p>You can define multple LoA's for a policy. Each LoA will be translated to a XACML rule. The attributes
                of a LoA will determine of the specified LoA is enforced by the Stepup-Gateway</p>

                <h3>Rule</h3>
                <p>The AND / OR rule specified if all the attributes defined for a LoA must match the attributes of the user or that one
                    match is sufficient for a 'Match'</p>

                <h3>Attributes</h3>

                <p>The attributes and their values actually define the LoA for a user.</p>

                <p>The value of an attribute may be a regular expression. Use .* to match any value.</p>

                <h3>IP Ranges</h3>

                <p>You can specify multiple IP addresses and their prefix to configure IP ranges. The IP
                address of the user is matched against the range and based on the negation the LoA is choosen or not.</p>
                <p>Multiple IP ranges within in LoA always are defined as logical OR.</p>

                <h3>Group name authorization</h3>

                <p>Special care must be taken when you choose <em>urn:collab:group:surfteams.nl</em> as a required
                    attribute.
                    The value must be the fully qualified group name where the user is a member of. Please consult the
                    responsible
                    technical product manager how to retrieve the fully qualified name of a certain group / team.</p>

            </div>
        );
    }
}

export default PolicyDetailHelpStepEn;
