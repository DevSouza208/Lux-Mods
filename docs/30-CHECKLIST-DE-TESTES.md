# Lux Mods — Checklist integrado do próximo playtest

> **Teste concentrado — NÃO executar agora.** Atualizado em 24/10/2026. Instância exclusiva `teste` do SKlauncher: Minecraft Java **1.21.1**, NeoForge **21.1.264**, JDK **21**.
>
> A PR #14 é **DRAFT**. Marque `[x]` apenas após comprovar o resultado em build/log/screenshot/jogo. *Código publicado* não é *feature validada*.

## Legenda de evidência

- ✅ [x] — resultado já confirmado pelo usuário em sessão anterior, com ressalvas específicas.
- ☐ [ ] — pendente de teste, mesmo que já exista código.
- ⚠️ Falhou — registrar erro e log, **não marcar [x]**.

## 0 — Build, integridade e rollback (PRIORIDADE CRÍTICA)

- [ ] 00.01 Fechar o Minecraft antes de instalar.
- [ ] 00.02 Abrir o `Atualizar Lux Mods.bat` já existente na área de trabalho e conferir `BUILD SUCCESSFUL`.
- [ ] 00.03 Confirmar **cinco** JARs na instância `teste/mods`: Engine, Discovery, Atlas, HUD e **Menu**; sem JAR antigo duplicado.
- [ ] 00.04 Abrir `scripts/reunir-evidencias.ps1` ou `scripts/TESTAR-PACOTE-LUX.bat` (gera inventário SHA256 local; não envia arquivos).
- [ ] 00.05 Abrir Minecraft e confirmar 5 mods no menu Mods, todos `State: loaded`.
- [ ] 00.06 Confirmar nenhum crash durante loading, render, abertura de mundo ou retorno ao título.
- [ ] 00.07 Verificar configurações CLIENT nos arquivos `lux_hud-client.toml`, `lux_atlas-client.toml`, `lux_menu-client.toml`.
- [ ] 00.08 Conferir existência do backup em `%APPDATA%\.sklauncher\instances\teste\lux-mods-backup` caso precise reverter.
- [ ] 00.09 Confirmar que o script de instalação não tocou em outra instância nem no save pessoal.

## 1 — Lux Engine (infraestrutura)

- [x] 01.01 Carregamento do `lux_engine` no cliente confirmado em 24/10, com screenshot do menu Mods (build anterior).
- [ ] 01.02 Carregamento do `lux_engine` na versão atual de 5 JARs.
- [ ] 01.03 Nenhuma textura de quadrado roxo/preto (missing texture) no HUD, menu ou bússola.
- [ ] 01.04 Recompilar com algum asset opcional ausente em instância separada: sem crash, layout base ainda legível (teste técnico opcional).

## 2 — Lux Discovery

- [x] 02.01 Anúncio `LOCATION DISCOVERED` ao entrar pela primeira vez em bioma (Savana), comprovado por screenshot.
- [x] 02.02 Sair e reentrar no save não repete anúncio no mesmo bioma; confirmado pelo usuário.
- [x] 02.03 Visitar bioma novo anuncia e retornar ao antigo não repete; confirmado pelo usuário.
- [ ] 02.04 Persistência continua funcionando com todos os 5 JARs após atualização.
- [ ] 02.05 Morrer e renascer preserva dados de descoberta, conforme política de attachment.
- [ ] 02.06 Criar segundo mundo não herda descobertas do primeiro.
- [ ] 02.07 Verificar traduções PT-BR e EN-US, legibilidade em resoluções variadas.
- [ ] 02.08 Validar Discovery e Engine em servidor dedicado (sem crash / classe de cliente carregada no lado servidor).

## 3 — Lux Atlas (bússola)

- [x] 03.01 HUD da bússola funciona no singleplayer e aparece junto da hotbar (print do usuário de 24/10).
- [x] 03.02 Clean Fantasy v2 carregado visualmente, molduras em PNG e ponteiro visíveis (print de 24/10).
- [ ] 03.03 Girar 360º: N, L, S, O correspondem ao rumo verdadeiro; conferir especialmente transição Norte.
- [ ] 03.04 `snapToHotbar=true` fica corretamente próximo da hotbar após alternar escala da GUI.
- [ ] 03.05 `snapToHotbar=false` respeita `distanceFromBottom`.
- [ ] 03.06 F1 oculta bússola e F1 novamente restaura.
- [ ] 03.07 Bússola não sobrepõe legendas, ação, hotbar ou interface em 16:9, 16:10 e janela reduzida.
- [ ] 03.08 Atlas funciona sem Discovery e sem HUD (somente Engine + Atlas, instância separada).

## 4 — Lux HUD (status do jogador, poções, colegas e missões)

