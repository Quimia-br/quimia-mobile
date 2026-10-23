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

O app usa MapLibre Native Android 13.6.1 (OpenGL ES) e o estilo público [Liberty do OpenFreeMap](https://openfreemap.org/quick_start/). Não exige chave de API nem conta de cobrança. O mapa de ruas permite zoom e arraste; não é uma imagem de satélite. Os créditos OpenFreeMap/OpenMapTiles/OpenStreetMap permanecem visíveis e acessíveis.

O botão de localização solicita permissões de primeiro plano do Android. Localização aproximada também é aceita. Uma leitura atual é obtida ao entrar/retornar à tela com permissão já concedida ou ao tocar no botão, com cancelamento ao sair e limite de 20 segundos. Não há rastreamento em segundo plano nem persistência/envio das coordenadas para o backend do Quimia. O provedor de mapas recebe as requisições de blocos da região visualizada, como em qualquer mapa online.

Há estados de carregamento/erro do mapa com tentativa novamente, permissão negada/bloqueada, localização desativada e posição indisponível. Acesso às configurações só ocorre por ação explícita. Sem permissão, o mapa continua navegável a partir de uma região inicial de São Paulo; essa região não é apresentada como a posição do aparelho. A câmera pode ser deslocada livremente e só recentraliza ao obter uma nova leitura ou selecionar um ponto.

Os pontos, endereços e distâncias de demonstração continuam identificados como exemplos; suas coordenadas são apenas dados de teste, não estabelecimentos verificados nem resultados reais próximos ao GPS. Consulta de CEP, rotas externas e persistência no backend continuam pendentes.

## Estados

- Endereço aberto/recolhido, filtros Descarte/Compra e ponto selecionado.
- Gaveta de pontos salvos, com abertura por toque ou arrasto para cima.
- Formulário de endereço com os campos visíveis no Figma, seleção de UF e validação local.
- Previews de celular compacto, celular, tablet e formulário com fonte ampliada.

Os detalhes de estado e dimensões foram lidos dos quadros `1332:5396`, `1415:3442`, `1618:5096` e `1618:5584`. A barra de status permanece nativa do Android; o componente iOS do protótipo não foi desenhado sobre ela.
