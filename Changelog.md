# Release notes

Starting from version 7.0.0, we note changes and new features per release in this file.

## 7.2.0

- Support grouped attribute conditions to allow AND between multiple values of the same attribute; added endpoint for Manage to
  generate XACML from the Manage JSON policy
  definition [OpenConext-manage#544](https://github.com/OpenConext/OpenConext-manage/issues/544).
  Endpoint: POST /pdp/api/manage/parse (basic-auth protected).
- Support IdP policies that apply to all SPs except a named few (negated SPs); empty ServiceProviders list is now allowed for
  such policies [OpenConext-manage#545](https://github.com/OpenConext/OpenConext-manage/issues/545).
- Fix policy access check to use coin:institution_guid instead of coin:
  institution_id [OpenConext-manage#551](https://github.com/OpenConext/OpenConext-manage/issues/551).
- Validate incoming policies and reject invalid ones; disallow empty SP entity IDs in
  policies [#244](https://github.com/OpenConext/OpenConext-pdp/issues/244). Note: an empty list of ServiceProviders is allowed
  when using the "exclude SPs" feature above.
- Logging improvements: log policy name on exceptions; only display policy XML in TRACE mode.
- Development: default local port changed to 8082 for the dev profile.
Configuration notes:
- The new Manage features (negated SPs, grouped attributes, XACML preview) require a compatible Manage release
  (OpenConext-manage 9.4.0 or newer). This version of PDP will run fine Manage >=9.0.1 releases, but the new features
  will not be available.

## 7.1.0

- Build arm64 container images
- Remove old PDD-gui related mailer code [#237](https://github.com/OpenConext/OpenConext-pdp/issues/237)

## 7.0.0

Note: this version requires Manage version 7.4 or higher. See [MIGRATION.md](MIGRATION.md)
for instructions for the migration from PDP-gui to Manage.

- migrate to Java 21
- migrate to Spring Boot 3
- upgrade OAuth client
- remove PDP-gui (replaced by new functionality of Manage)
- fix queries to SAB for users whose uid contains a "@" [#240](https://github.com/OpenConext/OpenConext-pdp/issues/240)
