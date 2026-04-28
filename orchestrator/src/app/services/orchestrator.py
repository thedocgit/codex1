from sqlalchemy.orm import Session

from app.models.conversation import Conversation, Message
from app.models.schemas import ChatResponse
from app.providers.registry import build_provider_registry

SYSTEM_PROMPT = (
    "Você é um engenheiro de software sênior. Responda com foco em entrega integral: "
    "arquitetura, módulos, contratos, dockerização, testes e checklist de auditoria."
)


class OrchestratorService:
    def __init__(self, db: Session):
        self.db = db
        self.providers = build_provider_registry()

    def _get_or_create_conversation(self, conversation_id: str | None, prompt: str) -> Conversation:
        if conversation_id:
            conv = self.db.get(Conversation, conversation_id)
            if conv:
                return conv

        conv = Conversation(title=prompt[:80])
        self.db.add(conv)
        self.db.flush()
        return conv

    async def run(self, conversation_id: str | None, prompt: str) -> ChatResponse:
        conv = self._get_or_create_conversation(conversation_id, prompt)
        self.db.add(Message(conversation_id=conv.id, role="user", provider="ui", content=prompt))

        if "chatgpt" not in self.providers:
            raise ValueError("Configure ao menos OPENAI_API_KEY para iniciar a orquestração.")

        planner = self.providers["chatgpt"]
        researcher = self.providers.get("perplexity", planner)
        coder = self.providers.get("deepseek", planner)

        plan = await planner.ask(SYSTEM_PROMPT, f"Crie um plano de execução completo para: {prompt}")
        architecture = await planner.ask(SYSTEM_PROMPT, f"Defina arquitetura detalhada com módulos e arquivos para: {prompt}")
        contracts = await planner.ask(SYSTEM_PROMPT, f"Defina contratos técnicos (API, eventos, DB) para: {prompt}")
        implementation = await coder.ask(
            SYSTEM_PROMPT,
            f"Com base neste plano:\n{plan}\n\ne nesta arquitetura:\n{architecture}\n"
            f"gere scripts completos por módulos para: {prompt}",
        )
        audit_report = await researcher.ask(
            SYSTEM_PROMPT,
            f"Audite o pacote final. Valide completude, riscos e critérios de pronto.\n"
            f"Plano:\n{plan}\nArquitetura:\n{architecture}\nContratos:\n{contracts}",
        )

        final_text = (
            f"## Plano\n{plan}\n\n"
            f"## Arquitetura\n{architecture}\n\n"
            f"## Contratos\n{contracts}\n\n"
            f"## Implementação\n{implementation}\n\n"
            f"## Auditoria\n{audit_report}"
        )
        self.db.add(Message(conversation_id=conv.id, role="assistant", provider="orchestrator", content=final_text))
        self.db.commit()

        return ChatResponse(
            conversation_id=conv.id,
            plan=plan,
            architecture=architecture,
            contracts=contracts,
            implementation=implementation,
            audit_report=audit_report,
        )
