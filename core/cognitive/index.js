import { assertNonEmptyString, freezeContract, newId, nowIso } from "./contracts.js";

const DEFAULT_CAPABILITIES = [
  { id: "interpret.intent", category: "interpretation", risk: "low" },
  { id: "model.goal", category: "goal", risk: "low" },
  { id: "plan.decompose", category: "planning", risk: "low" },
  { id: "reason.synthesize", category: "reasoning", risk: "low" },
  { id: "validate.output", category: "validation", risk: "low" },
  { id: "reflect.execution", category: "reflection", risk: "low" }
];

export class CapabilityRegistry {
  #capabilities = new Map();

  constructor(seed = DEFAULT_CAPABILITIES) {
    for (const capability of seed) this.register(capability);
  }

  register(capability) {
    const id = assertNonEmptyString(capability?.id, "capability.id");
    if (this.#capabilities.has(id)) throw new Error(`Capability already registered: ${id}`);
    const normalized = freezeContract({
      id,
      category: assertNonEmptyString(capability.category, "capability.category"),
      version: capability.version ?? "1.0.0",
      risk: capability.risk ?? "low",
      enabled: capability.enabled ?? true,
      metadata: capability.metadata ?? {}
    });
    this.#capabilities.set(id, normalized);
    return normalized;
  }

  get(id) {
    return this.#capabilities.get(id) ?? null;
  }

  require(ids) {
    return ids.map((id) => {
      const capability = this.get(id);
      if (!capability || !capability.enabled) throw new Error(`Unavailable capability: ${id}`);
      return capability;
    });
  }

  list() {
    return [...this.#capabilities.values()];
  }
}

export function perceive(rawInput, context = {}) {
  const content = assertNonEmptyString(rawInput, "rawInput");
  const normalized = content.replace(/\s+/g, " ").trim();
  return freezeContract({
    perceptionId: newId("perception"),
    inputType: "text",
    normalizedContent: normalized,
    language: context.language ?? "pt-BR",
    signals: {
      characterCount: normalized.length,
      hasQuestion: normalized.includes("?"),
      hasCodeFence: normalized.includes("```")
    },
    source: context.source ?? "api",
    createdAt: nowIso()
  });
}

const ACTION_PATTERNS = [
  [/(crie|construa|implemente|desenvolva)/i, "create"],
  [/(analise|avalie|revise|audite)/i, "analyze"],
  [/(explique|descreva|defina)/i, "explain"],
  [/(pesquise|busque|localize)/i, "research"]
];

export function interpret(perception) {
  const text = perception.normalizedContent;
  const matched = ACTION_PATTERNS.find(([pattern]) => pattern.test(text));
  const action = matched?.[1] ?? "respond";
  const risk = /(apagar|deletar|pagamento|senha|credencial|produção)/i.test(text) ? "high" : "low";
  return freezeContract({
    interpretationId: newId("interpretation"),
    perceptionId: perception.perceptionId,
    primaryIntent: action,
    objectiveText: text,
    constraints: [],
    assumptions: [],
    ambiguities: [],
    riskLevel: risk,
    requiresApproval: risk === "high",
    createdAt: nowIso()
  });
}

export function modelGoal(interpretation, options = {}) {
  return freezeContract({
    goalId: newId("goal"),
    interpretationId: interpretation.interpretationId,
    statement: interpretation.objectiveText,
    action: interpretation.primaryIntent,
    successCriteria: options.successCriteria ?? [
      "Deliver a coherent result aligned with the objective",
      "Preserve traceability of cognitive stages",
      "Return a contract-valid response"
    ],
    forbiddenOutcomes: options.forbiddenOutcomes ?? [
      "Silent policy bypass",
      "Unbounded execution loop",
      "Unvalidated external side effect"
    ],
    limits: {
      maxSteps: options.maxSteps ?? 8,
      maxIterations: options.maxIterations ?? 2,
      maxCostUsd: options.maxCostUsd ?? 1
    },
    riskLevel: interpretation.riskLevel,
    createdAt: nowIso()
  });
}

export function createPlan(goal, registry) {
  const capabilities = [
    "interpret.intent",
    "model.goal",
    "plan.decompose",
    "reason.synthesize",
    "validate.output",
    "reflect.execution"
  ];
  registry.require(capabilities);
  const steps = [
    { id: "interpret", capabilityId: "interpret.intent", dependsOn: [] },
    { id: "goal", capabilityId: "model.goal", dependsOn: ["interpret"] },
    { id: "plan", capabilityId: "plan.decompose", dependsOn: ["goal"] },
    { id: "synthesize", capabilityId: "reason.synthesize", dependsOn: ["plan"] },
    { id: "validate", capabilityId: "validate.output", dependsOn: ["synthesize"] },
    { id: "reflect", capabilityId: "reflect.execution", dependsOn: ["validate"] }
  ];
  return freezeContract({
    planId: newId("plan"),
    goalId: goal.goalId,
    strategy: "bounded-linear-with-validation",
    steps,
    limits: goal.limits,
    fallback: "deterministic-provider",
    createdAt: nowIso()
  });
}

export function validateCircuit(circuit) {
  const nodeIds = new Set(circuit.nodes.map((node) => node.nodeId));
  if (nodeIds.size !== circuit.nodes.length) throw new Error("Circuit contains duplicate nodes");
  for (const edge of circuit.edges) {
    if (!nodeIds.has(edge.source) || !nodeIds.has(edge.target)) {
      throw new Error(`Circuit edge references unknown node: ${edge.source} -> ${edge.target}`);
    }
  }
  const indegree = new Map([...nodeIds].map((id) => [id, 0]));
  const adjacency = new Map([...nodeIds].map((id) => [id, []]));
  for (const edge of circuit.edges) {
    indegree.set(edge.target, indegree.get(edge.target) + 1);
    adjacency.get(edge.source).push(edge.target);
  }
  const queue = [...indegree.entries()].filter(([, degree]) => degree === 0).map(([id]) => id);
  let visited = 0;
  while (queue.length) {
    const id = queue.shift();
    visited += 1;
    for (const target of adjacency.get(id)) {
      indegree.set(target, indegree.get(target) - 1);
      if (indegree.get(target) === 0) queue.push(target);
    }
  }
  if (visited !== nodeIds.size) throw new Error("Circuit contains an unbounded cycle");
  return true;
}

export function buildCircuit(plan) {
  const nodes = plan.steps.map((step) => ({
    nodeId: step.id,
    capabilityId: step.capabilityId,
    timeoutMs: 30000,
    maxAttempts: 2
  }));
  const edges = plan.steps.flatMap((step) =>
    step.dependsOn.map((source) => ({ source, target: step.id, condition: "success" }))
  );
  validateCircuit({ nodes, edges });
  return freezeContract({
    circuitId: newId("circuit"),
    planId: plan.planId,
    version: "1.0.0",
    nodes,
    edges,
    limits: plan.limits,
    status: "validated",
    createdAt: nowIso()
  });
}

class DeterministicProvider {
  id = "deterministic";
  model = "muai-rule-engine-v1";

  async generate({ goal, plan }) {
    const steps = plan.steps.map((step, index) => `${index + 1}. ${step.capabilityId}`).join("\n");
    return {
      content: [
        "=== MUAI COGNITIVE CORE ===",
        `Objective: ${goal.statement}`,
        `Action: ${goal.action}`,
        "Cognitive circuit:",
        steps,
        "Result: cognitive plan built, validated and executed without external side effects."
      ].join("\n")
    };
  }
}

export class ModelGateway {
  constructor({ providers = [], defaultProvider = "deterministic" } = {}) {
    this.providers = new Map(providers.map((provider) => [provider.id, provider]));
    this.defaultProvider = defaultProvider;
    if (!this.providers.has("deterministic")) this.providers.set("deterministic", new DeterministicProvider());
  }

  async generate(request) {
    const providerId = request.providerId ?? this.defaultProvider;
    const provider = this.providers.get(providerId) ?? this.providers.get("deterministic");
    const started = performance.now();
    const output = await provider.generate(request);
    return {
      providerId: provider.id,
      model: provider.model,
      content: assertNonEmptyString(output.content, "model.content"),
      usage: output.usage ?? { inputTokens: 0, outputTokens: 0 },
      latencyMs: Math.round(performance.now() - started)
    };
  }
}

function validateOutput(content) {
  const errors = [];
  if (typeof content !== "string" || content.trim().length < 20) errors.push("Output is empty or too short");
  return freezeContract({ passed: errors.length === 0, errors });
}

function reflect({ goal, plan, circuit, modelResponse, validation }) {
  const passed = validation.passed === true;
  return freezeContract({
    reflectionId: newId("reflection"),
    goalId: goal.goalId,
    planId: plan.planId,
    circuitId: circuit.circuitId,
    goalAchievementScore: passed ? 1 : 0,
    planQualityScore: plan.steps.length > 0 ? 1 : 0,
    identifiedFailures: passed ? [] : validation.errors,
    identifiedStrengths: [
      "Bounded circuit",
      "Independent output validation",
      `Provider ${modelResponse.providerId} completed`
    ],
    memoryCandidates: [],
    evolutionCandidates: [],
    createdAt: nowIso()
  });
}

export class CognitiveRuntime {
  constructor({ modelGateway }) {
    this.modelGateway = modelGateway;
  }

  async execute({ goal, plan, circuit }) {
    const response = await this.modelGateway.generate({ goal, plan, circuit });
    const validation = validateOutput(response.content);
    return freezeContract({
      executionId: newId("cognitive_execution"),
      status: validation.passed ? "completed" : "failed_validation",
      result: response.content,
      model: response,
      validation,
      reflection: reflect({ goal, plan, circuit, modelResponse: response, validation }),
      completedAt: nowIso()
    });
  }
}
