# Proximidade — referência visual

Ticket: `1dfbyc93n2q`.

Figma: [Proximidade](https://www.figma.com/design/G3uKvQ50skcAwYzGPuPkku/App-Componentes?node-id=1332-4287).

## Recursos locais

| Camada Figma | Recurso | Uso e geometria |
| --- | --- | --- |
| `1332:5397` | `drawable-nodpi/proximity_map_figma.png` | Imagem original usada somente nos previews do editor, sem rede, GPS ou inicialização do SDK nativo. O app em execução usa mapa real. |
| Coração em `1618:5096` | `assets/figma/heart_filled.svg` e `drawable-nodpi/figma_heart_filled.png` | SVG original, rasterizado sem alteração de desenho; exibido em 20 dp dentro do botão de 45 dp. |
| Chevron, lápis, X e map-pinned | Biblioteca Lucide já usada pelo projeto | Mesmos ícones nomeados na referência, com cores e dimensões dos componentes existentes. |

## Mapa e GPS em execução

O app usa o Google Maps SDK for Android 20.1.0, em modo híbrido: satélite com ruas e nomes, como a referência. O mapa permite zoom, arraste e seleção dos pontos. Os marcadores circulares usam a cor do design system e o ponto selecionado recebe um halo. A área útil do mapa reserva espaço para os filtros, o cartão selecionado e a gaveta inferior, sem esconder o logotipo e os créditos nativos do Google.

### Configurar a chave

No arquivo `local.properties` da raiz do projeto, preencha a propriedade abaixo, mantendo a configuração `sdk.dir` já existente:

```properties
MAPS_API_KEY=COLE_SUA_CHAVE_AQUI
```

Esse arquivo é ignorado pelo Git. Não coloque a chave nos arquivos Kotlin, no manifesto, no catálogo de versões ou na documentação. Em integração contínua, a variável de ambiente `MAPS_API_KEY` tem prioridade sobre a configuração local. Depois de preencher a chave, sincronize o projeto, compile e reinstale o app. Sem chave, a tela mostra uma orientação de configuração no debug, sem inicializar o mapa; em release, mostra indisponibilidade genérica.

No Google Cloud:

1. Habilite **Maps SDK for Android** no mesmo projeto da chave e confira a conta de faturamento vinculada.
2. Restrinja a chave a **Apps Android**, com o pacote `com.quimia.android` e a impressão digital SHA-1 do certificado usado no APK. Cadastre debug e release conforme os ambientes utilizados.
3. Restrinja as APIs permitidas a **Maps SDK for Android**.

Para obter os certificados locais, execute `./gradlew :app:signingReport` no terminal do projeto. Não compartilhe senhas nem arquivos de assinatura. Uma chave Android faz parte do APK; ignorar o arquivo local evita vazamento no repositório, mas não substitui as restrições no Cloud.

Referências oficiais: [configuração do SDK](https://developers.google.com/maps/documentation/android-sdk/config), [configuração do projeto Cloud](https://developers.google.com/maps/documentation/android-sdk/cloud-setup) e [restrições de segurança](https://developers.google.com/maps/api-security-best-practices).

O botão de localização solicita permissões de primeiro plano do Android. Localização aproximada também é aceita. Uma leitura atual é obtida ao entrar/retornar à tela com permissão já concedida ou ao tocar no botão, com cancelamento ao sair e limite de 20 segundos. Não há rastreamento em segundo plano nem persistência/envio das coordenadas para o backend do Quimia. O provedor de mapas recebe as requisições de blocos da região visualizada, como em qualquer mapa online.

Há estados de chave ausente, Google Play Services indisponível, carregamento/erro do mapa com tentativa novamente, permissão negada/bloqueada, localização desativada e posição indisponível. Acesso às configurações só ocorre por ação explícita. Com a chave configurada e sem permissão de localização, o mapa continua navegável a partir de uma região inicial de São Paulo; essa região não é apresentada como a posição do aparelho. A câmera pode ser deslocada livremente e só recentraliza ao obter uma nova leitura ou selecionar um ponto. A fonte de localização do mapa usa a leitura controlada pelo app, sem iniciar um provedor contínuo adicional.

Os pontos, endereços e distâncias de demonstração continuam identificados como exemplos; suas coordenadas são apenas dados de teste, não estabelecimentos verificados nem resultados reais próximos ao GPS. Consulta de CEP, rotas externas e persistência no backend continuam pendentes.

## Estados

- Endereço aberto/recolhido, filtros Descarte/Compra e ponto selecionado.
- Gaveta de pontos salvos, com abertura por toque ou arrasto para cima; sem botões extras no rodapé que não existem na referência.
- Formulário de endereço com os campos visíveis no Figma, seleção de UF e validação local.
- Previews de celular compacto, celular, tablet e formulário com fonte ampliada.

Os detalhes de estado e dimensões foram lidos dos quadros `1332:5396`, `1415:3442`, `1618:5096` e `1618:5584`. A barra de status permanece nativa do Android; o componente iOS do protótipo não foi desenhado sobre ela.
