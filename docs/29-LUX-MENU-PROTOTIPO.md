# Lux Menu — primeira versão visual das telas do jogo

**Alvo:** Minecraft Java 1.21.1, NeoForge 21.1.264, Lux Engine obrigatório; Discovery, HUD e Atlas independentes.

## O que foi implementado em Lux Menu (quinto mod)

- Menu principal (`TitleScreen`): botões vanilla comuns com acabamento grafite/roxo, hover lilás e tipografia padrão Minecraft.
- Menu de pausa (`PauseScreen`): mesma linguagem visual, sem modificar navegação ou eventos de gameplay.
- Pequena assinatura `LUX MODS / CLEAN FANTASY` e legenda inferior, sem substituir o logo Minecraft, o panorama ou o splash.
- Config local (`config/lux_menu-client.toml`): `enabled`, `styleButtons`, `showBranding`.
- `ScreenEvent.Init.Post` substitui somente instâncias **exatamente** `Button.class` por um wrapper que chama `original.onPress()` para preservar ações vanilla. Outros tipos de controles, mods e sliders ficam como estavam. O wrapper ainda replica o estado active/visible.
- `ScreenEvent.Render.Post` desenha assinatura fora dos botões e não cancela o render da tela.
- Não usa novos PNGs. Reaproveita `ornament_diamond.png` e `lux_mark.png` já existentes no Engine; mesmo sem eles, a interface procedural funciona.

## Por que deixamos o loading para depois

A tela vermelha que aparece durante a inicialização é do **sistema de carregamento NeoForge**, antes do ciclo normal de GUI do Minecraft. Não é um `TitleScreen` e não recebe os mesmos eventos. Substituí-la exige projeto/testes separados em vez de hooks perigosos que possam bloquear o carregamento do jogo.

## Playtest do usuário

1. Fechar o Minecraft; executar `Atualizar Lux Mods.bat`.
2. **Exigir BUILD SUCCESSFUL** e exatamente cinco JARs na instância `teste`.
3. Na tela inicial, verificar botões Play/Singleplayer/Multiplayer/Mods/Options funcionando.
4. Entrar no mesmo save, apertar ESC, conferir `Resume Game`, `Options`, `Save and Quit to Title`.
5. Testar escala de GUI e navegação por teclado/tab/controle.
6. Se algo ficar sem clique, travar, ou mudar de função: remover só `lux-menu-0.1.0-SNAPSHOT.jar` da instância e enviar `logs/latest.log`. Os outros quatro mods continuam ativos.

**Status:** implementação publicada em branch draft; compilação e funcionamento em jogo **ainda não confirmados**. Não modificar a main nem distribuir release antes de testes.
