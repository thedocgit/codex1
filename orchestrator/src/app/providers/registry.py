from app.core.settings import settings
from app.providers.openai_compatible import OpenAICompatibleProvider


def build_provider_registry() -> dict[str, OpenAICompatibleProvider]:
    providers: dict[str, OpenAICompatibleProvider] = {}

    if settings.openai_api_key:
        providers["chatgpt"] = OpenAICompatibleProvider(
            name="chatgpt",
            api_key=settings.openai_api_key,
            base_url="https://api.openai.com/v1",
            model="gpt-4.1",
            timeout_seconds=settings.default_timeout_seconds,
        )

    if settings.perplexity_api_key:
        providers["perplexity"] = OpenAICompatibleProvider(
            name="perplexity",
            api_key=settings.perplexity_api_key,
            base_url="https://api.perplexity.ai",
            model="sonar-pro",
            timeout_seconds=settings.default_timeout_seconds,
        )

    if settings.deepseek_api_key:
        providers["deepseek"] = OpenAICompatibleProvider(
            name="deepseek",
            api_key=settings.deepseek_api_key,
            base_url="https://api.deepseek.com/v1",
            model="deepseek-chat",
            timeout_seconds=settings.default_timeout_seconds,
        )

    return providers
