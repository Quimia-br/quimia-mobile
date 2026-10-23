# Proximidade — referência visual

Ticket: `1dfbyc93n2q`.

Figma: [Proximidade](https://www.figma.com/design/G3uKvQ50skcAwYzGPuPkku/App-Componentes?node-id=1332-4287).

## Recursos locais

| Camada Figma | Recurso | Uso e geometria |
| --- | --- | --- |
| `1332:5397` | `drawable-nodpi/proximity_map_figma.png` | Imagem original do mapa; `QuimiaImage` com `ContentScale.Crop`, sem editar o arquivo. No quadro 360 × 800, o mapa ocupa 360 × 475 com endereço aberto e 360 × 584 com endereço recolhido. |
| Coração em `1618:5096` | `assets/figma/heart_filled.svg` e `drawable-nodpi/figma_heart_filled.png` | SVG original, rasterizado sem alteração de desenho; exibido em 20 dp dentro do botão de 45 dp. |
| Chevron, lápis, X e map-pinned | Biblioteca Lucide já usada pelo projeto | Mesmos ícones nomeados na referência, com cores e dimensões dos componentes existentes. |

O mapa é uma imagem estática da referência, não um serviço de mapas. Os marcadores e endereços continuam demonstrativos. GPS, consulta de CEP, rotas externas e persistência no backend não estão conectados.

## Estados

- Endereço aberto/recolhido, filtros Descarte/Compra e ponto selecionado.
- Gaveta de pontos salvos, com abertura por toque ou arrasto para cima.
- Formulário de endereço com os campos visíveis no Figma, seleção de UF e validação local.
- Previews de celular compacto, celular, tablet e formulário com fonte ampliada.

Os detalhes de estado e dimensões foram lidos dos quadros `1332:5396`, `1415:3442`, `1618:5096` e `1618:5584`. A barra de status permanece nativa do Android; o componente iOS do protótipo não foi desenhado sobre ela.
