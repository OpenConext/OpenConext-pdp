import React from "react";

class PolicyDetailHelpStepEn extends React.Component {
    render() {
        return (
            <div className="form-element about">
                <h1>Hoe maak je Stepup-Gateway autorisatieregels?</h1>

                <p>Autorisatieregels voor de Stepup-Gateway bepalen het Level of Assurance (LoA) voor gebruikers als zij inloggen op OpenConext.
                    De keuze welke LoA toe te passen wordt bepaald door de attributen van de gebruiker - eventueel inclusief het IP adres - zoals
                    deze zijn vrijgegeven door de Identity Provider.</p>

                <h2>Dienst</h2>

                <p>De dienst bepaalt op welke Service Provider deze autorisatieregel van toepassing is. Er kan precies
                    één dienst gekoppeld zijn aan een autorisatieregel.</p>

                <h2>Instelling</h2>

                <p>De instelling bepaalt voor welke instelling deze autorisatieregel van toepassing is. De instelling in
                    deze context
                    is de 'Identity Provider' die de gebruiker heeft geauthenticeerd. Je kunt nul of meer instellingen
                    koppelen aan een autorisatieregel.</p>

                <h2>Level of Assurance</h2>

                <p>Je kan meerdere LoA's configureren voor een autorisatieregel. Elke LoA zal worden vertaald naar een XACML rule. De attributen
                    van een LoA bepalen of deze wordt toegepast door de Stepup-Gateway</p>

                <section className="sub-form">
                    <h3>Autorisatieregel</h3>
                    <p>De EN / OF keuze bepaalt of alle attributen moeten matchen (=EN) of dat een enkele match voldoende is (=OF).</p>

                    <h3>Attributen</h3>

                    <p>De attributen en de waardes bepalen welke LoA zal worden toegepast.</p>

                    <h3>IP Ranges</h3>

                    <p>Je kan meerdere IP adressen / prefixes configuren. PDP zal het IP
                        adres van een gebruiker toetsen aan de range en afhankelijk van de negatie wordt de LoA toegepast. De checkbox 'negatie'
                        is een logische NOT voor een IP range.</p>

                    <p>Meerdere IP ranges binnen een LoA resulteren altijd in een logische OR.</p>

                    <h3>Autorisatie op teamnaam</h3>

                    <p>Let op als je het attribuut <em>urn:collab:group:surfteams.nl</em> kiest. De waarde(s) van dit
                        attribuut
                        moeten de geldige en volledige ID zijn van een team in SURFconext Teams. Neem contact op met
                        SURFconext support
                        als je twijfelt over de juiste waarde.</p>


                </section>
            </div>
        );
    }
}

export default PolicyDetailHelpStepEn;
