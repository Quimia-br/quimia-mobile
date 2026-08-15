param(
    [switch]$CheckOnly
)

$ErrorActionPreference = 'Stop'

function Write-Step($message) {
    Write-Host "[quimia] $message" -ForegroundColor Cyan
}

$repoRoot = (Resolve-Path -LiteralPath (Split-Path -Parent $MyInvocation.MyCommand.Path)).Path
Set-Location -LiteralPath $repoRoot

if ($repoRoot -match '&') {
    Write-Warning 'O caminho do projeto contém "&". O PowerShell interpreta isso como operador. Use o arquivo .cmd do projeto ou execute com caminho entre aspas.'
    Write-Host 'Exemplo: setup-quimia-mobile.cmd' -ForegroundColor Yellow
    Write-Host 'Ou: powershell -ExecutionPolicy Bypass -File "C:\caminho\para\setup-quimia-mobile.ps1"' -ForegroundColor Yellow
}

$projectSettings = Join-Path $repoRoot 'settings.gradle.kts'
$wrapperProps = Join-Path $repoRoot 'gradle\wrapper\gradle-wrapper.properties'
$localProperties = Join-Path $repoRoot 'local.properties'
$pluginsDir = Join-Path $repoRoot 'plugins'

$defaultSettingsText = @'
pluginManagement {
    repositories {
        google()
        mavenCentral()
        gradlePluginPortal()
    }
}

dependencyResolutionManagement {
    repositories {
        google()
        mavenCentral()
    }
}

rootProject.name = "mobile"
include(":app", ":designSystem")
'@

$defaultAndroidCommons = @'
// Placeholder android-commons.gradle
// Add common Android configuration here if needed.
// This placeholder prevents build failures when the shared script is missing.

// No-op
'@

$defaultFeatureDependencies = @'
// Placeholder feature-dependencies.gradle
// Add feature dependency logic here if needed.

// No-op
'@