- [x] 04.01 Painel da própria skin, barras VIDA/FOME/XP e quadro de missões na direita funcionam (print do usuário).
- [x] 04.02 Visual Clean Fantasy v2 com molduras/ícones/bússola aprovado pelo usuário (print de 24/10).
- [ ] 04.03 Perder vida e se curar atualiza preenchimento e valor numérico corretamente.
- [ ] 04.04 Comer e ficar com fome atualiza barra e valor real.
- [ ] 04.05 Ganhar XP e avançar de nível atualiza barra e nível.
- [ ] 04.06 Poções: ícones vanilla em bandeja externa, duração diminui e expirados desaparecem.
- [ ] 04.07 Cinco ou mais efeitos não encobrem quadro de missões ou HUD; sem corte de texto.
- [ ] 04.08 F1, chat, inventário e pausa não sofrem sobreposição.
- [ ] 04.09 Modo `CLASSIC` restaura barras vanilla; modo `RPG` volta a exibir HUD Lux.
- [ ] 04.10 Em um time de scoreboard com dois jogadores, colega aparece **menor abaixo** com skin e HP reais.
- [ ] 04.11 Jogador fora do grupo não aparece; sair do time limpa cartões na próxima atualização.
- [ ] 04.12 Quadro de missões diz 'Nenhuma missão acompanhada' sem Quest real — não inventar objetivos.
- [ ] 04.13 Verificar visual em baixa resolução/escala automática e com nome de jogador longo.
- [ ] 04.14 HUD e sincronização com clientes multiplayer + servidor dedicado (servidor não carrega GUI).

## 5 — Lux Menu (NOVO — QUINTO MOD; NENHUM TESTE REAL ATÉ AGORA)

- [ ] 05.01 O quinto mod `lux_menu` aparece como Loaded sem afetar os quatro existentes.
- [ ] 05.02 Tela principal: botões gráficos grafite/roxo, hover lilás, logo Minecraft e panorama preservados.
- [ ] 05.03 `Singleplayer`, `Multiplayer`, `Mods`, `Options`, `Quit Game` continuam acionando destinos corretos.
- [ ] 05.04 Abrir mundo, apertar ESC: `Resume Game`, `Options`, `Save and Quit to Title` funcionam normalmente.
- [ ] 05.05 `Options`: somente botões simples estilizados; sliders, teclas, acessibilidade e opções continuam utilizáveis.
- [ ] 05.06 `Select World`: navegar lista, selecionar save, cancelar, criar mundo, editar/excluir só em save DESCARTÁVEL, com confirmação.
- [ ] 05.07 `Multiplayer`: navegar servidores, atualizar lista, abrir Direct Connection e cancelar sem alterar dados.
- [ ] 05.08 Navegação por TAB, ENTER, ESC e teclado; foco visível e narração/acessibilidade não regrediram.
- [ ] 05.09 Hover e botão desabilitado aparecem distintos; texto atualizado acompanha estado vanilla.
- [ ] 05.10 `config/lux_menu-client.toml`: desligar `styleSubmenus` preserva título/pausa sem modificar submenus.
- [ ] 05.11 `enabled=false` volta todos os menus ao padrão; `enabled=true` restaura visual.
- [ ] 05.12 `showBranding=false` remove assinatura sem desativar botões.
- [ ] 05.13 Testar escalas GUI 2, 3, Auto; garantir que logo e assinatura não cobrem widgets.
- [ ] 05.14 Testar com outros mods UI de terceiros e comparar comportamento sem Lux Menu.
- [ ] 05.15 Sem crash após sair de mundo e voltar ao menu principal repetidamente.

## 6 — Tela vermelha de loading / UI Shell futuro (NÃO IMPLEMENTADO)

- [ ] 06.01 Verificar ponto de extensão específico do NeoForge 21.1.264, sem tocar no boot atual.
- [ ] 06.02 Preparar tema de loading em módulo/pacote isolável; exigir plano de rollback caso impeça iniciar.
- [ ] 06.03 Testar loading sem trocar `game assets` da UI em tempo de boot.
- [ ] 06.04 Depois: apenas se estável, personalizar telas de opções internas/inventário/diálogo (escopos futuros).

- [ ] 06.05 Ler o guia `docs/32-LUX-BOOT-CARREGAMENTO.md` sobre incompatibilidade entre FancyModLoader main e versão 1.21.1.
- [ ] 06.06 Rodar `scripts/lux-boot-theme.ps1 -Action Preview` e confirmar que não modifica a instância.
- [ ] 06.07 (Opcional; somente com jogo fechado) `-Action Apply` grava **apenas** darkMojangStudiosBackground em options.txt, guarda backup, preserva earlyWindowControl e não troca JARs.
- [ ] 06.08 Verificar se o jogo inicia com o modo escuro **sem perder as barras reais de carregamento FML** (o script agora não altera fml.toml).
- [ ] 06.09 `-Action Restore` recupera **apenas** a opção de fundo anterior, preservando outras configurações feitas depois; confirmar que a opção volta ao estado original.
- [ ] 06.10 **NÃO MARCAR** tema Lux completo como pronto: ainda falta implementação de provedor próprio e teste isolado.

- [ ] 06.11 Se o modo escuro for testado, confirmar que o Minecraft permanece encerrável e inicializável mesmo após voltar do menu principal.
- [ ] 06.12 Confirmar que `Apply` repetido não cria backups extras e que o backup permanece fora das pastas `mods` e `config`.

