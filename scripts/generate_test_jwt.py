#!/usr/bin/env python3
"""
테스트용 JWT 토큰 생성 스크립트

사용법:
  python scripts/generate_test_jwt.py
  python scripts/generate_test_jwt.py --uuid <UUID>

참고: 서버의 JwtUtil은 서명 검증 없이 payload만 디코딩합니다.
      (JwtUtil.java: Base64.getUrlDecoder().decode(parts[1]) 로 payload 추출)
      따라서 secret은 형식 유지를 위한 더미값으로 사용됩니다.
"""

import base64
import json
import hmac
import hashlib
import argparse
import uuid as uuid_module
from datetime import datetime, timezone, timedelta


def base64url_encode(data: bytes) -> str:
    return base64.urlsafe_b64encode(data).rstrip(b"=").decode()


def generate_jwt(user_uuid: str, secret: str = "test-secret", expires_in_hours: int = 24) -> str:
    header = {"alg": "HS256", "typ": "JWT"}
    now = datetime.now(timezone.utc)
    payload = {
        "user_uuid": user_uuid,
        "iat": int(now.timestamp()),
        "exp": int((now + timedelta(hours=expires_in_hours)).timestamp()),
    }

    header_b64 = base64url_encode(json.dumps(header, separators=(",", ":")).encode())
    payload_b64 = base64url_encode(json.dumps(payload, separators=(",", ":")).encode())

    signing_input = f"{header_b64}.{payload_b64}"
    signature = hmac.new(secret.encode(), signing_input.encode(), hashlib.sha256).digest()
    signature_b64 = base64url_encode(signature)

    return f"{signing_input}.{signature_b64}"


def main():
    parser = argparse.ArgumentParser(description="테스트용 JWT 토큰 생성")
    parser.add_argument("--uuid", default=str(uuid_module.uuid4()), help="user_uuid 값 (미입력시 랜덤 생성)")
    parser.add_argument("--secret", default="test-secret", help="JWT 서명용 시크릿 (기본값: test-secret)")
    parser.add_argument("--expires", type=int, default=24, help="만료 시간(시간 단위, 기본값: 24)")
    args = parser.parse_args()

    token = generate_jwt(args.uuid, args.secret, args.expires)

    print(f"\n[user_uuid]")
    print(f"  {args.uuid}")
    print(f"\n[JWT Token]")
    print(f"  {token}")
    print(f"\n[Authorization Header]")
    print(f"  Bearer {token}")
    print(f"\n[curl 예시]")
    print(f'  curl -H "Authorization: Bearer {token}" http://localhost:8084/api/...')


if __name__ == "__main__":
    main()
