# Lux HUD — proposta de reorganização (13/11/2026)

## Decisões do teste in-game
- O painel superior ao lado do retrato apresenta VIDA, FOME e SEDE.
- A experiência deixa de ocupar uma barra permanente: XP e nível ficam visíveis na aba UPGRADES do Lux Atlas no futuro.
- Barras vanilla e informações do rodapé nunca aparecem sobre a hotbar decorada.
- Efeitos de poção atuais estão comprimidos e precisam de um redesenho em cartões legíveis, com ícone, duração e nível, contextuais.
- Vinhetas discretas de contexto: saúde baixa = vermelho; fome crítica = amarelo/âmbar; ar baixo = preto, sem medidor extra; jamais ocultar completamente a visão.
- O medidor de salto do cavalo não deve aparecer sobre a hotbar nem como barra adicional.

## Integração de sede (bloqueio antes do código)
O jogador adicionou um mod de sede, mas ainda não foi confirmado o identificador e versão exatos do JAR.
É necessário obter o estado REAL de hidratação do mod e ocultar SOMENTE o overlay original, sem inventar uma barra com valor fictício, depender de API instável, ou introduzir dependência dura ao Lux HUD.
Candidatos diferentes: Thirst Was Taken, Thirst Was Taken 2. APIs e IDs diferem.
Sem a identificação exata, não substituir a barra XP por um placeholder no runtime.

## Ordem de implementação
1. Confirmar mod-id e API/configuração do mod de sede instalado; testar acesso ao valor no cliente.
2. Renderizar sede no terceiro slot do HUD; mover XP para a futura aba de upgrades.
3. Ocultar indicador inferior do mod de sede usando a integração apropriada.
4. Redesenhar efeitos em linha/cartões, respeitando escala F8 e telas pequenas.
5. Testar Forge/NeoForge, diferentes escalas e compatibilidade com AppleSkin e outros overlays.

Estado: **especificação de design**, ainda não implementada no jogo.