- [ ] 06.13 Abrir `experimental/lux-boot-preview/index.html` no navegador (fora do Minecraft) e verificar layout na largura desktop e mobile.
- [ ] 06.14 Clicar nos controles Reiniciar/Pausar e confirmar que o progresso indica **SIMULADO** permanentemente.
- [ ] 06.15 Conferir que o protótipo HTML/JS não foi instalado na pasta mods nem incluído em nenhum JAR NeoForge.


## 6B — Lux Loading Slideshow (FOTOS DO USUÁRIO, NOVO / NÃO TESTADO EM JOGO)

- [ ] 06B.01 Extrair `Lux_Mods_Slideshow_Fotos_e_Upload_v1.zip` e colocar 1–8 JPG/PNG em `FOTOS-AQUI`.
- [ ] 06B.02 Abrir `PREVIA-FOTOS.html`, selecionar fotos e conferir crossfade e `CARREGANDO...` animado.
- [ ] 06B.03 Executar `ENVIAR-FOTOS-GITHUB.bat`, conferir ordem, autorizar `S` e confirmar push na branch de desenvolvimento.
- [ ] 06B.04 Conferir no GitHub `modules/lux-menu/src/main/resources/assets/lux_menu/textures/gui/loading/slide_01.png` até o último slide enviado.
- [ ] 06B.05 Rodar atalho de atualização com Minecraft fechado; Gradle build SUCCESSFUL e 5 JARs sem duplicação.
- [ ] 06B.06 Entrar/criar mundo: crossfade de 1,2s após 5s por foto, sem travamentos na imagem.
- [ ] 06B.07 Animar `CARREGANDO`, `CARREGANDO.`, `CARREGANDO..`, `CARREGANDO...`; não mostrar progresso numérico fictício.
- [ ] 06B.08 Tela de carregamento de mundo sem fotos mantém vanilla intacta e evita quadriculado roxo/preto.
- [ ] 06B.09 Em partida multiplayer, confirmar que não encobre um botão essencial, erro de conexão ou confirmação importante.
- [ ] 06B.10 Mudar `loadingSlideshow=false` em `lux_menu-client.toml`: voltar às telas vanilla durante o loading de mundos.
- [ ] 06B.11 Abrir Minecraft do zero: tela vermelha inicial NeoForge e Mojang LoadingOverlay continuam originais (não foram tematizadas).
- [ ] 06B.12 Testar com 1, 2 e 8 imagens; proporção 16:9 e janela 16:10; sem imagem esticada.

**Evidência adicional:** o cálculo de transição e reticências em `SlideTiming.java` passou teste Java 21 isolado em 24/10/2026, mas este teste NÃO valida o render NeoForge nem PowerShell do upload no Windows.

## 7 — Testes de resiliência, entrega e fechamento

- [ ] 07.01 Rodar `scripts/smoke-pure-java.bat` (lógica isolada), registrar sucesso real.
- [ ] 07.02 Rodar `gradlew.bat assembleTestPack --no-daemon` (se não for pelo atalho), registrar build completo.
- [ ] 07.03 Se Python 3.11+ disponível, rodar `python scripts/verify_jars.py`.
- [ ] 07.04 Confirmar sem erros graves novos em `logs/latest.log`; revisar qualquer relatório de crash.
- [ ] 07.05 Tirar screenshots: tela inicial, opções, mundos, multiplayer, ESC, HUD/Atlas com poção.
- [ ] 07.06 Testar servidor dedicado NeoForge com cinco mods (ou apenas Engine + módulos comuns).
- [ ] 07.07 Comparar screenshots com mockup e registrar ajustes necessários, sem mudar identidade.
- [ ] 07.08 Quando os testes críticos passarem, atualizar PR #14 para review; NÃO fazer merge automático.

## Registro de testes

| Sessão | Data/hora | Instância | Commit GitHub | Resultado | Evidências / erro |
|---|---|---|---|---|---|
| Prévia Engine + Discovery | 24/10/2026 | teste, singleplayer | versão anterior | parcial ✅ | prints e confirmação usuário |
| Prévia Atlas + HUD v2 | 24/10/2026 | teste, singleplayer | versão anterior | visual aprovado ✅ | print do usuário |
| Pacote 5 mods | — | teste | a verificar | ☐ pendente | não testar até o usuário estar disponível |

### Em caso de erro no Lux Menu

**Rollback rápido sem tocar em mundos:** fechar Minecraft; mover apenas `lux-menu-0.1.0-SNAPSHOT.jar` para fora de `mods`; abrir novamente com os quatro mods previamente aprovados. Não remover Engine. Se o build falhar, o instalador não instala novos JARs.

### Privacidade

Os logs brutos podem conter nome de usuário, URLs de servidor, caminhos locais e outras informações. **Não publicar automaticamente**; revisar e ocultar dados antes de compartilhar. O script de inventário não copia nem envia logs brutos.
