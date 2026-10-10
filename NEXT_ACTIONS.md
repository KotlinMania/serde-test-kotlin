# Immediate Actions - High-Value Files

Based on AST analysis, here are the concrete next steps.

## Summary

- **Files Present:** 7/7 (100.0%)
- **Function parity:** 80/80 matched (target 307) — 100.0%
- **Class/type parity:** 8/26 matched (target 88) — 30.8%
- **Combined symbol parity:** 88/106 matched (target 395) — 83.0%
- **Average inline-code cosine:** 0.51 (function body across 7 matched files)
- **Average documentation cosine:** 0.31 (doc text across 7 matched files)
- **Cheat-zeroed Files:** 1
- **Critical Issues:** 3 files with <0.60 function similarity

## Priority 1: Fix Incomplete High-Dependency Files

No incomplete high-dependency files detected.

## Priority 2: Port Missing High-Value Files

Critical missing files (>10 dependencies):

No missing high-value files detected.

## Detailed Work Items

Every matched file is listed below with function and type symbol parity.

### 1. token

- **Target:** `serdetest.Token`
- **Similarity:** 0.21
- **Dependents:** 4
- **Priority Score:** 4000208.0
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
- **Priority Score:** 104803.9
- **Functions:** 37/37 matched (target 49)
- **Missing functions:** _none_
- **Types:** 1/11 matched (target 9)
- **Missing types:** `Ok`, `Error`, `SerializeSeq`, `SerializeTuple`, `SerializeTupleStruct`, `SerializeTupleVariant`, `SerializeMap`, `SerializeStruct`, `SerializeStructVariant`, `Variant`
- **Lint issues:** 2

### 4. de

- **Target:** `serdetest.De`
- **Similarity:** 0.69
- **Dependents:** 0
- **Priority Score:** 73803.1
- **Functions:** 29/29 matched (target 89)
- **Missing functions:** _none_
- **Types:** 2/9 matched (target 8)
- **Missing types:** `Error`, `DeserializerSeqVisitor`, `DeserializerMapVisitor`, `DeserializerEnumVisitor`, `Variant`, `EnumMapVisitor`, `BytesDeserializer`

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
- **Similarity:** 0.57
- **Dependents:** 0
- **Priority Score:** 504.3
- **Functions:** 5/5 matched (target 15)
- **Missing functions:** _none_
- **Types:** 0/0 matched (target 3)
- **Missing types:** _none_

### 7. lib

- **Target:** `serdetest.Lib [ZERO]`
- **Similarity:** 0.00
- **Dependents:** 0
- **Priority Score:** 10.0
- **Functions:** 0/0 matched (target 6)
- **Missing functions:** _none_
- **Types:** 0/0 matched (target 6)
- **Missing types:** _none_

## Success Criteria

For each file to be considered "complete":
- **Similarity ≥ 0.85** (Excellent threshold)
- All public APIs ported
- All tests ported
- Documentation ported
- port-lint header present

