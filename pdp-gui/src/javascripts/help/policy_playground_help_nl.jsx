import React from "react";

class PolicyPlaygroundHelpNl extends React.Component {
  render() {
    return (
      <div className="form-element about">
        <h1>Hoe kun je Playground gebruiken?</h1>

        <p>Met de SURFconext Policy Administration Point (PAP) kan je <a href="https://en.wikipedia.org/wiki/XACML"
            target="_blank">XACML</a> autorisatieregels
          beheren. Autorisatieregels definiëren wie wordt geautoriseerd voor welke combinatie van instelling en
          dienst op basis
          van de attributen van de gebruiker.</p>

        <p>Deze playground kan worden gebruikt om je autorisatieregels te testen. Nieuwe en/of bewerkte autorisatieregels kunnen
          direct worden getest.</p>

        <p>Door een autorisatieregel te selecteren kun je direct deze regel testen zonder alle juiste input
          parameters te kiezen.</p>

        <h2>Dienst (SP) en instelling (IdP)</h2>

        <p>Kies de dienst die je hebt gedefinieerd in je autorisatieregel. Mocht je de autorisatieregel hebben
          gedefinieerd zonder instelling, dan moet je toch een instelling
          hier kiezen om een geldig autorisatieverzoek te doen. De gekozen instelling zal worden genegeerd als er
          geen
          instelling is gekozen in je autorisatieregel.</p>

        <h2>Attributen</h2>

        <p>De attributen die je selecteert en de waardes die je toevoegt worden toegevoegd aan het autorisatieverzoek
          naar de
          'Policy Definition Point' (PDP). Op deze manier kan je verschillende uitkomsten van je regel testen.
        </p>

        <p>Als je het attribuut <em>urn:collab:group:surfteams.nl</em> kiest dan moet je ofwel een geldige volledige
          teamnaam invullen of
          - om de integratie met teams te testen - een <em>urn:oasis:names:tc:SAML:1.1:nameid-format:unspecified</em>
          attribuut met
          daarin de geldige en volledige id van een gebruiker die lid is van de gedefinieerde groep in je autorisatie
          regel.</p>

        <h2>Resultaten</h2>

        <p>Er zijn 4 mogelijke resultaten van een autorisatieverzoek:</p>
        <ul>
          <li><span>'Permit'</span> - Minstens één autorisatieregel was van toepassing en de 'Permit' regel was een
            match met de attributen
          </li>
          <li><span>'Deny'</span> - Minstens één autorisatieregel was van toepassing en er was geen match met de
            attributen
          </li>
          <li><span>'Not Applicable'</span> - Geen enkele autorisatieregel was van toepassing op basis van de
            geselecteerde dienst en instelling
          </li>
          <li><span>'Indeterminate'</span> - Een verplicht attribuut voor de autorisatieregel is niet meegestuurd.
            Dit kan alleen gebeuren bij 'Deny' regels.
          </li>
        </ul>
        <p>Als het resultaat dan wel 'Permit' of 'Not Applicable' is, dan zou de gebruiker zijn geautoriseerd voor het
          gebruik van de dienst.</p>
      </div>
    );
  }
}

export default PolicyPlaygroundHelpNl;
