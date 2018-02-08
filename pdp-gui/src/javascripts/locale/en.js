// Interpolation works as follows:
//
// Make a key with the translation and enclose the variable with {{}}
// ie "Hello {{name}}" Do not add any spaces around the variable name.
// Provide the values as: I18n.t("key", {name: "John Doe"})
import I18n from "i18n-js";
import isEmpty from "lodash/isEmpty";

I18n.entityName = function (entity) {
    let name = entity["name" + (I18n.locale === "en" ? "En" : "Nl")];
    if (isEmpty(name)) {
        name = entity["name" + (I18n.locale === "en" ? "Nl" : "En")];
    }
    return name;
};
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
        ROLE_PEP: "Institution admin",
        ROLE_ADMIN: "SURFnet admin"
    },

    navigation: {
        policies: "Policies",
        violations: "Denies",
        conflicts: "Conflicts",
        loas: "Loa",
        decisions: "Stats",
        playground: "Playground",
        about: "About",
        my_idp: "My institute",
        new_policy: "+ New Policy",
        new_ssa_policy: "+ New Stepup Policy",
        identity: "Identity"
    },

    policies: {
        name: "Name",
        description: "Description",
        serviceProviderId: "Service",
        activatedSr: "Activated in SR",
        isActive: "Active",
        identityProviderId: "Institution",
        identityProviderIds: "Institution(s)",
        violations: "Denies",
        revisions: "Revisions",
        search: "Search policies...",
        flash: "Policy '{{policyName}}' was successfully {{action}}",
        flash_updated: "updated",
        flash_created: "created",
        flash_deleted: "deleted",
        confirmation: "Are your sure you want to remove policy {{policyName}}?",
        edit: "Edit",
        type: "Type",
        type_reg: "EB",
        type_step: "Stepup",
        delete: "Delete"
    },

    datatable: {
        lengthMenu: "Showing _MENU_ entries",
        zeroRecords: "No data present",
        infoEmpty: "",
        info: "Showing _START_ to _END_ of _TOTAL_ entries",
        paginate_first: "First",
        paginate_previous: "Previous",
        paginate_next: "Next",
        paginate_last: "Last"
    },

    policy_detail: {
        update_policy_reg: "Update policy",
        create_policy_reg: "Create new policy",
        update_policy_step: "Update Stepup policy",
        create_policy_step: "Create new Stepup policy",
        confirmation: "Are your sure you want to leave this page?",
        name: "Name",
        description: "Description",
        access: "Access",
        permit: "Permit",
        permit_info: "Permit policies enforce that a only a successful match of the attributes defined will result in a Permit. No match will result in a Deny.",
        deny: "Deny",
        deny_info: "Deny policies are less common to use. If the attributes in the policy match those of the person trying to login then this will result in a Deny. No match will result in a Permit.",
        deny_message: "Deny message",
        deny_message_info: "This is the message displayed to the user if access is denied based on this policy.",
        deny_message_nl: "Deny message in Dutch",
        sp_placeholder: "Select the Service Provider - required",
        idps_placeholder: "Select the Identity Providers - zero or more",
        rule: "Rule",
        rule_and: "AND",
        rule_and_info: "Policies with a logical AND rule enforce that all attributes defined must match those of the person trying to login.",
        rule_or: "OR",
        rule_or_info: "Policies defined with a logical OR only require one of the attributes to match the attributes of the person requesting access.",
        rule_info_add: " Note that attribute values with the same attribute name always be evaluated with the logical OR.",
        rule_info_add_2: "Note that a Deny access policy always and implicitly uses the logical AND for different attribute names.",
        submit: "Submit",
        cancel: "Cancel",
        sub_title: "Created by {{displayName}} on {{created}}",
        autoFormat: "AutoFormat policy description",
        isActiveDescription: "Mark the policy active",
        isActiveInfo: " Inactive policies are not evaluated in enforcement decisions",
        isActive: "Active",
        spScopeInfo: "The available Services are scoped to your services if you don't select an Institution",
        activated_false: "Not activated in Service Registry",
        activated_true: "Activated in Service Registry"
    },

    violations: {
        search: "Search denies...",
        policyName: "Policy name: ",
        table: {
            sp_idp: "Institution and Service",
            decision: "Decision",
            created: "Date",
            is_playground: "Playground"
        }
    },

    playground: {
        policy: "Policy",
        policy_info: "Optional - the selected policy is only used to set defaults for the Service Provider, Identity Provider and the attributes",
        policy_search: "Select the Policy",
        idp_placeholder: "Select the institution - required",
        adventurous_title: "Feeling adventurous?",
        adventurous_text: "You can directly edit the raw source on the right.",
        check_policies: "Check policies again",
        clear_policies: "Clear",
        reload_policy: "Reload to apply changes made below"

    },
    revisions: {
        title: "Revisions",
        revision: "Revision number",
        name: "Name",
        description: "Description",
        denyRule: "Access Permit rule?",
        serviceProviderName: "Service",
        identityProviderNames: "Institution(s)",
        allAttributesMustMatch: "Logical OR rule?",
        attributes: "Attributes",
        denyAdvice: "Deny message",
        denyAdviceNl: "Deny message in Dutch",
        active: "Active",
        changes_info_html: "Showing the changes between <span class=\"prev\"> revision number {{prevRevisionNbr}}</span> and <span class=\"curr\">revision number {{currRevisionNbr}}</span> made by {{userDisplayName}} from {{authenticatingAuthorityName}} on {{createdDate}}.",
        changes_first_html: "This is the first <span class=\"curr\">initial revision {{currRevisionNbr}}</span> created by {{userDisplayName}} from {{authenticatingAuthorityName}} on {{createdDate}}.",
        identical: "This revision is identical to the previous revision"
    },
    policy_attributes: {
        attribute_value_placeholder: "Attribute value...",
        attribute: "Attribute",
        values: "Values(s)",
        new_value: "Add a new value...",
        new_attribute: "Add new attribute....",
        group_info: " The value(s) must be fully qualified group IDs e.g. 'urn:collab:group:surfteams.nl:nl:surfnet:diensten:admins'",
        sab_info: " The value(s) must be valid roles in SAB e.g. 'Instellingsbevoegde'"
    },
    policy_loas: {
        loa: "Level of Assurance",
        new_loa: "Add new LoA...",
        and_or_rule: "An AND rule requires all the attributes to match. An OR rule only requires one of the attributes to match."
    },
    loas: {
        policy_id: "Policy name",
        search: "Search for policies"
    },
    policy_cidr: {
        add_cidr: "Add new IP range",
        capacity: "Nbr of IPs in range: ",
        broadcastAddress: "Higher bound IP address: ",
        networkAddress: "Lower bound IP address: ",
        invalid: "Invalid IP address",
        negate: "Negate"
    },
    identity: {
        title: "Identity impersonation",
        subTitle: "",
        confirmation: "Change identity",
        idpEntityId: "Institution / IdP",
        idpEntityIdPlaceHolder: "Select one Identity Provider",
        unspecifiedNameId: "Name ID",
        unspecifiedNameIdPlaceholder: "urn:collab:person:example.com:admin",
        unspecifiedNameIdInfo: "The unique name ID is stored with each new policy revision",
        displayName: "Display name",
        displayNameInfo: "The display name is also stored with new policy revisions",
        submit: "Impersonate",
        clear: "Reset impersonations",
        cancel: "Cancel"
    },
    conflicts: {
        title: "Conflicts",
        no_conflicts: "There are no policy conflicts",
        service_provider: "Service Provider",
        table: {
            name: "Policy name",
            idps: "Identity Providers"
        },
        hide_inactive: "Hide inactive conflicts",
        hide_inactive_note: " See right info box for details"
    },
    decisions: {
        avg_total: "Average Total",
        avg_pdp: "Average PDP internal",
        avg_teams: "Average Teams",
        avg_sab: "Average SAB",
        days: {
            "one": "{{count}} day",
            "other": "{{count}} days"
        }
    },
    contact: {
        email: "Service support email"
    },

    not_found: {
        title: "The PDP application is currently unavailable.",
        description_html: "Please try again later or contact <a href=\"mailto:support@surfconext.nl\">support@surfconext.nl</a>."
    },

    server_error: {
        title: "The PDP application is currently unavailable.",
        description_html: "Please try again later or contact <a href=\"mailto:support@surfconext.nl\">support@surfconext.nl</a>."
    },

    logout: {
        title: "Logout completed successfully.",
        description_html: "You <strong>MUST</strong> close your browser to complete the logout process."
    },

    footer: {
        surfnet_html: "<a href=\"https://www.surfnet.nl/en\" target=\"_blank\">SURFnet</a>",
        terms_html: "<a href=\"https://wiki.surfnet.nl/display/conextsupport/Terms+of+Service+%28EN%29\" target=\"_blank\">Terms of Service</a>",
        contact_html: "<a href=\"mailto:support@surfconext.nl\">support@surfconext.nl</a>"
    }

};
