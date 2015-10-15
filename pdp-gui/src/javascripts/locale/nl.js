// Interpolation works as follows:
//
// Make a key with the translation and enclose the variable with {{}}
// ie "Hello {{name}}" Do not add any spaces around the variable name.
// Provide the values as: I18n.t("key", {name: "John Doe"})


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
    PAP_CLIENT: "Instellings beheerder",
    PAP_ADMIN: "SURFnet beheerder"
  },

  navigation: {
    policies: "Autorisatie regels",
    violations: "Ongeautoriseerde logins",
    playground: "Playground",
    my_idp: "Mijn instelling",
    new_policy: "+ Nieuwe autorisatie regel"
  },

  policies: {
    name: "Naam",
    description: "Omschrijving",
    serviceProviderId: "Dienst",
    identityProviderIds: "Instelling",
    violations: "Overtredingen",
    search: "Zoek autorisatie regels...",
    flash: "Autorisatie regel '{{policyName}}' is succesvol {{action}}",
    flash_updated: "bijgewerkt",
    flash_created: "aangemaakt",
    flash_deleted: "verwijderd"
  },

  policy_detail: {
    update_policy: "Bijwerken autorisatie regel" ,
    create_policy: "Nieuwe autorisatie regel",
    confirmation: "Weet je zeker dat je deze pagina wilt sluiten?",
    name: "Naam",
    description: "Omschrijving",
    access: "Toegang",
    permit: "'Permit'",
    permit_info: "'Permit' regels dwingen af dat de gebruiker alleen wordt geautoriseerd als de attributen matchen. Als er geen match is dan wordt de gebruiker niet toegelaten tot de dienst.",
    deny: "'Deny'",
    deny_info: "'Deny' regels zijn minder gebruikelijk. Als de attributen matchen dan mag de gebruiker niet naar de dienst. Als de attributen niet matchen dan wel.",
    deny_message: "Ongeautoriseerd melding in het Engels",
    deny_message_info: "Dit is de melding die de gebruiker ziet bij een 'Deny' op basis van deze regel.",
    deny_message_nl: "Ongeautoriseerd melding",
    sp_placeholder: "Selecteer de dienst - verplicht",
    idps_placeholder: "Selecteer de instellingen - 0 of meer",
    rule: "Regel",
    rule_and: "EN",
    rule_and_info: "Autorisatie regels met een logische EN dwingen af dat alle gedefinieerde attributen moeten matchen.",
    rule_or: "OF",
    rule_or_info: "Voor autorisatie regels met een logische OF is het slechts vereist dat 1 attribuut matched.",
    rule_info_add: " Attribuut waardes van hetzelfde attribuut zullen altijd worden geevalueerd met de logische OF.",
    rule_info_add_2: "Een 'Deny' autorisatie regel wordt altijd geevalueerd met de logische EN voor attribuut waarden van verschillende attributen.",
    submit: "Verstuur",
    cancel: "Annuleer"
  },

  violations: {
    search: "Zoek ongeautoriseerde logins...",
    policyName: "Naam van de regel: ",
    table: {
      sp_idp: "Instelling en Dienst",
      decision: "Besluit",
      created: "Datum"
    }
  },

  playground: {
    policy: "Autorisatie regel",
    policy_info: "Optioneel, de geselecteerde regel wordt alleen gebruikt voor het zetten van de default voor de diesnt, instelling en attributen",
    policy_search: "Selecteer een regel",
    idp_placeholder: "Selecteer de instelling - verplicht",
    adventurous_title: "Code hacken?",
    adventurous_text: "Je kan direct de code veranderen in het rechter response scherm.",
    check_policies: "Nieuw autorisatie verzoek",
    clear_policies: "Clear",
    reload_policy: "Voer een nieuwe check uit met onderstaande wijzigingen"
  },

  policy_attributes: {
    attribute_value_placeholder: "Attribuut waarde...",
    attribute: "Attribuut",
    values: "Waarde(s)",
    new_value: "Voeg een nieuwe waarde toe...",
    new_attribute: "Voeg een nieuw attribuut toe..."
  },

  contact: {
    email: "Service support e-mail"
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
  }


};
