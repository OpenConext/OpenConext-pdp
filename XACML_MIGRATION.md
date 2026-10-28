# Migration plan: replace XACML / openaz-xacml with a native policy engine (Strategy A)

> Audience: an AI coding agent in a fresh session (or a developer). Everything needed to execute is in this file.
> Not to be confused with `MIGRATION.md` (moving policy storage to Manage) and `SPRING_MIGRATION.md`.
> Repo rule from the user's global CLAUDE.md: **never commit or push**; leave all changes in the working tree.

---

## 1. Goal, scope, non-goals

**Goal.** Remove `org.apache.openaz:openaz-xacml(-pdp):0.0.4-OPENCONEXT` (retired Apache project, private fork) and all XACML
evaluation machinery from the PDP, with **zero change for clients** (EngineBlock, Stepup-gateway, Manage playground).

**Frozen contract (must not change):**
- `POST /pdp/api/decide/policy` and `POST /pdp/api/manage/decide`: XACML-JSON request in, XACML-JSON response out
  (byte-for-byte equal as parsed JSON `Map`, array order included; this is how `PolicyHarnessTest` compares).
- `PUT /pdp/api/manage/push` input (`List<PdpPolicyDefinition>`), basic auth, `/internal/*` actuator, stats JSON, DB push-version
  refresh mechanism for multi-node.
- **Removed (decided by the user): `POST /pdp/api/manage/parse`.** Nothing needs XACML output anymore. Delete the endpoint in Phase 1.
  The templates and `PolicyTemplateEngine` are still needed until Phase 7 only because the openaz engine (oracle + rollback) is fed from the rendered XML;
  after that they are deleted too (move `getPolicyId(name)` to a small `PolicyIds` utility first).

**Non-goals.** New API (that is "Strategy B", later), changing policy semantics, changing Manage, changing the policy JSON model.

## 2. Current architecture (facts, verified in code)

Java 21, Spring Boot 4.1.1, MariaDB + Flyway, single module `pom.xml`. Main Java ~2.1k LOC; openaz-coupled ~700 LOC + 7 Mustache templates (404 lines).

| Concern | Files |
|---|---|
| Engine wiring | `pdp/xacml/{PDPEngineHolder,OpenConextPDPEngineFactory,OpenConextPDPEngine,OpenConextEvaluationContextFactory,OpenConextConfigurableEngineFinder,CustomFunctionDefinitionFactory}.java`, `src/main/resources/xacml.conext.properties`, `PdpApplication.java` (sets `XACMLProperties` sysprop) |
| Policy to XML | `pdp/xacml/PolicyTemplateEngine.java` + `src/main/resources/templates/*.xml`; XML stored in `pdp_policies.policy_xml`; parsed by `pdp/xacml/PdpPolicyDefinitionParser.java` (`DOMPolicyDef.load`) |
| Custom functions | `pdp/bag/BagFunctionDefinition`, `pdp/ip/IPRangeFunctionDefinition`, `pdp/negate/NegateFunctionDefinition` |
| PIPs | `pdp/xacml/AbstractConfigurableEngine`, `pdp/sab/SabPIP` (+`SabClient`), `pdp/teams/TeamsPIP` (+`VootClient`) |
| HTTP | `pdp/web/PdpController.java` (JSONRequest/JSONResponse, reflection on `Wrapper.wrappedObject`, global `ReentrantLock` around decide) |
| Model | `pdp/domain/{PdpPolicyDefinition,PdpAttribute,LoA,CidrNotation}.java` (engine-neutral, keep) |
| Tests | `src/test/java/pdp/PolicyHarnessTest.java` + 58 dirs in `src/test/resources/test-harness/` (41 `reg_*`, 17 `step_*`; each has `policy*.json`, `request.json`, `response.json`; `-Dpolicy=<dir>`, `-Drecord=true`), `NegateFunctionDefinitionTest`, `VootClientTest` (3 files import openaz) |
| Stale/unused | `policies.cachePolicies`, `period.policies.refresh`, `performance.pre.policy.loader`, `provision.policies.directory` in `application.yml` (unread by Java); README mentions GUI/`PdpEngineTest` which no longer exist |

