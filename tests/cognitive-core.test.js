import test from "node:test";
import assert from "node:assert/strict";
import { orchestrator, listCapabilities } from "../core/orchestrator.js";
import { validateCircuit } from "../core/cognitive/index.js";

test("executes the bounded cognitive lifecycle", async () => {
  const response = await orchestrator("Implemente um serviço modular auditável");
  assert.equal(response.status, "completed");
  assert.match(response.result, /MUAI COGNITIVE CORE/);
  assert.equal(response.trace.circuit.status, "validated");
  assert.equal(response.trace.validation.passed, true);
  assert.ok(response.trace.reflection.goalAchievementScore > 0);
});

test("requires approval for high-risk objectives", async () => {
  const response = await orchestrator("Apagar credencial de produção");
  assert.equal(response.status, "awaiting_approval");
});

test("rejects cyclic circuits", () => {
  assert.throws(() => validateCircuit({
    nodes: [{ nodeId: "a" }, { nodeId: "b" }],
    edges: [{ source: "a", target: "b" }, { source: "b", target: "a" }]
  }), /unbounded cycle/);
});

test("publishes the canonical capability catalog", () => {
  assert.ok(listCapabilities().length >= 6);
});
