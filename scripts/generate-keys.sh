#!/usr/bin/env bash
# =============================================================================
# Generate RSA keypair for JWT signing.
# =============================================================================
set -euo pipefail

DIR="$(cd "$(dirname "$0")/.." && pwd)"
KEYS_DIR="$DIR/keys"
mkdir -p "$KEYS_DIR"

PRIV="$KEYS_DIR/private_key.pem"
PUB="$KEYS_DIR/public_key.pem"

echo "Generating 2048-bit RSA keypair..."
openssl genpkey -algorithm RSA -pkeyopt rsa_keygen_bits:2048 -out "$PRIV"
openssl rsa -in "$PRIV" -pubout -out "$PUB"

echo "Wrote $PRIV"
echo "Wrote $PUB"