Policies are authored in Manage (JSON) and pushed via `/manage/push`; the push renders XML, validates by parsing, `deleteAll()+saveAll()`,
increments `pdp_policy_push_version`, and reloads the engine. Only XML is stored, **the JSON definition is not persisted** (important for Phase 3).

## 3. Exact semantics the native engine must reproduce (the specification)

Derived from templates + openaz behaviour. Where "verify" appears, the differential tests (Phase 5) are the arbiter: **openaz output is the oracle**.

### 3.1 Request handling (`PdpController.decide`)
- Parse XACML-JSON `Request.AccessSubject.Attribute[]` and `Request.Resource.Attribute[]` (`AttributeId`, `Value` (scalar **or array**),
  optional `DataType`, optional `IncludeInResult`). Resource carries `SPentityID`, `IDPentityID`, `ClientID`. Subject carries user attrs, IP
  (`urn:mace:surfnet.nl:collab:xacml-attribute:ip-address`), `NAME_ID`, `SCHAC_HOME_ORGANIZATION`, `UID` (constants are in `PdpPolicyDefinitionParser`/`AbstractConfigurableEngine`).
- Force `ReturnPolicyIdList=true`. If resource has no `ClientID`, inject `ClientID=EngineBlock` (`returnPolicyIdInList`).
- Stats: record IdP, SP from resource attrs; after decision record decision, max LoA obligation value (string max), policy id; PIP timings. Keep `StatsContextHolder`.

### 3.2 Evaluation structure
`root deny-overrides` (policy-combining, XACML 3.0 deny-overrides, configurable property today, default deny-overrides) over all **active** policies
(`PdpPolicyRepository.findAll()`, filter `isActive()` unless `includeInactivePolicies`), each policy = `Target` + rules combined by `first-applicable`.
Policy id = `getPolicyId(name)` = `"urn:surfconext:xacml:policy:id:" + name.replaceAll("[^\\w]+","_").toLowerCase()` (keep this function).
Root ids in response: `PolicySetIdReference {Id:"urn:openconext:pdp:root:policyset", Version:"1.0"}`; `PolicyIdReference {Id:<policyId>, Version:"1"}`.
Result `Indeterminate` with status message "No matching root policy found" is converted to `NotApplicable` (`OpenConextPDPEngine`). Keep that behaviour.

### 3.3 Policy Target (all policy types)
AND of: (a) if `!serviceProvidersNegated` and SP list non-empty: `SPentityID` equals any of `serviceProviderIds`; (b) if IdP list non-empty:
`IDPentityID` equals any of `identityProviderIds`; (c) `ClientID` equals `"EngineBlock"`. All three designators are `MustBePresent=true`, so a missing
attribute gives **Indeterminate** for the policy target (XACML: target Indeterminate => policy Indeterminate, participates in deny-overrides). Comparison = `string-equal` (case-sensitive).

