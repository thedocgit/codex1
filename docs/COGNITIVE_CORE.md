# MUAI Cognitive Core v0.2.0

This release replaces the former three-prompt chain with a contract-driven cognitive lifecycle:

1. perception;
2. interpretation and risk classification;
3. goal modeling;
4. capability selection and planning;
5. circuit construction and DAG validation;
6. model gateway execution;
7. independent output validation;
8. reflection and trace emission.

The default provider is deterministic, so the core is executable without API credentials. External model adapters can be registered behind `ModelGateway` without changing orchestration contracts.

## Safety boundaries

- high-risk objectives require explicit approval;
- circuits reject unknown nodes and cycles;
- model output is validated independently;
- all cognitive artifacts are returned in a trace;
- external side effects are not implemented in this release.

## Validation

Run `npm test`. The suite covers the complete lifecycle, approval gating, cycle rejection, and capability registration.
