# Planned Test Harness Cases

This document lists proposed additions to the test harness. Each case should be implemented as a new directory under
`src/test/resources/test-harness` with a minimal `policy.json`, `request.json`, and `response.json` captured from
Manage's policy playground.

When a case is implemented, remove the `[ ]` marker and add the directory name.

## Decisions

- [ ] permit_basic_match
- [ ] deny_basic_match
- [ ] notapplicable_no_match
- [ ] indeterminate_missing_attribute

## Attribute Presence And Typing

- [ ] deny_missing_required_attr
- [ ] deny_null_attribute_value
- [ ] deny_empty_attribute_value
- [ ] indeterminate_type_mismatch

## Multi-valued Attributes

- [ ] permit_multi_value_any_match
- [ ] deny_multi_value_no_match
- [ ] permit_multi_value_all_match
- [ ] order_insensitive_multi_value

## Negation

- [ ] permit_negated_attribute_match
- [ ] deny_negated_attribute_fail
- [ ] permit_double_negation
- [ ] negation_with_additional_constraint

## Ranges And Boundaries

- [ ] permit_numeric_range_lower_bound
- [ ] permit_numeric_range_upper_bound
- [ ] deny_numeric_range_outside
- [ ] permit_ip_range_lower_bound
- [ ] permit_ip_range_upper_bound
- [ ] deny_ip_range_outside

## LoA / Step-up

- [ ] permit_loa_equal_required
- [ ] permit_loa_higher_than_required
- [ ] deny_loa_lower_than_required
- [ ] indeterminate_loa_missing

## Combining / Multiple Policies In One Directory

- [ ] deny_overrides_permit
- [ ] permit_when_others_notapplicable
- [ ] notapplicable_all
- [ ] indeterminate_with_other_permit

## Entity Scope

- [ ] permit_user_attr_only
- [ ] permit_sp_attr_only
- [ ] permit_idp_attr_only
- [ ] deny_cross_scope_mismatch

## Advice / Obligations

- [ ] permit_with_single_advice
- [ ] permit_with_multiple_advice
- [ ] advice_order_stability
