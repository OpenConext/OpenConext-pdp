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
    PAP_ADMIN: "SURFnet beheerder",
  },

  navigation: {
    policies: "Authenticatie regels",
    violations: "Overtredingen",
    playground: "Playground",
    about: "About",
    my_idp: "Mijn instelling",
    new_policy: "+ Nieuwe regel"
  },

  policies: {
    name: "Naam",
    description: "Omschrijving",
    serviceProviderId: "Dienst",
    identityProviderIds: "Instelling",
    violations: "Overtredingen",
    search: "Zoek authenticatie regels..."
  },

  playground: {
    policy:"Authenticatie regel",
    policy_info:"Optioneel, de geselecteerde regel wordt alleen gebruikt voor het zetten van de default voor de diesnt, instelling en attributen",
    policy_search:"Selecteer een regel"
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
