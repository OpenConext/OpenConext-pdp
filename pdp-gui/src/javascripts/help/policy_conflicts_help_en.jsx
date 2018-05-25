import React from "react";

class PolicyConflictsHelpEn extends React.Component {
    render() {
        return (
            <div className="form-element about">
                <h1>What are policy conflicts?</h1>

                <p>If two or more policies are configured for the same Service Provider and either there are no
                    Identity Providers configured or there is at least one Identity Provider that is configured in both
                    policies then those policies
                    are considered conflicting.</p>

                <h2>Consequences</h2>

                <p>Conflicting policies can influence each other because if one of the policies results in
                    a negative decision then the user is not allowed access while the user might be allowed access
                    based on the other - conflicting - policies.</p>

                <h2>Inactive conflicts</h2>

                <p>If filtering out the inactive - marked PAP inactive OR not activated in Manage - policies resolves the
                    conflict then we consider the conflict inactive.</p>
            </div>



        );
    }
}

export default PolicyConflictsHelpEn;
