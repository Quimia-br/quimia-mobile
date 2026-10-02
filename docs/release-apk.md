# Release do APK

## Criar a chave de assinatura

No computador de desenvolvimento, gere uma chave e guarde o arquivo `.jks` em local seguro:

```powershell
keytool -genkeypair -v -keystore quimia-release.jks -alias quimia-release -keyalg RSA -keysize 2048 -validity 10000
```

Nunca envie o `.jks` para o repositório.

## Secrets do GitHub Actions

Em `Settings > Secrets and variables > Actions`, crie estes repository secrets:

- `KEYSTORE_BASE64`: conteúdo Base64 do arquivo `quimia-release.jks`.
- `KEYSTORE_PASSWORD`: senha do keystore.
- `KEY_ALIAS`: normalmente `quimia-release`.
- `KEY_PASSWORD`: senha da chave.
- `GOOGLE_SERVICES_JSON`: conteúdo do `google-services.json` do Firebase de produção.

Para gerar o Base64 no PowerShell:

```powershell
[Convert]::ToBase64String([IO.File]::ReadAllBytes('.\quimia-release.jks'))
```

## Gerar e publicar

O APK assinado é gerado pelo workflow `Quimia Mobile CI and APK Release` (`ci.yml`) quando uma Release é publicada no GitHub.

1. Em `Releases > Draft a new release`, crie uma tag no formato `vMAJOR.MINOR.PATCH` (por exemplo `v1.0.0`) e publique a Release.
2. Build, Unit tests e Android lint precisam passar. Em seguida o job `Signed release APK` gera o `app-release.apk`, verifica a assinatura com `apksigner` e anexa o arquivo `quimia-<tag>.apk` à Release.
3. `versionName` vem da tag (sem o `v`) e `versionCode` é o número da execução do workflow.

O APK também fica disponível como artifact do workflow por 30 dias. Pull requests e pushes na `main` geram apenas o APK de debug, sem usar os secrets de assinatura.