### 3.4 `type == "reg"` (templates `policy-definition.xml`, `target-partial-logical-and/or.xml`, `negated-service-providers-policy.xml`)
Attributes are `PdpAttribute{name,value,groupID,negated}`; `sortAttributes()` sorts by name before rendering; `allAttributesGrouped` groups by `(name,groupID)`.
Match is `string-regexp-match(value-as-regex, subject attribute bag)`, designator category = access-subject, `MustBePresent = denyRule`.
- `allAttributesMustMatch=true` ("and" partial): one Target with one `AnyOf` per `(name,groupID)` group, values inside a group OR'ed. => AND over groups, OR within group.
- `allAttributesMustMatch=false` ("or" partial): a single `AnyOf` with one `AllOf` per attribute => OR over all attributes.
- No attributes: empty target (matches all) vs empty `AnyOf` (verify!).
- Rule list when `denyRule=false`: `[ (negated-SP rule if serviceProvidersNegated and SP list non-empty), Permit(attrTarget), Deny(empty target, advice) ]`.
- Rule list when `denyRule=true`: `[ (negated-SP rule), Deny(attrTarget, advice), Permit(empty target) ]`.
- Negated-SP rule (first, Permit, no target): condition `bag(SPentityID(MustBePresent=false), sp1..spN)` true when request SP is one of the listed SPs => **Permit immediately** (negation means "attributes apply to all SPs except these"). Note: the policy Target omits the SP match in that case.
- Deny advice (`AppliesTo=Deny`): `AdviceId` rewritten to policy id; assignments in this order, category resource: `DenyMessage:en` (string, `denyAdvice`), `DenyMessage:nl` (string, `denyAdviceNl`), `IdPOnly` (boolean, `isIdpOnly()` = IdP list non-empty). Advice is only returned for Deny.

### 3.5 `type == "step"` (templates `policy-definition-step.xml`, `target-partial-step-logical-and/or.xml`)
- `sortLoas()`: descending by `level` (string compare, reversed). One Permit rule per LoA, in that order, **first-applicable**, followed by a final unconditional Permit
  (rule id `...DenyRulePolicyEmptyPermit.permitAccess`) with no obligation.
- Each LoA rule has Obligation `urn:openconext:stepup:loa` (FulfillOn Permit) with one assignment: `AttributeId=urn:loa:level`, category access-subject, string `level`.
- LoA with no CIDRs and no attributes (`empty()`): no Condition => always applies.
- Otherwise Condition = `[and( not(or(string-is-in(sp, SPentityID)...)) , X )]` when `serviceProvidersNegated`, else `X`; `X` = `and` if `LoA.allAttributesMustMatch` else `or`, over:
  - CIDR operand (if any CIDRs): `or(ip:range(cidr, ip-address)...)`, wrapped in `not(...)` if `negateCidrNotation`. `ip:range` uses Spring `IpAddressMatcher`; invalid IP => processing-error (Indeterminate).
  - attribute operands: per `(name,groupID)` group: `or( per value: negated ? negation(value, attr) : string-is-in(value, attr) )`; designators `MustBePresent=false`.
- Custom functions: `negation(policyValue, bag)` = **true when no bag value equalsIgnoreCase policyValue** (case-insensitive! empty/blank bag values are ignored; arity != 2 => syntax error); `string-is-in` is standard XACML (case-sensitive exact); `bag(actual, v1..vn)` = `actual in [v...]` (case-sensitive); `ip:range` as above.
- `and`/`or`/`not` are XACML three-valued: evaluated in argument order, short-circuit, Indeterminate propagates only if not short-circuited.
- `first-applicable`: first rule whose result is not NotApplicable decides (Indeterminate stops evaluation and is returned).

### 3.6 Combining and result assembly
- deny-overrides (XACML 3.0, policy level): any Deny => Deny; else any Indeterminate => Indeterminate (per spec Indeterminate{D}/{P}/{DP} rules); else any Permit => Permit; else NotApplicable.
  Advice/obligations are collected from the policies that yield the final decision. Order of multiple policies = repository order (`findAll()`); see `reg_advice_order_stability`. **Make order deterministic (sort by id) in both engines before diffing.**
- Response `Result`: `Status` (`urn:oasis:names:tc:xacml:1.0:status:ok`, or `missing-attribute` with message "Missing required attribute" for Indeterminate from `MustBePresent`), `Decision`,
  `Obligations[]`, `AssociatedAdvice[]`, `PolicyIdentifier{PolicySetIdReference[],PolicyIdReference[]}`, and the quirk below.
