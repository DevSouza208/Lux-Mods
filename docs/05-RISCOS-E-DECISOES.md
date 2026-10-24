# Riscos, hipóteses e registro de decisões

## Riscos prioritários

| Risco | Impacto | Tratamento |
|---|---|---|
| Slots acima dos 36 vanilla não vistos por outros mods | Alto | API de inventário + adapters, testes com pack final |
| Empilhamento virtual 128 gerar dupe/perda | Crítico | representação válida, transação atômica, casos de carga/morte/desconexão |
| IA e reparos consumirem TPS | Alto | filas, quotas por tick, simulação distante simplificada |
| Worldgen de vilas falhar em encostas | Alto | seleção de terreno, fundamentos, testes com seeds, fallback sem vila |
| Villager destruir casas de jogadores | Crítico | máscaras de propriedade precisas, auditoria, áreas fora do lote protegidas |
| Grief/segurança e patrulha injusta | Alto | regras server-side, advertência proporcional, cooldown, whitelist de ações |
| História multiplayer ficar inconsistente | Médio/Alto | fatos por jogador + mundo, regras explícitas para eventos globais |
| Conteúdo carregado por diretório quebrar registry | Alto | registrar na inicialização, manifesto/compat hash, validação |
| Nome/status de vila se perder ao recarregar chunks | Alto | IDs persistentes, dados migráveis, testes save/reload |
| Dependências de mods externos/licenças | Médio/Alto | lista aprovada por loader+versão, APIs opcionais, sem copiar assets |
| Escopo muito amplo antes do primeiro build | Alto | releases verticais, gates e protótipos obrigatórios |

## Decisões aceitas (produto)

- ADR-001: visão “mundo com histórias, não campanha obrigatória”.
- ADR-002: monorepo e Engine mínimo para descoberta e integrações.
- ADR-003: nomes de vilas persistentes e compartilhados; conhecimento de jogador individual.
- ADR-004: reparos restauram bens de villagers mesmo após vandalismo do jogador, dentro da máscara de lote; construções externas são protegidas.
- ADR-005: vila nasce em tier definido e permanece nesse tier na v1.
- ADR-006: demandas de NPC são aleatórias POR CICLO, nunca POR DIÁLOGO.
- ADR-007: Inventory conserva slots vanilla e trata extras como armazenamento adicional; não promete compatibilidade automática irrestrita.
- ADR-008: Food v1 sem crafting; taberna com ofertas especiais.

## Questões a validar no protótipo

- Versão base exata de Minecraft/NeoForge e JDK, Curios opcional e nível mínimo de API.
- UX de HUD para batalha, tela pequena e multiplayer com muitos jogadores.
- Prioridades de reparo e uso de recursos; reconstrução de inventários/blocos de entidade proibida por padrão.
- Comportamento de cama/profissão quando morador morre; reposição sem spawn infinito.
- Progresso de End Dragon no servidor: exploração do End por jogador, multiplayer e “conclusão” da investigação.
- Crafting com slots extras, compatibilidade com recipe book/modded tables.
- Valores finais dos custos de upgrades e dos limites de contrato/cooldown.
- Formato de pacote e limites do editor Studio.

## Standby explícito

Materials (priorizar minério/lingote/pó e geração por mod do pack), Launcher próprio, progressão de tiers/novas casas automáticas, sede/temperatura, simulação econômica real, sistema complexo de culinária e main quest obrigatória.

## Processo de ADR

Toda mudança estrutural relevante vira docs/decisoes/ADR-NNN-titulo.md: contexto, decisão, alternativas, trade-offs, testes e status. Não confundir sugestão do assistente com decisão de produto do autor.