try {
    Write-Step 'Verificando ambiente do projeto...'

    $jdkOk = $false
    $javaCmd = Get-Command java -ErrorAction SilentlyContinue
    if ($javaCmd) {
        $prevErrorActionPreference = $ErrorActionPreference
        $ErrorActionPreference = 'Continue'
        try {
            $javaVersionOutput = & java -version 2>&1
        } finally {
            $ErrorActionPreference = $prevErrorActionPreference
        }

        $javaVersion = $javaVersionOutput | Select-Object -First 1
        if ($javaVersion -match 'version\s+"?(\d+)') {
            $javaMajor = [int]$Matches[1]
            if ($javaMajor -ge 17) {
                $jdkOk = $true
            }
        }
    }

    if (-not $jdkOk) {
        Write-Step 'JDK nao encontrado ou incompatível. Tente instalar JDK 17/21.'
        if (-not $CheckOnly) {
            try {
                winget install --id Microsoft.OpenJDK.17 -e -s winget
            } catch {
                Write-Warning 'Nao foi possivel iniciar o winget. Instale manualmente o JDK 17.'
            }
        }
    } else {
        Write-Step "JDK OK: $javaVersion"
    }

    $sdkCandidates = @(
        $env:ANDROID_SDK_ROOT,
        $env:ANDROID_HOME,
        "$env:LOCALAPPDATA\Android\Sdk",
        "${env:ProgramFiles(x86)}\Android\Sdk",
        "${env:ProgramFiles}\Android\Sdk",
        "$HOME\AppData\Local\Android\Sdk"
    ) | Where-Object { $_ }

    $sdkRoot = $sdkCandidates | Where-Object { Test-Path $_ } | Select-Object -First 1

    if (-not $sdkRoot) {
        Write-Step 'Android SDK nao encontrado. O projeto precisa do SDK do Android configurado.'
        if (-not $CheckOnly) {
            $sdkRoot = Join-Path $env:LOCALAPPDATA 'Android\Sdk'
            New-Item -ItemType Directory -Force -Path $sdkRoot | Out-Null
            Write-Step "Diretório do SDK criado em: $sdkRoot"
        }
    } else {
        Write-Step "SDK Android encontrado em: $sdkRoot"
    }

    if (-not $CheckOnly) {
        if (-not (Test-Path $projectSettings)) {
            Set-Content -Path $projectSettings -Value $defaultSettingsText -Encoding UTF8
            Write-Step 'settings.gradle.kts foi criado com configuração compatível.'
        } else {
            $settingsContent = Get-Content -Raw -Path $projectSettings
            if ($settingsContent -notmatch 'google\(\)' -or $settingsContent -notmatch 'mavenCentral\(\)') {
                Set-Content -Path $projectSettings -Value $defaultSettingsText -Encoding UTF8
                Write-Step 'settings.gradle.kts foi corrigido para incluir google() e mavenCentral().'
            }
        }

        if (-not (Test-Path $wrapperProps)) {
            New-Item -ItemType Directory -Force -Path (Split-Path -Parent $wrapperProps) | Out-Null
            Set-Content -Path $wrapperProps -Value "distributionBase=GRADLE_USER_HOME`ndistributionPath=wrapper/dists`ndistributionUrl=https\\://services.gradle.org/distributions/gradle-9.1.0-bin.zip`nnetworkTimeout=60000`nvalidateDistributionUrl=true`nzipStoreBase=GRADLE_USER_HOME`nzipStorePath=wrapper/dists`n" -Encoding ASCII
            Write-Step 'gradle-wrapper.properties foi ajustado para Gradle 9.1.0.'
        } else {
            $wrapperText = Get-Content -Raw -Path $wrapperProps
            if ($wrapperText -notmatch 'gradle-9\.1\.0-bin\.zip') {
                $wrapperText = $wrapperText -replace 'distributionUrl=.*', 'distributionUrl=https\://services.gradle.org/distributions/gradle-9.1.0-bin.zip'
                Set-Content -Path $wrapperProps -Value $wrapperText -Encoding ASCII
                Write-Step 'gradle-wrapper.properties atualizado para gradle-9.1.0.'
            }
        }

        if ($sdkRoot) {
            $localText = "sdk.dir=$sdkRoot"
            if (-not (Test-Path $localProperties)) {
                Set-Content -Path $localProperties -Value $localText -Encoding UTF8
                Write-Step 'local.properties foi criado com o SDK.'
            } else {
                $localCurrent = Get-Content -Raw -Path $localProperties
                if ($localCurrent -notmatch 'sdk\.dir=') {
                    Add-Content -Path $localProperties -Value $localText
                    Write-Step 'local.properties atualizado com sdk.dir.'
                }
            }
        }

        New-Item -ItemType Directory -Force -Path $pluginsDir | Out-Null

        $androidFile = Join-Path $pluginsDir 'android-commons.gradle'
        $featureFile = Join-Path $pluginsDir 'feature-dependencies.gradle'

        if (-not (Test-Path $androidFile)) {
            Set-Content -Path $androidFile -Value $defaultAndroidCommons -Encoding UTF8
        }

        if (-not (Test-Path $featureFile)) {
            Set-Content -Path $featureFile -Value $defaultFeatureDependencies -Encoding UTF8
        }

        $appBuild = Join-Path $repoRoot 'app\build.gradle.kts'
        if (Test-Path $appBuild) {
            $appText = Get-Content -Raw -Path $appBuild
            if ($appText -notmatch 'compileSdk\s*=\s*34') {
                $appText = $appText -replace 'android \{', "android {`n    compileSdk = 34"
                Set-Content -Path $appBuild -Value $appText -Encoding UTF8
                Write-Step 'app/build.gradle.kts corrigido com compileSdk = 34.'
            }
        }

        $designBuild = Join-Path $repoRoot 'designSystem\build.gradle.kts'
        if (Test-Path $designBuild) {
            $designText = Get-Content -Raw -Path $designBuild
            if ($designText -notmatch 'compileSdk\s*=\s*34') {
                $designText = $designText -replace 'android \{', "android {`n    compileSdk = 34"
                Set-Content -Path $designBuild -Value $designText -Encoding UTF8
                Write-Step 'designSystem/build.gradle.kts corrigido com compileSdk = 34.'
            }
        }
    }

    Write-Step 'Verificação concluída.'
    Write-Host ''
    Write-Host 'Próximo passo:' -ForegroundColor Yellow
    Write-Host '  1) Instale/atualize o Android SDK, se necessário.' -ForegroundColor Yellow
    Write-Host '  2) Rode: .\gradlew --version' -ForegroundColor Yellow
    Write-Host '  3) Rode: .\gradlew assembleDebug' -ForegroundColor Yellow

    if ($CheckOnly) {
        exit 0
    }
} catch {
    $msg = $_.Exception.Message
    $pos = $_.InvocationInfo.PositionMessage
    Write-Error "Erro no setup: $msg"
    if ($pos) {
        Write-Host "Detalhes do erro: $pos" -ForegroundColor Red
    }
    exit 1
}