from abc import ABC, abstractmethod


class Provider(ABC):
    name: str

    @abstractmethod
    async def ask(self, system_prompt: str, user_prompt: str) -> str:
        raise NotImplementedError
