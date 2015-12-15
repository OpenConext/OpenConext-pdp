/** @jsx React.DOM */

App.Help.IdentityHelpNl = React.createClass({
  render: function () {
    return (
        <div className="form-element about">
          <h1>Identiteit</h1>

          <p>Gebruikers van de PDP applicatie (dit GUI) zijn administrators en kunnen dus alle regels en
            schendingen te bekijken.
            Administrators kunnen ook beleid voor elke IdP en SP te maken.</p>

          <h2>Vertrouwde API</h2>

          <p>Andere gebruikers van de API van de PDP - zoals Dashboard Institution beheerders - zal
            toegankelijkheid en functionaliteit zijn beperkt op basis van de identiteit van de gebruiker.
          </ p>

          <h2>Identiteit Imitatie</h2>

          <p>Om te testen en zie de gevolgen van de beperkingen die u kunt imiteren een ander
            gebruiker van een bepaalde IdP. Op basis van de IdP de volgende beperkingen zijn van toepassing:</p>
          <ul>

            <li>De creatie, verwijdering en actualisering van een beleid is alleen toegestaan ​​(OR):
              <ul>
                <li>Als de IdP (s) van het beleid zijn een subset van de IdP (s) van de gebruiker - verbonden door
                  Instelling ID
                </ li>
                <li>als het beleid heeft geen IdP en de SP van het beleid is gekoppeld aan een van de IdP van de
                  gebruiker
                </ li>
              </ ul>
            </ li>
            <li>Daarnaast verwijderen en updaten van het beleid wordt verder beperkt:
              <ul>
                <li>de IdP van de gebruiker die het beleid gemaakt, moet de IDP van de gebruiker die wil om te
                  verwijderen / updaten van het beleid gelijk
                </ li>
              </ ul>
            </ li>
            <li>Beleid voor het overzicht en drop-down in de speeltuin worden gefilterd en alleen terug als (OR):
              <ul>
                <li>IdP's van het beleid zijn leeg en de SP van het beleid is toegestaan ​​door een van de IdP's van de
                  gebruiker
                </ li>
                <li>een van de IdP's van het beleid is gelijk aan de IdP van de gebruiker</ li>
                <li>de SP van het beleid is gekoppeld aan een van de IdP's van de gebruiker</ li>
              </ ul>
            </ li>
            <li>beleid schendingen van het overzicht worden gefilterd en alleen terug als (OR):</ li>
            <ul>
              <ul>
                <li>de IdP in de JSON verzoek van de overtreding is gelijk aan één van de IdP's van de gebruiker</ li>
                <li>de SP in de JSON verzoek van de overtreding wordt gekoppeld aan een IdP's van de gebruiker</ li>
              </ ul>
            </ ul>
          </ ul>
        </div>

    );
  }
});