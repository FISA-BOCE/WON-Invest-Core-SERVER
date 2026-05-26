#!/usr/bin/env node
/**
 * 테스트용 JWT 토큰 생성 스크립트 (Node.js 내장 모듈만 사용)
 *
 * 사용법:
 *   node scripts/generate_test_jwt.js
 *   node scripts/generate_test_jwt.js --uuid <UUID>
 *   node scripts/generate_test_jwt.js --uuid <UUID> --secret <SECRET>
 *
 * 참고: 서버의 JwtUtil은 서명 검증 없이 payload만 디코딩합니다.
 *       (JwtUtil.java: Base64.getUrlDecoder().decode(parts[1]) 로 payload 추출)
 *       따라서 secret은 형식 유지를 위한 더미값으로 사용됩니다.
 */

const crypto = require("crypto");

function base64urlEncode(str) {
  return Buffer.from(str)
    .toString("base64")
    .replace(/=/g, "")
    .replace(/\+/g, "-")
    .replace(/\//g, "_");
}

function generateUUID() {
  return crypto.randomUUID();
}

function generateJwt(userUuid, secret = "test-secret", expiresInHours = 24) {
  const header = { alg: "HS256", typ: "JWT" };
  const now = Math.floor(Date.now() / 1000);
  const payload = {
    user_uuid: userUuid,
    iat: now,
    exp: now + expiresInHours * 3600,
  };

  const headerB64 = base64urlEncode(JSON.stringify(header));
  const payloadB64 = base64urlEncode(JSON.stringify(payload));
  const signingInput = `${headerB64}.${payloadB64}`;

  const signature = crypto
    .createHmac("sha256", secret)
    .update(signingInput)
    .digest("base64")
    .replace(/=/g, "")
    .replace(/\+/g, "-")
    .replace(/\//g, "_");

  return `${signingInput}.${signature}`;
}

// 인자 파싱
const args = process.argv.slice(2);
const getArg = (flag) => {
  const idx = args.indexOf(flag);
  return idx !== -1 ? args[idx + 1] : null;
};

const userUuid = getArg("--uuid") || generateUUID();
const secret = getArg("--secret") || "test-secret";
const expires = parseInt(getArg("--expires") || "24", 10);

const token = generateJwt(userUuid, secret, expires);

console.log("\n[user_uuid]");
console.log(`  ${userUuid}`);
console.log("\n[JWT Token]");
console.log(`  ${token}`);
console.log("\n[Authorization Header]");
console.log(`  Bearer ${token}`);
console.log("\n[curl 예시]");
console.log(`  curl -H "Authorization: Bearer ${token}" http://localhost:8084/api/...`);