# ADR-003 — Sorteio de necessidades por NPC no ciclo do mundo

**Status:** decisão de produto · **Data:** 2026-10-24

## Contexto
Necessidades de taverneiros/ferreiros/bruxas devem parecer orgânicas sem simulação cara de estoque/economia. Sortear a cada fala é inconsistente em multiplayer e explorável.

## Decisão
O servidor sorteia e persiste necessidade de **cada NPC** em ciclo de dia/noite do Minecraft; revisão após 2–3 dias (configurável), com oferta estável até revisão/aceitação. Repetir interação não sorteia. Todos os jogadores veem a mesma demanda pública. O contrato aceito tem estado separado (prazos/recompensa), sem expirar arbitrariamente por reavaliação da necessidade.

## Aplicação
Se NPC ficou descarregado, na próxima carga reavaliar conforme gameDay com limite de catch-up. Jogadores podem avisar amigos e planejar entrega; impedir recompensa dupla com transação atômica.

## Não objetivos
Economia física de estoque/produção, reavaliação a cada tick, missão obrigatória, pedido garantido toda vez.
