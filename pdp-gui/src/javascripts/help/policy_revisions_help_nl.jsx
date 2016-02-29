/** @jsx React.DOM */

App.Help.PolicyRevisionsHelpNl = React.createClass({
  render: function () {
    return (
        <div className="form-element about">
          <h1>Wat zijn autorisatieregel revisies?</h1>

          <p>Elke keer als een autorisatieregel wordt bijgewerkt dan wordt er een copie van de
            oude regel opgeslagen als een revisie van de nieuw bijgewerkte regel. Door revisies
            met elkaar en met meest actuele regel te vergelijken kan een audit log worden getoond van alle veranderingen
            die zijn gemaakt van een autorisatieregel.
          </p>

          <h2>Retentie</h2>

          <p>Wanneer een autorisatieregel wordt verwijderd dan worden ook alle revisies van die regel verwijderd.</p>
        </div>
    );
  }
});
