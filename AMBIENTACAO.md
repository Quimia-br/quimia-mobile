# Ambientação do trabalho — Quimia Mobile

Passos rápidos para configurar o ambiente de desenvolvimento:

1) Requisitos
- Java: JDK 17+ (recomendado JDK 17 ou 21).
- Android SDK: `platforms;android-34` e `build-tools;34.0.0` instalados.
- Variáveis: setar ANDROID_SDK_ROOT (ou ANDROID_HOME) apontando para o SDK.
- Gradle wrapper: usar a wrapper do projeto (./gradlew) — o projeto usa Gradle 9.3.0.
- AGP: 9.3.1 — requer Gradle >= 9.3.x (já configurado na wrapper).

2) Script de correção para amigos
- O projeto inclui `setup-quimia-mobile.ps1` e `setup-quimia-mobile.cmd` na raiz.
- Como rodar no Windows pelo CMD:
    1. Abra o Prompt de Comando (CMD) na pasta do projeto.
    2. Execute:
       `setup-quimia-mobile.cmd`
    3. Se quiser rodar manualmente em PowerShell:
       `powershell -ExecutionPolicy Bypass -File ".\setup-quimia-mobile.ps1"`
    4. Se o caminho do projeto contiver `&` (ex.: `...\J&F\...`), prefira o `.cmd` ou passe o caminho entre aspas, porque o PowerShell interpreta `&` como operador.
- Esse script tenta corrigir os problemas mais comuns:
    - `Unresolved reference 'RepositoriesMode'`
    - Gradle 9.0.0 / AGP 9.x incompatível
    - `local.properties` ausente
    - `settings.gradle.kts` sem google()/mavenCentral()
    - `compileSdk` ausente
    - `plugins/android-commons.gradle` e `plugins/feature-dependencies.gradle` inexistentes

3) Problema comum: ScriptCompilationException com "Unresolved reference 'RepositoriesMode'"
- Causa: o Kotlin DSL de settings pode falhar ao compilar o script temporário e não enxergar o tipo RepositoriesMode naquele contexto.
- Correção aplicada: removemos a chamada a `repositoriesMode.set(...)` do `settings.gradle.kts` para evitar o erro de compilação do script. Os repositórios (google() e mavenCentral()) permanecem configurados.

4) Passo-a-passo para validar e resolver problemas de plugin (AGP) sem mudar a versão do Gradle
1. Confirmar conectividade com repositórios (google() e mavenCentral()).
2. Conferir `gradle/libs.versions.toml` — agp deve apontar para a versão esperada (ex: 9.0.0).
3. Verificar wrapper: `./gradlew --version` (deve ser 9.x para AGP 9.x).
4. Limpar cache e forçar refresh: `./gradlew --refresh-dependencies --no-build-cache`.
5. Rodar um build de verificação: `./gradlew assembleDebug --stacktrace`.
6. Se o erro persistir, inspecionar proxy/firewall ou bloqueio do Maven Google.

5) Como reintroduzir repositoriesMode no futuro (opcional)
- Se for necessário forçar o modo de repositórios, reintroduza `repositoriesMode.set(...)` somente após confirmar que o ambiente de compilação do Kotlin DSL aceita a referência. Uma alternativa segura é definir a política por projeto ou documentar a exigência do ambiente.

6) Problemas com SDK / downloads (ex.: "This version only understands SDK XML versions up to 3" / connection timed out)
- Sintoma: o Gradle/AGP tenta baixar metadados do SDK e falha com timeout ou encontra um formato de SDK incompatível entre ferramentas (ex: SDK Manager mais novo que as ferramentas instaladas).
- Causas comuns:
    - Android SDK não instalado ou ANDROID_SDK_ROOT não configurado.
    - Ferramentas de linha de comando desatualizadas (cmdline-tools)
    - Falta do platform desejado (ex: platforms;android-34) ou build-tools correspondente.
    - Firewall/proxy bloqueando acesso ao repositório remoto do Google.

Passos de correção:
1. Verificar variável de ambiente (PowerShell):
   $env:ANDROID_SDK_ROOT (deve apontar para a pasta do SDK local)
2. Instalar/atualizar ferramentas via sdkmanager (na pasta cmdline-tools/bin):
   sdkmanager "platform-tools" "cmdline-tools;latest" "platforms;android-34" "build-tools;34.0.0"
3. Aceitar licenças:
   sdkmanager --licenses
4. Conferir conexão: se ocorrer timeout, tentar em rede sem proxy ou configurar proxy nas variáveis HTTP_PROXY/HTTPS_PROXY.
5. Verificar versão dos command-line tools e, se necessário, instalar a versão compatível com o SDK.

7) Comandos úteis
- `./gradlew --version`
- `./gradlew --refresh-dependencies`
- `./gradlew assembleDebug --stacktrace`
- `setup-quimia-mobile.cmd`

8) Fluxo recomendado para qualquer colega
1. Baixar o zip do projeto.
2. Abrir CMD na pasta do repositório.
3. Rodar: `setup-quimia-mobile.cmd`
4. Se o JDK 17+ ou SDK não estiver instalado, concluir a instalação.
5. Rodar: `gradlew.bat --version`
6. Rodar: `gradlew.bat assembleDebug`

Observação: esse bootstrap é para distribuição entre colegas e tenta automatizar os principais erros comuns do ambiente Android/Gradle. Se o JDK 17+ ou SDK ainda não estiver instalado, o script aponta o problema sem mudar a versão do Gradle.