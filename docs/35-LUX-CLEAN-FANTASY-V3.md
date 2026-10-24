# Lux Clean Fantasy v3 — revisão visual aplicada

**Motivação:** screenshots do usuário em 24/10/2026 exibiram textos redundantes VIDA/FOME/XP, XP separada, retrato baixo, ícone de missões pouco expressivo/margens insuficientes e hotbar vanilla destoando da bússola.

## Implementação nesta branch (não mesclada em main)

- **Lux HUD:** retrato 46px com frame 52px. Três barras alinhadas nos Y 25, 43 e 61 em relação ao painel: ícones, barras e valores; sem texto redundante. Nível permanece acima à direita. XP mostra porcentagem do progresso real rumo ao próximo nível, sem inventar nível.
- **Missões:** ícone novo pergaminho pixel-art (`assets/lux_engine/textures/gui/lux/quest_scroll.png`), padding horizontal 14–17 px, painel mais alto, subtítulo suavizado e posição do objetivo ajustada.
- **Hotbar:** duas texturas PNG transparentes pixel-art dentro de `assets/lux_hud/textures/gui/hotbar/`. O módulo desenha acima da camada `VanillaGuiLayers.HOTBAR`, não cancela nem recria inventário ou atalhos; preserva itens, quantidade, overlay de durabilidade e seleção vanilla. `decorateHotbar=true` habilita; `mode=CLASSIC` mantém vanilla. A bússola fica imediatamente acima sem precisar mudar seu próprio layout.
- **Lux Menu:** na tela de título passa a reconhecer especificamente o botão com texto `Mods` mesmo quando ele for subclass de `Button` do NeoForge; demais subclasses mantidas intactas.

## Prioridade de teste

O pacote deve ser compilado no JDK21, NeoForge 21.1.264 e instância isolada `teste` pelo atualizador já existente. Registrar screenshots para painel principal, painel de missão, hotbar com item selecionado em posições 1/5/9 e Mods (hover+click). Testar offhand, GUI scale, F1, `mode=CLASSIC`.

**Evidência:** alterações de fonte/PNG publicadas; render NeoForge desta revisão **não testado no Minecraft** até novo playtest. Proteger PR #14 em draft, sem alterar a main.
