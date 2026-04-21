from pydantic_settings import BaseSettings, SettingsConfigDict


class Settings(BaseSettings):
    app_name: str = "AI Orchestrator Desktop"
    app_env: str = "dev"
    database_url: str
    default_timeout_seconds: int = 120

    openai_api_key: str | None = None
    perplexity_api_key: str | None = None
    anthropic_api_key: str | None = None
    deepseek_api_key: str | None = None
    grok_api_key: str | None = None

    model_config = SettingsConfigDict(env_file=".env", env_file_encoding="utf-8", extra="ignore")


settings = Settings()
