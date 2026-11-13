param([ValidateSet('init', 'build')][string]$Action = 'build')
$ErrorActionPreference = 'Stop'
$keyDir = Join-Path $env:LOCALAPPDATA 'ChagokSigning'
$keyFile = Join-Path $keyDir 'chagok-release.p12'
$passwordFile = Join-Path $keyDir 'password.dpapi'
$alias = 'chagok-release'
$env:JAVA_HOME = if ($env:JAVA_HOME) { $env:JAVA_HOME } else { 'C:\Program Files\Android\Android Studio\jbr' }
$keytool = Join-Path $env:JAVA_HOME 'bin\keytool.exe'

if ($Action -eq 'init') {
    if ((Test-Path -LiteralPath $keyFile) -or (Test-Path -LiteralPath $passwordFile)) {
        throw 'Signing material already exists; refusing to overwrite it.'
    }
    New-Item -ItemType Directory -Path $keyDir -Force | Out-Null
    $bytes = New-Object byte[] 32
    $rng = [System.Security.Cryptography.RandomNumberGenerator]::Create()
    try { $rng.GetBytes($bytes) } finally { $rng.Dispose() }
    $password = [Convert]::ToBase64String($bytes)
    $secure = ConvertTo-SecureString $password -AsPlainText -Force
    $secure | ConvertFrom-SecureString | Set-Content -LiteralPath $passwordFile -Encoding Ascii
    try {
        $env:CHAGOK_KEYTOOL_PASSWORD = $password
        & $keytool -genkeypair -keystore $keyFile -storetype PKCS12 -storepass:env CHAGOK_KEYTOOL_PASSWORD -keypass:env CHAGOK_KEYTOOL_PASSWORD -alias $alias -keyalg RSA -keysize 4096 -validity 10000 -dname 'CN=Chagok Typing' -noprompt
        if ($LASTEXITCODE -ne 0) { throw 'Key creation failed. Inspect the signing directory before retrying.' }
    } finally {
        Remove-Item Env:CHAGOK_KEYTOOL_PASSWORD -ErrorAction SilentlyContinue
        $password = $null
    }
    Write-Host "Signing key created in $keyDir. Back up the keystore and recoverable password securely."
    exit
}

if (!(Test-Path -LiteralPath $keyFile) -or !(Test-Path -LiteralPath $passwordFile)) {
    throw 'Signing material not found. Run release-signing.ps1 init first.'
}
$secure = (Get-Content -LiteralPath $passwordFile -Raw).Trim() | ConvertTo-SecureString
$ptr = [Runtime.InteropServices.Marshal]::SecureStringToBSTR($secure)
try {
    $env:CHAGOK_RELEASE_STORE_FILE = $keyFile
    $env:CHAGOK_RELEASE_KEY_ALIAS = $alias
    $env:CHAGOK_RELEASE_STORE_PASSWORD = [Runtime.InteropServices.Marshal]::PtrToStringBSTR($ptr)
    $env:CHAGOK_RELEASE_KEY_PASSWORD = $env:CHAGOK_RELEASE_STORE_PASSWORD
    Push-Location $PSScriptRoot
    try {
        & .\gradlew.bat :app:assembleRelease :app:bundleRelease -PdistributionSigning=true --offline --console=plain
        if ($LASTEXITCODE -ne 0) { throw 'Release build failed.' }
    } finally { Pop-Location }
} finally {
    [Runtime.InteropServices.Marshal]::ZeroFreeBSTR($ptr)
    Remove-Item Env:CHAGOK_RELEASE_STORE_FILE, Env:CHAGOK_RELEASE_KEY_ALIAS, Env:CHAGOK_RELEASE_STORE_PASSWORD, Env:CHAGOK_RELEASE_KEY_PASSWORD -ErrorAction SilentlyContinue
}
