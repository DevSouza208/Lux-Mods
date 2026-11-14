# Lux Atlas — rodada de estabilidade da navegação (14/11/2026)

## Alterações realmente enviadas

- `AtlasViewport.java`: matemática única para amostras de terreno, cursor, posição do jogador e marcadores. Usa arredondamento coerente inclusive para X/Z negativos.
- `AtlasMapScreen.java`: renderização e cliques agora usam a mesma origem de viewport. O zoom centrado no cursor continua preservado.
- `AtlasTerrainCache.java`: nova gravação com escopo de região/dimensão capturado quando o mundo está ativo, inclusive em transições de dimensão.
- `AtlasWalkDiscovery.java`: tenta salvar antes de trocar de mundo; amostras que falham na escrita ficam numa fila de reenvio durante a sessão, segregadas pelo diretório original, sem serem misturadas ao próximo mundo.
- `AtlasViewportTest.java`: testes JUnit de coordenadas negativas, limite de região, células de mapa e zoom.
- `scripts/src/LuxAtlasViewportSmoke.java`: smoke test Java puro do sistema de coordenadas, agora incluído em `scripts/smoke-pure-java.sh`.

## Limites e validação

Ainda não há compilação NeoForge 1.21.1 concluída nesta rodada. Os testes foram **adicionados**, não anunciados como executados. O pipeline existente pode falhar sem sequer iniciar os jobs de Gradle. Backups de `lux-atlas/` e do mundo continuam obrigatórios antes de testar atualizações de armazenamento.

Os arquivos da região persistidos dependem de amostras confirmadas de chunks carregados; não existe minimapa, nem revelação automática de terrenos não explorados. Ainda faltam gerenciamento de IO assíncrono, mapas subterrâneos, LOD renderizador otimizado, minimização de repintura e validação de centenas de milhares de regiões. O objetivo de superar JourneyMap ainda é uma meta de produto, não um resultado validado.

**Regressões obrigatórias:** teste de X/Z negativos, dimensão/portal, world exit, mapa fechado/aberto, aproximação do cursor, ícones alinhados e persistência ao navegar para áreas antigas.
