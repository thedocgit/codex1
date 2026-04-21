from fastapi import FastAPI

from app.api.routes import router
from app.core.settings import settings
from app.db.base import Base
from app.db.session import engine

app = FastAPI(title=settings.app_name)
app.include_router(router)


@app.on_event("startup")
def on_startup() -> None:
    Base.metadata.create_all(bind=engine)
