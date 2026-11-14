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
- Fotos opcionais ilimitadas por descoberta em `assets/lux_menu/textures/gui/loading/slide_01.png`…`slide_08.png`; falta usuário subir fotos pelo pacote ZIP.
- Transição suave e `CARREGANDO...` animado; agora randomização sem repetição até completar rodadas. A matemática de embaralhamento foi exercitada em Java 21 isoladamente, **não** compilada nem testada no NeoForge 1.21.1.
- Guia: `docs/33-LUX-LOADING-SLIDESHOW.md`; checklist estendido em `docs/30-CHECKLIST-DE-TESTES.md`.

## Clean Fantasy v3 — refinamentos do playtest (24/10/2026)

A partir dos prints do usuário:
- **Lux HUD:** removidos rótulos redundantes VIDA/FOME/XP, retrato alinhado ao bloco de três barras iguais (vida, fome, progresso XP); nível permanece no cabeçalho.
- **Missões:** novo recurso PNG pixel-art `quest_scroll.png`, bordas com respiro, subtítulo reposicionado.
- **Hotbar:** duas novas texturas PNG transparentes em `assets/lux_hud/textures/gui/hotbar`, com detalhes lilás/champagne e destaque do slot selecionado. Render extra acima da hotbar vanilla, sem cancelá-la nem interceptar itens/controles; `decorateHotbar=true` configurável, modo CLASSIC ignora a decoração.
- **Lux Menu:** tratamento localizado do botão `Mods` de NeoForge, mesmo que seja subclass de `Button`, sem estilizar outras subclasses indiscriminadamente.

**Evidência:** fontes publicadas e os três PNGs verificados pela assinatura PNG, dimensões, CRC32 e fechamento de chunks. **Ainda falta build e teste em Minecraft após esta mudança.** O visual anterior dos cinco mods e slideshow foi confirmado em screenshots, mas não garante estas mudanças novas.

Guia de layout e testes: `docs/35-LUX-CLEAN-FANTASY-V3.md` + seção 4B de `docs/30-CHECKLIST-DE-TESTES.md`.

## Próximas fases

1. Revisar compatibilidade compile/API do Lux Menu e dos botões temáticos; teste conjunto dos cinco módulos quando o usuário estiver disponível.
2. Depois do teste do modo escuro nativo, estudar implementação **segura e isolável** de um provider do FML 1.21.1 para a arte roxa do Lux Boot (boot antecipado); não tratá-lo como `TitleScreen`.
3. Concluir Lux Atlas Mapa `M` e depois Lux Quests, conectando dados verdadeiros ao quadro de missões; respeitar módulos independentes.
4. Dedicated server e testes multiplayer com time scoreboard (aproximação do sistema futuro Lux Party).

**Nota:** a imagem-conceito aprovada do Lux HUD é referência de design, não comprovação de recursos ainda ausentes como missões reais ou Lux Party completo.

## Próxima arquitetura — tela vermelha FML e todos os loadings

Ver `docs/34-LUX-LOADING-TODAS-AS-ETAPAS.md`. Existe um mod de provedor inicial compatível com 1.21.1, mas não foi instalado nem conectado ao nosso slideshow. A janela vermelha inicial e overlay Mojang ainda não foram substituídos. Kit de fotos V2 aceita quantidade sem limite numérico fixo, mas a RAM/VRAM seguem limitadas.
