import { callOpenAI } from "../agents/openai.js";

export async function orchestrator(prompt) {

  const step1 = await callOpenAI("Defina escopo completo: " + prompt);
  const step2 = await callOpenAI("Crie arquitetura: " + step1);
  const step3 = await callOpenAI("Execute completo: " + step2);

  return {
    result: step3,
    steps: [step1, step2]
  };
}