- **Quirk to reproduce: `Category` echo.** Recorded responses contain `Category:[{CategoryId:<attrName>, Attribute:[{AttributeId,Value,DataType}]}]` for `reg` policies when the
  *deciding rule's Target* matched on a subject attribute (e.g. `reg_permit_basic_match` Permit, `reg_deny_basic_match` Deny), and no `Category` for `step`, negated-SP-only, no-attribute or Deny-by-default-rule cases.
  Values shown are the request's values for that attribute. Exact rule must be pinned by differential testing (matrix of reg cases), then encoded as a unit-tested `CategoryEcho` helper.
- Pretty/compact printing: `JSONResponse.toString(response, debug)`; harness compares parsed maps, so only structure and array order matter.

### 3.7 PIPs (SAB, VOOT teams)
Resolved lazily when a policy attribute name equals `urn:collab:group:surfteams.nl` (Teams, needs `NAME_ID`) or `urn:collab:sab:surfnet.nl` (SAB, needs `SCHAC_HOME_ORGANIZATION`+`UID`, user urn `urn:collab:person:<schac>:<uid>`),
and the request does not already supply it. Missing prerequisites => status missing-attribute => Indeterminate. Empty result => empty bag. Record call duration in `StatsContext.addPipResponse(name, ms)`. Call each PIP at most once per request (memoize).

## 4. Target architecture

New package `pdp.engine` (no openaz imports):

- `PolicyEngine` (interface): `DecisionResponse decide(DecisionRequest)`; `void reload()`/snapshot swap handled by holder.
- `DecisionRequest`, `DecisionResponse`, `DecisionResult`, `Obligation`, `Advice`, `AttributeAssignment` as Java records; `Decision` enum {Permit, Deny, Indeterminate, NotApplicable}.
- `pdp.engine.json`: `XacmlJsonCodec` (Jackson) with `DecisionRequest read(String)` / `String write(DecisionResponse)`. Must match openaz field names, nesting, scalar-vs-array handling.
- `pdp.engine.nativeimpl`:
  - `PolicyCompiler`: `PdpPolicyDefinition` -> `CompiledPolicy` (immutable; precompiles regexes and CIDR matchers).
  - Sealed `Expr` AST evaluating to `Tri {TRUE,FALSE,INDETERMINATE(status)}`: `And`, `Or`, `Not`, `StringEqual`, `StringIsIn`, `RegexMatch`, `IpRange`, `NegatedValue`, `SpInList`.
  - `Rule(effect, target/condition, obligations, advice)`, `FirstApplicable`, `DenyOverrides`, `PolicySnapshot` (`List<CompiledPolicy>`), `AtomicReference<PolicySnapshot>` in `NativePolicyEngine`.
  - `AttributeContext`: request attrs + lazy `AttributePip` (interface; `TeamsAttributePip`, `SabAttributePip` wrapping existing `VootClient`/`SabClient`), per-request memoization, stats hook.
- `OpenAzPolicyEngine` (adapter, temporary): wraps current `PDPEngineHolder`/JSON classes so the controller only sees `PolicyEngine`.
- `ShadowPolicyEngine` (temporary): calls primary, then secondary, compares, logs/metrics, returns primary.
- Config: `pdp.engine.mode = openaz | native | shadow` (default `openaz` until Phase 6).
- No global lock for native (immutable snapshot). Keep the lock only around the openaz engine.

Persistence: add column `pdp_policies.policy_json` (TEXT/LONGTEXT, nullable) via Flyway `V19___policy_json.sql`; entity `PdpPolicy` gets `policyJson`. `/manage/push` stores both XML (until Phase 7) and JSON (serialized `PdpPolicyDefinition`, after `sortLoas/sortAttributes` and `policyId` set).

## 5. Phased plan (each phase independently mergeable; run `mvn test` after every phase)

