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
- O **checklist único de 70 verificações** está em `docs/30-CHECKLIST-DE-TESTES.md`. Há também scripts locais `scripts/TESTAR-PACOTE-LUX.bat` e `scripts/reunir-evidencias.ps1` que inventariam os JARs sem alterar o mundo ou enviar logs.

## Próximas fases

1. Revisar compatibilidade compile/API do Lux Menu e dos botões temáticos; teste conjunto dos cinco módulos quando o usuário estiver disponível.
2. Estudar modificação **segura e isolável** da tela vermelha de inicialização do NeoForge (boot antecipado); não tratá-la como `TitleScreen`.
3. Concluir Lux Atlas Mapa `M` e depois Lux Quests, conectando dados verdadeiros ao quadro de missões; respeitar módulos independentes.
4. Dedicated server e testes multiplayer com time scoreboard (aproximação do sistema futuro Lux Party).

**Nota:** a imagem-conceito aprovada do Lux HUD é referência de design, não comprovação de recursos ainda ausentes como missões reais ou Lux Party completo.
