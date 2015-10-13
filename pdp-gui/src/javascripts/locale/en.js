// Interpolation works as follows:
//
// Make a key with the translation and enclose the variable with {{}}
// ie "Hello {{name}}" Do not add any spaces around the variable name.
// Provide the values as: I18n.t("key", {name: "John Doe"})


I18n.translations.en = {
  code: "EN",
  name: "English",
  select_locale: "Select English",

  boolean: {
    yes: "Yes",
    no: "No"
  },

  date: {
    month_names: [null, "January", "February", "March", "April", "May", "June", "July", "August", "September", "October", "November", "December"]
  },

  header: {
    title: "Policy Administration Point",
    welcome: "Welcome ",
    links: {
      help_html: "<a href=\"https://github.com/OpenConext/OpenConext-pdp#policy-limitations\" target=\"_blank\">Help</a>",
      logout: "Logout",
      exit: "Exit"
    },
    idps: "Your institution(s)",
    sps: "Your service(s)",
    role: "Role"
  },

  profile: {
    PAP_CLIENT: "Institution admin",
    PAP_ADMIN: "SURFnet admin",
  },

  navigation: {
    policies: "Policies",
    violations: "Violations",
    playground: "Playground",
    about: "About",
    my_idp: "My institute",
    new_policy: "+ New Policy"
  },

  policies: {
    name: "Name",
    description: "Description",
    serviceProviderId: "Service",
    identityProviderIds: "Institution",
    violations: "Bad logins",
    search: "Search policies..."
  },

  contact: {
    email: "Service support email"
  },

  not_found: {
    title: "The requested page could not be found.",
    description_html: "Please check the spelling of the URL or go to the <a href=\"/\">homepage</a>."
  },

  server_error: {
    title: "Something went wrong when opening this page.",
    description_html: "Please try again later or contact <a href=\"mailto:support@surfconext.nl\">support@surfconext.nl</a>."
  },

  logout: {
    title: "Logout completed successfully.",
    description_html: "You <strong>MUST</strong> close your browser to complete the logout process."
  },

  footer: {
    surfnet_html: "<a href=\"http://www.surfnet.nl/en\" target=\"_blank\">SURFnet</a>",
    terms_html: "<a href=\"https://wiki.surfnet.nl/display/conextsupport/Terms+of+Service+%28EN%29\" target=\"_blank\">Terms of Service</a>",
    contact_html: "<a href=\"mailto:support@surfconext.nl\">support@surfconext.nl</a>"
  }

 };
