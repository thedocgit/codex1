# Arquitetura alvo (produção)

## Objetivo
Entregar uma plataforma desktop para orquestrar múltiplas IAs com memória persistente, governança e auditoria de ponta a ponta para geração de software completo.

## Macrocomponentes

1. **Desktop App (Tauri/Electron)**
   - Chat único
   - Painel de configuração de provedores
   - Visualização de artefatos e logs
2. **Orchestrator API (FastAPI)**
   - Pipeline fixo
   - Regras de qualidade
   - Gestão de conversas e versões
3. **Memory Layer (PostgreSQL + Vector Store)**
   - Histórico integral
   - Resumos incrementais
   - Recuperação contextual por tarefa
4. **Execution Layer (Workers + Queue)**
   - Geração de código por lotes
   - Execução de testes/build
   - Auditoria automatizada
5. **Packaging Layer**
   - Empacotamento único autoinstalável
   - Scripts de bootstrap e autoconfiguração

## Pipeline fixo recomendado

- Etapa 1: Requisitos e escopo fechado
- Etapa 2: Arquitetura (C4, módulos, DDD, boundaries)
- Etapa 3: Contratos (OpenAPI, eventos, esquema SQL)
- Etapa 4: Plano de arquivos com nomes e responsabilidades
- Etapa 5: Implementação completa por pacotes
- Etapa 6: Testes automáticos (unitário, integração, e2e)
- Etapa 7: Auditoria (completude, segurança, performance)
- Etapa 8: Build e pacote final assinado

## Governança e segurança

- Cofre local criptografado para chaves (AES-256 + chave mestra do usuário)
- RBAC local para perfis (admin, operador, auditor)
- Redação automática de segredos em logs
- Auditoria imutável por hash encadeado

## Modelo mínimo de dados

- `conversations`
- `messages`
- `artifacts`
- `audit_events`
- `provider_runs`
- `secrets_metadata`

## SLOs de referência

- Disponibilidade API: 99.9%
- Latência P95 (resposta inicial): < 4s
- MTTR: < 30min

## Critério de pronto (DoD)

Um projeto só é marcado como finalizado quando:

1. arquitetura aprovada,
2. contratos versionados,
3. 100% dos módulos previstos gerados,
4. suíte de testes verde,
5. auditoria sem bloqueios críticos,
6. pacote autoinstalável produzido e validado.
