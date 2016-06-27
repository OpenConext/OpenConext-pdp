/** @jsx React.DOM */

App.Pages.Decisions = React.createClass({

  getInitialState: function () {
    return {data: []}
  },

  componentDidMount: function () {
    this.initGraph();
  },

  initGraph: function () {
    var decisions = this.props.decisions;
    var pdpInternal = [];
    var teams = [];
    var sab = [];
    decisions.forEach(function(decision, index){
      var dec = JSON.parse(decision.decisionJson);
      pdpInternal.push({x: index + 1, y: dec.responseTimeMs});
      teams.push({x: index + 1, y: dec.pipResponses['teams_pip'] || 0});
      sab.push({x: index + 1, y: dec.pipResponses['sab_pip'] || 0});
    });
    var graph = new Rickshaw.Graph({
      element: document.querySelector("#chart"),
      width: document.getElementById('chart').offsetWidth,//* 2,
      height: 400,
      renderer: 'bar',
      stroke: true,
      preserve: true,
      series: [{
        data: pdpInternal,
        color: '#4DB3CF',//$blue
        name: 'PDP internal'
      }, {
        data: teams,
        color: '#519B00',//$green
        name: 'Teams PIP'
      }, {
        data: sab,
        color: '#ec9a0a',//$orange
        name: 'SAB PIP'
      }]
    });

    var formatX = function(n) {
      return n;
    };
    var formatY = function(n) {
      return n + 'ms';
    };

    new Rickshaw.Graph.Axis.X( {
      graph: graph,
      orientation: 'bottom',
      element: document.getElementById('x_axis'),
      pixelsPerTick: 150,
      tickFormat: formatX
    } );
    new Rickshaw.Graph.Axis.Y( {
      graph: graph,
      orientation: 'left',
      tickFormat: formatY,
      pixelsPerTick: 75,
      element: document.getElementById('y_axis')
    } );
    graph.render();

    new Rickshaw.Graph.HoverDetail({
      graph: graph,
      xFormatter: function (x) {
        return new Date(decisions[x - 1].created).toLocaleDateString();
      },
      yFormatter: function (y) {
        return y === null ? y : y.toFixed(2) + 'ms';
      }
    });

    var legend = new Rickshaw.Graph.Legend({
      graph: graph,
      element: document.getElementById('legend')
    });

    new Rickshaw.Graph.Behavior.Series.Toggle({
      graph: graph,
      legend: legend
    });
    new Rickshaw.Graph.Behavior.Series.Highlight({
      graph: graph,
      legend: legend
    });

  },

  render: function () {
    return (
      <div className="mod-decisions">
        <div id="legend"></div>
        <div className="graph-container">
          <div id="y_axis"></div>
          <div id="chart" className="rickshaw_graph"></div>
          <div id="x_axis"></div>
        </div>
      </div>
    );
  }
})
;
