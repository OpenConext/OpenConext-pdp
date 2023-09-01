import React from "react";
import I18n from "i18n-js";
import moment from "moment";
import "jsondiffpatch/public/formatters-styles/html.css";

import {DiffPatcher} from "jsondiffpatch/src/diffpatcher";
import HtmlFormatter from "jsondiffpatch/src/formatters/html";

import PolicyRevisionsHelpNl from "../help/policy_revisions_help_nl";
import PolicyRevisionsHelpEn from "../help/policy_revisions_help_en";

import {getRevisions} from "../api";

const ignoreInDiff = ["created", "id", "revisionNbr", "actionsAllowed", "activatedSr", "numberOfRevisions", "numberOfViolations"];

class PolicyRevisions extends React.Component {

    constructor() {
        super();
        this.state = {data: [], revisions: []};
        this.differ = new DiffPatcher();
    }

    componentWillMount() {
        getRevisions(this.props.params.id).then(revisions => this.setState({revisions}));
    }

    renderDiff(passedPrev, curr) {
        let prev = passedPrev;
        //means someone if looking at the first initial revision
        if (!prev) {
            prev = {attributes: []};
        }

        return (
            <section>
                {this.renderTopDiff(prev, curr)}
                <div className="form-element">
                    <div className="diff-panel">
                        {this.renderDiffsBetweenCurrentAndPrevious(curr, prev)}
                    </div>
                </div>
            </section>
        );
    }

    renderDiffsBetweenCurrentAndPrevious = (curr, previous) => {
        const rev = {...curr};
        ignoreInDiff.forEach(ignore => delete rev[ignore]);
        const prev = {...previous};
        ignoreInDiff.forEach(ignore => delete prev[ignore]);

        const delta = this.differ.diff(prev, rev);
        const html = HtmlFormatter.format(delta, prev);

        return delta ? <p dangerouslySetInnerHTML={{__html: html}}/> : <p>{I18n.t("revisions.identical")}</p>;
    };

    renderTopDiff(prev, curr) {
        if (prev.revisionNbr !== undefined && prev.revisionNbr !== curr.revisionNbr) {
            return (
                <div className="top-diff" dangerouslySetInnerHTML={{
                    __html: I18n.t("revisions.changes_info_html",
                        {
                            userDisplayName: curr.userDisplayName,
                            authenticatingAuthorityName: curr.authenticatingAuthorityName,
                            createdDate: this.createdDate(curr),
                            currRevisionNbr: curr.revisionNbr,
                            prevRevisionNbr: prev.revisionNbr
                        }
                    )
                }}>
                </div>
            );
        }
        return (
            <div className="top-diff" dangerouslySetInnerHTML={{
                __html: I18n.t("revisions.changes_first_html",
                    {
                        userDisplayName: curr.userDisplayName,
                        authenticatingAuthorityName: curr.authenticatingAuthorityName,
                        createdDate: this.createdDate(curr),
                        currRevisionNbr: curr.revisionNbr
                    }
                )
            }}>
            </div>
        );
    }

    handleCompare(revision) {
        return function (e) {
            e.preventDefault();
            e.stopPropagation();
            const prev = this.state.revisions.filter(rev => {
                return rev.revisionNbr === (revision.revisionNbr - 1);
            });
            this.setState({curr: revision});
            this.setState({prev: prev[0]});
        }.bind(this);
    }

    renderComparePanel() {
        const prev = this.state.prev;
        const curr = this.state.curr;

        if (prev || curr) {
            return this.renderDiff(prev, curr);
        }

        return this.renderAboutPage();
    }

    renderAboutPage() {
        return I18n.locale === "en" ? <PolicyRevisionsHelpEn/> : <PolicyRevisionsHelpNl/>;
    }

    renderOverview() {
        return (<div>
            <p className="form-element title">{I18n.t("revisions.title")}</p>
            {this.renderRevisions()}
        </div>);
    }

    renderRevisions() {
        this.state.revisions.sort((rev1, rev2) => {
            return rev2.revisionNbr - rev1.revisionNbr;
        });
        return this.state.revisions.map((revision, index) => {
            return this.renderRevision(revision, index);
        });
    }

    createdDate(revision) {
        const created = moment(revision.created);
        created.locale(I18n.locale);
        return created.format("LLLL");
    }

    renderRevision(revision, index) {
        const className = index === 0 ? "success" : "failure";
        const linkClassName = this.state.curr && this.state.curr.revisionNbr === revision.revisionNbr ? "selected" : "";
        return (
            <div key={index}>
                <div className={"form-element split sub-container " + className}>
                    {this.renderRevisionMetadata(revision)}
                    <a className={"c-button white compare " + linkClassName} href="#"
                       onClick={this.handleCompare(revision)}>&lt; &gt;</a>
                </div>
                <div className="bottom"></div>
            </div>
        );
    }

    renderRevisionMetadata(revision) {
        return (<div><p className="label before-em">{revision.name}</p>
            <p className="before-em">{I18n.t("revisions.revision") + " " + revision.revisionNbr}</p>
            <p className="before-em smaller">{I18n.t("policy_detail.created_title", {
                displayName: revision.userDisplayName,
                created: this.createdDate(revision)
            })}</p>
        </div>);
    }

    render() {
        return (
            <div className="l-center mod-revisions" ref={node => this.node = node}>
                <div className="l-split-left form-element-container box">
                    {this.renderOverview()}
                </div>
                <div className="l-split-right form-element-container box">
                    {this.renderComparePanel()}
                </div>
            </div>
        );
    }
}

PolicyRevisions.propTypes = {
    params: React.PropTypes.shape({
        id: React.PropTypes.string
    })
};

export default PolicyRevisions;
