# Lux Atlas — áreas parcialmente exploradas (14/11/2026)

## Implementação
- `AtlasLevelOfDetail.Cell` agora contém **cor, amostras conhecidas e área total**.
- Ao fazer zoom, cada célula de 2/4/8/16 blocos distingue terreno totalmente observado de trechos parcialmente mapeados.
- Dados LXT1 antigos (2×2) representam quatro posições; amostras modernas (1×1) substituem apenas suas respectivas posições, sem contagem duplicada.
- Não é gerada cor para células sem amostras. Áreas parcialmente registradas não são desenhadas como um quadrado inteiramente conhecido: a interface usa um padrão de um a quatro pixels e atenua os pixels com cobertura ínfima.
- Legenda "EXPLORACAO / Completa / Parcial" adicionada ao Atlas, mantendo navegação via tecla M e sem minimapa.

## Verificação
- `scripts/src/LuxAtlasLodSmoke.java` ampliado: cobertura parcial, total, coordenadas negativas, desconhecido, migração antiga e sobreposição de dados.
- Em sessão local isolada foi executado um **smoke test Java 21 reproduzindo o código de AtlasViewport/AtlasLevelOfDetail**, com resultado `LUX ATLAS LOD: smoke OK`. Isso NÃO equivale à execução do pipeline GitHub, nem valida integração NeoForge.
- **Ainda não houve build dos JARs nem teste in-game**; backup do mundo e pasta `lux-atlas` continua recomendado.

## Próximo passo
- Verificar integração visual/GUI NeoForge 1.21.1 em diversos zooms e resoluções.
- Instrumentar leitura/gravação das regiões; reduzir IO síncrono.
- Refinar a precisão visual de cobertura em zoom distante (uma célula 2×2 pixels só admite níveis visuais aproximados; a contagem numérica é exata).
