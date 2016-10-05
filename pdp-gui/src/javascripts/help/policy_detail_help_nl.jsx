import React from "react";

class PolicyDetailHelpNl extends React.Component {
  render() {
    return (
      <div className="form-element about">
        <h1>Hoe maak je autorisatieregels?</h1>

        <p>Autorisatieregels definiëren of een gebruiker toegang heeft tot een bepaalde dienst. De keuze wordt
          gemaakt op basis van de attributen die zijn
          vrijgegeven door de instelling die tijdens het inloggen de gebruiker heeft geauthenticeerd.</p>

        <h2>Toegang</h2>

        <p>Wij raden aan om voor een 'Permit' regel te kiezen in plaats van een 'Deny'.</p>

        <p>Het algoritme wat gebruikt wordt om te bepalen of iemand toegang heeft tot een dienst op basis van een autorisatieregel is 'first-applicable'.
          Dit betekent dat de eerste match van een regel het resultaat  - 'Deny' of 'Permit' - bepaalt.
        </p>

        <p>Meer informatie over de implicaties van een 'Deny' regel kan
          <a target="_blank" href="http://docs.oasis-open.org/xacml/3.0/xacml-3.0-core-spec-os-en.html#_Toc325047268"> hier </a> worden gevonden.
        </p>

        <h2>Dienst</h2>

        <p>De dienst bepaalt op welke Service Provider deze autorisatieregel van toepassing is. Er kan precies één dienst gekoppeld zijn aan een autorisatieregel.</p>

        <h2>Instelling</h2>

        <p>De instelling bepaalt voor welke instelling deze autorisatieregel van toepassing is. De instelling in deze context
          is de 'Identity Provider' die de gebruiker heeft geauthenticeerd. Je kunt nul of meer instellingen koppelen aan een autorisatieregel.</p>

        <h2>Regel</h2>

        <p>Je kunt kiezen of alle attributen een match moeten opleveren, of dat een enkele match voldoende is om de gebruiker te autoriseren voor de dienst.</p>

        <h2>Attributen</h2>

        <p>De attributen en hun respectievelijke waardes bepalen of een gebruiker succesvol wordt geautoriseerd voor de dienst. De attributen
          worden gematcht tegen de attributen van de gebruiker.</p>

        <h2>Autorisatie op teamnaam</h2>

        <p>Let op als je het attribuut <em>urn:collab:group:surfteams.nl</em> kiest. De waarde(s) van dit attribuut
          moeten de geldige en volledige ID zijn van een team in SURFconext Teams. Neem contact op met SURFconext support
          als je twijfelt over de juiste waarde.</p>
      </div>
    );
  }
}

export default PolicyDetailHelpNl;
