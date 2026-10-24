# Lux Clean Fantasy — guia de identidade oficial

Estilo aprovado pelo usuário: **pixel art do Minecraft, clean e sofisticada**. Referência visual é o mockup aprovado em 24/10/2026. Manter fundo grafite translúcido, bordas duplas finas roxas, pedras brilhantes discretas nos cantos, numeração dentro das barras, emblemas pixelados e tipografia vanilla legível.

## Layout de interface
- Esquerda, topo: personagem principal, retrato real da skin, vida, fome e XP.
- Abaixo: integrantes reais do grupo em cartões menores com vida e nível.
- Ao lado da vida: efeitos de poção genuínos e temporizadores em bandeja compacta.
- Direita, topo: missões ativas reais. Sem quest vinculada, mostrar corretamente estado vazio.
- Centro, baixo: bússola curta e fina logo acima da hotbar, centralizada; N/L/S/O corretos.
- Manter hotbar e indicadores essenciais vanilla (ar, armadura, etc.).

## Componentes
- Java + NeoForge: todos os dados, layout, barras, estados e escala.
- PNG RGBA originais: ornamentos, borda do retrato, ícones e ponteiro.
- Engine: `LuxGuiStyle` como base única reutilizável dos módulos.
- HUD: `HudPanel`, `HudPartyClientState`, `HudQuestTracker`.
- Atlas: `AtlasCompassHud` e configuração `snapToHotbar`.

## Tokens principais (ARGB)
- Fundo: `0xD4191724`
- Bordas: `0xFF65507F`
- Roxo: `0xFFAF84EE`
- Texto: `0xFFF7F1FF`
- Secundário: `0xFFC9C0D4`
- Champagne: `0xFFF4CE87`
- Vida: `0xFFD44D66`
- Fome: `0xFFE1A64F`
- XP: `0xFF65B786`

## Recursos
A primeira leva V1 teve 13 PNGs; a revisão V2 tem 18. Referência de empacotamento e requisitos em `docs/28-LUX-CLEAN-FANTASY-V2.md`.

## Critério de estilo
Evitar painéis gigantes, transparência excessiva, fontes lisas demais e HUD cheia de informações inventadas. O projeto deve continuar parecendo parte do Minecraft com acabamento de RPG premium.

## Status
V1 foi instalada e fotografada no jogo pelo usuário. **V2 é nova e ainda precisa de build e testes in-game**. Não confundir imagem conceitual de apresentação com saída real do renderizador.
