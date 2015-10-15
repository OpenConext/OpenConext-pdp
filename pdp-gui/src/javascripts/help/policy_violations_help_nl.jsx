/** @jsx React.DOM */

App.Help.PolicyViotaltionsHelpNl = React.createClass({
  render: function () {
    return (
        <div className="about form-element">
          <h1>Wat zijn ongeautoriseerde logins?</h1>

          <p>Elke keer als er een negatief besluit vanuit de Policy Decision Point (PDP) naar het Policy Enforcement
            Point (PEP) wordt gestuurd dan wordt de gebruiker niet doorgestuurd naar de dienst waar hij / zij
            probeerde op in te loggen.</p>

          <h2>Negatieve besluiten</h2>

          <p>Alle negatieve besluiten gemaakt door de PDP worden opgeslagen. Klik op het oog icon als je de details
            van een ongeautoriseerde
            login wilt bekijken.</p>

          <p>Voor elke ongeautoriseerde login worden de volgende gegevens bewaard (voor 30 dagen):</p>
          <ul>
            <li>Het originele JSON verzoek van de PEP</li>
            <li>Het JSON antwoord van de PDP</li>
            <li>Een referentie naar de autorisatie regel verantwoordelijk voor het negatieve besluit</li>
            <li>Het besluit - e.g. Deny of Indeterminate - van de PDP</li>
          </ul>

        </div>
    );
  }
});