### Phase 0: characterization & test hardening (about 2-3 days)
1. Run `mvn test` to confirm baseline green; note count (58 harness tests).
2. Extend harness: parameterize `PolicyHarnessTest` with `-Dpdp.engine=<mode>` (reads property into Spring context). Add a `README` note.
3. Implement the cases in `test-harness/PLANNED_CASES.md`, and add new `reg_`/`step_` cases for: regex anchoring (`foo.*`, `foo` vs `xfoo`), case-sensitivity (`string-is-in` vs negation), blank/empty values, multi-valued request attrs (array `Value`), non-string `Value` (number, boolean), missing `SPentityID`/`IDPentityID`/`ClientID`, no attributes (reg and/or), `groupID` combos, negated SP + deny rule, IPv6 and invalid IP, empty LoA, multiple LoAs ordering, many policies for advice ordering, zero policies loaded, SAB/Teams PIP present/absent/empty (use WireMock like `VootClientTest`). Record with the openaz engine (`-Drecord=true`), review each diff by eye.
4. Write a `DifferentialEngineTest` skeleton (filled in Phase 5).
Done when: harness covers every branch listed in section 3 and is green on openaz.

### Phase 1: introduce the seam (about 1 day, no behaviour change)
1. Add `PolicyEngine` using plain strings for this phase (typed records come in Phase 2): `String decide(String xacmlJson)` + `reload()`. Implement `OpenAzPolicyEngine` by moving the code out of `PdpController` (JSON load/save, `returnPolicyIdInList` reflection, lock, Result->stats extraction).
2. `PdpController` depends only on `PolicyEngine` + stats extraction via a small `DecisionSummary` returned alongside the JSON.
2b. Delete the `POST /manage/parse` endpoint (`parsePolicyDefinition`) and any test or doc reference to it. Extract `getPolicyId(name)` from `PolicyTemplateEngine` into `pdp.domain.PolicyIds` (same behaviour) and update its callers (`OpenConextEvaluationContextFactory`, `PolicyTemplateEngine`).
3. Tests green unchanged.

### Phase 2: DTOs and JSON codec (about 2-3 days)
1. Create records in `pdp.engine`, `XacmlJsonCodec`.
2. Golden tests: for every `request.json` in the harness, `codec.read` equals what `JSONRequest.load` produced (compare via attribute lists); for every `response.json`, `codec.write(codec.read(response))` round-trips.
3. Move `OpenAzPolicyEngine` to the typed interface by converting at its edge (so native and openaz share one interface `DecisionResponse decide(DecisionRequest)`).

### Phase 3: persist the JSON definition (about 1 day)
1. Flyway `V19___policy_json.sql`: `ALTER TABLE pdp_policies ADD COLUMN policy_json LONGTEXT NULL;` (check V1/V15 for engine and charset).
2. `PdpPolicy.policyJson`; `PdpController.pushPolicyDefinitions` writes it (ObjectMapper). `PolicyHarnessTest.storePolicy` also writes it. (XML rendering stays in the push until Phase 7 because the openaz engine needs it.)
3. Existing rows have NULL JSON: **a Manage re-push is required before switching to native** (the XML cannot be reliably reverse-engineered). Native engine logs an ERROR and refuses to start in `native` mode if any active row lacks JSON; in `shadow` mode it logs and skips those rows.

### Phase 4: native evaluator (about 1-1.5 weeks)
Implement per section 3, bottom-up with unit tests for each piece:
1. `Tri` and `And/Or/Not` incl. short-circuit/Indeterminate (table-driven test).
2. Leaf exprs: `StringEqual`, `StringIsIn`, `RegexMatch` (decide anchored vs find by testing openaz; port `string-regexp-match` semantics exactly), `NegatedValue` (equalsIgnoreCase, blank filtering), `SpInList`, `IpRange` (reuse `IpAddressMatcher`, invalid IP -> Indeterminate processing-error).
3. `PolicyCompiler`: build target, rule list for `reg` (permit/deny shapes, negated-SP rule) and `step` (LoA ordering, empty LoA, trailing permit), advice and obligation objects, `MustBePresent` flags.
4. `FirstApplicable`, `DenyOverrides` (spec table incl. Indeterminate{D,P,DP}), `NativePolicyEngine` (snapshot, root NotApplicable conversion, ClientID injection stays in controller/codec layer).
5. `AttributeContext` + PIPs with memoization and stats; delete reliance on `StatsContextHolder` thread-locals if feasible, else keep.
6. `CategoryEcho` helper per 3.6.
7. Run harness with `-Dpdp.engine=native`; fix until all green.

