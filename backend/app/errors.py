from fastapi import HTTPException


class ApiError(HTTPException):
    """Raise this (not plain HTTPException) so every error response comes back
    as {"error": {"code", "message"}} per API_CONTRACT.md."""

    def __init__(self, status_code: int, code: str, message: str):
        super().__init__(status_code=status_code, detail={"code": code, "message": message})
