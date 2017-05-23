// Interpolation works as follows:
//
// Make a key with the translation and enclose the variable with {{}}
// ie "Hello {{name}}" Do not add any spaces around the variable name.
// Provide the values as: I18n.t("key", {name: "John Doe"})
import I18n from "i18n-js";


I18n.translations.nl = {
    code: "NL",
    name: "Nederlands",
    select_locale: "Selecteer Nederlands",

    header: {
        title: "Policy Administration Point",
        welcome: "Welkom ",
        links: {
            help_html: "<a href=\"https://github.com/OpenConext/OpenConext-pdp#policy-limitations\" target=\"_blank\">Help</a>",
            logout: "Uitloggen",
            exit: "Exit"
        },
        idps: "Je instelling(en)",
        sps: "Je dienst(en)",
        role: "Rol"
    },

    profile: {
        ROLE_PEP: "Instellingsbeheerder",
        ROLE_ADMIN: "SURFnet-beheerder"
    },

    navigation: {
        policies: "Autorisatieregels",
        violations: "Ongeautoriseerde logins",
        conflicts: "Conflicten",
        decisions: "Statistieken",
        playground: "Playground",
        my_idp: "Mijn instelling",
        new_policy: "+ Nieuwe autorisatieregel",
        identity: "Identiteit"
    },

    policies: {
        name: "Naam",
        description: "Omschrijving",
        serviceProviderId: "Dienst",
        activatedSr: "Geactiveerd in SR",
        isActive: "Actief",
        identityProviderIds: "Instelling(en)",
        identityProviderId: "Instelling",
        violations: "Overtredingen",
        revisions: "Revisies",
        search: "Zoek autorisatieregels...",
        flash: "Autorisatieregel '{{policyName}}' is succesvol {{action}}",
        flash_updated: "bijgewerkt",
        flash_created: "aangemaakt",
        flash_deleted: "verwijderd",
        confirmation: "Weet je zeker dat je autorisatieregel {{policyName}} wilt verwijderen?",
        edit: "Bewerk",
        delete: "Verwijder"
    },

    datatable: {
        lengthMenu: "Toon _MENU_ rijen",
        zeroRecords: "Er zijn geen gegevens aanwezig",
        infoEmpty: "",
        info: "_START_ tot _END_ van _TOTAL_ rijen",
        paginate_first: "Eerste",
        paginate_previous: "Vorige",
        paginate_next: "Volgende",
        paginate_last: "Laatste"
    },

    policy_detail: {
        update_policy: "Bijwerken autorisatieregel",
        create_policy: "Nieuwe autorisatieregel",
        confirmation: "Weet je zeker dat je deze pagina wilt sluiten?",
        name: "Naam",
        description: "Omschrijving",
        access: "Toegang",
        permit: "Permit",
        permit_info: "Permit-regels dwingen af dat de gebruiker alleen wordt geautoriseerd als de attributen matchen. Als er geen match is dan wordt de gebruiker niet toegelaten tot de dienst.",
        deny: "Deny",
        deny_info: "Deny regels zijn minder gebruikelijk. Als de attributen matchen dan mag de gebruiker niet naar de dienst. Als de attributen niet matchen dan wel.",
        deny_message: "Ongeautoriseerd-melding in het Engels",
        deny_message_info: "Dit is de melding die de gebruiker ziet bij een 'Deny' op basis van deze regel.",
        deny_message_nl: "Ongeautoriseerd-melding",
        sp_placeholder: "Selecteer de dienst - verplicht",
        idps_placeholder: "Selecteer de instellingen - 0 of meer",
        rule: "Regel",
        rule_and: "EN",
        rule_and_info: "Autorisatieregels met een logische EN dwingen af dat alle gedefinieerde attributen moeten matchen.",
        rule_or: "OF",
        rule_or_info: "Voor autorisatieregels met een logische OF is het slechts vereist dat 1 attribuut matcht.",
        rule_info_add: " Attribuutwaardes van hetzelfde attribuut zullen altijd worden geëvalueerd met de logische OF.",
        rule_info_add_2: "Een 'Deny' autorisatieregel wordt altijd geëvalueerd met de logische EN voor attribuutwaarden van verschillende attributen.",
        submit: "Verstuur",
        cancel: "Annuleer",
        sub_title: "Aangemaakt door {{displayName}} op {{created}}",
        autoFormat: "AutoFormat regel omschrijving",
        isActiveDescription: "Markeer de regel actief",
        isActiveInfo: " Inactieve regels worden niet geëvalueerd in access-beslissingen",
        isActive: "Actief",
        spScopeInfo: "De beschikbare diensten zijn beperkt tot je eigen diensten zolang er geen Instelling is gekozen",
        activated_false: "Niet geactiveerd in Service Registry",
        activated_true: "Geactiveerd in Service Registry"
    },

    violations: {
        search: "Zoek ongeautoriseerde logins...",
        policyName: "Naam van de regel: ",
        table: {
            sp_idp: "Instelling en Dienst",
            decision: "Besluit",
            created: "Datum",
            is_playground: "Playground"
        }
    },

    playground: {
        policy: "Autorisatieregel",
        policy_info: "Optioneel, de geselecteerde regel wordt alleen gebruikt voor het zetten van de default voor de dienst, instelling en attributen",
        policy_search: "Selecteer een regel",
        idp_placeholder: "Selecteer de instelling - verplicht",
        adventurous_title: "Code hacken?",
        adventurous_text: "Je kan direct de code veranderen in het rechter JSON request scherm.",
        check_policies: "Nieuw autorisatieverzoek",
        clear_policies: "Clear",
        reload_policy: "Voer een nieuwe check uit met onderstaande wijzigingen"
    },

    revisions: {
        title: "Revisies",
        revision: "Revisie nummer",
        name: "Naam",
        description: "Omschrijving",
        denyRule: "Toegang Permit-regel?",
        serviceProviderName: "Dienst",
        identityProviderNames: "Instelling(en)",
        allAttributesMustMatch: "Logische OF regel?",
        attributes: "Attributen",
        denyAdvice: "Ongeautoriseerd-melding in het Engels",
        denyAdviceNl: "Ongeautoriseerd-melding",
        active: "Actief",
        changes_info_html: "Veranderingen tussen <span class=\"prev\"> revisie nummer {{prevRevisionNbr}}</span> en <span class=\"curr\">revisie nummer {{currRevisionNbr}}</span> gemaakt door {{userDisplayName}} van {{authenticatingAuthorityName}} op {{createdDate}}.",
        changes_first_html: "Dit is de <span class=\"curr\">eerste revisie {{currRevisionNbr}}</span> aangemaakt door {{userDisplayName}} van {{authenticatingAuthorityName}} op {{createdDate}}."
    },

    policy_attributes: {
        attribute_value_placeholder: "Attribuutwaarde...",
        attribute: "Attribuut",
        values: "Waarde(s)",
        new_value: "Voeg een nieuwe waarde toe...",
        new_attribute: "Voeg een nieuw attribuut toe...",
        group_info: " De waarde(s) moeten volledige unieke groep ID zijn, b.v. 'urn:collab:group:surfteams.nl:nl:surfnet:diensten:admins'",
        sab_info: " De waarde(s) moet geldige rollen in SAB zijn, b.v. 'Instellingsbevoegde'"
    },
    conflicts: {
        title: "Conflicten",
        no_conflicts: "Er zijn geen conflicterende autorisatieregels",
        service_provider: "Dienstverlener",
        table: {
            name: "Naam autorisatieregel",
            idps: "Instituties"
        },
        hide_inactive: "Verberg inactieve conflicten",
        hide_inactive_note: " Zie het rechter infoblok voor details"
    },
    decisions: {
        avg_total: "Gemiddelde totaal",
        avg_pdp: "Gemiddelde PDP internal",
        avg_teams: "Gemiddelde Teams",
        avg_sab: "Gemiddelde SAB",
        days: {
            "one": "{{count}} dag",
            "other": "{{count}} dagen"
        }
    },
    identity: {
        title: "Identiteitsimpersonatie",
        subTitle: "",
        confirmation: "Verander identiteit",
        unspecifiedNameId: "NameID",
        unspecifiedNameIdInfo: "De unieke NameID wordt opgeslagen bij elke regel verandering",
        displayName: "Naam",
        displayNameInfo: "De naam van de gebruiker wordt ook opgeslagen bij elke nieuwe regel-revisie",
        submit: "Verstuur",
        clear: "Stop impersonatie",
        cancel: "Annuleer"
    },

    contact: {
        email: "Service support e-mail"
    },

    server_error: {
        title: "De PDP-applicatie is momenteel niet beschikbaar.",
        description_html: "Probeer het later nog eens of neem contract op met <a href=\"mailto:support@surfconext.nl\">support@surfconext.nl</a>."
    },

    not_found: {
        title: "Deze pagina kon niet worden gevonden.",
        description_html: "Controleer of het adres correct gespeld is of ga terug naar de <a href=\"/\">homepage</a>."
    },

    logout: {
        title: "Succesvol uitgelogd.",
        description_html: "Je <strong>MOET</strong> de browser afsluiten om het uitlogproces af te ronden."
    },

    footer: {
        surfnet_html: "<a href=\"https://www.surfnet.nl/\" target=\"_blank\">SURFnet</a>",
        terms_html: "<a href=\"https://wiki.surfnet.nl/display/conextsupport/Terms+of+Service+%28NL%29\" target=\"_blank\">Gebruikersvoorwaarden</a>",
        contact_html: "<a href=\"mailto:support@surfconext.nl\">support@surfconext.nl</a>"
    }


};
