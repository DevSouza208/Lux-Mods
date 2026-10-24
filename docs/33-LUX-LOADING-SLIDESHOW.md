# Lux Loading — Slideshow de imagens aleatórias v2

**Status:** Java de descoberta aleatória publicado; ZIP `Lux_Mods_Slideshow_Aleatorio_v2.zip` gerado para adicionar imagens. Ainda sem build NeoForge + playtest desta versão. PR #14 draft.

## Comportamento solicitado

- Sem limite de **oito** imagens: o Lux Menu procura dinamicamente todos os recursos `lux_menu:textures/gui/loading/slide_<numero>.png` presentes no mod. O upload renomeia imagens para `slide_0001.png`, `slide_0002.png`, …
- Antes de cada carregamento normal de mundo, embaralha uma lista de todas as imagens disponíveis e as reproduz uma vez cada, sem repetir na mesma rodada.
- Prepara antecipadamente a ordem da próxima rodada e evita imagens idênticas na fronteira entre rodadas. A última foto faz crossfade sem corte para a primeira da próxima rodada.
- Por padrão, a imagem aparece por 5 segundos e faz fade de 1,2 segundo. Se o mundo carregar antes, o Minecraft fecha a tela e o slideshow para **imediatamente**.
- `CARREGANDO...` anima os pontos a cada 500ms sem inventar porcentagens de progresso.
- Fotos dimensionadas proporcionalmente a qualquer janela, com recorte central se necessário; com zero imagens, mantém carregamento vanilla.
- Configuração CLIENT: `loadingSlideshow = true`, `slideSeconds = 5`, `fadeMillis = 1200`.

## Onde aparece de verdade

O módulo Lux Menu atual cobre **telas `Screen` de carregamento de mundo**: `LevelLoadingScreen`, `ReceivingLevelScreen`, `ProgressScreen`. Outras etapas (tela vermelha FancyModLoader/FML, Mojang LoadingOverlay, reload de resource pack, transição de dimensão/respawn sem Screen de loading) são processos distintos e **NÃO estão garantidos** pelo mesmo handler. Isso não significa que estão impossíveis; significa que exigem integrações adicionais. Ver `docs/34-LUX-LOADING-TODAS-AS-ETAPAS.md`.

## Pacote de upload v2

Baixe `Lux_Mods_Slideshow_Aleatorio_v2.zip`, extraia e coloque quaisquer fotos JPG/JPEG/PNG em `FOTOS-AQUI/`. `PREVIA-FOTOS.html` simula a galeria no navegador (ordem aleatória sem repetição); `ENVIAR-FOTOS-GITHUB.bat` organiza, converte para PNG 1920x1080 e só envia após confirmação `S`. O arquivo `ENVIAR-FOTOS.ps1` não altera fotos originais, saves nem outros JARs; trabalha na branch `feat/engine-discovery-bootstrap`.

**Atenção ao desempenho:** ausência de um limite numérico não significa memória infinita. Fotos a 1920x1080 podem ocupar cerca de 8MB cada decodificadas em RGBA na GPU; muitas dezenas podem pressionar VRAM e aumentar o tamanho do JAR. Recomendação de início: 15–30 imagens, observar o consumo e ajustar. O mod não mantém o mundo esperando para completar a lista.

## Testes

Smoke isolado Java 21 executado em 24/10/2026: 250 fotos em 500 rodadas, sem repetição por rodada/fronteira. Ainda não confirma compatibilidade integral com a API NeoForge ou rendering de textura. Checklist completo em `docs/30-CHECKLIST-DE-TESTES.md`.
