import React from "react";

class PolicyConflictsHelpNl extends React.Component {
    render() {
        return (
            <div className="form-element about">
                <h1>Wat zijn autorisatieregelconflicten?</h1>

                <p>Als twee of meer autorisatieregels voor dezelfde service provider zijn geconfigureerd en er zijn
                    ofwel geen
                    Instellingen ofwel overlappende Instellingen geconfigureerd dan beschouwen we deze autorisatieregels
                    als
                    conflicterend.</p>

                <h2>Consequenties</h2>

                <p>Conflicterende autorisatieregels kunnen elkaar beïnvloeden, omdat als één van de regels in een
                    negatieve
                    beslissing resulteert, dan de gebruiker toegang tot de desbetreffende dienst wordt geweigerd. Dit
                    terwijl
                    de andere conflicterende regel(s) wellicht een positieve beslissing tot gevolg gehad hadden.</p>

                <h2>Inactieve conflicten</h2>

                <p>Als het conflict is 'opgelost' door alle niet-actieve - niet geactiveerd in Manage of PAP - regels te
                    filteren dan is een conflict inactief.</p>

            </div>

        );
    }
}

export default PolicyConflictsHelpNl;
