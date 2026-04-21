from pydantic import BaseModel, Field


class ChatRequest(BaseModel):
    conversation_id: str | None = Field(default=None)
    prompt: str = Field(min_length=3)


class ChatResponse(BaseModel):
    conversation_id: str
    plan: str
    architecture: str
    contracts: str
    implementation: str
    audit_report: str
