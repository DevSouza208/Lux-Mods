# Lux Menu — telas secundárias (incremento sem novos assets)

Código em branch `feat/engine-discovery-bootstrap`. Aplica a identidade Lux Clean Fantasy v2 também aos botões **vanilla comuns** de:
- `OptionsScreen` — Opções (apenas Button exato; não muda CycleButton, slider, teclas ou controles de mods).
- `SelectWorldScreen` — seleção de mundos (mantém lista de saves e seleção intactas).
- `JoinMultiplayerScreen` — lista de servidores (mantém lista, endereço e dados).
- `TitleScreen` e `PauseScreen` — já incluídos no primeiro protótipo.

`ScreenEvent.Init.Post` altera exclusivamente instâncias com `getClass() == Button.class`, e a classe `LuxMenuButton` delega o clique para o original. Eventos de render do Minecraft são preservados; o texto do botão é sincronizado a cada frame, mas nenhum destino de navegação é substituído.

A assinatura superior `LUX MODS` e rodapé continuam restritos ao menu inicial e à pausa: não cobrir widgets em Opções/Mundos/Multiplayer.

## Configurações
No `config/lux_menu-client.toml` (cliente), sem comandos de servidor:
- `enabled=true`: ativa efeitos visuais.
- `styleButtons=true`: botões principais.
- `styleSubmenus=true`: estiliza também os botões simples dessas três telas.
- `showBranding=true`: assinatura no título e pausa.

## Limites e rollback
A tela vermelha **anterior** ao menu é renderizada pelo NeoForge antes do cliente normal; fica fora do escopo desta mudança. Não instalar plugins externos para alterá-la sem análise de compatibilidade.

Se um botão parar de funcionar, parar de renderizar ou perder foco por teclado: retirar somente `lux-menu-0.1.0-SNAPSHOT.jar` do diretório `mods`; Engine, Discovery, HUD e Atlas permanecem. Reportar qual tela e botão e anotar em `docs/30-CHECKLIST-DE-TESTES.md`.

**Status:** integração escrita, **ainda não compilada nem testada no Minecraft**.
