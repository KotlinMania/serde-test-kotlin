# Immediate Actions - High-Value Files

Based on AST analysis, here are the concrete next steps.

## Summary

- **Files Present:** 6/7 (85.7%)
- **Function parity:** 80/80 matched (target 300) — 100.0%
- **Class/type parity:** 14/26 matched (target 81) — 53.8%
- **Combined symbol parity:** 94/106 matched (target 381) — 88.7%
- **Average inline-code cosine:** 0.68 (function body across 6 matched files)
- **Average documentation cosine:** 0.28 (doc text across 6 matched files)
- **Cheat-zeroed Files:** 0
- **Critical Issues:** 1 files with <0.60 function similarity

## Priority 1: Fix Incomplete High-Dependency Files

No incomplete high-dependency files detected.

## Priority 2: Port Missing High-Value Files

Critical missing files (>10 dependencies):

No missing high-value files detected.

## Detailed Work Items

Every matched file is listed below with function and type symbol parity.

### 1. token

- **Target:** `serdetest.Token`
- **Similarity:** 0.52
- **Dependents:** 4
- **Priority Score:** 4000204.8
- **Functions:** 1/1 matched (target 10)
- **Missing functions:** _none_
- **Types:** 1/1 matched (target 41)
- **Missing types:** _none_

### 2. error

- **Target:** `serdetest.Error`
- **Similarity:** 0.85
- **Dependents:** 2
- **Priority Score:** 2000501.5
- **Functions:** 4/4 matched (target 6)
- **Missing functions:** _none_
- **Types:** 1/1 matched
- **Missing types:** _none_

### 3. ser

- **Target:** `serdetest.Ser`
- **Similarity:** 0.61
- **Dependents:** 0
- **Priority Score:** 94803.9
- **Functions:** 37/37 matched (target 48)
- **Missing functions:** _none_
- **Types:** 2/11 matched (target 7)
- **Missing types:** `Ok`, `Error`, `SerializeSeq`, `SerializeTuple`, `SerializeTupleStruct`, `SerializeTupleVariant`, `SerializeMap`, `SerializeStruct`, `SerializeStructVariant`

### 4. de

- **Target:** `serdetest.De`
- **Similarity:** 0.70
- **Dependents:** 0
- **Priority Score:** 23803.0
- **Functions:** 29/29 matched (target 90)
- **Missing functions:** _none_
- **Types:** 7/9 matched
- **Missing types:** `Error`, `Variant`

### 5. configure

- **Target:** `serdetest.Configure`
- **Similarity:** 0.63
- **Dependents:** 0
- **Priority Score:** 10803.7
- **Functions:** 4/4 matched (target 132)
- **Missing functions:** _none_
- **Types:** 3/4 matched (target 20)
- **Missing types:** `Value`

### 6. assert

- **Target:** `serdetest.Assert`
- **Similarity:** 0.75
- **Dependents:** 0
- **Priority Score:** 502.5
- **Functions:** 5/5 matched (target 14)
- **Missing functions:** _none_
- **Types:** 0/0 matched (target 3)
- **Missing types:** _none_

## Success Criteria

For each file to be considered "complete":
- **Similarity ≥ 0.85** (Excellent threshold)
- All public APIs ported
- All tests ported
- Documentation ported
- port-lint header present

## Reexport / Wiring Modules

These files match `reexport_modules` patterns in `.ast_distance_config.json`. They are filtered out of
normal priority and missing-file ladders because they are wiring
modules, not direct logic ports. Consult them for call-site routing;
do not treat them as the next implementation target by default.

### Missing

| Source | Expected target | Deps | Source path | Expected path |
|--------|-----------------|------|-------------|---------------|
| `lib` | `Lib` | 0 | `lib.rs` | `Lib.kt` |

