# Migrate PDP storage to Manage

The PDP policies used to be stored by the PDP in its own database and administered in its own GUI.
This is deprecated and this guide describes how to migrate your rules management into Manage.
The PDP remains as the server (with only an API) which decides on the policies at login time.

## Background

Because Manage did not exist yet at the time, the PDP was built with a separate admin GUI and database storage of its policies. Although necessary at the time, this is now inconvenient because Manage does not proviode any information that/which rules exist for an entity, there's two admin UIs to work in which leads to confusion, there's only loose referential checking possible (e.g. renaming an entityID drops any associated policies on the floor), and two UIs need to be maintained codewise.

Now that Manage is there, we can move storage of the policies into the Manage backend (MongoDB) and frontend, so we can make use of the existing facilities for managing the history, integrate the admin UI and enforce referential integrity.

The storage in Manage is done in JSON format. Upon PUSH in Manage this is sent to the PDP-server which translates it to XACML and stores it in its local database for runtime evaluation. The PUSH in Manage is when policy changes become active, no delays anymore.

## How does the migration work

A new policies table is added to PDP next to the existing one. Manage can import the existing policies, process and store them and push them to the new table.

The migration can be done in two stages:
1. The new table exists besides the old one but is not used for policy evaluation. Manage can pull from the old table (as often as desired), and push back to the new table. Manage can then also show any differences between old and new, so certainty can be gained that the conversion is successful. Changing policies at this time still happens from PDP GUI.
2. The new table is switched to production. The old table is no longer used and the PDP GUI also not.

## Requirements

To migrate your rules from PDP to Manage, you need:

* OpenConext-pdp >= 4.1.0 (>= 5.1 preferred), << 7.0.0
* OpenConext-manage >= 7.4.0 (>= 7.4.9 preferred), << 9.0.0
* OpenConext-dashboard (when using), >= 12.3.17

## Preparation

Ensure that Manage's `application.yml` contains a PUSH section for PDP and a Spring MySQL datasouce (for comparing the result, only needed during the migration).

```
push:
  pdp:
    url: http://localhost:8081/pdp/api/manage/push
    policy_url: http://localhost:8081/pdp/api/manage/policies
    decide_url: http://localhost:8081/pdp/api/manage/decide
    user: pdp_admin
    name: OpenConext PDP
    password: secret
    enabled: true

spring:
  datasource:
    url: jdbc:mysql://localhost:3306/pdpserver?permitMysqlScheme
    username: root
    password:
    driverClassName: org.mariadb.jdbc.Driver
```

In PDP's `application.properies`, besides the API user listed above, also set pushTestMode=True:
```
manage.pushTestMode=True
```

## Load policies in Manage and test

Now in Manage, you can ask Manage to import all policies from PDP under the Policies tab. This will report what it does and if you push it back, it also provides ways to compare. You can view the imported policies under a related entity (e.g. an SP) in the Policies tab for that entity. You can repeat the fetch and push as often as you like as long as `manage.pushTestMode=False`.

## The switchover

Ready to take Manage into production as the storage for policies? Do the following:

1. Disable any access to the PDP GUI from now on so no more changes are made there. Shut down Dashboard (when using) so no changes are made.
2. In Manage: do the final "Policies -> Import Policies from PDP"
3. Change PDP feature flag `manage.pushTestMode` to `False` and restart
4. In Manage, press Push
5. In Dashboard (when using), switch `dashboard.feature.pdpSource` from PDP to Manage. Restart & re-enable Dashboard

## Optionally: clean up, upgrade PDP to remove UI

When the migration is fully done, you can at your convenience upgrade PDP to 7.0.0 or higher (will drop the unused admin GUI), and/or Manage to 9.0.0 or higher (drops the migration code).
You can remove the `spring.datasource` for pdp from Manage's config.
