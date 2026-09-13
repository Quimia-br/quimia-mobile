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

- Para testar manualmente: abra `Actions > Release APK > Run workflow` e informe `version_name` e `version_code`.
- Para publicar automaticamente no GitHub: crie e envie uma tag, por exemplo `v1.0.0`.

O workflow gera `app-release.apk`, verifica a assinatura e publica o APK como artifact. Quando acionado por uma tag, também cria uma GitHub Release com o APK anexado.