### Phase 5: differential testing and shadow mode (about 1 week, includes soak)
1. `DifferentialEngineTest` (JUnit): build both engines in-process; feed (a) every harness policy x its request, (b) a **seeded random generator** of `PdpPolicyDefinition` (reg/step, negations, groups, CIDRs, 0-5 SP/IdP, 0-6 attributes, LoAs) x random requests built from the policy's own values plus perturbations (missing attrs, case flips, extra values); assert parsed-JSON equality. Seeds logged; failures print policy JSON + request. Run 50k cases in CI-nightly profile, 2k in default.
2. `ShadowPolicyEngine`: run native after openaz on the same request using a **request-scoped memoizing decorator over `VootClient`/`SabClient`** (so PIP HTTP is not doubled). Compare decision, status, obligations, advice, policy ids, category echo. On mismatch: WARN log (policy ids + request hash, no PII values), Micrometer counter `pdp_shadow_mismatch_total`, timer `pdp_engine_native_seconds`. Never alter the response.
3. Deploy to test/acceptance with `pdp.engine.mode=shadow`; exit criterion: **0 mismatches over at least 1 week and >= 100k decisions** (or all remaining mismatches explained as openaz bugs and agreed explicitly).

### Phase 6: cutover (about 2-3 days)
1. Trigger Manage push so every row has `policy_json`.
2. Flip default to `native`; keep `openaz` selectable for one release as instant rollback (`pdp.engine.mode=openaz`).
3. Performance check with `jmeter/` scripts against both modes (expect equal or better; no global lock).
4. Staged production rollout (one node, then all), watching error rate, latency, stats JSON `decision` distribution vs previous week.

### Phase 7: removal (about 2-3 days, next release after cutover is stable)
Delete: all of `pdp/xacml/*` (including `PolicyTemplateEngine`), the 7 templates in `src/main/resources/templates/`, the Mustache dependency if nothing else uses it (check `pom.xml`), `pdp/bag`, `pdp/ip/IPRangeFunctionDefinition`, `pdp/negate`, `PdpPolicyDefinitionParser`, `OpenAzPolicyEngine`, `ShadowPolicyEngine`, `xacml.conext.properties`, `src/test/docu/xacml.txt` (if unreferenced), `NegateFunctionDefinitionTest` (port assertions to `NegatedValue` test), the two `openaz` dependencies in `pom.xml`, the `XACMLProperties` sysprop in `PdpApplication`, the unused `StatsContext` `IdReference` import, and unused `application.yml` keys.
`/manage/push` then only validates and stores the JSON definition (no XML rendering or parsing).
Flyway `V20___drop_policy_xml.sql` (drop `policy_xml`, make `policy_json` NOT NULL) only once rollback to openaz is no longer wanted (**separate, later release**).
Docs: update `README.md` (remove GUI references, add engine description), `Security.md` (stale `PolicyIdpAccessEnforcer`), `Changelog.md`, link this file.
Done when: `mvn dependency:tree | grep -i openaz` is empty and `grep -ri "org.apache.openaz" src` is empty.

### Phase 8 (optional, later): native API v2 (Strategy B), only if EngineBlock wants it.

## 6. Risk register (and where it is covered)

