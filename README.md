<div align="center">

# Quimia Mobile

**Conheça seus produtos. Organize sua casa. Entenda os riscos de uma mistura.**

Aplicativo Android do ecossistema Quimia, desenvolvido em Kotlin com Jetpack Compose.

[![Android CI](https://github.com/Quimia-br/quimia-mobile/actions/workflows/android-ci.yml/badge.svg)](https://github.com/Quimia-br/quimia-mobile/actions/workflows/android-ci.yml)
[![Release](https://img.shields.io/github/v/release/Quimia-br/quimia-mobile?sort=semver)](https://github.com/Quimia-br/quimia-mobile/releases)
[![Kotlin](https://img.shields.io/badge/Kotlin-7F52FF?logo=kotlin&logoColor=white)](https://kotlinlang.org/)
[![Jetpack Compose](https://img.shields.io/badge/Jetpack_Compose-4285F4?logo=jetpackcompose&logoColor=white)](https://developer.android.com/compose)
[![Android](https://img.shields.io/badge/Android-7.0%2B-3DDC84?logo=android&logoColor=white)](app/src/main/AndroidManifest.xml)
[![Licença MIT](https://img.shields.io/badge/Licen%C3%A7a-MIT-blue)](LICENSE)

[Baixar APK](https://github.com/Quimia-br/quimia-mobile/releases/latest) · [Funcionalidades](#funcionalidades) · [Como executar](#como-executar) · [Arquitetura](#arquitetura) · [Requisitos da disciplina](#requisitos-da-disciplina)

</div>

## Sobre o projeto

O Quimia reúne os produtos químicos usados em casa em uma estante digital. Pelo aplicativo, é possível buscar um produto, ler seu código de barras, organizar os itens por cômodo, consultar informações de segurança e analisar a compatibilidade entre dois produtos. A Kemi complementa essa experiência com uma interface de conversa, enquanto o mapa ajuda a encontrar pontos de descarte e compra.

Este repositório contém o aplicativo Android e seu Design System. Os dados de conta, catálogo, estante, cômodos e análises são fornecidos pela [Quimia API](https://github.com/Quimia-br/quimia-api); as respostas da assistente são processadas pelo [Quimia Agent](https://github.com/Quimia-br/quimia-agent), por meio da API.

## Funcionalidades

| Área | O que o usuário encontra |
| --- | --- |
| **Acesso** | Cadastro, login com e-mail/senha ou Google, recuperação de senha e restauração da sessão. |
| **Home** | Atalhos para os principais fluxos, histórico de misturas e acesso ao ponto de descarte salvo. |
| **Catálogo** | Pesquisa, leitura de código de barras pela câmera, detalhes do produto e informações da FDS disponíveis no cadastro. |
| **Minha estante** | Produtos e substâncias, favoritos, pesquisa e ordenação dos produtos. |
| **Cômodos** | Criação, edição e exclusão de cômodos, associação de produtos e visualização das informações retornadas pela API. |
| **Misturas** | Consulta de compatibilidade entre dois produtos da estante e histórico de análises. |
| **Proximidade** | Mapa com pontos de compra e descarte, pontos salvos e centralização na localização do aparelho ou no endereço cadastrado. |
| **Kemi** | Envio de mensagens para a assistente e encerramento de conversa para iniciar um novo chat. |
| **Perfil** | Edição de nome, nascimento e foto; e-mail exibido sem permitir alteração. |
| **Preferências** | Temas claro, escuro, alto contraste e sistema, tamanho do texto e opções de notificação. |
| **Notificações** | Avisos de resultados de misturas e lembrete semanal opcional para revisar a estante. |

As telas incluem estados de carregamento, skeletons, mensagens de erro e estados vazios. A interface possui adaptações para celular e tablet e componentes compartilhados para manter a identidade visual entre os fluxos.

### Dados e limites dos recursos

- A leitura da câmera identifica o **código de barras** e consulta o produto no catálogo; não interpreta a composição química a partir de uma foto do rótulo.
- As informações químicas dependem dos produtos, das FDS e dos dados disponíveis no backend. Uma combinação sem avaliação não deve ser apresentada como segura.
- O seletor de misturas inclui produtos e substâncias na interface, mas o contrato atual de análise aceita **pares de produtos**. A presença da aba não significa suporte da API a uma análise com substâncias.
- O chat depende da disponibilidade da API e do agente. Dados de ferramentas e respostas da IA não são produzidos pelo aplicativo Android.

## Como executar

### Pré-requisitos

- **JDK 21**, usado também pelo CI.
- Android Studio com suporte ao Android Gradle Plugin configurado no projeto.
- **Android SDK Platform 36** e ferramentas de compilação instaladas.
- Aparelho ou emulador com **Android 7.0 / API 24 ou superior**.
- Conexão à internet para os fluxos integrados ao backend.

O projeto inclui o Gradle Wrapper; não é necessário instalar o Gradle separadamente. Os valores de SDK ficam em [`plugins/android-commons.gradle`](plugins/android-commons.gradle), e as versões das dependências estão em [`gradle/libs.versions.toml`](gradle/libs.versions.toml).

### Instalação local

```bash
git clone https://github.com/Quimia-br/quimia-mobile.git
cd quimia-mobile
```

No Windows, o projeto oferece um script de preparação do ambiente:

```powershell
.\setup-quimia-mobile.cmd
```

Abra a pasta no Android Studio, sincronize o Gradle, selecione um dispositivo e execute o módulo **app**. Para compilar ou instalar pelo terminal:

```powershell
# Windows — gerar APK de desenvolvimento
.\gradlew.bat :app:assembleDebug

# Windows — instalar no dispositivo conectado
.\gradlew.bat :app:installDebug
```

```bash
# Linux / macOS
chmod +x gradlew
./gradlew :app:installDebug
```

O APK de desenvolvimento é gerado em `app/build/outputs/apk/debug/app-debug.apk`.

## Configuração

### API

A URL base está definida em [`app/build.gradle.kts`](app/build.gradle.kts), no campo `BuildConfig.AUTH_BASE_URL`:

```text
https://quimia.discloud.app/
```

O aplicativo acessa a API via HTTPS. O acesso ao banco de dados e as credenciais dos serviços internos pertencem ao backend; não devem ser incluídos no APK.

### Google Maps

Configure `MAPS_API_KEY` como variável de ambiente ou no arquivo local `local.properties`:

```properties
MAPS_API_KEY=SUA_CHAVE_DO_MAPS_SDK_FOR_ANDROID
```

A variável de ambiente tem prioridade sobre o arquivo local. A chave deve pertencer a um projeto com o Maps SDK for Android habilitado e estar restrita ao pacote `com.quimia.android` e ao certificado de assinatura correspondente. Sem uma chave configurada, o build sinaliza o mapa como não configurado por `GOOGLE_MAPS_CONFIGURED`.

### Login com Google e Firebase

1. Configure o aplicativo Android de pacote `com.quimia.android` no projeto Firebase utilizado pelo Quimia.
2. Habilite o provedor Google e registre as impressões digitais dos certificados usados nas instalações **debug** e **release**.
3. Baixe a configuração atualizada e coloque-a em `app/google-services.json`.
4. Compile novamente o aplicativo para incluir os recursos de configuração gerados pelo plugin Google Services.

O build pode compilar sem esse arquivo para permitir CI e previews, mas isso **não configura o login Google**. O APK distribuído precisa conter a configuração OAuth correta para sua assinatura. O workflow de release verifica essa configuração no APK final.

### Arquivos locais

`local.properties`, `google-services.json`, `keystore.properties` e arquivos de assinatura estão no `.gitignore`. Mantenha valores reais fora de commits, logs e descrições de PR.

## Arquitetura

O projeto é organizado em camadas e por funcionalidade. Repositórios concentram o acesso aos dados, modelos de domínio representam os conceitos do app e telas Compose renderizam os estados da apresentação. Fluxos como autenticação e preferências utilizam ViewModels com estado observável.

```text
quimia-mobile/
├── app/
│   ├── schemas/                         Esquemas exportados do Room
│   └── src/main/java/com/quimia/android/
│       ├── data/                        API, repositórios, sessão e persistência
│       ├── domain/                      Modelos e contratos de domínio
│       └── presentation/
│           ├── auth/                    Estado e coordenação da autenticação
│           ├── base/                    Infraestrutura da apresentação
│           └── screens/                 Telas organizadas por funcionalidade
├── designSystem/                        Componentes e recursos visuais compartilhados
├── docs/                                Convenções e processo de release
├── plugins/                             Configuração Gradle compartilhada
├── tools/                               Utilitários e verificações de build
└── .github/workflows/                    Integração contínua e distribuição
```

### Padrões aplicados

| Padrão / organização | Exemplo no código | Finalidade |
| --- | --- | --- |
| **Repository** | [`AuthRepository`](app/src/main/java/com/quimia/android/data/auth/AuthRepository.kt), [`CatalogRepository`](app/src/main/java/com/quimia/android/data/catalog/CatalogRepository.kt) | Concentrar operações de dados e tratamento dos contratos consumidos pela apresentação. |
| **ViewModel e estado observável** | [`AuthViewModel`](app/src/main/java/com/quimia/android/presentation/auth/AuthViewModel.kt), [`ProfilePreferencesViewModel`](app/src/main/java/com/quimia/android/presentation/screens/profile/ProfilePreferencesViewModel.kt) | Coordenar ações e expor estados para a interface Compose. |
| **Design System compartilhado** | [`designSystem`](designSystem/src/main/java) | Reutilizar componentes, temas e padrões de feedback entre telas. |
| **DAO e migrações Room** | [`QuimiaDatabase`](app/src/main/java/com/quimia/android/data/local/QuimiaDatabase.kt) | Persistir preferências estruturadas e evoluir o banco local por migrações. |

### Onde os dados ficam

| Dados | Armazenamento |
| --- | --- |
| Conta, catálogo, estante, cômodos e análises | Backend, acessado pelos repositórios da API. |
| Preferências de acessibilidade e notificações | Room/SQLite, no banco local `quimia.db`. |
| Tokens da sessão | Arquivo criptografado com AES-GCM e chave no Android Keystore, por [`AndroidSessionStore`](app/src/main/java/com/quimia/android/data/auth/AndroidSessionStore.kt). |

O uso de Firebase no login Google não implica armazenamento do catálogo ou das preferências no Firebase. A persistência relacional local deste projeto é implementada com **Room sobre SQLite**.

## Requisitos da disciplina

Os links abaixo levam diretamente às implementações no repositório.

| Demanda | Evidência no código |
| --- | --- |
| **Controle de acesso** | [`AuthRepository`](app/src/main/java/com/quimia/android/data/auth/AuthRepository.kt) e [`AndroidSessionStore`](app/src/main/java/com/quimia/android/data/auth/AndroidSessionStore.kt). |
| **Recurso de hardware — câmera** | [`BarcodeCameraPreview`](app/src/main/java/com/quimia/android/presentation/screens/catalog/BarcodeCameraPreview.kt). |
| **Integração e consumo de API** | [`CatalogRepository`](app/src/main/java/com/quimia/android/data/catalog/CatalogRepository.kt) e [`QuimiaAuthApi`](app/src/main/java/com/quimia/android/data/auth/QuimiaAuthApi.kt). |
| **Persistência — SQLite / Room** | [`QuimiaDatabase`](app/src/main/java/com/quimia/android/data/local/QuimiaDatabase.kt) e [`RoomProfilePreferencesRepository`](app/src/main/java/com/quimia/android/data/preferences/RoomProfilePreferencesRepository.kt). |
| **Chatbot** | [`KemiScreen`](app/src/main/java/com/quimia/android/presentation/screens/kemi/KemiScreen.kt) e [`KemiRepository`](app/src/main/java/com/quimia/android/data/kemi/KemiRepository.kt). |
| **Configuração de perfil e avatar** | [`ProfileRoute`](app/src/main/java/com/quimia/android/presentation/screens/profile/ProfileRoute.kt). |
| **Notificações do aplicativo** | [`QuimiaNotifications`](app/src/main/java/com/quimia/android/data/notifications/QuimiaNotifications.kt) e [`ShelfReminderWorker`](app/src/main/java/com/quimia/android/data/notifications/ShelfReminderWorker.kt). |
| **Apresentação em celular e tablet** | Adaptações em [`ShelfScreen`](app/src/main/java/com/quimia/android/presentation/screens/shelf/ShelfScreen.kt) e componentes do [`Design System`](designSystem/src/main/java). |
| **GPS do dispositivo** | [`NearbyLocationController`](app/src/main/java/com/quimia/android/presentation/screens/proximity/NearbyLocationController.kt). |

## Tecnologias

| Responsabilidade | Tecnologias |
| --- | --- |
| Linguagem e interface | Kotlin, Jetpack Compose e Material 3 |
| Estado e execução assíncrona | ViewModel, StateFlow e corrotinas |
| Rede e serialização | Retrofit, OkHttp e Kotlinx Serialization |
| Imagens | Coil |
| Persistência local | Room, SQLite e KSP |
| Autenticação Google | Firebase Auth e Google Sign-In |
| Câmera e código de barras | CameraX e ZXing |
| Mapas | Google Maps SDK for Android |
| Notificações e agendamento | Android Notifications e WorkManager |
| Testes | JUnit, Robolectric, Compose UI Test e MockWebServer |
| Build e entrega | Gradle Kotlin DSL e GitHub Actions |

## Validação e testes

Execute os mesmos comandos centrais utilizados pelo CI:

```powershell
.\gradlew.bat test lint
.\gradlew.bat assembleDebug
```

Em Linux/macOS, substitua `.\gradlew.bat` por `./gradlew`. Relatórios são gerados nas pastas `build/reports` dos módulos; o CI também os disponibiliza como artifacts.

Para conferir os fluxos integrados, use uma conta de teste e valide no aparelho: login/restauração de sessão, consulta do catálogo, leitura de um EAN cadastrado, inclusão na estante, criação de cômodo, análise de mistura, conversa com Kemi e uso do mapa. Câmera, localização e notificações dependem das permissões concedidas pelo usuário. Confira também teclado aberto, temas e tamanhos de texto em celular e tablet.

## Build, CI e distribuição

Para instalar uma versão publicada, acesse as [Releases do Quimia Mobile](https://github.com/Quimia-br/quimia-mobile/releases). O APK de release é assinado e utiliza otimização de código e recursos.

| Workflow | Responsabilidade |
| --- | --- |
| [`Android CI`](.github/workflows/android-ci.yml) | Executa testes e lint, gera o APK debug e disponibiliza relatórios. |
| [`PR Quality`](.github/workflows/pr-quality.yml) | Valida o título Conventional Commits e o padrão de nome da branch. |
| [`Release APK`](.github/workflows/release-apk.yml) | Compila o APK assinado, verifica assinatura e configuração Google e disponibiliza o artifact. Em tags `v*`, publica também uma GitHub Release. |

O workflow de release aceita execução manual com `version_name` e `version_code`. A versão também pode ser informada no build pelas propriedades Gradle `versionName` e `versionCode` ou pelas variáveis `ANDROID_VERSION_NAME` e `ANDROID_VERSION_CODE`. O código de versão deve aumentar a cada atualização distribuída.

A configuração da distribuição utiliza os secrets `KEYSTORE_BASE64`, `KEYSTORE_PASSWORD`, `KEY_ALIAS`, `KEY_PASSWORD` e `GOOGLE_SERVICES_JSON`; o mapa utiliza `MAPS_API_KEY`. O procedimento de assinatura e publicação está em [`docs/release-apk.md`](docs/release-apk.md).

Para avaliar desempenho em um dispositivo conectado ao Android Studio, existe também a variante `performance`: ela utiliza as otimizações de release, não é depurável e é assinada com a chave debug para permitir a atualização de uma instalação de desenvolvimento compatível.

## Contribuição

Use uma branch no formato `tipo/ticket`, por exemplo `fix/QUI-identificador`, e um título de PR em Conventional Commits, como `fix(auth): corrige restauração da sessão`. Descreva a mudança, os passos de validação e as evidências relevantes no [template de pull request](.github/pull_request_template.md).

- [Padrão de branches e pull requests](docs/branch-e-pull-request-padrao.md)
- [Padrão de Conventional Commits](docs/conventional-commits-padrao.md)
- [Documentação de design](docs/design)
- [Issues do projeto](https://github.com/Quimia-br/quimia-mobile/issues)

## Ecossistema Quimia

| Repositório | Responsabilidade |
| --- | --- |
| [quimia-mobile](https://github.com/Quimia-br/quimia-mobile) | Aplicativo Android e Design System. |
| [quimia-api](https://github.com/Quimia-br/quimia-api) | Contratos e serviços consumidos pelo aplicativo. |
| [quimia-agent](https://github.com/Quimia-br/quimia-agent) | Agente e ferramentas da assistente Kemi. |
| [quimia-bd](https://github.com/Quimia-br/quimia-bd) | Estrutura e recursos do banco de dados. |

## Licença

Distribuído sob a [licença MIT](LICENSE). Desenvolvido pela equipe [Quimia](https://github.com/Quimia-br).
