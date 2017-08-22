import React from "react";

class LoasHelpNl extends React.Component {
    render() {
        return (
            <div className="form-element about">
                <h1>Level of Assurances</h1>

                <p>Iedere keer als een Stepup autorisatieregel een Level of Assurance (Loa) retourneert omdat
                    de attributen / IP address van een gebruiker gelijk is aan configuratie dan
                    wordt deze opgeslagen in de database. </p>

                <p>De tabel hieronder toont per Loa en per autorisatieregel het aantal keren
                    dat deze Loa is afgedwongen door de Stepup applicatie.</p>

            </div>
        );
    }
}

export default LoasHelpNl;
