import {
  CapabilityRegistry,
  CognitiveRuntime,
  ModelGateway,
  buildCircuit,
  createPlan,
  interpret,
  modelGoal,
  perceive
} from "./cognitive/index.js";

const registry = new CapabilityRegistry();
const gateway = new ModelGateway();
const runtime = new CognitiveRuntime({ modelGateway: gateway });

export async function orchestrator(prompt, options = {}) {
  const perception = perceive(prompt, options.context);
  const interpretation = interpret(perception);

  if (interpretation.requiresApproval && options.approved !== true) {
    return {
      status: "awaiting_approval",
      result: "Execution requires explicit approval because the objective was classified as high risk.",
      trace: { perception, interpretation }
    };
  }

  const goal = modelGoal(interpretation, options.goal);
  const plan = createPlan(goal, registry);
  const circuit = buildCircuit(plan);
  const execution = await runtime.execute({ goal, plan, circuit });

  return {
    status: execution.status,
    result: execution.result,
    trace: {
      perception,
      interpretation,
      goal,
      plan,
      circuit,
      validation: execution.validation,
      reflection: execution.reflection
    },
    metrics: {
      provider: execution.model.providerId,
      model: execution.model.model,
      latencyMs: execution.model.latencyMs,
      capabilityCount: registry.list().length,
      circuitNodeCount: circuit.nodes.length
    }
  };
}

export function listCapabilities() {
  return registry.list();
}
