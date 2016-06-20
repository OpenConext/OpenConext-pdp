/** @jsx React.DOM */

App.Help.PolicyConflictsHelpNl = React.createClass({
  render: function () {
    return (
        <div className="form-element about">
          <h1>Wat zijn autorisatieregel conflicten?</h1>

          <p>Als twee of meer autorisatieregels voor dezelfde dienstverlener zijn geconfigureerd en er zijn dan wel geen
            instituties dan wel overlappende instituties geconfigureerd dan beschouwen we deze autorisatieregels als
            conflicterend..</p>

          <h2>Consequenties</h2>

          <p>Conflicterende autorisatieregels kunnen elkaar beinvloeden omdat als één van de regels in een negatieve
            beslissing resulteert dan wordt de gebruiker toegang tot de desbetreffende dienst gewijgerd terwijl
            de andere conflicterende regel(s) wellicht een positieve beslissing hadden.</p>

          <h2>Inactieve conflicten</h2>

          <p>Als het conflict is 'opgelost' door alle niet actieve - niet geactiveerd in dan wel SR of PAP - regels eruit te filteren dan is een conflict inactief.</p>

        </div>

    );
  }
});
