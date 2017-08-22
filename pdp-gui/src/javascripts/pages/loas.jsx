import React from "react";
import I18n from "i18n-js";
import $ from "jquery";

import {getAllowedLoas, getLoasStats} from "../api";
import LoasHelpEn from "../help/loas_help_en";
import LoasHelpNl from "../help/loas_help_nl";

class Loas extends React.Component {

    constructor(props) {
        super(props);
        this.state = {
            stats: {},
            allowedLoas: []
        };
    }

    componentWillMount() {
        Promise.all([getLoasStats(), getAllowedLoas()])
            .then(results =>
                this.setState({stats: results[0], allowedLoas: results[1]}, () => this.initDataTable()));
    }

    initDataTable() {
        $("#loas_table").DataTable({
            paging: false,
            searching: false,
            language: {
                lengthMenu: I18n.t("datatable.lengthMenu"),
                zeroRecords: I18n.t("datatable.zeroRecords"),
                infoEmpty: I18n.t("datatable.infoEmpty"),
                info: I18n.t("datatable.info"),
            }
        });
    }

    componentWillUnmount() {
        $("#loas_table").DataTable().destroy();
    }

    renderAboutPage() {
        return I18n.locale === "en" ? <LoasHelpEn/> : <LoasHelpNl/>;
    }

    renderTable() {
        const {stats, allowedLoas} = this.state;
        return (
            <table className='table table-bordered box' id='loas_table'>
                <thead>
                <tr className='success'>
                    <th className="loas_policy_id">{I18n.t("loas.policy_id")}</th>
                    {allowedLoas.map(loa =>
                        <th className="loa-level" key={loa}>{loa}</th>)}
                </tr>
                </thead>
                <tbody>
                {Object.keys(stats).map(policyId =>
                    <tr key={policyId}>
                        <td>{policyId}</td>
                        {allowedLoas.map(loa =>
                            <td className="count" key={`${policyId}_${loa}`}>
                                {stats[policyId][loa] || 0}
                            </td>
                        )}
                    </tr>
                )}
                </tbody>
            </table>
        );
    }

    render() {

        return (
            <div className='loas table-responsive'>
                {this.renderAboutPage()}
                {this.renderTable()}
            </div>
        );
    }
}

Loas.propTypes = {};

export default Loas;
