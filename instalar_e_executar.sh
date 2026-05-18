#!/usr/bin/env bash
set -euo pipefail

ROOT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
cd "$ROOT_DIR"

if ! command -v node >/dev/null 2>&1; then
  echo "❌ Node.js não encontrado. Instale Node.js 18+ e tente novamente."
  exit 1
fi

if ! command -v npm >/dev/null 2>&1; then
  echo "❌ npm não encontrado. Instale npm e tente novamente."
  exit 1
fi

echo "📦 Instalando dependências..."
npm install

if [ ! -f .env ] && [ -n "${OPENAI_API_KEY:-}" ]; then
  echo "ℹ️ Variável OPENAI_API_KEY detectada no ambiente."
fi

echo "🚀 Iniciando aplicação em http://localhost:3000"
npm start
