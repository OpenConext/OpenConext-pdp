// Interpolation works as follows:
//
// Make a key with the translation and enclose the variable with {{}}
// ie "Hello {{name}}" Do not add any spaces around the variable name.
// Provide the values as: I18n.t("key", {name: "John Doe"})


I18n.translations.nl = {
  code: "NL",
  name: "Nederlands",
  select_locale: "Selecteer Nederlands",

  boolean: {
    yes: "Ja",
    no: "Nee"
  },

  date: {
    month_names: [null, "januari", "fabruari", "maart", "april", "mei", "juni", "juli", "augustus", "september", "ocktober", "november", "december"]
  },

  header: {
    title: "Dashboard",
    welcome: "Welkom,",
    links: {
      help_html: "<a href=\"https://wiki.surfnet.nl/display/conextsupport/Dashboard+van+SURFconext+%28NL%29\" target=\"_blank\">Help</a>",
      logout: "Uitloggen",
      exit: "Exit"
    },
    you: "Jij",
    profile: "Profiel",
    switch_idp: "Kies IDP"
  },

  navigation: {
    apps: "Services",
    notifications: "Notificaties",
    history: "Logboek",
    stats: "Statistieken",
    my_idp: "Mijn instelling"
  },

  facets: {
    title: "Filters",
    reset: "reset",
    download: "Download overzicht",
    unknown: "Unknown",
    totals: {
      all: "Alle {{total}} services worden weergegeven",
      filtered: "{{count}} uit {{total}} services worden weergegeven"
    },
    static: {
      connection: {
        name: "Dienst gekoppeld",
        has_connection: "Ja",
        no_connection: "Nee"
      },
      license: {
        name: "Licentie",
        unknown: "Onbekend",
        not_needed: "Niet nodig",
        no_license: "Nee",
        has_license_sp: "Ja, bij service provider",
        has_license_surfmarket: "Ja, bij SURFmarket"
      },
      used_by_idp: {
        name: "Aangeboden door mijn instelling",
        yes: "Ja",
        no: "Nee"
      },
      published_edugain: {
        name: "Gepubliceerd in eduGAIN federatie",
        yes: "Ja",
        no: "Nee"
      }
    }
  },

  apps: {
    overview: {
      name: "Service",
      license: "Licentie",
      license_unknown: "Onbekend",
      connected: "Dienst gekoppeld",
      search_hint: "Zoeken",
      search: "Zoek",
      connect: "",
      connect_button: "Activeren",
      no_results: "Geen services beschikbaar"
    },
    detail: {
      overview: "Overzicht",
      license_info: "Licentie",
      attribute_policy: "Attributen",
      idp_usage: "Gebruikt door",
      how_to_connect: "Dienst koppelen",
      how_to_disconnect: "Dienst ontkoppelen",
      application_usage: "Service gebruik"
    }
  },

  app_meta: {
    question: "Heb je een vraag?",
    eula: "Algemene voorwaarden",
    website: "Website",
    support: "Support pagina",
    login: "Login pagina"
  },

  license_info_panel: {
    title: "Licentie informatie",
    has_license_surfmarket_html: "Er is via <a href=\"https://www.surfmarket.nl\" target=\"_blank\">SURFmarket</a> een licentie afgesloten voor deze service.",
    has_license_sp_html: "Er is via <a href=\"{{serviceUrl}}\" target=\"_blank\">{{serviceName}}</a> een licentie afgesloten voor deze service.",
    no_license_html: "Jouw instelling heeft voor deze service geen licentie via <a href=\"https://www.surfmarket.nl\" target=\"_blank\">SURFmarket</a>.",
    not_needed_html: "Voor deze dienst is geen licentie nodig",
    unknown_license: "Het is onbekend welke licentie voor deze service geldt.",
    no_license_description_html: "" +
      "<ul>" +
      "   <li>Laat de licentiecontactpersoon van jouw instelling een licentie afsluiten bij <a href=\"https://www.surfmarket.nl\" target=\"_blank\">SURFmarket</a></li>" +
      "</ul>" +
      "<br />In sommige gevallen is de licentie direct bij de aanbieder van de service afgesloten.",
    unknown_license_description_html: "Er zijn verschillende opties:" +
      "<ul>" +
      "   <li>SURF of een andere instelling biedt deze service gratis aan.</li>" +
      "   <li>De licentie moet direct bij de aanbieder van de service worden afgesloten.</li>" +
      "   <li>De licentie is nog niet bijgewerkt in de administratie van <a href=\"https://www.surfmarket.nl\" target=\"_blank\">SURFmarket</a>.</li>" +
      "</ul>" +
      "<p>SURFnet zal, indien nodig, contact opnemen met de aanbieder of <a href=\"https://www.surfmarket.nl\" target=\"_blank\">SURFmarket</a> alvorens de koppeling te activeren.</p>"
  },

  license_info: {
    unknown_license: "Geen licentieinformatie beschikbaar",
    has_license_surfmarket: "Licentie beschikbaar via SURFmarket",
    has_license_sp: "Licentie beschikbaar via service supplier",
    no_license: "Licentie is niet aanwezig",
    no_license_needed: "Geen licentie nodig",
    license_info: "Lees hoe je een licentie kunt verkrijgen",
    license_unknown_info: "Lees meer",
    valid: "Licentie is geldig t/m {{date}}"
  },

  overview_panel: {
    wiki_info_html: "Voor deze service is extra informatie beschikbaar in de SURFconext <a href=\"{{link}}\" target=\"_blank\">wiki</a>.",
    no_description: "Er is geen beschijving voor deze service.",
    description: "Beschrijving",
    has_connection: "Dienst gekoppeld",
    no_connection: "Dienst niet gekoppeld",
    how_to_connect: "Lees hoe je een dienst koppelt",
    disconnect: "Lees hoe je een dienst ontkoppelt",
    normen_kader: "Juridisch normenkader cloudservices hoger onderwijs",
    normen_kader_html: "{{name}} heeft zijn compliance en non-compliance met het Juridisch normenkader cloudservices hoger onderwijs <a href=\"{{link}}\" target=\"_blank\">hier</a> gepubliceerd. Voor meer informatie over het framework zie de <a href=\"https://www.surf.nl/kennis-en-innovatie/kennisbank/2013/juridisch-normenkader-cloud-services-hoger-onderwijs.html\" target=\"_blank\">SURFnet website</a>",
    no_normen_kader_html: "Het is onbekend in welke mate {{name}} voldoet aan het Juridisch normenkader cloudservices hoger onderwijs. Voor meer informatie over het framework zie de <a href=\"https://www.surf.nl/kennis-en-innovatie/kennisbank/2013/juridisch-normenkader-cloud-services-hoger-onderwijs.html\" target=\"_blank\">SURFnet website</a>",
    single_tenant_service: "Single tenant dienst",
    single_tenant_service_html: "{{name}} is een single tenant dienst en als een consequentie daarvan is er een aparte applicatie instantie vereist voor elk instituut dat een connectie wil met deze dienst. Zie de <a href=\"https://wiki.surfnet.nl/display/services/(Cloud)services\" target=\"_blank\">SURFnet wiki</a> voor meer informatie over single tenant diensten."
  },

  attributes_policy_panel: {
    title: "Attributen",
    subtitle: "De volgende attributen worden uitgewisseld met {{name}}. Let wel: alle attributen moeten met de juiste waarden gevuld zijn. Als dit niet het geval is, zijn er extra stappen nodig om de connectie te activeren.",
    attribute: "Attribuut",
    your_value: "Jouw waarde",
    hint: "De attributen en hun waarden voor jouw persoonlijke account worden getoond. Dit is misschien niet representatief voor andere accounts binnen jouw instelling.",
    arp: {
      noarp: "Er is geen 'Attribute Release Policy' bekend. Alle bekende attributen zullen worden uitgewisseld met {{name}}.",
      noattr: "Er zullen geen attributen worden uitgewisseld met {{name}}."
    }
  },

  idp_usage_panel: {
    title: "Gebruikt door",
    subtitle: "De volgende instituties zijn gekoppeld aan {{name}}.",
    subtitle_none: "Er zijn geen instituties gekoppeld aan {{name}}.",
    institution: "Institutie"
  },

  how_to_connect_panel: {
    info_title: "Verbinding activeren",
    info_sub_title: "Je kunt een verbinding activeren vanuit dit dashboard. We adviseren je om de checklist na te lopen en de specifieke informatie over deze service door te nemen voordat je een verbinding activeert.",
    connect_title: "Activeer {{app}}",
    checklist: "Loop deze checklist na voordat je een connectie activeert:",
    check: "Controleer",
    read: "Lees de",
    license_info: "de licentieinformatie",
    attributes_policy: "het attribuutbeleid",
    wiki: "wiki voor deze service",
    connect: "Activeer connectie",
    cancel: "Annuleren",
    terms_title: "Met het activeren van de connectie ga je akkoord met de volgende voorwaarden:",
    comments_title: "Eventuele opmerkingen?",
    comments_description: "Opmerkingen worden verstuurd naar SURFconext.",
    comments_placeholder: "Voer hier je opmerkingen in...",
    provide_attributes: {
      before: "Het is de verantwoordelijkheid van mijn instelling om de juiste ",
      after: " aan te leveren."
    },
    forward_permission: {
      before: "SURFnet heeft toestemming om de ",
      after: " door te sturen naar {{app}}."
    },
    obtain_license: {
      before: "Het is de verantwoordelijkheid van mijn instelling om eventueel een ",
      after: " aan te schaffen voor het gebruik van {{app}}."
    },
    attributes: "attributen",
    license: "licentie",
    accept: "Ik bevestig dat ik de voorwaarden heb gelezen en deze in naam van mijn instelling accepteer.",
    back_to_apps: "Terug naar alle services",
    done_title: "Verbinding gemaakt!",
    done_subtitle_html: "Er zal contact worden opgenomen om deze aanvraag af te ronden. Als je voor die tijd nog vragen hebt, neem dan contact op met <a href=\"mailto:support@surfconext.nl\">support@surfconext.nl</a>.",
    disconnect_title: "Deactiveer connectie met {{app}}",
    accept_disconnect: "Ja, ik ga akkoord dat {{app}} niet meer beschikbaar zal zijn voor mijn organisatie",
    disconnect: "Verbinding deactiveren",
    done_disconnect_title: "Verzoek om verbinding te deactiveren is aangevraagd!",
    done_disconnect_subtitle_html: "Er zal contact worden opgenomen om deze aanvraag af te ronden. Als je voor die tijd nog vragen hebt, neem dan contact op met <a href=\"mailto:support@surfconext.nl\">support@surfconext.nl</a>.",
    single_tenant_service_warning: "Verzoeken voor activatie van single tenant diensten duren langer om te verwerken. SURFnet zal contact opnemen zodra het dit verzoek heeft ontvangen."
  },

  application_usage_panel: {
    title: "Service gebruik",
    description: "Aantal logins",
    logins: "Logins",
    last_week: "Afgelopen week",
    last_month: "Afgelopen maand",
    last_three_months: "Afgelopen 3 maanden",
    last_year: "Afgelopen jaar",
    download: "Download",
    error_html: "Op dit moment zijn de statistieken niet beschikbaar. <a href=\"mailto:support@surfconext.nl\">Neem contact op</a> met de supportafdeling, voor meer informatie."
  },

  contact: {
    email: "Service support e-mail"
  },

  search_user: {
    switch_identity: "Switch identiteit",
    search: "Filter op naam",
    name: "Naam",
    switch_to: "Switch naar rol",
    switch: {
      role_dashboard_viewer: "Viewer",
      role_dashboard_admin: "Admin"
    }
  },

  stats: {
    logins_for: "Logins voor {{service}}",
    legend: "Legenda"
  },

  server_error: {
    title: "Er is iets misgegaan tijdens het openen van de pagina.",
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
    surfnet_html: "<a href=\"http://www.surfnet.nl/\" target=\"_blank\">SURFnet</a>",
    terms_html: "<a href=\"https://wiki.surfnet.nl/display/conextsupport/Terms+of+Service+%28NL%29\" target=\"_blank\">Gebruikersvoorwaarden</a>",
    contact_html: "<a href=\"mailto:support@surfconext.nl\">support@surfconext.nl</a>"
  },

  notifications: {
    title: "Notificaties",
    icon: "Icoon",
    name: "Naam",
    license: "Licentie",
    connection: "Connectie",
    messages: {
      fcp: "Onderstaande diensten zijn mogelijkerwijs nog niet beschikbaar, omdat de licentie of de technische connectie nog niet aanwezig is."
    }
  },

  my_idp: {
    title: "Mijn instelling",
    sub_title_html: "De volgende rollen zijn toegekend (<a target=\"_blank\" href=\"https://wiki.surfnet.nl/pages/viewpage.action?pageId=25198606\">toelichting</a>):",
    role: "Rol",
    users: "Gebruiker(s)",
    SURFconextverantwoordelijke: "SURFconextverantwoordelijke",
    SURFconextbeheerder: "SURFconextbeheerder",
    "Dashboard supergebruiker": "Dashboard supergebruiker",
    services_title: "Deze services worden aangeboden door jouw instelling:",
    service_name: "Naam service",
    license_contact_html: "Primaire licensie contact persoon (<a target=\"_blank\" href=\"https://wiki.surfnet.nl/pages/viewpage.action?pageId=25198606\">toelichting</a>):",
    license_contact_name: "Naam",
    license_contact_email: "Email",
    license_contact_phone: "Telefoonnummer"
  },

  history: {
    title: "Logboek",
    requestDate: "Datum",
    type: "Type",
    jiraKey: "Ticket ID",
    status: "Status",
    userName: "Door",
    action_types: {
      LINKREQUEST: "Verbinden met {{serviceName}}",
      UNLINKREQUEST: "Ontbinden met {{serviceName}}",
      QUESTION: "Vraag"
    },
    statusses: {
      OPEN: "In behandeling",
      CLOSED: "Afgehandeld"
    }
  },

  profile: {
    title: "Profiel",
    sub_title: "Van uw instelling hebben wij de volgende gegevens ontvangen. Deze gegevens, alsmede uw groepsrelaties, worden opgeslagen in (en gebruikt door) SURFconext. Tevens is het mogelijk dat deze gegevens worden verstrekt aan diensten die u via SURFconext benadert.",
    my_attributes: "Mijn attributen",
    attribute: "Attribuut",
    value: "Waarde",
    my_roles: "Mijn rollen",
    my_roles_description: "De volgende rollen zijn toegekend",
    role: "Rol",
    role_description: "Omschrijving",
    roles: {
      ROLE_DASHBOARD_ADMIN: {
        name: "SURFconextverantwoordelijke",
        description: "U bent gemachtigd om voor uw instelling de connecties met Service Providers te beheren"
      },
      ROLE_DASHBOARD_VIEWER: {
        name: "SURFconextbeheerder",
        description: "U bent gemachtigd om voor uw instelling de connecties met Service Providers in te zien"
      },
      ROLE_DASHBOARD_SUPER_USER: {
        name: "Dashboard supergebruiker",
        description: "U bent een super gebruiker binnen het dashboard"
      }
    },
    attribute_map: {
      "Shib-uid": {
        name: "UID",
        description: "jouw unieke gebruikersnaam binnen jouw instelling"
      },
      "Shib-surName": {
        name: "Achternaam",
        description: "jouw achternaam"
      },
      "Shib-givenName": {
        name: "Voornaam",
        description: "voornaam/roepnaam"
      },
      "Shib-commonName": {
        name: "Volledige persoonsnaam",
        description: "volledige persoonsnaam"
      },
      "Shib-displayName": {
        name: "Weergavenaam",
        description: "weergave naam zoals getoond in applicaties"
      },
      "Shib-email": {
        name: "E-mailadres",
        description: "jouw e-mailadres zoals bekend binnen jouw instelling"
      },
      "Shib-eduPersonAffiliation": {
        name: "Relatie",
        description: "geeft de relatie aan tussen jou en jouw instelling"
      },
      "Shib-eduPersonEntitlement": {
        name: "Rechtaanduiding",
        description: "rechtaanduiding; URI (URL of URN) dat een recht op iets aangeeft; wordt bepaald door een contract tussen dienstaanbieder en instelling"
      },
      "Shib-eduPersonPN": {
        name: "Net-ID",
        description: "jouw unieke gebruikersnaam binnen jouw instelling aangevuld met @instellingsnaam.nl"
      },
      "Shib-preferredLanguage": {
        name: "Voorkeurstaal",
        description: "een tweeletterige afkorting van de voorkeurstaal volgens de ISO 639 taalafkortings codetabel; geen subcodes"
      },
      "Shib-homeOrg": {
        name: "Organisatie",
        description: "aanduiding voor de organisatie van een persoon gebruikmakend van de domeinnaam van de organisatie; syntax conform RFC 1035"
      },
      "Shib-schacHomeOrganizationType": {
        name: "Type Organisatie",
        description: "aanduiding voor het type organisatie waartoe een persoon behoort, gebruikmakend van de waarden zoals geregisteerd door Terena op: http://www.terena.org/registry/terena.org/schac/homeOrganizationType"
      },
      "Shib-nlEduPersonHomeOrganization": {
        name: "Weergavenaam van de Instelling",
        description: "weergavenaam van de instelling"
      },
      "Shib-nlEduPersonOrgUnit": {
        name: "Afdelingsnaam",
        description: "naam van de afdeling"
      },
      "Shib-nlEduPersonStudyBranch": {
        name: "Opleiding",
        description: "opleiding; numerieke string die de CROHOcode bevat. leeg als het een niet reguliere opleiding betreft"
      },
      "Shib-nlStudielinkNummer": {
        name: "Studielinknummer",
        description: "studielinknummer van student zoals geregistreerd bij www.studielink.nl"
      },
      "Shib-nlDigitalAuthorIdentifier": {
        name: "DAI",
        description: "Digital Author Identifier (DAI)"
      },
      "Shib-nlEduPersonHomeOrganization": {
        name: "Weergavenaam van de Instelling",
        description: "weergavenaam van de instelling"
      },
      "Shib-nlEduPersonStudyBranch": {
        name: "Opleiding",
        description: "opleiding; numerieke string die de CROHOcode bevat. leeg als het een niet reguliere opleiding betreft"
      },
      "Shib-userStatus": {
        name: "Gebruikersstatus",
        description: "Status van deze gebruiker in SURFconext"
      },
      "Shib-accountstatus": {
        name: "Accountstatus",
        description: "Status van deze account in SURFconext"
      },
      "name-id": {
        name: "Identifier",
        description: "Status van deze account in SURFconext"
      },
      "Shib-voName": {
        name: "Naam Virtuele Organisatie",
        description: "De naam van de Virtuele Organisatie waarvoor je bent ingelogd."
      },
      "Shib-user": {
        name: "Identifier",
        description: "Status van deze account in SURFconext"
      },
      "Shib-memberOf": {
        name: "Lidmaatschap",
        description: "Lidmaatschap van virtuele organisaties en SURFconext"
      },
    }
  }
};
