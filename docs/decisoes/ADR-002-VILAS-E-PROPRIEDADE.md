# ADR-002 — Propriedade de vilas e reparos

**Status:** decisão de produto · **Data:** 2026-10-24

## Contexto
Desejo de villagers autônomos e mundo que reage a interferências. Tentativa anterior de proteger toda modificação do jogador dentro de casa de villager foi rejeitada.

## Decisão
Vila é dona de edifícios, lotes, ruas e praças registrados; pode consertar danos e remover blocos obstrutivos colocados **inclusive por jogadores** nas propriedades. A mesma ação pode resultar em reclamação, reputação ruim e guardas. O jogador preserva sua liberdade de construir **fora** dos lotes/ruas públicas.

## Guardrails
Máscaras precisas e plantas originais com blocos e ar esperados. Não afetar construções fora da máscara. Nunca apagar contents de baús/entidades/blocos de proteção; registrar conflitos desconhecidos, especialmente modded block entities. Sem escalonamento implacável de guardas nem ciclos de reparo sem limite.

## Consequência
A experiência passa a exigir respeito pelos direitos dos NPCs. Implementação mais complexa que proteção ingênua de autoria, mas necessária para a fantasia de sociedade independente.

## Teste
Quebrar parede da biblioteca: villager restaura. Colocar terra na porta: villager remove. Construir casa fora do lote: permanece intacta mesmo que encostada no limite municipal.
