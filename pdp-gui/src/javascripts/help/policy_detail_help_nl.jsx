/** @jsx React.DOM */

App.Help.PolicyDetailHelpNl = React.createClass({
  render: function () {
    return (
        <div className="about form-element">
          <h1>Hoe maak je autorisatie regels?</h1>

          <p>Autorisatie regels definiëren of een gebruiker toegang heeft tot een bepaalde dienst. De keuze wordt
            gemaakt op basis van de attributen die zijn
            vrijgegeven door de instelling die tijdens het inloggen de gebruiker heeft geautenticeerd..</p>

          <h2>Toegang</h2>

          <p>Wij raden aan om voor een 'Permit' regel te kiezen in plaats van een 'Deny'.</p>

          <p>Het algoritme was gebruikt wordt om te bepalen of iemand toegang heeft tot een dienst op basis van een autorisatie regel is 'first-applicable'.
            Dit betekent dat de eerste match van een regel het resultaat  - 'Deny' of 'Permit' - bepaald.
          </p>

          <p>Voor meer informatie over de implicaties van een 'Deny' regel kan
            <a target="_blank" href="http://docs.oasis-open.org/xacml/3.0/xacml-3.0-core-spec-os-en.html#_Toc325047268"> hier </a> worden gevonden.
          </p>

          <h2>Dienst</h2>

          <p>De dienst bepaalt voor welke dienst deze autorisatie regel van toepassing is. Je kan enkel 1 dienst koppelen aan een autorisatie regel.</p>

          <h2>Instelling</h2>

          <p>De instelling bepaalt voor welke instelling deze autorisatie regel van toepassing is. De instelling in deze context
            is de 'Identity Provider' die de gebruiker heeft geautenticeerd. Je kan 0 of meer instellingen koppelen aan een autorisatie regel.</p>

          <h2>Regel</h2>

          <p>Kies of dat alle attributen een match moeten opleveren of dat een enkele match voldoende is om de gebruiker te autoriseren voor de dienst.</p>

          <h2>Attributen</h2>

          <p>De attributen en de respectievelijke waardes bepalen of een gebruiker succesvol wordt geautoriseerd voor de dienst.</p>

        </div>
    );
  }
});
