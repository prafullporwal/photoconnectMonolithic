# =============================================================================
# Generate RSA keypair for JWT signing.
# =============================================================================
# Run this once before first boot. Drops PEMs into ./keys/.
# Re-run any time to rotate the keypair (existing JWTs will fail validation).
# =============================================================================

$ErrorActionPreference = 'Stop'

# Locate openssl: PATH first, then Git for Windows fallback.
$openssl = (Get-Command openssl -ErrorAction SilentlyContinue).Source
if (-not $openssl) {
    foreach ($candidate in @(
        'C:\Program Files\Git\usr\bin\openssl.exe',
        'C:\Program Files\Git\mingw64\bin\openssl.exe',
        'C:\Program Files (x86)\Git\usr\bin\openssl.exe'
    )) {
        if (Test-Path $candidate) { $openssl = $candidate; break }
    }
}
if (-not $openssl) {
    throw "openssl not found. Install OpenSSL or Git for Windows, or add openssl.exe to PATH."
}

$keysDir = Join-Path $PSScriptRoot '..\keys'
if (-not (Test-Path $keysDir)) {
    New-Item -ItemType Directory -Path $keysDir | Out-Null
}

$priv = Join-Path $keysDir 'private_key.pem'
$pub  = Join-Path $keysDir 'public_key.pem'

Write-Host "Generating 2048-bit RSA keypair using $openssl..."
& $openssl genpkey -algorithm RSA -pkeyopt rsa_keygen_bits:2048 -out $priv
if ($LASTEXITCODE -ne 0) { throw "openssl genpkey failed (exit $LASTEXITCODE)" }

& $openssl rsa -in $priv -pubout -out $pub
if ($LASTEXITCODE -ne 0) { throw "openssl rsa -pubout failed (exit $LASTEXITCODE)" }

Write-Host "Wrote $priv"
Write-Host "Wrote $pub"
