# Lux Mods — Estado verificável da implementação

**Atualizado:** 24/10/2026 • PR #14 • branch `feat/engine-discovery-bootstrap` (DRAFT).

## 5 módulos no código (Minecraft 1.21.1 / NeoForge 21.1.264)

| Mod | Funcionalidade | Evidência |
|---|---|---|
| Lux Engine | serviços/eventos/tema visual compartilhado | carregou no Minecraft do usuário; lógica pura testada antes |
| Lux Discovery | anúncio de biomas, persistência por jogador | **validado em singleplayer**: primeiro anúncio, relog sem duplicar, novo bioma com anúncio |
| Lux Atlas | bússola com N/L/S/O e estilo Clean Fantasy v2 | **validado visualmente** em singleplayer; ainda falta giro 360° e escalas |
| Lux HUD | skin, barras de vida/fome/XP, quadro de missões, efeitos opcionais, cartões dos colegas reais | **carregamento e visual v2 aprovados** em screenshots; efeitos/multiplayer não exercitados ainda |
| Lux Menu | menus título/pausa + botões simples em opções/mundos/multiplayer | **código publicado; NÃO compilado nem testado in-game após adicionar quinto mod** |

## Estado do build / entrega

- A instalação automática do **pacote de 4 mods** funcionou em 24/10/2026, com `BUILD SUCCESSFUL` comprovado pelo usuário e screenshots in-game.
- Após criar Lux Menu, `settings.gradle`, `build.gradle`, `assembleTestPack`, `scripts/install-to-sklauncher.ps1` e `scripts/verify_jars.py` foram adaptados para **5 JARs**, mas **nenhum build real da revisão com 5 mods foi confirmado**.
- GitHub Actions teve incidentes `startup_failure` sem jobs (issue #15); não presumir CI verde.
- Não alterar `main`, não concluir PR #14, não publicar release antes do teste integrado e dedicated server.
- O usuário prefere **fazer todos os testes em uma sessão posterior**, não solicitar teste em cada nova atualização.
- O **checklist único de 81 verificações** está em `docs/30-CHECKLIST-DE-TESTES.md`. Há também scripts locais `scripts/TESTAR-PACOTE-LUX.bat` e `scripts/reunir-evidencias.ps1` que inventariam os JARs sem alterar o mundo ou enviar logs.

## Experimento de boot (sem teste)

- `docs/32-LUX-BOOT-CARREGAMENTO.md` documenta o estudo da janela vermelha NeoForge e a incompatibilidade de versões entre loaders.
- `scripts/lux-boot-theme.ps1` revisado: modo opt-in altera **somente options.txt** da instância `teste`, definindo `darkMojangStudiosBackground:true`. O FancyModLoader 1.21.1 suporta diretamente esse modo de fundo preto, preservando a janela antecipada e as barras de progresso reais, sem alterar earlyWindowControl. A ferramenta tem Preview, Apply e Restore com backup local. **Não faz parte do atualizador, não está ativada nem testada no jogo**.
- O tema completo de loading Lux ainda **não está implementado**: o protótipo visual interativo em `experimental/lux-boot-preview/index.html` reproduz apenas o visual com animação de progresso **explicitamente simulada**. Não está vinculado ao boot FML nem aos JARs.
- O protótipo mantém referência de dimensões 854x480, paleta própria em `theme.json` e três controles/estados de simulação para avaliação futura.


## Slideshow de fotos no carregamento de mundos (novo, pendente)

- Lux Menu agora contém `LuxLoadingSlideshow`, `SlideTiming` e opções `loadingSlideshow`, `slideSeconds`, `fadeMillis`.
- Carregamento de mundos via telas normais (`LevelLoadingScreen`, `ReceivingLevelScreen`, `ProgressScreen`); **NÃO** altera tela vermelha inicial do FML nem `LoadingOverlay` da Mojang.
- Até 8 fotos opcionais em `assets/lux_menu/textures/gui/loading/slide_01.png`…`slide_08.png`; falta usuário subir fotos pelo pacote ZIP.
- Transição suave e `CARREGANDO...` animado; sem porcentagem fictícia. A matemática foi exercitada em Java 21 isoladamente, **não** compilada nem testada no NeoForge 1.21.1.
- Guia: `docs/33-LUX-LOADING-SLIDESHOW.md`; checklist estendido em `docs/30-CHECKLIST-DE-TESTES.md`.

## Próximas fases

1. Revisar compatibilidade compile/API do Lux Menu e dos botões temáticos; teste conjunto dos cinco módulos quando o usuário estiver disponível.
2. Depois do teste do modo escuro nativo, estudar implementação **segura e isolável** de um provider do FML 1.21.1 para a arte roxa do Lux Boot (boot antecipado); não tratá-lo como `TitleScreen`.
3. Concluir Lux Atlas Mapa `M` e depois Lux Quests, conectando dados verdadeiros ao quadro de missões; respeitar módulos independentes.
4. Dedicated server e testes multiplayer com time scoreboard (aproximação do sistema futuro Lux Party).

**Nota:** a imagem-conceito aprovada do Lux HUD é referência de design, não comprovação de recursos ainda ausentes como missões reais ou Lux Party completo.
