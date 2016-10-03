import React from "react";

class IdentityHelpNl extends React.Component {
  render: function () {
    return (
        <div className="form-element about">
          <h1>Identiteit</h1>

          <p>Gebruikers van de PDP applicatie (deze GUI) zijn administrators en kunnen dus alle regels en
            schendingen te bekijken.
            Administrators kunnen ook policy&quot;s voor elke IdP en SP maken.</p>

          <h2>Vertrouwde API</h2>

          <p>Voor andere gebruikers van de API van de PDP - zoals het Dashboard voor Instellingsbeheerders - zal
            de toegankelijkheid en functionaliteit zijn beperkt op basis van de identiteit van de gebruiker.
          </p>

          <h2>Identiteit-imitatie</h2>

          <p>Om de gevolgen van de autorisatieregels te testen kunt u een gebruiker van een bepaalde IdP impersoneren.
            Op basis van de IdP zijn de volgende beperkingen van toepassing:</p>
          <ul>

            <li>Het aanmaken, verwijderen en bijwerken van een policy is alleen toegestaan:
              <ul>
                <li>Als de IdP(s) van de policy een subset zijn van de IdP(s) van de gebruiker, OF
                </li>
                <li>als de policy geen IdP heeft en de SP van de policy is gekoppeld aan de IdP van de
                  gebruiker
                </li>
              </ul>
            </li>
            <li>Daarnaast is verwijderen en updaten van een policy verder beperkt:
              <ul>
                <li>de IdP van de gebruiker die de policy heeft gemaakt, moet de IDP van de gebruiker zijn die de policy wil verwijderen of bijwerken
                </li>
              </ul>
            </li>
            <li>Policy&quot;s kunnen in alleen-lezenmodus bekeken worden als:
              <ul>
                <li>De verzameling IdP&quot;s van de policy leeg is en de SP van de policy toegang openstelt voor &eacute;&eactute;n van de IdP&quot;s van de gebruiker, OF</li>
                <li>E&eacute;n van de IdP&quot;s van de policy de IdP van de gebruiker is, OF</li>
                <li>De SP van de policy verbonden is met &eacute;&eacute;n van de IdP&quot;s van de gebruiker.</li>
              </ul>
            </li>
            <li>Schendingen worden gefilterd en alleen getoond als:</ li>
            <ul>
              <ul>
                <li>De IdP in het JSON-verzoek van de overtreding gelijk is aan één van de IdP&quot;s van de gebruiker</li>
              </ul>
            </ul>
          </ul>
        </div>

    );
  }
  }

  export default IdentityHelpNl;
