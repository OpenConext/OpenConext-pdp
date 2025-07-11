# Release notes

Starting from version 7.0.0, we note changes and new features per release in this file.

## 7.1.0

 - Build arm64 container images
 - Remove old PDD-gui related mailer code [#237](https://github.com/OpenConext/OpenConext-pdp/issues/237)

## 7.0.0

Note: this version requires Manage version 7.4 or higher.  See [MIGRATION.md](MIGRATION.md)
for instructions for the migration from PDP-gui to Manage.

 - migrate to Java 21
 - migrate to Spring Boot 3
 - upgrade OAuth client
 - remove PDP-gui (replaced by new functionality of Manage)
 - fix queries to SAB for users whose uid contains a "@" [#240](https://github.com/OpenConext/OpenConext-pdp/issues/240)
