# Arquitetura técnica inicial (ADR-mãe)

## Plataforma candidata

- Minecraft Java 1.21.1 + NeoForge + JDK 21 + Gradle. Confirmar com prova de conceito e lista final de mods do pack.
- Gradle multi-project / monorepo. Um projeto por mod executável, com build independente e testes.
- Módulos server+client separando código comum, client-only HUD/render, autoridade do servidor e rede.
- Lux Studio é app web separado; não compilar app web dentro de mod Java.
- Não fixar versão de NeoForge/Curios/Gradle por palpite; usar versões publicadas verificadas ao iniciar o código.

Documentação de referência: https://docs.neoforged.net/docs/1.21.1/gettingstarted/ e https://docs.neoforged.net/docs/1.21.1/items/.

## Repositório previsto

~~~text
Lux-Mods/
  docs/                    GDD, arquitetura, decisões, exemplos e testes
  modules/
    lux-engine/
    lux-discovery/
    lux-atlas/
    lux-hud/
    lux-npc/
    lux-lore/
    lux-quests/
    lux-villages/
    lux-progression/
    lux-inventory/
    lux-items/
    lux-food/
  apps/lux-studio/         aplicativo web (etapa própria)
  examples/                presets, estruturas e conteúdo fictício testável
  testpacks/               manifests de integração sem JAR de terceiros
~~~

## Contratos públicos do Engine

- **ModuleDiscovery:** ID, versão semântica, feature flags, provedores ativos, compatibilidade de protocolos.
- **EventBus próprio:** eventos versionados (descoberta de vila, interação com NPC, dano de estrutura, mudança de reputação, contrato aceito etc.). Evitar eventos a cada tick.
- **ConfigService:** configurações globais por mundo/servidor + preferências individuais de cliente; esquema com versionamento/migrações; validação de privilégios.
- **WorldRegistry:** identificadores estáveis de vilas, prédios, NPCs, nomes e localização. Nunca usar só nomes como chaves.
- **StoryFacts:** fatos persistidos (jogador viu item, leu livro, NPC perdeu profissão, dragão derrotado), com escopo jogador/entidade/mundo e política de retenção.
- **NetworkSync:** pacotes discriminados por direção, sincronização incremental, anti-replay/idempotência para transações.
- **Diagnostics:** comandos de inspeção, logs limitados e dump sanitizado de estado.

Proibir dependências circulares. Modules referenciam apenas Engine em compilação sempre que possível; integração opcional por interface/adaptador. IDs com namespaces exclusivos e contratos versionados.

## Autoridade e persistência

Servidor é fonte de verdade para inventário, comércio, contratos, NPCs, vilas, proteção de lotes e progressão. Cliente apresenta HUD/mapa/efeitos e envia intenções, jamais alterações autoritativas. Estado de entidade pode usar data attachments; dados globais do mundo podem usar SavedData, com dirty flag/codec e migrações. Data components são candidatos para estados de ItemStack. Validar comportamento de morte, troca de dimensão e descarregamento de chunk.

## Ciclos e performance

- Nenhum NPC distante com IA por tick: tarefas são agendadas somente nos chunks carregados.
- Necessidades têm dia de próxima revisão persistido; ao carregar NPC, computar transições de forma limitada, sem fazer milhares de sorteios retroativos.
- Reconstrução de edifícios usa fila de blocos, budget por tick, máximo de tarefas simultâneas, raio e auditoria.
- Discovery usa debounce/cooldown para não piscar em fronteiras de bioma.
- Interação multiplayer recebe deduplicação e locks transacionais: dois jogadores não resgatam o mesmo contrato duas vezes.
- Benchmarks em servidor dedicado e hardware modesto antes de prometer suporte amplo.

## Inventário — limites críticos

Até nível 6: hotbar de 9 + até 27 posições vanilla. Bloqueio é **lógico e visual**, NÃO um item bloqueador real. Validar insertion de pickup, shift-click, crafting, comandos, morte e handlers de outros mods.
Níveis 7–10: storage adicional + API de acesso unificado. O vanilla e muitos mods só consultam 36 slots; adaptadores para crafting/recipe book e integrações externas são requisitos próprios. Empilhamento virtual 128 **não** é um ItemStack vanilla normal: armazenar representações físicas válidas e consumir/transacionar sem perda e dupe.

## Mundo, propriedade e reparos

Cada estrutura possui planta de referência, máscara de lote (incluindo vias públicas), proprietários, camas e bloco de trabalho; registrar explicitamente o AR que deveria estar livre. Dentro de área da vila, jogador que obstrui/vandaliza não recebe imunidade. Reconstruir com política de segurança: não destruir itens de inventário/bloco especial nem quebrar entidades ou construções fora da propriedade. Eventos desconhecidos devem acionar auditoria/log antes de operação destrutiva. Fora de lote/rua, preservação prioritária a edifícios do jogador. Mudança de geração só em chunks novos na v1.

## Pacotes de itens

Um pack externo contém manifesto versionado, arquivos de modelo/textura, tradução e definição data-driven. Novos IDs são registrados na inicialização; alterar definições registradas exige reiniciar. Mudanças cosméticas podem ter recarga separada após testes. Cliente e servidor precisam concordar em hash/versão dos packs; não distribuir código Java arbitrário em ZIP de conteúdo. Separar item, bloco colocável, animação e suporte de rendering. Módulos de comportamento são classes predefinidas, não scripts arbitrários privilegiados.

## Segurança e distribuição

Não embutir tokens/credenciais, JARs de terceiros ou ativos sem licença. Validar paths dentro de packs, impedir path traversal e descompressão abusiva. Regras do servidor prevalecem sobre UI do cliente; limites de tamanho de payload; backups automáticos antes de migração de dados.
