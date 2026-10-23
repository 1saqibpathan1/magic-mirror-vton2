from pydantic_settings import BaseSettings, SettingsConfigDict


class Settings(BaseSettings):
    device_api_key: str = "change-me-device-key"
    fashn_api_key: str = ""
    mock_fashn_provider: bool = False

    database_url: str = "sqlite:///./magic_mirror.db"
    media_root: str = "./media"
    base_url: str = "http://localhost:8000"

    model_config = SettingsConfigDict(env_file=".env", env_file_encoding="utf-8")


settings = Settings()
