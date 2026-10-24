# Lux Clean Fantasy — Design System v1

**Status em 24/10/2026:** sistema de estilo implementado e publicado na branch `feat/engine-discovery-bootstrap`; o pacote PNG é distribuído separadamente para upload manual no GitHub. O novo código visual **ainda não foi compilado nem testado no Minecraft** depois dessa alteração. Manter PR #14 como draft.

## Filosofia

**Minecraft nativo, só que melhor acabado.** Não copiar UI de outros mods nem substituir o estilo quadrado/pixel art do jogo por glassmorphism moderno. Sofisticação vem de espaçamento, contraste, grade de pixels, tipografia Minecraft legível e poucos ornamentos. Projeto se chama `Lux Clean Fantasy`.

Base funcional fica por código Java (`LuxGuiStyle`); sprites PNG **originais e opcionais** aprimoram molduras e ícones. O desaparecimento do arquivo PNG nunca pode impedir o Minecraft de abrir ou desenhar as informações importantes.

## Paleta (tokens ARGB e correspondentes RGB)

| Uso | ARGB | RGB |
|---|---|---|
| Fundo translúcido | `0xBC151720` | `#151720` |
| Borda suave | `0xFF534864` | `#534864` |
| Roxo acento | `0xFF8467B4` | `#8467B4` |
| Texto principal | `0xFFF1ECED` | `#F1ECED` |
| Texto secundário | `0xFFD2C9DB` | `#D2C9DB` |
| Champagne | `0xFFE1C69A` | `#E1C69A` |
| Vida | `0xFFCF5061` | `#CF5061` |
| Fome | `0xFFD39B54` | `#D39B54` |
| Experiência | `0xFF68B690` | `#68B690` |

## Ritmo visual

- Espaçamento base: múltiplos de 4 pixels da interface; exceções técnicas de 1 px para bordas.
- HUD pessoal no topo esquerdo; skin Minecraft intacta, vida/fome/XP sempre vindos do jogador real.
- Efeitos ativos imediatamente ao lado da vida, com sprites do próprio Minecraft (não inventar poções).
- Membros da equipe abaixo do cartão pessoal, menores; continuam vindo do servidor via time scoreboard temporário. Não exibir pessoas aleatórias do servidor.
- Missões no topo direito; sem Lux Quests, informar corretamente que não há missão ativa.
- Hotbar e bússola de navegação embaixo; Atlas não inclui mapa na tecla M nesta etapa.
- Borda 1 px com cantos opcionais PNG; sem molduras pesadas, luz piscante ou gradiente neon.
- Regras de segurança: HUD desaparece em F1/menus; preservar ar, armadura, montaria e alertas críticos; opção `CLASSIC` restaura vanilla.

## Implementação no código

- `modules/lux-engine/src/main/java/dev/luxmods/engine/client/LuxGuiStyle.java` (paleta, painel, barras, ornamentos PNG opcionais).
- HUD: `HudPanel`, `HudPartyClientState`, `HudQuestTracker` usam o estilo; vida e níveis permanecem dinâmicos.
- Atlas: `AtlasCompassHud` reutiliza estilo sem arruinar cálculos angulares aprovados no jogo.
- Discovery: a notificação já funciona e permanece funcional; facelift cinematográfico detalhado deve ser feito após validar HUD/Atlas, para não alterar persistência nem pacotes de rede.

## Assets separados: ZIP `Lux_Mods_Clean_Fantasy_Assets_v1.zip`

Os 13 PNGs originais ficam dentro de:

`modules/lux-engine/src/main/resources/assets/lux_engine/textures/gui/lux/`

Arquivos incluídos:

```text
compass_end_left.png
compass_end_right.png
divider.png
effect_slot.png
frame_corner_tl.png
frame_corner_tr.png
frame_corner_bl.png
frame_corner_br.png
lux_mark.png
ornament_diamond.png
potion_marker.png
quest_badge.png
quest_check.png
```

**Só o código foi publicado no GitHub nesta etapa.** O usuário precisa extrair o ZIP e adicionar as imagens ao repositório; não enviar o ZIP fechado como substituto das pastas. O pacote inclui `ENVIAR-ASSETS-GITHUB.bat` e `.ps1`, que copiarão e enviarão somente esses PNGs para a branch correta **após confirmação do usuário**.

Os assets ausentes são detectados pelo `LuxGuiStyle` com ResourceManager; o componente desenha o contorno/barras por código sem depender dos PNGs. Depois de adicionar PNGs, recompilar e reiniciar o jogo.

## Fluxo de teste e release

1. Confirmar branch e histórico; antes de testar fechar o Minecraft.
2. Enviar imagens via script do ZIP; inspecionar diff da branch.
3. Rodar atalho `Atualizar Lux Mods.bat`; exigir `BUILD SUCCESSFUL` e 4 JARs.
4. Abrir instância `teste`, conferir todos quatro mods carregados.
5. Testar HUD vazio e cheio (nome longo, vida baixa, fome baixa, XP e poção), F1, modo CLASSIC, colega em equipe, missões sem objetivo.
6. Girar câmera 360° para conferir cardinais da bússola; testar diferentes escalas GUI e aspecto ultrawide.
7. Registrar screenshots claros, com noite e menus; conferir uso de assets e se nenhum elemento deixa de aparecer caso as imagens faltem.
8. Só considerar publicável após compilar e playtest. Servidor dedicado continua gate separado.

## Referências técnicas

- NeoForge 1.21.1 Screen Texture Docs: https://docs.neoforged.net/docs/1.21.1/gui/screens/
- Referência `GuiGraphics`: https://lexxie.dev/neoforge/1.21.1/net/minecraft/client/gui/GuiGraphics.html
- Referência operacional: https://github.com/rehan-remade/universal-modder/blob/main/skills/game-automation/SKILL.md
- Referência estilo assets: https://github.com/rehan-remade/universal-modder/tree/main/skills/asset-pipeline

## Próximos incrementos

Animações suaves (com acessibilidade), painéis compactos de missões, localização consistente e transições Discovery são a **fase 2** do acabamento, não estão concluídos nesta revisão. Mapa M do Atlas é módulo funcional posterior.
