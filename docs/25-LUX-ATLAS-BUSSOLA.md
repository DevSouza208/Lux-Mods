# Lux Atlas — protótipo de bússola (Minecraft 1.21.1)

**Objetivo deste incremento:** adicionar um terceiro mod `lux_atlas`, com Engine obrigatório e Discovery opcional. Primeiro incremento focado numa única funcionalidade visual verificável.

## Implementado no código
- Bússola horizontal com marcações de 15°, rótulos brasileiros N/L/S/O e ponteiro central.
- Estilo discreto roxo/dourado, centralizado e posicionado por padrão 84 px acima da borda inferior.
- Cliente NeoForge: `RegisterGuiLayersEvent` + `VanillaGuiLayers.HOTBAR`, sem importar classes de cliente no caminho de carregamento do servidor dedicado.
- Preferências `showCompass` e `distanceFromBottom` no arquivo CLIENT do mod.
- Cálculo de heading puro (`CompassMath`), testável sem Minecraft.
- `assembleTestPack` e instalador do SKlauncher atualizados para três JARs, mantendo backup fora de `mods`.

## Como testar na instância `teste`
1. Fechar o Minecraft.
2. Dar dois cliques em **Atualizar Lux Mods.bat** (git fetch/pull, build e instalação).
3. Verificar `lux-atlas-0.1.0-SNAPSHOT.jar` junto ao Engine e Discovery em `mods`.
4. Abrir Minecraft, conferir `Lux Atlas` na lista e entrar em um mundo.
5. Girar 360° devagar: bússola deve correr; F1 deve ocultar; voltar ao jogo deve restaurar.
6. Conferir N/L/S/O nas posições certas: ao olhar para a direção Norte (-Z), `N` fica centralizado.

**Não foram implementados:** mapa na tecla M, marcadores, zoom, cache de exploração e integração com Discovery. Vêm depois do teste in-game da bússola.

**Status:** código e instalador publicados; compilação NeoForge e renderização real do Atlas ainda não validadas pelo usuário. A PR continua draft.

## Referências
- https://lexxie.dev/neoforge/1.21.1/net/neoforged/neoforge/client/event/RegisterGuiLayersEvent.html
- https://lexxie.dev/neoforge/1.21.1/net/neoforged/neoforge/client/gui/VanillaGuiLayers.html
