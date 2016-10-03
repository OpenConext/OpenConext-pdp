import React from "react";

class PolicyViotaltionsHelpNl extends React.Component {
  render() {
    return (
      <div className="form-element about">
        <h1>Wat zijn ongeautoriseerde logins?</h1>

        <p>Elke keer als er een negatief besluit vanuit de Policy Decision Point (PDP) naar het Policy Enforcement
          Point (PEP) wordt gestuurd, dan wordt de gebruiker niet doorgestuurd naar de dienst waar hij/zij
          probeerde op in te loggen, maar krijgt hij/zij een meldingspagina te zien.</p>

        <h2>Negatieve besluiten</h2>

        <p>Alle negatieve besluiten gemaakt door de PDP worden opgeslagen. Klik op het oog-pictogram als je de details
          van een ongeautoriseerde
          login wilt bekijken.</p>

        <p>Voor elke ongeautoriseerde login worden de volgende gegevens bewaard (gedurende 30 dagen):</p>
        <ul>
          <li>Het originele JSON-verzoek van de PEP</li>
          <li>Het JSON-antwoord van de PDP</li>
          <li>Een referentie naar de autorisatieregel verantwoordelijk voor het negatieve besluit</li>
          <li>Het besluit - e.g. Deny of Indeterminate - van de PDP</li>
        </ul>

      </div>
    );
  }
}

export default PolicyViotaltionsHelpNl;
