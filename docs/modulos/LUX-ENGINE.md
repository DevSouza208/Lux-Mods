# Lux Engine — GDD funcional e especificação

**Papel:** núcleo técnico que detecta módulos instalados, integra eventos e centraliza configurações. **Não** altera gameplay sozinho.

## MVP
- Detectar ID/versão de mod Lux e compatibilidade de API; mostrar lista de módulos ativos e disponíveis.
- Menu de configuração central com páginas registradas pelos módulos instalados.
- Configuração local por jogador (HUD, keybind, notificações), e por mundo/servidor (IA, contratos, geração, proteção e balanceamento); controles administrativos com permissão explícita.
- API de serviços registráveis: atlas, discovered places, village property, dialogue provider, quest provider, HUD widgets, player skills, item storage.
- Barramento de eventos versionados e persistência básica, sem deixar o mundo carregado desnecessariamente.
- Diagnóstico de dependência ausente: um módulo fica com integração desligada sem crash.

## Modelo sugerido
~~~text
ModuleDescriptor { id, modVersion, engineApiRange, features[], needs[] }
EventEnvelope { schemaVersion, idempotencyKey, scope, worldId, entityId?, playerId?, gameDay, payload }
IntegrationProvider<T> { id, version, isAvailable, capabilities }
~~~

## Escopos dos dados
- **Mundo:** vilas, nomes, propriedades, contratos disponíveis, acontecimentos globais.
- **Entidade:** NPC específico, personalidade, casa, profissão, agenda e demanda.
- **Jogador:** conhecimento, reputação, crimes, progressão e melhorias.
- **Cliente:** visibilidade, escala, layout e animações de HUD.

## Nomenclatura de eventos candidata
place.discovered; village.created; village.building.damaged; village.building.repaired; npc.talked; npc.need.reevaluated; npc.trade.completed; quest.accepted; quest.completed; player.fact.learned; player.reputation.changed; player.inventory.upgraded. IDs públicos só serão fixados após testes.

## Negociação entre módulos
Módulo envia intenção → Engine valida escopo e despacha para serviço competente → servidor executa e salva → Engine publica evento → renderizadores recebem cópia sanitizada. Evitar que renderização de HUD exija classes internas de Villages.

## Não objetivos
Engine não contém geração, profissões, mapa, narrativa, inventário ou perks. Não virar “mod monolítico”.

## Aceite v1
Engine inicia isoladamente em cliente e servidor dedicado; módulo-falso é encontrado; nenhuma exceção por módulos não instalados; configuração client/server tem permissões separadas; estado persiste após logout/restart; evento versionado chega uma vez ao consumidor.
