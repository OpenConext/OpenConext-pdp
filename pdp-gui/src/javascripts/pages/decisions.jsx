import React from "react";
import I18n from "i18n-js";
// This fixes a bug in Rickshaw
window.jQuery = undefined;
import Rickshaw from "rickshaw";

import { getDecisions } from "../api";

class Decisions extends React.Component {

  constructor() {
    super();
    this.state = {
      avg: {},
      decisions: []
    };
  }

  componentWillMount() {
    getDecisions().then(decisions => this.setState({ decisions }, () => this.initGraph()));
  }

  average(arr) {
    return arr.map(o => {
      return o.y;
    }).filter(n => {
      return n !== 0;
    }).reduce((prev, curr, i, arr) => {
      const total = prev + curr;
      return i == arr.length - 1 ? total / arr.length : total;
    }, 0).toFixed(0);
  }

  initGraph() {
    const decisions = this.state.decisions;
    const total = [];
    const pdp = [];
    const teams = [];
    const sab = [];
    decisions.forEach((decision, index) => {
      const dec = JSON.parse(decision.decisionJson);
      total.push({ x: index + 1, y: dec.responseTimeMs });
      const yTeams = dec.pipResponses["teams_pip"] || 0;
      teams.push({ x: index + 1, y: yTeams });
      const ySab = dec.pipResponses["sab_pip"] || 0;
      sab.push({ x: index + 1, y: ySab });
      pdp.push({ x: index + 1, y: dec.responseTimeMs - yTeams - ySab });
    });
    this.setState({
      avg: {
        total: this.average(total),
        sab: this.average(sab),
        teams: this.average(teams),
        pdp: this.average(pdp)
      }
    });
    const graph = new Rickshaw.Graph({
      element: document.querySelector("#chart"),
      width: document.getElementById("chart").offsetWidth,//* 2,
      height: 400,
      renderer: "bar",
      stroke: true,
      preserve: true,
      series: [{
        data: pdp,
        color: "#4DB3CF",//$blue
        name: "PDP internal"
      }, {
        data: teams,
        color: "#519B00",//$green
        name: "Teams PIP"
      }, {
        data: sab,
        color: "#ec9a0a",//$orange
        name: "SAB PIP"
      }]
    });

    const formatX = function(n) {
      return n;
    };
    const formatY = function(n) {
      return n + "ms";
    };

    new Rickshaw.Graph.Axis.X({
      graph: graph,
      orientation: "bottom",
      element: document.getElementById("x_axis"),
      pixelsPerTick: 150,
      tickFormat: formatX
    });
    new Rickshaw.Graph.Axis.Y({
      graph: graph,
      orientation: "left",
      tickFormat: formatY,
      pixelsPerTick: 75,
      element: document.getElementById("y_axis")
    });
    graph.render();

    new Rickshaw.Graph.HoverDetail({
      graph: graph,
      xFormatter: function(x) {
        return new Date(decisions[x - 1].created).toLocaleDateString();
      },
      yFormatter: function(y) {
        return y === null ? y : y.toFixed(0) + "ms";
      }
    });

    const legend = new Rickshaw.Graph.Legend({
      graph: graph,
      element: document.getElementById("legend")
    });

    new Rickshaw.Graph.Behavior.Series.Toggle({
      graph: graph,
      legend: legend
    });
    new Rickshaw.Graph.Behavior.Series.Highlight({
      graph: graph,
      legend: legend
    });

  }

  render() {
    return (
      <div className="mod-decisions">
        <div id="legend"></div>
        <div className="stats">
          <section>
            <span>{I18n.t("decisions.avg_total")}</span>
            <span>{this.state.avg.total + " ms"}</span>
          </section>
          <section>
            <span>{I18n.t("decisions.avg_pdp")}</span>
            <span>{this.state.avg.pdp + " ms"}</span>
          </section>
          <section>
            <span>{I18n.t("decisions.avg_teams")}</span>
            <span>{this.state.avg.teams + " ms"}</span>
          </section>
          <section>
            <span>{I18n.t("decisions.avg_sab")}</span>
            <span>{this.state.avg.sab + " ms"}</span>
          </section>
        </div>
        <div className="graph-container">
          <div id="y_axis"></div>
          <div id="chart" className="rickshaw_graph"></div>
          <div id="x_axis"></div>
        </div>
      </div>
    );
  }
}

export default Decisions;
