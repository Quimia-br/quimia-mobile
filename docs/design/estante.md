# Estante — referência visual

Ticket: `1dfbyc93n2q`.

Figma: [Estante](https://www.figma.com/design/G3uKvQ50skcAwYzGPuPkku/App-Componentes?node-id=1130-3319).

Quadros conferidos: produtos `1130:4377`, substâncias `1329:2786`, ações `1131:6965` e informações `1131:7140`.

## Recursos locais

| Camada | Recurso | Geometria |
| --- | --- | --- |
| Avatar do produto e imagem das informações | `drawable-nodpi/shelf_mr_musculo_figma.png` | Original 1000 × 1000. Avatar circular de 45 dp com Crop. Nas informações, janela 147 × 172 dp com o espelhamento, escala horizontal 156,4% e deslocamento da referência. |
| Estrela preenchida dos produtos | `assets/figma/star_filled.svg` e `drawable-nodpi/figma_star_filled.png` | SVG original rasterizado sem redesenho, exibido em 20 dp no botão de 45 dp. |
| Coração preenchido das substâncias | `assets/figma/heart_filled.svg` e `drawable-nodpi/figma_heart_filled.png` | SVG original rasterizado sem redesenho, exibido em 20 dp no botão de 45 dp. |
| Bubbles das abas | `assets/figma/bubbles.svg` e `drawable-nodpi/figma_bubbles.png` | Original 20 × 20 do quadro `1329:2786`, exibido em 20 dp. |
| Demais ícones | Biblioteca Lucide existente | Mesmos ícones nomeados no Figma, incluindo Atom nas misturas e Bath nos cômodos. |

São reutilizados QuimiaSectionTitle, QuimiaCard, QuimiaListItem, QuimiaChip, QuimiaSearchBar, QuimiaIconButton, QuimiaButton, QuimiaImage, QuimiaShapeIcon, QuimiaMenuBar e QuimiaFAB. Os controles inferiores mantêm os tamanhos solicitados posteriormente pelo usuário: menu de 84 dp, ícones de 30 dp e FAB de 72 dp.

## Estados e limites

Produtos, substâncias, filtros, favoritos, expansão/recolhimento, ações, informações e confirmações usam dados locais de demonstração. O texto `Description` do protótipo não é uma orientação de segurança validada. Misturas e cômodos são encaminhados pelos callbacks da navegação; nenhuma integração nova de backend foi adicionada aqui.

Previews incluem 320 dp, celulares maiores e tablets em retrato/paisagem. Em cartões estreitos, o botão Misturar passa para uma linha própria, sem reduzir os alvos de toque. Informações reutilizam a descrição expansível após cinco linhas, conforme a personalização já solicitada.
