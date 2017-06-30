import React from "react";
import I18n from "i18n-js";
// This fixes a bug in Rickshaw
window.jQuery = undefined;
import Rickshaw from "rickshaw";

import {getDecisions} from "../api";

class Decisions extends React.Component {

    constructor() {
        super();

        this.state = {
            avg: {},
            decisions: [],
            period: 7
        };

        this.pdp = [];
        this.sab = [];
        this.teams = [];
    }

    componentWillMount() {
        getDecisions(this.state.period)
            .then(decisions => this.setState({decisions}, () => this.initGraph()));
    }

    updateGraph(period) {
        this.setState({period});

        getDecisions(period)
            .then(decisions => this.setState({decisions}, () => this.redrawGraph()));
    }

    redrawGraph() {
        this.fillData();

        this.graph.update();
    }

    average(arr) {
        return arr.map(o => o.y)
            .filter(n => n !== 0)
            .reduce((prev, curr, i, arr) => {
                const total = prev + curr;
                return i === arr.length - 1 ? total / arr.length : total;
            }, 0).toFixed(0);
    }

    fillData() {
        const total = [];

        this.state.decisions.forEach((decision, index) => {
            const dec = JSON.parse(decision.decisionJson);
            total[index] = {x: decision.created / 1000, y: dec.responseTimeMs};
            const yTeams = dec.pipResponses["teams_pip"] || 0;
            this.teams[index] = {x: decision.created / 1000, y: yTeams};
            const ySab = dec.pipResponses["sab_pip"] || 0;
            this.sab[index] = {x: decision.created / 1000, y: ySab};
            this.pdp[index] = {x: decision.created / 1000, y: dec.responseTimeMs - yTeams - ySab};
        });

        while (this.pdp.length > this.state.decisions.length) {
            this.pdp.pop();
        }

        while (this.teams.length > this.state.decisions.length) {
            this.teams.pop();
        }

        while (this.sab.length > this.state.decisions.length) {
            this.sab.pop();
        }

        this.setState({
            avg: {
                total: this.average(total),
                sab: this.average(this.sab),
                teams: this.average(this.teams),
                pdp: this.average(this.pdp)
            }
        });
    }

    initGraph() {
        this.fillData();

        this.graph = new Rickshaw.Graph({
            element: document.querySelector("#chart"),
            width: document.getElementById("chart").offsetWidth,//* 2,
            height: 400,
            renderer: "area",
            interpolation: "linear",
            series: [{
                data: this.pdp,
                color: "#4DB3CF",//$blue
                name: "PDP internal"
            }, {
                data: this.teams,
                color: "#519B00",//$green
                name: "Teams PIP"
            }, {
                data: this.sab,
                color: "#ec9a0a",//$orange
                name: "SAB PIP"
            }]
        });

        const formatY = n => n + "ms";

        new Rickshaw.Graph.Axis.Time({
            graph: this.graph,
            orientation: "bottom",
            element: document.getElementById("x_axis"),
            timeFixture: new Rickshaw.Fixtures.Time.Local()
        });

        new Rickshaw.Graph.Axis.Y({
            graph: this.graph,
            orientation: "left",
            tickFormat: formatY,
            element: document.getElementById("y_axis")
        });

        this.graph.render();

        new Rickshaw.Graph.HoverDetail({
            graph: this.graph,
            xFormatter: x => new Date(x * 1000).toLocaleDateString(),
            yFormatter: y => y === null ? y : y.toFixed(0) + "ms"
        });

        const legend = new Rickshaw.Graph.Legend({
            graph: this.graph,
            element: document.getElementById("legend")
        });

        new Rickshaw.Graph.Behavior.Series.Toggle({
            graph: this.graph,
            legend: legend
        });

        new Rickshaw.Graph.Behavior.Series.Highlight({
            graph: this.graph,
            legend: legend
        });
    }

    renderPeriodButton(period) {
        let className = "c-button";

        if (period !== this.state.period) {
            className += " cancel";
        }

        return (
            <button className={className} onClick={() => this.updateGraph(period)}>
                { I18n.t("decisions.days", {count: period}) }
            </button>
        );
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
                <div className="period-buttons">
                    { this.renderPeriodButton(1) }
                    { this.renderPeriodButton(7) }
                    { this.renderPeriodButton(30) }
                </div>
            </div>
        );
    }
}

export default Decisions;