| Risk | Detail | Mitigation |
|---|---|---|
| Regex semantics | XACML `string-regexp-match` is XPath-style (unanchored find); openaz may use `matches()` | Phase 0 cases `foo.*`, `foo`, `^foo$`; differential test |
| Case rules inconsistent | `string-is-in` and regexp are case-sensitive; `negation` is `equalsIgnoreCase` | Port as-is, document, test |
| Indeterminate vs NotApplicable | `MustBePresent` = `denyRule` for reg attrs; true for SP/IdP/ClientID; first-applicable stops at Indeterminate | Table-driven tests; harness `reg_*_missing_*`, `step_indeterminate_*` |
| deny-overrides Indeterminate algebra | XACML 3.0 {D},{P},{DP} extended Indeterminate | Implement spec table; random differential tests with many policies |
| Category echo quirk | non-spec openaz behaviour | Pin by experiment, unit-test `CategoryEcho` |
| Array order stability | `Obligations`, `AssociatedAdvice`, `AttributeAssignment`, policy ids | Deterministic sort by policy id, assert arrays in order |
| Mustache escaping | `{{value}}` HTML-escapes `& ' " =`; XML parser unescapes, so semantics equal raw value, but whitespace/trim handled by DOM parse may differ | Differential test with special chars and padded whitespace |
| Numeric/boolean request values | `DataType` inference from JSON, `reg_indeterminate_type_mismatch` currently Permit | Phase 0 cases, codec mirrors openaz inference |
| PIP double calls in shadow | VOOT/SAB load doubled | Request-scoped memoizing decorator |
| Existing rows without JSON | cannot switch before Manage re-push | Startup guard + runbook step in Phase 6 |
| Rollback | | `pdp.engine.mode` flag, XML retained until Phase 7+1 |
| Concurrency | openaz engine serialized by lock; native lock-free | Immutable snapshot, `AtomicReference`, concurrency test with parallel streams |

## 7. Testing & commands

```bash
mvn test                                                    # everything (harness runs against configured mode)
mvn test -Dtest=PolicyHarnessTest -Dpdp.engine=native       # harness on the new engine
mvn test -Dtest=PolicyHarnessTest -Dpolicy=reg_deny_basic_match
mvn test -Dtest=DifferentialEngineTest -Ddiff.cases=50000 -Ddiff.seed=42
mvn clean test -Dtest=PolicyHarnessTest -Drecord=true       # re-record (ONLY with the openaz oracle; review git diff)
mvn dependency:tree | grep -i openaz                        # must be empty after Phase 7
```
Definition of done per phase: all tests green, no new warnings, no unrelated reformatting, nothing committed.

## 8. Effort summary

| Phase | Effort |
|---|---|
| 0 Characterization | 2-3 d |
| 1 Seam | 1 d |
| 2 DTO + codec | 2-3 d |
| 3 Persist JSON | 1 d |
| 4 Native evaluator | 5-8 d |
| 5 Differential + shadow (incl. soak) | 3 d dev + 1 wk soak |
| 6 Cutover | 2-3 d |
| 7 Removal + docs | 2-3 d |
| **Total** | **about 3-4 weeks elapsed, 17-24 dev days** |

## 9. Open questions (answer before or during Phase 0)
- Q1. RESOLVED: `/manage/parse` is removed; no XACML output is needed anymore.
- Q2. Do EngineBlock / Stepup-gateway read the `Category` echo or `PolicySetIdReference`? If not, the echo could be dropped later (still replicate now for zero risk).
- Q3. Are there policy `type` values other than `reg` and `step` in production?
- Q4. Is a Manage re-push at cutover acceptable operationally (all environments)?
- Q5. Real production request samples (anonymized, from `LOG.debug` of decide) for the differential/replay corpus: available?

## 10. Instructions for the executing AI session
1. Read this file fully, then `PdpController.java`, `OpenConextEvaluationContextFactory.java`, `OpenConextPDPEngine.java`, the 7 templates, `PdpPolicyDefinition.java`, `LoA.java`, `PolicyHarnessTest.java`.
2. Work one phase at a time, in order; keep a task list; run `mvn test` at the end of each phase.
3. **Never commit or push.** Leave changes uncommitted.
4. openaz is the oracle: when this document and the oracle disagree, trust the oracle and update section 3 of this file.
5. Do not change the response contract or the harness expectations except by re-recording against the openaz engine and reviewing the diff.
6. Keep the new code in the style of the surrounding code (Lombok usage, package layout, logging via SLF4J `LOG`).
