# Lux Atlas — Mapa e bússola

## Fantasia
Orientação de RPG com uma barra minimalista e um mapa de exploração sem esconder toda a paisagem.

## Funcionalidades v1
- Tecla M abre mapa e fecha preservando o estado normal do jogo.
- Mapa mostra terreno **efetivamente explorado**, posição do jogador e coordenadas configuráveis.
- Zoom, pan e marcadores pessoais persistentes.
- Dimensões separadas com identificador distinto; não misturar coordenadas Nether/Overworld.
- Bússola de direção integrada por padrão **próxima e acima da hotbar**, configurável para posição superior ou ocultação (Lux HUD desenha quando instalado).
- Marcadores de vilas e regiões descobertas por Lux Discovery/Villages, quando estes estiverem presentes.
- Opção de ocultar coordenadas/waypoints e proteção de dados em multiplayer.

## Design visual
Usar escala discreta, cardinais N/S/L/O, ícones de pontos de interesse com legendas; evitar números em todo lugar. Não copiar mapas/UI/recursos de outros mods.

## Política de descoberta
O Atlas só deve marcar o que o jogador descobriu ou recebeu legitimamente (quando configuração permitir). O nome compartilhado da vila vem do WorldRegistry; a descoberta da vila pode ser pessoal. No multiplayer, compartilhar waypoint é ato explícito.

## Técnica
Separar map tiles/cache por mundo+dimensão+versão; evitar varredura ilimitada de chunks, vazamento de informação de região não explorada e picos de IO. Ter limite de cache, persistência segura e estratégia de invalidação.

## Dependências
Engine requerido; HUD/Discovery/Villages opcionais. Map marker service tolera provedores ausentes.

## Pós-v1
Rota/nav avançada, mapa de superfície com filtros múltiplos, waypoint compartilhável com autorização, mapas ricos de estruturas modded.

## Aceite
M abre/fecha; zoom/pan funcionam; novo mundo não herda mapa anterior; Nether separado; bússola não conflita com elementos vanilla/menus; informação de outros jogadores não vaza.
