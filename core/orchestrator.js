import { callOpenAI } from "../agents/openai.js";

function buildEbookPrompt(userInput) {
  return `Você é um escritor, editor e pesquisador premium de não-ficção.

Objetivo: gerar um e-book completo em português do Brasil com padrão premium, conteúdo informativo, confiável e útil para aplicação prática.

Tema solicitado: ${userInput}

Regras obrigatórias:
1) Escreva em Markdown.
2) Estruture o e-book com:
   - Título impactante
   - Subtítulo
   - Capa (texto)
   - Sumário
   - Introdução
   - 8 a 12 capítulos completos
   - Estudos de caso ou exemplos aplicáveis
   - Checklist prático por capítulo
   - Conclusão
   - FAQ (10 perguntas)
   - Plano de ação de 30 dias
   - Referências sugeridas
3) Entregue conteúdo profundo, sem superficialidade e sem preencher com frases vazias.
4) Linguagem clara, profissional e didática.
5) Inclua dados, práticas e recomendações baseadas em boas práticas reconhecidas (sem inventar fontes específicas).
6) Evite promessas irreais; destaque limitações quando necessário.
7) Inclua seção "Como usar este e-book" para ajudar iniciantes e avançados.
8) Finalize com CTA elegante para próximo passo do leitor.

Formatação:
- Use títulos ## e ###.
- Use tabelas quando útil.
- Use listas objetivas e blocos de destaque para ações.
- Mantenha alta legibilidade.

Agora gere o e-book completo.`;
}

export async function orchestrator(prompt) {
  const normalized = String(prompt || "").trim();

  const planning = await callOpenAI(
    `Crie um plano editorial de alto nível para este e-book premium: ${normalized}.` +
      " Retorne em tópicos curtos com: público-alvo, promessa central, diferenciais, estrutura e tom."
  );

  const finalEbook = await callOpenAI(buildEbookPrompt(normalized));

  return {
    mode: "ebook-premium",
    result: finalEbook,
    steps: [planning]
  };
}
