# Lux Clean Fantasy v2 — fidelidade ao mockup aprovado

**Status:** código publicado na branch `feat/engine-discovery-bootstrap`; 18 sprites PNG distribuídos em ZIP para o usuário adicionar ao GitHub. Sem novo teste completo de build NeoForge ou screenshot da versão 2 dentro do Minecraft. PR #14 permanece em draft.

## Referência visual oficial

Usar `REFERENCIA-MOCKUP-APROVADO.png` do pacote **Lux_Clean_Fantasy_v2_Pacote_Completo.zip** como objetivo de estilo, não como screenshot do código atual nem promessa de resultados idênticos em toda resolução. É um conceito e algumas partes dependem de módulos ainda não implementados (Lux Quests, grupos formais e customização de ícones).

### Componentes ajustados

| Módulo | Mudança V2 |
|---|---|
| Engine | `LuxGuiStyle`: molduras com dupla linha contínua e ornamentos opcionais; barra chanfrada com trilho, brilho de 1 px e valores corretos |
| HUD pessoal | quadro refinado, borda de skin, ícones para vida/fome/XP, números dentro da barra, separador fino |
| Efeitos | pequena bandeja independente ao lado do painel pessoal, sprites dos efeitos genuínos do jogo e tempo calculado do efeito, até 4 |
| Party | cartões de colegas compactos, com cabeça e barra de vida real, abaixo do jogador; sem placeholders de jogadores |
| Missões | cabeçalho e separador, título e progresso real quando houver dados de Lux Quests; estado 'Nenhuma missão acompanhada' sem dados |
| Atlas | bússola compacta `162x18` GUI px, adornos e marcador central; alinhamento automático acima da hotbar |

### Ajuste da bússola

A configuração `config/lux_atlas-client.toml` agora possui:
- `showCompass = true`
- `snapToHotbar = true` (novo; prevalece sobre o valor antigo de `distanceFromBottom`)
- `distanceFromBottom = 47` (alternativa somente quando snap estiver desligado)

O novo campo `snapToHotbar` evita que uma configuração antiga de 84 px mantenha a bússola afastada. O render do Atlas usa a mesma fórmula de rumo já testada.

### Kit de imagens V2

Colocar os 18 PNGs exatamente em:

`modules/lux-engine/src/main/resources/assets/lux_engine/textures/gui/lux/`

São 13 atualizações das imagens V1 e cinco novos assets: `portrait_frame.png`, `stat_health.png`, `stat_hunger.png`, `stat_xp.png`, `compass_needle.png`. Todos são originais (não copiados do Minecraft nem de outras interfaces de mods), PNG RGBA pixel art.

O ZIP inclui:
- `ENVIAR-ASSETS-V2-GITHUB.bat` e `.ps1`: copiar e enviar só PNGs para a branch; exige confirmação antes do commit e push.
- `MANIFEST-ASSETS.json` com SHA256.
- `PREVIA-ASSETS-V2.png`: contato das texturas realmente criadas.
- `REFERENCIA-MOCKUP-APROVADO.png`: imagem conceitual alvo.
- `LEIA-ME-PRIMEIRO.txt`: instruções de dois cliques.

**O ZIP não substitui o JAR.** É preciso extrair, adicionar assets ao projeto, recompilar o Engine e demais mods, instalar JARs pelo atalho já existente e reiniciar o cliente.

### Critérios de aprovação do playtest

1. Minecraft 1.21.1 + NeoForge 21.1.264, 4 mods carregados, sem crash.
2. Skin e barras de vida/fome/XP legíveis, com os novos ícones e acabamentos.
3. Efeitos de poção aparecem em bandeja sem cobrir quests; após expirar, desaparecem.
4. F1, menus, modo CLÁSSICO e telas de inventário permanecem corretos.
5. Party na esquerda apenas para membros do time real, com níveis/HP sincronizados.
6. Bússola N/L/S/O alinhada e mais próxima da hotbar, mesmo que o usuário tivesse `distanceFromBottom = 84` salvo.
7. Testar escalas GUI 2, 3 e automático, nas proporções 16:9 e 16:10.
8. Registrar screenshots comparativos, verificar bordas/ícones sem pixel stretch indevido.
9. Testar servidor dedicado depois do cliente; não dar release ou merge sem testes.

**Limites assumidos:** o painel de quests não possui conteúdos fictícios; as quests do mockup são ilustrativas. O grupo real e sua sincronização continuam dependendo do time scoreboard temporário, não são sincronização Lux Party por convite. O código não modifica a lógica da Discovery.
