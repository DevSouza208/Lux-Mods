# Roadmap técnico e dependências

**Sem prazos inventados.** Este documento define ordem, entregáveis e gates de qualidade; não representa trabalho já executado.

## Etapa 0 — Planejamento (atual)

- Documentar visão, MVP por módulo, contratos de eventos, decisões em aberto, riscos e critérios de aceite.
- Definir stack candidata e plano de compatibilidade com modpack.
- Preparar issues e organização do repositório.
- Saída: documentação revisável e backlog priorizado.

## Etapa 1 — Base compilável / Engine mínimo

- Importar MDK oficial do NeoForge para versão escolhida.
- Wrapper Gradle, settings multi-project, verificação de Java 21, CI de build, LICENSE/NOTICE definidos.
- Engine: registro de módulos, config comum/cliente, API de evento testável, logging e comandos diagnósticos.
- Testes client e dedicated server sem qualquer outro Lux.
- Gate: JAR do Engine sobe e desliga sem crash, config persiste e módulo falso é detectado.

## Etapa 2 — Primeiro loop visual (baixo risco relativo)

- Discovery: exibir nome do bioma com debounce e fade, sem alterar mundo.
- Atlas protótipo: bússola/hotkey M, mapa explorado mínimo. Mapa 3D/world streaming avançado é etapa posterior.
- HUD protótipo: painéis vanilla reposicionados, autoocultação, modo fixo e telas de opções.
- Gate: dois clients e servidor funcionam; sem spam de títulos, FPS/legibilidade aceitáveis.

## Etapa 3 — Dados, NPC, histórias

- NPC: diálogo ramificado básico e comércio integrado para villager; piglin, pillager e bruxa em incrementos separados.
- Lore: livros escritos, fatos do jogador e opções contextuais baseadas em itens.
- Quests: contratos por NPC versionados no dia de jogo, multiplayer consistente; editor de grafo em formato de dados primeiro, UI visual depois.
- Gate: pérola libera fala adequada sem iniciar quest; oferta é idêntica para dois jogadores; pedidos não rerrolam a cada diálogo.

## Etapa 4 — Inventário e itens (em paralelo quando seguro)

- Inventory: bloqueios 27 slots, 10 níveis, crafting vanilla integrado, slots extras e teste de dupe.
- Items: pacote simples (item vanilla-like) registrado no início; render e comportamento conhecido.
- Food: primeiros consumíveis sem receita, disponibilizados no comércio.
- Studio: protótipo web de preview/exportação após manifesto estável.
- Gate: crafting, morte, logout, sincronização e movimentação entre containers sem perdas.

## Etapa 5 — Sociedades e integração difícil

- Villages: novas estruturas em chunks novos, lotes, nomes, camas e ocupações.
- Reparos/ruínas, memória de infrações e guardas; sem progressão automática de tier.
- Progression: XP RPG separado da XP vanilla, perks balanceados e HUD.
- Blueprint para missões de construção (estátua piglin) depois do motor de condições.
- Gate: parede explodida é reparada; jogador pode bloquear porta e o NPC a remove; casa fora do lote é preservada; sem abuso de reconstrução.

## Etapa 6 — Integração do modpack e release experimental

- Compatibilidade com mods selecionados, testes de worldgen em muitas seeds, perfis de desempenho, migrações de save.
- Configurações de módulos, tradução, acessibilidade, debug, documentação de instalação.
- Prototipar sistema Materials **somente após conhecer lista real de mods**.
- Release experimental com changelog e aviso de backup.

## Dependências em forma de texto

Engine → Discovery → [Atlas, HUD].
Engine → NPC → Lore → Quests.
Engine → Inventory → Items/Studio (podem evoluir em paralelo).
Engine → Villages ↔ integração opcional NPC/Lore/Discovery/Quests.
Engine → Progression → HUD (integração opcional).
NPC + Quests + Villages + sistema de blueprint → contratos de obras complexas.

**Aviso:** o plano ordena risco e dependência, não promete que todo escopo caberá num único release. Replanejar após cada gate técnico.
