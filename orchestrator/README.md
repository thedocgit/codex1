# AI Orchestrator Desktop (Base Técnica)

Este projeto entrega uma base robusta para um **orquestrador multi-IA** com:

- pipeline fixo de execução (plano → arquitetura → contratos → implementação → auditoria),
- memória externa persistente em PostgreSQL,
- trilha de auditoria por conversa,
- API backend preparada para integrar com cliente desktop.

## Provedores suportados nesta base

- ChatGPT (OpenAI API)
- Perplexity
- DeepSeek

> Observação: Claude, Grok, NotebookLM e Manus podem ser adicionados no mesmo padrão do módulo `openai_compatible.py` ou com adapters específicos.

## Subir ambiente

```bash
cp .env.example .env
docker compose up --build
```

## Endpoint principal

`POST /api/chat`

```json
{
  "conversation_id": null,
  "prompt": "Crie um app X com requisitos Y"
}
```

Retorno contém seções separadas para plano, arquitetura, contratos, implementação e auditoria.

## Próximos passos para produto desktop completo

1. Criar shell desktop (Tauri recomendado) e consumir `POST /api/chat`.
2. Implementar painel seguro de credenciais com criptografia local e rotação de chaves.
3. Adicionar job queue e execução assíncrona para projetos muito longos.
4. Gerar pacote final autoinstalável (MSI/DMG/AppImage) com auto-configuração.
5. Acoplar módulo de validação de entrega (testes, build, checklist de pronto, assinatura do artefato).
